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

/**
 * Plugin bridge, which is used to instantiate the actual plugin class.
 */
function PluginBridge(canvas, elementRegistry, eventBus, modeling, moddle) {
  new Plugin({ canvas, elementRegistry, eventBus, modeling, moddle });
}

PluginBridge.$inject = [ "canvas", "elementRegistry", "eventBus", "modeling", "moddle" ];

registerBpmnJSModdleExtension(bpmndt);
registerBpmnJSPlugin({ __init__: [ PluginBridge ] });
registerClientExtension(ModelerExtension);
