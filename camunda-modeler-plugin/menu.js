"use strict";

module.exports = function(electronApp, menuState) {
  return [{
    label: "Show / Hide",
    accelerator: "CommandOrControl+T",
    enabled: function() {
      return menuState.bpmn;
    },
    action: function() {
      electronApp.emit("menu:action", "toggleBpmnDrivenTesting");
    }
  }];
};
