import { MODE_EDITOR, MODE_MIGRATION, MODE_SELECTION } from "./Constants";

import PathFinder from "./PathFinder";
import PathMarker from "./PathMarker";
import PathValidator from "./PathValidator";
import pluginTabState from "./PluginTabState";
import PluginView from "./PluginView";
import TestCaseModdle from "./TestCaseModdle";

import CoverageMode from "./mode/CoverageMode";
import EditorMode from "./mode/EditorMode";
import MigrationMode from "./mode/MigrationMode";
import SelectionMode from "./mode/SelectionMode";

export default class Plugin {
  constructor(options) {
    const { canvas, elementRegistry, eventBus, moddle, modeling } = options;

    this._elementRegistry = elementRegistry;
    this._eventBus = eventBus;
    this._moddle = moddle;
    this._modeling = modeling;

    this._pathMarker = new PathMarker(elementRegistry, canvas);
    this._pathFinder = new PathFinder(elementRegistry);
    this._pathValidator = new PathValidator(elementRegistry, this._pathFinder);

    this._view = new PluginView(this);

    // modes
    this._modes = {};
    this._addMode(new CoverageMode(this));
    this._addMode(new EditorMode(this));
    this._addMode(new MigrationMode(this));
    this._addMode(new SelectionMode(this));

    // state
    this._activeMode = this._modes[MODE_SELECTION];
    this._elementsChanged = false;
    this._enabled = false;
    this._testCases = [];

    // subscribe events
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
        this._activeMode = this._modes[MODE_SELECTION];
      } else {
        this._activeMode = this._modes[MODE_EDITOR];
      }

      // validate test cases
      setTimeout(() => {
        this._testCases.forEach(testCase => {
          testCase.problems = this._pathValidator.validate(testCase);
          
          if (testCase.autoResolveProblem()) {
            this.markAsChanged();

            if (this._enabled) {
              this.mode.enable();
            }
          }
        });

        this.updateView();
      }, 1000);
    });

    eventBus.on("elements.changed", () => {
      this._elementsChanged = true;
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

    if (this._testCases === undefined) {
      return;
    }

    // validate test cases
    setTimeout(() => {
      this._testCases.forEach(testCase => {
        testCase.problems = this._pathValidator.validate(testCase);
        
        if (testCase.autoResolveProblem()) {
          this.markAsChanged();

          if (this._enabled) {
            this.mode.enable();
          }
        }
      });

      this.updateView();
    }, 1000);
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
    if (this._updateView) {
      this._updateView();
    }
  }

  addTestCase(testCase) {
    this._testCases.push(testCase);

    // mark diagram as changed
    this.markAsChanged();
  }

  /**
   * Called by the editor mode, when modal is shown or hidden.
   */
  editTestCase() {
    // mark diagram as changed
    this.markAsChanged();
  }

  markAsChanged() {
    this._testCaseModdle.markAsChanged();
  }

  migrateTestCase(testCase) {
    const mode = this._modes[MODE_MIGRATION];
    mode.testCase = testCase;

    this.setMode(mode.name);
  }

  removeTestCase(index) {
    // remove test case
    this._testCases.splice(index, 1);

    // mark diagram as changed
    this.markAsChanged();
  }

  resolveProblem(strategy) {
    const mode = this._modes[MODE_SELECTION];
    mode.strategy = strategy;

    this.setMode(mode.name);
  }

  setMode(modeName, reset) {
    // disable currently active mode
    this.mode.disable();

    this._activeMode = this._modes[modeName];

    if (reset) {
      this._activeMode.reset();
    }

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
