"use strict";

module.exports = function(electronApp, menuState) {
  return [{
    label: "Show / Hide",
    accelerator: "CommandOrControl+T",
    enabled: function() {
      // enable menu action for tab type "bpmn", but not for "cloud-bpmn"
      // property "activeTab" is available since Camunda Modeler 5.9.0
      return menuState.bpmn && (menuState.activeTab === undefined || menuState.activeTab.type === "bpmn");
    },
    action: function() {
      electronApp.emit("menu:action", "toggleBpmnDrivenTesting");
    }
  }];
};
