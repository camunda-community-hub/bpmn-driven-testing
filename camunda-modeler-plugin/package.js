// script to package the Camunda Modeler plugin
// used via 'npm run package'
const AdmZip = require('adm-zip');

const zip = new AdmZip();
zip.addLocalFile("./index.js");
zip.addLocalFile("./menu.js");
zip.addLocalFolder('./dist', 'dist');

zip.writeZip('bpmn-driven-testing-plugin.zip');
