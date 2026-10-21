/** Revisa documentación y sincroniza copias opcionalmente. No ejecuta código del producto. */
import fs from 'node:fs';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';

const localRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const root = fs.existsSync(path.join(localRoot, 'app/src')) ? localRoot : path.resolve(localRoot, '..');
const web = path.join(root, 'admin-web');
const docdir = path.join(root, 'docs/codigo');
const issues = [];
const report = { coverage: {}, checkedLinks: 0, checkedMarkdown: 0, mirroredFiles: 0, mirroredSources: 0 };
const sharedSources = ['supabase_schema.sql', 'supabase_migration_v3_admin_security.sql', 'supabase_migration_v4_report_aggregation.sql', 'missing_policies.sql', 'tests/sql/admin_security.sql', 'tests/sql/report_aggregation.sql', 'supabase/.gitignore', 'supabase/config.toml',
  'supabase/migrations/20261016125703_admin_security_v3.sql', 'supabase/migrations/20261016140944_aggregate_citizen_reports.sql', 'tests/report-aggregation-concurrency.cjs',
  ...['index.ts', 'router.mjs', 'firebase.mjs', 'pac-schedule-core.js'].map(n => `supabase/functions/pac-api/${n}`)].map(n => path.join(root, n));
function walk(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const absolute = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(absolute) : [absolute];
  });
}
const documentFiles = [
  ...['DOCUMENTACION_SISTEMA.md', 'DOCUMENTACION_CODIGO.md', 'ANALISIS_SISTEMA_PAC_BARINAS_2026-10-15.md'].map(n => path.join(root, n)),
  ...['ACTIVACION_SEGURIDAD.md', 'CAMBIOS_SEGURIDAD_Y_ALERTAS.md', 'OPERACION_GATEWAY_SUPABASE.md', 'CAMBIOS_MAPA_ANUNCIOS_Y_SINCRONIZACION.md', 'RELEASE_1.4.0.md'].map(n => path.join(root, 'docs', n)).filter(fs.existsSync),
  ...walk(docdir)
];
function syncCopies() {
  for (const source of [...documentFiles, ...sharedSources]) {
    const target = path.join(web, path.relative(root, source));
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.copyFileSync(source, target);
  }
}
if (process.argv.includes('--sincronizar')) syncCopies();
const inventory = JSON.parse(fs.readFileSync(path.join(docdir, 'inventario-funciones.json'), 'utf8'));
for (const entry of inventory.files) {
  const file = path.join(root, entry.file);
  if (!fs.existsSync(file)) { if (!entry.localOnly) issues.push(`Fuente inventariada ausente: ${entry.file}`); continue; }
  const actual = createHash('sha256').update(fs.readFileSync(file, 'utf8').replace(/\r\n/g, '\n')).digest('hex');
  if (entry.sha256 !== actual) issues.push(`Fuente modificada desde el inventario; revisar explicación y regenerar: ${entry.file}`);
}
const chapterNames = [...new Set(inventory.symbols.map(s => s.chapter))];
for (const chapter of chapterNames) {
  const absolute = path.join(docdir, chapter);
  if (!fs.existsSync(absolute)) { issues.push(`Falta capítulo ${chapter}`); continue; }
  const content = fs.readFileSync(absolute, 'utf8');
  const stop = content.search(/^##\s+(?:\d+\.?\s+)?(?:Índice exhaustivo|Índice de funciones y cobertura|Inventario final|Inventario de funciones|Inventario verificable)/mi);
  const body = stop < 0 ? content : content.slice(0, stop);
  const entries = inventory.symbols.filter(s => s.chapter === chapter);
  const missing = entries.filter(s => !body.includes(s.name));
  report.coverage[chapter] = { declarations: entries.length, absentNames: missing.length };
  for (const entry of missing) issues.push(`Sin explicación nominal en ${chapter}: ${entry.file}:${entry.line} ${entry.name}`);
}
function headingIds(content) {
  const ids = new Set();
  const seen = new Map();
  let inFence = false;
  for (const line of content.split(/\r?\n/)) {
    if (/^\s*```/.test(line)) { inFence = !inFence; continue; }
    if (inFence) continue;
    const match = line.match(/^#{1,6}\s+(.+?)\s*#*$/);
    if (!match) continue;
    const base = match[1].toLowerCase().replace(/<[^>]*>/g, '').replace(/[^\p{L}\p{N}\s_-]/gu, '').trim().replace(/\s/g, '-');
    const n = seen.get(base) ?? 0;
    seen.set(base, n + 1);
    ids.add(base + (n ? `-${n}` : ''));
  }
  return ids;
}
const markdown = [...documentFiles.filter(p => p.endsWith('.md') && !path.basename(p).startsWith('ANALISIS_')), path.join(root, 'README.md'), path.join(web, 'README.md')];
for (const file of markdown) {
  const content = fs.readFileSync(file, 'utf8');
  report.checkedMarkdown++;
  if ((content.match(/^\s*```/gm) ?? []).length % 2) issues.push(`Bloque de código sin cierre: ${file}`);
  if (/eyJ[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+|-----BEGIN (?:RSA |EC )?PRIVATE KEY-----|AIza[0-9A-Za-z_-]{30,}/.test(content)) issues.push(`Posible credencial copiada: ${file}`);
  for (const match of content.matchAll(/!?\[[^\]\n]*\]\((?:<([^>]+)>|([^\s)]+))\)/g)) {
    let target = match[1] ?? match[2];
    if (/^(https?:|mailto:|tel:|app:|codex:|market:)/i.test(target)) continue;
    target = decodeURIComponent(target);
    const hash = target.indexOf('#');
    const fragment = hash >= 0 ? target.slice(hash + 1) : '';
    let filename = hash >= 0 ? target.slice(0, hash) : target;
    const lineMatch = filename.match(/:(\d+)$/);
    if (lineMatch) filename = filename.slice(0, -lineMatch[0].length);
    const absolute = !filename ? file : /^[A-Za-z]:[\\/]/.test(filename) || path.isAbsolute(filename) ? filename : path.resolve(path.dirname(file), filename);
    report.checkedLinks++;
    if (!fs.existsSync(absolute)) { issues.push(`Enlace roto en ${path.relative(root, file)}: ${target}`); continue; }
    if (lineMatch && fs.statSync(absolute).isFile() && Number(lineMatch[1]) > fs.readFileSync(absolute, 'utf8').split('\n').length) issues.push(`Línea inexistente: ${target}`);
    if (fragment && absolute.endsWith('.md') && !headingIds(fs.readFileSync(absolute, 'utf8')).has(fragment)) issues.push(`Ancla Markdown inexistente en ${path.relative(root, file)}: ${target}`);
  }
}
for (const source of documentFiles) {
  const target = path.join(web, path.relative(root, source));
  if (!fs.existsSync(target)) { issues.push(`Falta copia web ${path.relative(root, source)}`); continue; }
  if (!fs.readFileSync(source).equals(fs.readFileSync(target))) issues.push(`Copia web diferente: ${path.relative(root, source)}`);
  report.mirroredFiles++;
}
for (const source of sharedSources) {
  const target = path.join(web, path.relative(root, source));
  if (!fs.existsSync(target) || !fs.readFileSync(source).equals(fs.readFileSync(target))) issues.push(`Fuente compartida web diferente: ${path.relative(root, source)}`);
  else report.mirroredSources++;
}
report.issues = issues;
console.log(JSON.stringify(report, null, 2));
if (issues.length) process.exitCode = 1;
