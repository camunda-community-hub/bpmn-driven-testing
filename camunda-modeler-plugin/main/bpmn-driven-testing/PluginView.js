import React from "react";
import ReactDOM from "react-dom";

import Root from "./view/Root";

// class name of the view's parent DOM node
const PARENT_CLASS_NAME = "bjs-container";

const STYLE = `
  .djs-element {cursor: pointer}
  .djs-minimap { display: none; }
  .djs-overlay-context-pad { display: none; }
  .djs-palette { display: none; }
  .properties { display: none; }
  .secondary.links { display: none; }
`;

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
      this._style.appendChild(document.createTextNode(STYLE));

      // find parent element by class name
      const parent = document.getElementsByClassName(PARENT_CLASS_NAME)[0];
      // append root node
      parent.appendChild(this._root);
      // render view
      ReactDOM.render(<Root plugin={this._plugin} />, this._root);
    }
  }
}
