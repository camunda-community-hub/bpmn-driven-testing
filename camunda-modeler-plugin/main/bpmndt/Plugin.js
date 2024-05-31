import React from "react";
import { createRoot } from "react-dom/client";

import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_SELECT,
  MODE_SHOW_COVERAGE,
  MODE_VIEW,
  PLUGIN_VIEW_PARENT_CLASS_NAME,
  PLUGIN_VIEW_STYLE,
  UNSUPPORTED_ELEMENT_TYPES
} from "./constants";

import PathFinder from "./PathFinder";
import PathMarker from "./PathMarker";
import PathValidator from "./PathValidator";
import TestCaseModdle from "./TestCaseModdle";

import PluginView from "./PluginView";

import EditMode from "./mode/EditMode";
import SelectMode from "./mode/SelectMode";
import ShowCoverageMode from "./mode/ShowCoverageMode";
import ViewMode from "./mode/ViewMode";
import MigrateMode from "./mode/MigrateMode";

export default class Plugin {
  constructor(canvas, elementRegistry, eventBus, modeling, moddle) {
    this.bpmnModelChanged = false;
    this.mode = null;
    this.testCases = [];
    this.visible = false;

    // plugin modules
    this.pathFinder = new PathFinder(elementRegistry);
    this.pathMarker = new PathMarker(canvas, elementRegistry);
    this.pathValidator = new PathValidator(elementRegistry);
    this.testCaseModdle = new TestCaseModdle(elementRegistry, modeling, moddle);

    // DOM elements, required for PluginView
    this.styleElement = document.createElement("style");
    document.head.appendChild(this.styleElement);

    this.root = null;
    this.rootElement = document.createElement("div");
    this.rootElement.className = "bpmndt";

    // subscribe events
    eventBus.on("commandStack.element.updateProperties.postExecuted", this._updateFlowNodeId);
    eventBus.on("diagram.destroy", this._destroy);
    eventBus.on("editorActions.init", this._registerMenuActions);
    eventBus.on("element.click", 1500, this._handleClickElement);
    eventBus.on("element.dblclick", 3500, this._ignoreEvent);
    eventBus.on("elements.changed", this._markBpmnModelAsChanged);
    eventBus.on("import.done", this._loadTestCases);
    eventBus.on("saveXML.start", this._saveTestCases);
    eventBus.on("shape.move.start", 3500, this._ignoreEvent);
  }

  enrichTestCase(testCase) {
    this.testCaseModdle.enrichTestCase(testCase);
  }

  hide() {
    const { pathMarker, rootElement, styleElement } = this;

    if (this.root !== null) {
      // show diagram elements
      styleElement.firstChild.remove();

      // unmount view
      this.root.unmount();
      this.root = null;
      // remove root element
      rootElement.remove();
    }

    pathMarker.removeAll();

    this.visible = false;
  }

  mark(markers) {
    this.pathMarker.mark(markers);
  }

  markAsChanged() {
    this.testCaseModdle.markAsChanged();
  }

  markError() {
    this.pathMarker.markError();
  }

  setMode(newModeId, oldMode) {
    this.mode = this._createMode(newModeId, oldMode);
    this.updateView();
  }

  show() {
    const { mode, testCases } = this;

    if (mode) {
      mode.updateMarkers();
    } else if (testCases.length !== 0) {
      this.mode = new ViewMode(this);
    } else {
      this.mode = new SelectMode(this);
    }

    this._validateTestCases();

    const { rootElement, styleElement } = this;

    if (styleElement.childNodes.length === 0) {
      // hide diagram elements
      styleElement.appendChild(document.createTextNode(PLUGIN_VIEW_STYLE));

      // find parent element by class name
      const parent = document.getElementsByClassName(PLUGIN_VIEW_PARENT_CLASS_NAME)[0];
      // append root node
      parent.appendChild(rootElement);
      // render view
      this.root = createRoot(rootElement);
      this.root.render(<PluginView plugin={this} />);
    }

    this.visible = true;
  }

  _createMode(newModeId, oldMode) {
    switch (newModeId) {
      case MODE_EDIT:
        return new EditMode(this, oldMode);
      case MODE_MIGRATE:
        return new MigrateMode(this, oldMode);
      case MODE_SELECT:
        return new SelectMode(this, oldMode);
      case MODE_SHOW_COVERAGE:
        return new ShowCoverageMode(this, oldMode);
      case MODE_VIEW:
        return new ViewMode(this, oldMode);
      default:
        throw new Error(`Unsupported mode '${newModeId}'`);
    }
  }

  _destroy = () => {
    this.hide();
    this.unregister();
  }

  _handleClickElement = (event) => {
    if (!this.visible) {
      return;
    }

    const { element } = event;
    if (UNSUPPORTED_ELEMENT_TYPES.has(element.type)) {
      // skip click on unsupported element
      return false;
    }

    this.mode.handleClickElement(element);

    return false;
  }

  _ignoreEvent = () => {
    if (this.visible) {
      // ignore event, when plugin is enabled
      return false;
    }
  }

  _loadTestCases = () => {
    const { testCaseModdle } = this;

    this.testCases = testCaseModdle.getTestCases();

    // allow initial validation
    this._markBpmnModelAsChanged();
    this._validateTestCases();
  }

  _markBpmnModelAsChanged = () => {
    this.bpmnModelChanged = true;
  }

  _registerMenuActions = (event) => {
    event.editorActions.register("toggleBpmnDrivenTesting", () => {
      if (this.visible) {
        this.hide();
      } else {
        this.show();
      }
    });
  }

  _saveTestCases = () => {
    const { testCases, testCaseModdle } = this;
    testCaseModdle.setTestCases(testCases);
  }

  _updateFlowNodeId = (event) => {
    const { oldProperties, properties } = event.context;

    if (!oldProperties.id) {
      return;
    }

    this.testCases.forEach(testCase => testCase.updateFlowNodeId(oldProperties.id, properties.id));
  }

  _validateTestCases() {
    const { bpmnModelChanged, pathValidator, testCases } = this;

    if (!bpmnModelChanged || testCases.length === 0) {
      // nothing to validate
      return;
    }

    this.bpmnModelChanged = false;

    setTimeout(() => {
      testCases.forEach((testCase) => {
        const problems = pathValidator.validate(testCase);
        problems.forEach(problem => {
          this.testCaseModdle.enrichProblem(problem);
        });

        testCase.problems = problems;

        if (testCase.autoResolveProblem()) {
          this.markAsChanged();
          this.mode.updateMarkers();
        }
      });

      if (this.visible) {
        this.updateView();
      }
    }, 1000);
  }
}

Plugin.$inject = [ "canvas", "elementRegistry", "eventBus", "modeling", "moddle" ];
