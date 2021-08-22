import {
  MODE_EDITOR,
  MODE_SELECTOR
} from "./Constants";

import PathFinder from "./PathFinder";
import PathMarker from "./PathMarker";
import pluginTabState from "./PluginTabState";
import PluginView from "./PluginView";
import TestCaseModdle from "./TestCaseModdle";

import CoverageMode from "./mode/CoverageMode";
import EditorMode from "./mode/EditorMode";
import SelectorMode from "./mode/SelectorMode";

export default class Plugin {
  constructor(options) {
    this._elementRegistry = options.elementRegistry;
    this._eventBus = options.eventBus;
    this._modeling = options.modeling;
    this._moddle = options.moddle;

    this._pathMarker = new PathMarker(options.elementRegistry, options.canvas);
    this._pathFinder = new PathFinder(options.elementRegistry);

    this._view = new PluginView(this);

    // define possible modes
    this._modes = {};
    this._addMode(new CoverageMode(this));
    this._addMode(new EditorMode(this));
    this._addMode(new SelectorMode(this));

    // state
    this._activeMode = this._modes[MODE_SELECTOR];
    this._enabled = false;
    this._testCases = [];

    // subscribe events
    const eventBus = options.eventBus;

    eventBus.on("editorActions.init", (event) => {
      const editorActions = event.editorActions;
  
      editorActions.register("toggleBpmnDrivenTesting", () => {
        if (this._enabled) {
          this.disable();
        } else {
          this.enable();
        }
      });
    });
  
    eventBus.on("import.done", () => {
      this._tabId = pluginTabState.register(this);

      this._testCaseModdle = new TestCaseModdle({
        elementRegistry: this._elementRegistry,
        modeling: this._modeling,
        moddle: this._moddle
      });

      // get test cases from extension element
      this._testCases = this._testCaseModdle.getTestCases();

      if (this._testCases.length === 0) {
        this._activeMode = this._modes[MODE_SELECTOR];
      } else {
        this._activeMode = this._modes[MODE_EDITOR];
      }
    });

    eventBus.on("commandStack.element.updateProperties.postExecuted", event => {
      const { oldProperties, properties } = event.context;

      if (!oldProperties.id) {
        return;
      }

      // when element ID was changed, update path of all test cases
      this._testCases.forEach(testCase => testCase.update(oldProperties.id, properties.id));
    });

    eventBus.on("saveXML.start", () => {
      this._testCaseModdle.setTestCases(this._testCases);
    });

    eventBus.on("diagram.destroy", () => {
      this.disable();

      pluginTabState.unregister(this._tabId);
    });
  }

  enable() {
    this._enabled = true;
    this._activeMode.enable();
    this._view.show();
  }

  disable() {
    this._view.hide();
    this._activeMode.disable();
    this._enabled = false;
  }

  registerView(update) {
    this._updateView = update;
  }

  updateView() {
    this._updateView();
  }

  addTestCase(testCase) {
    this._testCases.push(testCase);

    // mark diagram as changed
    this._testCaseModdle.markAsChanged();
  }

  /**
   * Called by the editor mode, when modal is shown or hidden.
   */
  editTestCase() {
    // mark diagram as changed
    this._testCaseModdle.markAsChanged();
  }

  removeTestCase(index) {
    // remove test case
    this._testCases.splice(index, 1);

    // mark diagram as changed
    this._testCaseModdle.markAsChanged();
  }

  setMode(modeName) {
    this.mode.disable();

    this._activeMode = this._modes[modeName];
    this._activeMode.enable();

    this._updateView();
  }

  _addMode(mode) {
    this._modes[mode.name] = mode;
  }

  get elementRegistry() {
    return this._elementRegistry;
  }
  get eventBus() {
    return this._eventBus;
  }

  get pathFinder() {
    return this._pathFinder;
  }
  get pathMarker() {
    return this._pathMarker;
  }

  get enabled() {
    return this._enabled;
  }
  get mode() {
    return this._activeMode;
  }
  get testCases() {
    return this._testCases;
  }
}
