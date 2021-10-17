import React from "react";
import ReactDOM from "react-dom";

import { PLUGIN_VIEW_PARENT_CLASS_NAME, PLUGIN_VIEW_STYLE } from "./constants";

import PluginController from "./PluginController";
import pluginTabState from "./PluginTabState";
import PluginView from "./PluginView";

export default class Plugin {
  constructor(options) {
    const controller = new PluginController({ hidePlugin: this.hide, ...options });
    
    this.controller = controller;
    this.shown = false;

    // DOM elements, required for PluginView
    this.styleElement = document.createElement("style");
    document.head.appendChild(this.styleElement);

    this.rootElement = document.createElement("div");
    this.rootElement.className = "bpmndt";

    // subscribe events
    const { eventBus } = options;

    eventBus.on("commandStack.element.updateProperties.postExecuted", (event) => {
      const { oldProperties, properties } = event.context;

      controller.handleBpmnElementChanged(oldProperties, properties);
    });

    eventBus.on("diagram.destroy", () => {
      this.hide();

      pluginTabState.unregister(this.tabId); 
    });

    eventBus.on("editorActions.init", (event) => {
      event.editorActions.register("toggleBpmnDrivenTesting", () => {
        if (this.shown) {
          this.hide();
        } else {
          this.show();
        }
      });
    });

    eventBus.on("element.click", 1500, (event) => {
      if (this.shown) {
        controller.handleBpmnElementClicked(event);
        return false;
      }
    });

    eventBus.on("elements.changed", () => {
      controller.handleBpmnModelChanged();
    });

    eventBus.on("import.done", () => {
      this.tabId = pluginTabState.register(this);

      controller.handleLoadTestCases();
    });

    eventBus.on("saveXML.start", () => {
      controller.handleSaveTestCases();
    });
  }

  hide = () => {
    const { controller, rootElement, styleElement } = this;

    if (styleElement.childNodes.length !== 0) {
      // show diagram elements
      styleElement.firstChild.remove();

      // unmount view
      ReactDOM.unmountComponentAtNode(rootElement);
      // remove root element
      rootElement.remove();
    }

    controller.disable();

    this.shown = false;
  }

  show() {
    const { controller, rootElement, styleElement } = this;

    if (styleElement.childNodes.length === 0) {
      // hide diagram elements
      styleElement.appendChild(document.createTextNode(PLUGIN_VIEW_STYLE));

      // find parent element by class name
      const parent = document.getElementsByClassName(PLUGIN_VIEW_PARENT_CLASS_NAME)[0];
      // append root node
      parent.appendChild(rootElement);
      // render view
      ReactDOM.render(<PluginView controller={controller} />, rootElement);
    }

    controller.enable();

    this.shown = true;
  }
}
