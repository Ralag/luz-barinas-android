// Execute only after tests/sql/report_aggregation.sql in its disposable test DB.
const { spawn } = require('node:child_process');
const assert = require('node:assert/strict');
if (process.env.PGDATABASE !== 'report_aggregation_test') throw new Error('Requires disposable PGDATABASE=report_aggregation_test');

function runSql(sql) {
  return new Promise((resolve, reject) => {
    const child = spawn('psql', ['-X', '-A', '-t', '-v', 'ON_ERROR_STOP=1', '-c', sql], { env: process.env, windowsHide: true });
    let output = '', error = '';
    const timer = setTimeout(() => { child.kill(); reject(new Error('Concurrent SQL timed out')); }, 15000);
    child.stdout.on('data', data => { output += data; });
    child.stderr.on('data', data => { error += data; });
    child.on('error', reject);
    child.on('close', code => { clearTimeout(timer); code === 0 ? resolve(output.trim()) : reject(new Error(error)); });
  });
}

async function main() {
  await runSql(`INSERT INTO public.sectors(id,name,status,voltage,"lastUpdatedMillis")
    VALUES ('concurrency-test-a','Concurrency A','NORMAL',118,0),('concurrency-test-b','Concurrency B','NORMAL',118,0);`);
  const write = (suffix, order, kind, offset) => runSql(`BEGIN; SET LOCAL ROLE anon;
    INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
    SELECT ('40000000-0000-0000-0000-' || lpad((${suffix} + row_number() OVER())::text,12,'0'))::uuid,
      sector_id,'Concurrent report',false,'${kind}',0,
      (SELECT now_ms FROM test_support.clock) + ${offset}
    FROM (VALUES ${order.map(id => `('${id}')`).join(',')}) AS data(sector_id);
    SELECT pg_sleep(0.4); COMMIT;`);
  await Promise.all([
    write(10, ['concurrency-test-a','concurrency-test-b'], 'SIN_LUZ', -1000),
    write(20, ['concurrency-test-b','concurrency-test-a'], 'FALLA_IRREGULAR', 1000)
  ]);
  const rows = JSON.parse(await runSql(`SELECT json_agg(t) FROM
    (SELECT id,status,"confirmedReportsCount" AS count,"withoutPowerPercentage" AS percentage
    FROM public.sectors WHERE id IN ('concurrency-test-a','concurrency-test-b') ORDER BY id) t;`));
  assert.equal(rows.length, 2);
  rows.forEach(row => { assert.equal(row.count, 2); assert.equal(row.percentage, 100); assert.equal(row.status, 'IRREGULAR_OUTAGE'); });
  console.log('Concurrent reversed-order inserts preserve both samples and latest irregular status.');
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
