import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath, pathToFileURL } from 'node:url';

const docs = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const models = path.resolve(docs, '../src/main/java/frescuratotal/erp');
const require = createRequire(path.join(process.env.ERP_DIAGRAM_RUNTIME || path.join(docs, 'tools'), 'package.json'));
const { instance } = await import(pathToFileURL(require.resolve('@viz-js/viz')).href);
const { Resvg } = require('@resvg/resvg-js');
const escape = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
const lines = ['digraph Domain {',
  'graph [rankdir=LR,bgcolor="white",pad=0.4,nodesep=0.4,ranksep=1.5,splines=polyline,labelloc=t,fontname="Arial",fontsize=20,label="ERP Frescura Total — clases del dominio\\nAtributos privados (-); operaciones públicas (+); enums sin duplicar valores\\nFlecha continua: referencia de objeto | rombo blanco: colección agregada\\nFlecha discontinua: asociación por identificador (sin referencia Java directa)"];',
  'node [shape=plain,fontname="Arial",fontsize=11];',
  'edge [fontname="Arial",fontsize=10,color="#52657A",arrowsize=0.7];'];
const entities = new Map();
for (const [module, color] of [['inventory','#D6F4E8'],['suppliers','#FFF1CB'],['sales','#E2ECFF'],['logistics','#FFE4D7']]) {
  lines.push(`subgraph cluster_${module} { label="${module}/model"; color="#CBD5E0";`);
  for (const file of fs.readdirSync(path.join(models,module,'model')).filter(name => name.endsWith('.java')).sort()) {
    const source = fs.readFileSync(path.join(models,module,'model',file),'utf8');
    const name = file.replace('.java','');
    const fields = [...source.matchAll(/^\s*private\s+(?:final\s+)?([\w.<>?, ]+)\s+(\w+)\s*(?:=[^;]*)?;/gm)]
      .map(match => ({type:match[1].trim().replace('java.time.',''),name:match[2]}));
    const methods = [...source.matchAll(/^\s*public\s+(?:static\s+)?([\w.<>?, ]+)\s+(\w+)\(([^)]*)\)/gm)]
      .filter(match => !/^(get|set|is|equals|hashCode|toString)/.test(match[2]))
      .filter(match => !fields.some(field => field.name === match[2]))
      .map(match => `+ ${match[2]}(${match[3].replace(/\b(?:final\s+)?(\w+(?:<[^>]+>)?)\s+\w+/g,'$1')}) : ${match[1]}`);
    const isEnum = /public enum/.test(source);
    entities.set(name,fields);
    const rows = fields.map(field => `<TR><TD ALIGN="LEFT">- ${field.name} : ${escape(field.type)}</TD></TR>`).join('');
    lines.push(`${name} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="6" COLOR="#AAB8C8"><TR><TD BGCOLOR="${color}"><B>${isEnum?'&lt;&lt;enum&gt;&gt; ':''}${name}</B></TD></TR>${rows}<TR><TD ALIGN="LEFT" BORDER="1" SIDES="T">${methods.length?methods.map(escape).join('<BR ALIGN="LEFT"/>'):'+ constructor / accessors'}</TD></TR></TABLE>>];`);
  }
  lines.push('}');
}
let associations = 0;
for (const [name, fields] of entities) {
  for (const field of fields) {
    const collection = /^List<(.+)>$/.exec(field.type);
    const target = collection ? collection[1] : field.type;
    if (entities.has(target)) {
      lines.push(`${name} -> ${target} [label="${field.name}",headlabel="${collection?'0..*':'1'}",${collection?'dir=both,arrowtail=odiamond,arrowhead=none':'arrowhead=vee'}];`);
      associations++;
    }
  }
}
for (const [from,to,label] of [['Lot','Product','productId'],['Lot','Supplier','supplierId'],['Inspection','Lot','lotId'],['SaleItem','Product','productId'],['InvoiceLine','Lot','lotId'],['PurchaseRecord','Lot','lotId'],['CatalogProduct','Product','productId'],['RouteStop','DispatchOrder','saleId'],['PurchaseRecord','Supplier','supplierId'],['SupplierReliability','Supplier','supplierId'],['LotCustomer','Customer','id'],['AvailableProduct','Product','id'],['Lot','LotStatus','statusId (catalog)']]) {
  lines.push(`${from} -> ${to} [style=dashed,label="${label}",arrowhead=vee];`);
  associations++;
}
lines.push('}');
const dot = lines.join('\n');
const svg = (await instance()).renderString(dot,{format:'svg',engine:'dot'});
for (const [extension,data] of [['dot',dot],['svg',svg],['png',new Resvg(svg,{fitTo:{mode:'zoom',value:1.7}}).render().asPng()]]) {
  fs.writeFileSync(path.join(docs,`diagrama_clases.${extension}`),data);
}
console.log(JSON.stringify({classes:entities.size,associations}));
