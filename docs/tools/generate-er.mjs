import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath, pathToFileURL } from 'node:url';

const docs = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const runtime = process.env.ERP_DIAGRAM_RUNTIME || path.join(docs, 'tools');
const require = createRequire(path.join(runtime, 'package.json'));
const { instance } = await import(pathToFileURL(require.resolve('@viz-js/viz')).href);
const { Resvg } = require('@resvg/resvg-js');
const sql = fs.readFileSync(path.join(docs, 'database.sql'), 'utf8');
const tables = [];
const escape = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
for (const match of sql.matchAll(/CREATE TABLE(?: IF NOT EXISTS)? (\w+) \(([\s\S]*?)\) ENGINE=InnoDB;/g)) {
  const [, name, body] = match;
  const columns = [];
  const foreignKeys = [...body.matchAll(/FOREIGN KEY \((\w+)\) REFERENCES (\w+)\((\w+)\)/g)]
    .map(m => ({ column: m[1], parent: m[2], target: m[3] }));
  const composite = /PRIMARY KEY \(([^)]+)\)/.exec(body);
  const primary = composite ? composite[1].split(',').map(s => s.trim()) : [];
  for (const line of body.split('\n')) {
    const column = /^\s*(\w+)\s+([A-Z]+(?:\([^)]*\))?)(.*)/.exec(line);
    if (!column || ['FOREIGN', 'PRIMARY', 'UNIQUE', 'INDEX', 'CONSTRAINT', 'KEY'].includes(column[1])) continue;
    const [, field, type, rest] = column;
    columns.push({ name: field, type, primary: /PRIMARY KEY/.test(rest) || primary.includes(field),
      unique: /UNIQUE/.test(rest), nullable: !/NOT NULL|PRIMARY KEY/.test(rest),
      foreign: foreignKeys.some(fk => fk.column === field) });
  }
  tables.push({ name, columns, foreignKeys });
}
if (!tables.length) throw new Error('No se encontraron tablas en el SQL.');
const inventory = new Set(['lotes_inventario','registro_cuarentena','inspecciones_calidad','registro_mermas','operaciones_merma']);
const lines = ['digraph ER {', 'graph [rankdir=LR,bgcolor="white",pad=0.5,nodesep=0.6,ranksep=1.2,splines=polyline,overlap=false,labelloc=t,fontname="Arial",fontsize=22,label="ERP Frescura Total — esquema completo\\nPK: primaria | FK: foránea | UQ: única | ?: admite NULL\\nCardinalidades explícitas en ambos extremos de cada relación"];',
  'node [shape=plain,fontname="Arial",fontsize=11];',
  'edge [color="#718096",fontname="Arial",fontsize=10,arrowsize=0.7,labeldistance=2.2];'];
for (const table of tables) {
  const rows = table.columns.map(c => `<TR><TD PORT="${c.name}_in" ALIGN="LEFT">${[c.primary?'PK':'',c.foreign?'FK':'',c.unique?'UQ':''].filter(Boolean).join(' ')}</TD><TD ALIGN="LEFT">${c.name}${c.nullable?' ?':''}</TD><TD PORT="${c.name}_out" ALIGN="LEFT">${escape(c.type)}</TD></TR>`).join('');
  lines.push(`"${table.name}" [label=<<TABLE BORDER="1" COLOR="#CBD5E0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="6"><TR><TD COLSPAN="3" BGCOLOR="${inventory.has(table.name)?'#D6F4E8':'#E5ECF5'}"><B>${table.name}</B></TD></TR>${rows}</TABLE>>];`);
}
let relationships = 0;
for (const table of tables) {
  for (const fk of table.foreignKeys) {
    const column = table.columns.find(c => c.name === fk.column);
    const unique = column.unique || (column.primary && table.columns.filter(c => c.primary).length === 1);
    lines.push(`"${fk.parent}":${fk.target}_out:e -> "${table.name}":${fk.column}_in:w [taillabel="${column.nullable?'0..1':'1'}",headlabel="${unique?'0..1':'0..N'}"];`);
    relationships++;
  }
}
lines.push('}');
const dot = lines.join('\n');
const viz = await instance();
const svg = viz.renderString(dot, { format: 'svg', engine: 'dot' });
fs.writeFileSync(path.join(docs, 'diagrama_er.dot'), dot);
fs.writeFileSync(path.join(docs, 'diagrama_er.svg'), svg);
fs.writeFileSync(path.join(docs, 'diagrama_er.png'), new Resvg(svg, { fitTo: { mode: 'zoom', value: 1.7 } }).render().asPng());
console.log(JSON.stringify({ tablas: tables.length, atributos: tables.reduce((sum,t) => sum+t.columns.length,0), relaciones: relationships }));
