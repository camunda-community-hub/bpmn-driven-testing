import "@fortawesome/fontawesome-free/css/all.min.css";
import "./index.css";

import {
  registerBpmnJSModdleExtension,
  registerBpmnJSPlugin,
  registerClientExtension
} from "camunda-modeler-plugin-helpers";

import bpmndt from "./bpmndt.json";
import Plugin from "./bpmndt/Plugin";

import ModelerExtension from "./bpmndt/ModelerExtension";

registerBpmnJSModdleExtension(bpmndt);
registerBpmnJSPlugin({
  __init__: [ "bpmndt" ],
  "bpmndt": [ "type", Plugin ]
});
registerClientExtension(ModelerExtension);
