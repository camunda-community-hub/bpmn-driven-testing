import React from "react";
import ReactDOM from "react-dom";

import { PLUGIN_VIEW_PARENT_CLASS_NAME, PLUGIN_VIEW_STYLE } from "./Constants";
import PluginViewRoot from "./PluginViewRoot";

export default class PluginView {
  constructor(plugin) {
    this._plugin = plugin;

    this._style = document.createElement("style");
    document.head.appendChild(this._style);

    this._root = document.createElement("div");
    this._root.className = "bpmn-driven-testing";
  }

  hide() {
    if (this._style.childNodes.length !== 0) {
      // show diagram elements
      this._style.firstChild.remove();

      // unmount view
      ReactDOM.unmountComponentAtNode(this._root);
      // remove root node
      this._root.remove();
    }
  }

  show() {
    if (this._style.childNodes.length === 0) {
      // hide diagram elements
      this._style.appendChild(document.createTextNode(PLUGIN_VIEW_STYLE));

      // find parent element by class name
      const parent = document.getElementsByClassName(PLUGIN_VIEW_PARENT_CLASS_NAME)[0];
      // append root node
      parent.appendChild(this._root);
      // render view
      ReactDOM.render(<PluginViewRoot plugin={this._plugin} />, this._root);
    }
  }
}
