import fs from "fs";

import ElementRegistry from "./ElementRegistry";
import Plugin from "./Plugin";

function readBpmnFile(fileName) {
  return fs.readFileSync(`./test/bpmn/${fileName}`, "utf8");
}

export {
  readBpmnFile,
  ElementRegistry,
  Plugin
};
