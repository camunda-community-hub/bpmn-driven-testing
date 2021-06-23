import "@fortawesome/fontawesome-free/css/all.min.css";
import "./index.css";

import {
  registerBpmnJSModdleExtension,
  registerBpmnJSPlugin,
  registerClientExtension
} from "camunda-modeler-plugin-helpers";

import bpmndt from "./bpmn-driven-testing.json";
import Plugin from "./bpmn-driven-testing/Plugin";

import ModelerExtension from "./bpmn-driven-testing/ModelerExtension";

/**
 * Plugin bridge, which is used to instantiate the actual plugin class.
 */
function PluginBridge(canvas, elementRegistry, eventBus, modeling, moddle) {
  new Plugin({
    canvas: canvas,
    elementRegistry: elementRegistry,
    eventBus: eventBus,
    modeling: modeling,
    moddle: moddle
  });
}

PluginBridge.$inject = [ "canvas", "elementRegistry", "eventBus", "modeling", "moddle" ];

registerBpmnJSModdleExtension(bpmndt);
registerBpmnJSPlugin({ __init__: [ PluginBridge ] });
registerClientExtension(ModelerExtension);
