import { MODE_EDITOR, MODE_SELECTION, UNSUPPORTED_ELEMENT_TYPES } from "../Constants";

import PathSelection from "../PathSelection";
import TestCase from "../TestCase";

import StartEndStrategy from "./selection/StartEndStrategy";

export default class SelectionMode {
  constructor(plugin) {
    this._plugin = plugin;

    this._strategy = null;
  }

  enable() {
    this._paths = [];
    this._pathEquality = [];
    this._pathIndex = -1;

    if (this._strategy === null) {
      this._strategy = new StartEndStrategy();
    }

    // register click handler
    this._plugin.eventBus.on("element.click", 1500, this._onClick);

    if (!this.isMigration()) {
      return;
    }

    const paths = this._strategy.getPaths();
    if (paths.length !== 0) {
      this._checkPathEquality(paths);

      this._paths = paths;
      this._pathIndex = 0;

      this.selection.path = paths[0];
    }

    this._mark();
  }

  disable() {
    this._plugin.pathMarker.unmark();

    // unregister click handler
    this._plugin.eventBus.off("element.click", this._onClick);
  }

  reset() {
    this._strategy = null;
  }

  addTestCase() {
    const testCase = new TestCase({
      path: this._paths[this._pathIndex]
    });

    // add test case
    this._plugin.addTestCase(testCase);

    // remove path
    this._paths.splice(this._pathIndex, 1);

    if (this._paths.length !== 0) {
      this._pathIndex--;
      this.nextPath();
    } else {
      // reset selection
      this._paths = [];
      this._pathEquality = [];
      this._pathIndex = -1;
      this._strategy = new StartEndStrategy();
  
      this._plugin.pathMarker.unmark();
      this._plugin.updateView();
    }
  }

  /**
   * Determines if an existing test case should be migrated or new test cases should be added.
   * 
   * @returns true, if a test case should be migrated. Otherwise false.
   */
  isMigration() {
    return this._strategy.isMigration();
  }

  migrateTestCase() {
    const { problem, testCase } = this._strategy;

    const path = this._paths[this._pathIndex];

    testCase.path = this._strategy.migrate(path);

    testCase.removeProblem(problem);

    this._plugin.markAsChanged();
    this._plugin.setMode(MODE_EDITOR);
  }

  nextPath() {
    if (this._pathIndex >= (this._paths.length - 1)) {
      this._pathIndex = 0;
    } else {
      this._pathIndex++;
    }

    // update path
    this.selection.path = this._paths[this._pathIndex];
    this._mark();

    this._plugin.updateView();
  }

  prevPath() {
    if (this._pathIndex <= 0) {
      this._pathIndex = this._paths.length - 1;
    } else {
      this._pathIndex--;
    }

    // update path
    this.selection.path = this._paths[this._pathIndex];
    this._mark();

    this._plugin.updateView();
  }

  _checkPathEquality(paths) {
    for (let i = 0; i < paths.length; i++) {
      this._pathEquality[i] = this._plugin.testCases.find(testCase => testCase.equals(paths[i])) !== undefined;
    }
  }

  _mark() {
    this._plugin.pathMarker.mark(this._strategy.getMarkers());
  }

  /**
   * Called when a modeler element is clicked. This function handles the path selection. If start
   * and end element are selected, the PathFinder tries to find possible paths through the process.
   * 
   * @param {object} event 
   */
  _onClick = (event) => {
    if (UNSUPPORTED_ELEMENT_TYPES.has(event.element.type)) {
      // skip clicks on unsupported elements
      return false;
    }

    this._strategy.handleClick(event.element.id);

    // reset
    this._paths = [];
    this._pathEquality = [];
    this._pathIndex = -1;

    this._mark();

    const { selection } = this._strategy;

    if (!selection.hasStart() || !selection.hasEnd()) {
      this._plugin.updateView();
      return false;
    }

    selection.enrich(this._plugin.elementRegistry);

    // find possible paths between start and end
    const paths = this._plugin.pathFinder.findPaths(selection.start, selection.end);

    // check for paths that are equal to test cases
    this._checkPathEquality(paths);

    if (paths.length === 0) {
      this._plugin.pathMarker.markError();
    } else {
      this._paths = paths;
      this._pathIndex = 0;

      selection.path = paths[0];

      this._mark();
    }

    this._plugin.updateView();

    return false;
  }

  get name() {
    return MODE_SELECTION;
  }

  get paths() {
    return this._paths;
  }

  get pathEquality() {
    return this._pathEquality[this._pathIndex];
  }

  get pathIndex() {
    return this._pathIndex;
  }

  get selection() {
    return this._strategy.selection;
  }

  /**
   * Sets the strategy to use during selection, if a specific problem should be solved during a
   * test case migration.
   * 
   * @param {BaseStrategy} strategy The strategy to use.
   */
  set strategy(strategy) {
    this._strategy = strategy;
  }
}
