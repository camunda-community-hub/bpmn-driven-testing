import fs from "fs";

import ElementRegistry from "./ElementRegistry";
import PluginController from "./PluginController";

function readBpmnFile(fileName) {
  return fs.readFileSync(`./test/bpmn/${fileName}`, "utf8");
}

export {
  readBpmnFile,
  ElementRegistry,
  PluginController
};
