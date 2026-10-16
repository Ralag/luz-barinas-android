/** Inventario estático para verificar la documentación. No ejecuta el código del producto. */
import fs from 'node:fs';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { fileURLToPath, pathToFileURL } from 'node:url';

const localRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const root = fs.existsSync(path.join(localRoot, 'app/src')) ? localRoot : path.resolve(localRoot, '..');
if (!fs.existsSync(path.join(root, 'app/src')) || !fs.existsSync(path.join(root, 'admin-web/src'))) throw new Error('El inventario completo requiere el repositorio Android y el repositorio web colocado en admin-web.');
const output = path.join(root, 'docs/codigo');
const { parseAst } = await import(pathToFileURL(path.join(root, 'admin-web/node_modules/rolldown/dist/parse-ast-index.mjs')));
const symbols = [];
const files = [];
const errors = [];
const tracked = new Set([
  ...execFileSync('git', ['ls-files'], { cwd: root, encoding: 'utf8' }).trim().split('\n'),
  ...execFileSync('git', ['ls-files'], { cwd: path.join(root, 'admin-web'), encoding: 'utf8' }).trim().split('\n').map(p => `admin-web/${p}`)
]);
const lineAt = (source, offset) => source.slice(0, offset).split('\n').length;

function walk(dir, accept) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const p = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(p, accept) : accept(p) ? [p] : [];
  });
}
function relative(p) { return path.relative(root, p).replaceAll('\\', '/'); }
function chapter(file) {
  if (/^app\/src\/(test|androidTest)/.test(file) || /^app\/src\/main\/java\/com\/example\/(data|engine|utils)\//.test(file)) return '02-android-datos-y-motor.md';
  if (file.endsWith('/assets/map/map.html')) return '03-android-interfaz-y-segundo-plano.md';
  if (file.startsWith('app/')) return '03-android-interfaz-y-segundo-plano.md';
  if (file === 'admin-web/public/app.js' || file === 'admin-web/app.js') return '05-panel-clasico.md';
  if (file.startsWith('admin-web/src/') || file.startsWith('admin-web/api/') || file.startsWith('admin-web/scripts/') || file.startsWith('admin-web/tests/') || file === 'admin-web/public/admin-security.js' || file === 'admin-web/vite.config.mjs') return '04-web-react-y-api.md';
  return '06-base-de-datos-configuracion-y-operacion.md';
}
function add(file, source, name, start, kind, scope = '', offset = 0) {
  const entry = { file: relative(file), name, line: lineAt(source, start) + offset, kind, scope };
  entry.chapter = chapter(entry.file);
  if (!symbols.some(x => x.file === entry.file && x.line === entry.line && x.name === entry.name)) symbols.push(entry);
}
function parseJavaScript(file, source, offset = 0) {
  let ast;
  try { ast = parseAst(source, { lang: file.endsWith('.jsx') ? 'jsx' : 'js' }, relative(file)); }
  catch (error) { errors.push({ file: relative(file), error: error.message.split('\n')[0] }); return; }
  function visit(node, parent = null, scope = '') {
    if (!node || typeof node !== 'object') return;
    let nextScope = scope;
    if (['FunctionDeclaration', 'FunctionExpression', 'ArrowFunctionExpression'].includes(node.type)) {
      let name = node.id?.name;
      if (!name && parent?.type === 'VariableDeclarator') name = parent.id?.name;
      if (!name && ['Property', 'MethodDefinition'].includes(parent?.type)) name = parent.key?.name ?? parent.key?.value;
      if (!name && parent?.type === 'AssignmentExpression') name = parent.left?.property?.name ?? parent.left?.name;
      if (name) { add(file, source, String(name), node.start, node.type, scope, offset); nextScope = scope ? `${scope}.${name}` : String(name); }
    }
    if (node.type === 'VariableDeclarator' && node.init?.type === 'CallExpression' && node.init.callee?.name === 'useCallback' && node.id?.name) {
      add(file, source, node.id.name, node.start, 'useCallback', scope, offset);
    }
    for (const [key, value] of Object.entries(node)) {
      if (['loc', 'range', 'tokens', 'comments'].includes(key)) continue;
      if (Array.isArray(value)) value.forEach(child => visit(child, node, nextScope));
      else if (value && typeof value === 'object') visit(value, node, nextScope);
    }
  }
  visit(ast);
}

const kotlin = walk(path.join(root, 'app/src'), p => p.endsWith('.kt'));
const javascript = [
  ...walk(path.join(root, 'admin-web/src'), p => /\.(jsx?|mjs)$/.test(p)),
  ...walk(path.join(root, 'admin-web/api'), p => p.endsWith('.js')),
  ...walk(path.join(root, 'admin-web/scripts'), p => /\.(mjs|js)$/.test(p)),
  ...walk(path.join(root, 'admin-web/tests'), p => /\.(mjs|cjs|js)$/.test(p)),
  ...fs.readdirSync(root).filter(n => n.endsWith('.js')).map(n => path.join(root, n)),
  ...['public/app.js', 'public/admin-security.js', 'app.js', 'vite.config.mjs', 'patch_donations.js'].map(n => path.join(root, 'admin-web', n))
];
for (const file of kotlin) {
  const source = fs.readFileSync(file, 'utf8');
  files.push({ file: relative(file), chapter: chapter(relative(file)), language: 'Kotlin' });
  // Las declaraciones reales empiezan en una línea de código, con modificadores opcionales.
  const pattern = /^\s*(?:(?:public|private|internal|protected|override|suspend|inline|tailrec|operator|infix|open|final|abstract|external)\s+)*fun\s+(?:<[^>]+>\s*)?(?:[\w<>?.,]+\.)?(`[^`]+`|[\w]+)\s*\(/gm;
  for (const match of source.matchAll(pattern)) add(file, source, match[1].replaceAll('`', ''), match.index + match[0].indexOf('fun '), 'Kotlin fun');
}
for (const file of javascript.filter(fs.existsSync)) {
  const source = fs.readFileSync(file, 'utf8');
  files.push({ file: relative(file), chapter: chapter(relative(file)), language: 'JavaScript/JSX' });
  parseJavaScript(file, source);
}
for (const file of [path.join(root, 'app/src/main/assets/map/map.html'), path.join(root, 'admin-web/admin.html')]) {
  const source = fs.readFileSync(file, 'utf8');
  files.push({ file: relative(file), chapter: chapter(relative(file)), language: 'HTML con JavaScript' });
  for (const match of source.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)) {
    if (!match[1].trim()) continue;
    parseJavaScript(file, match[1], lineAt(source, match.index + match[0].indexOf('>') + 1) - 1);
  }
}
const python = [...fs.readdirSync(root).filter(n => n.endsWith('.py')).map(n => path.join(root, n)), ...fs.readdirSync(path.join(root, 'admin-web')).filter(n => n.endsWith('.py')).map(n => path.join(root, 'admin-web', n))];
for (const file of python) {
  const source = fs.readFileSync(file, 'utf8');
  files.push({ file: relative(file), chapter: chapter(relative(file)), language: 'Python' });
  for (const match of source.matchAll(/^\s*(?:async\s+)?def\s+(\w+)\s*\(/gm)) add(file, source, match[1], match.index + match[0].indexOf('def '), 'Python def');
}
symbols.sort((a, b) => a.file.localeCompare(b.file) || a.line - b.line);
files.sort((a, b) => a.file.localeCompare(b.file));
for (const entry of files) {
  entry.localOnly = !tracked.has(entry.file);
  const bytes = fs.readFileSync(path.join(root, entry.file));
  entry.sha256 = createHash('sha256').update(bytes).digest('hex');
  entry.lines = bytes.toString('utf8').split('\n').length;
}
fs.writeFileSync(path.join(output, 'inventario-funciones.json'), JSON.stringify({ scope: 'Código propio; excluye dependencias, build/dist, código generado y skills. Callbacks anónimos se explican dentro de su función contenedora.', files, symbols, errors }, null, 2) + '\n');
const counts = {};
for (const entry of symbols) counts[entry.chapter] = (counts[entry.chapter] ?? 0) + 1;
const md = ['# Índice verificable de funciones', '', 'Generado mediante `node docs/codigo/herramientas/inventario.mjs`. No sustituye las explicaciones de los capítulos: sirve para localizar declaraciones y comprobar omisiones.', '', 'El inventario reconoce declaraciones Kotlin y Python y usa el parser local de Rolldown para JavaScript/JSX. Incluye funciones locales, componentes, métodos y callbacks con nombre. Los callbacks anónimos se explican con la operación que los contiene. Excluye dependencias de terceros, código generado y archivos de salida.', '', 'Las líneas corresponden al código local leído; al cambiarlo, regenera este índice. La detección de nombres no certifica por sí sola la calidad de una explicación. Los archivos marcados como locales no están versionados y su ausencia en un clon no invalida la documentación del producto.', '', '| Capítulo | Declaraciones |', '|---|---:|', ...Object.entries(counts).sort().map(([c, n]) => `| [${c}](${c}) | ${n} |`), '', `Archivos examinados: **${files.length}**. Declaraciones identificadas: **${symbols.length}**. Errores de análisis: **${errors.length}**.`, ''];
for (const file of files) {
  const entries = symbols.filter(x => x.file === file.file);
  md.push(`## ${file.file}`, '', `Explicación: [${file.chapter}](${file.chapter}).`, '');
  if (file.localOnly) md.push('Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.', '');
  if (!entries.length) md.push('No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.', '');
  else md.push('| Función | Línea | Contexto |', '|---|---:|---|', ...entries.map(e => `| \`${e.name.replaceAll('|', '\\|')}\` | ${e.line} | ${e.scope ? `\`${e.scope}\`` : e.kind} |`), '');
}
if (errors.length) md.push('## Errores del inventario', '', ...errors.map(e => `- ${e.file}: ${e.error}`), '');
fs.writeFileSync(path.join(output, '07-indice-funciones.md'), md.join('\n'));
console.log(JSON.stringify({ files: files.length, symbols: symbols.length, byChapter: counts, errors }, null, 2));
if (errors.length) process.exitCode = 1;
