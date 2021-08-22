import { MODE_SELECTOR } from "../Constants";
import PathSelection from "../PathSelection";
import TestCase from "../TestCase";

import { UNSUPPORTED_ELEMENT_TYPES } from "../Constants";

export default class SelectorMode {
  constructor(plugin) {
    this._plugin = plugin;

    this._onSelection = this._onSelection.bind(this);
  }

  enable() {
    this._paths = [];
    this._pathEquality = [];
    this._pathIndex = -1;
    this._selection = new PathSelection()

    // register click handler
    this._plugin.eventBus.on("element.click", this._onSelection);
  }

  disable() {
    this._plugin.pathMarker.mark(null);

    // unregister click handler
    this._plugin.eventBus.off("element.click", this._onSelection);
  }

  addPath() {
    const testCase = new TestCase({
      path: this._paths[this._pathIndex]
    });

    // add test case
    this._plugin.addTestCase(testCase);

    // remove path
    this._paths.splice(this._pathIndex, 1);

    if (this._paths.length !== 0) {
      this._pathIndex--;
      this.markNextPath();
    } else {
      // reset selection
      this._paths = [];
      this._pathEquality = [];
      this._pathIndex = -1;
      this._selection = new PathSelection();
  
      this._plugin.pathMarker.mark(null);
      this._plugin.updateView();
    }
  }

  markNextPath() {
    if (this._pathIndex >= (this._paths.length - 1)) {
      this._pathIndex = 0;
    } else {
      this._pathIndex++;
    }

    // update path
    this._selection.path = this._paths[this._pathIndex];

    this._plugin.pathMarker.mark(this._selection);
    this._plugin.updateView();
  }

  markPrevPath() {
    if (this._pathIndex <= 0) {
      this._pathIndex = this._paths.length - 1;
    } else {
      this._pathIndex--;
    }

    // update path
    this._selection.path = this._paths[this._pathIndex];

    this._plugin.pathMarker.mark(this._selection);
    this._plugin.updateView();
  }

  /**
   * Called when a modeler element is clicked. This function handles the selection of a path's
   * start and end element. If start and end element are selected, the PathFinder tries to find
   * possible paths.
   * 
   * @param {object} event 
   */
  _onSelection(event) {
    const { id, type } = event.element;
    const selection = this._selection;

    // reset
    selection.path = [];

    if (UNSUPPORTED_ELEMENT_TYPES.has(event.element.type)) {
      // skip clicks on unsupported elements
      return;
    } else if (selection.hasEnd() && id === selection.start) {
      selection.end = null;
      selection.start = null;
    } else if (selection.hasEnd() && id === selection.end) {
      selection.end = null;
    } else if (selection.hasEnd()) {
      selection.end = id;
      selection.endType = type;
    } else if (selection.hasStart() && id === selection.start) {
      selection.start = null;
    } else if (selection.hasStart() && !selection.hasEnd()) {
      selection.end = id;
      selection.endType = type;
    } else {
      selection.start = id;
      selection.startType = type;
    }

    this._plugin.pathMarker.mark(selection);

    this._paths = [];
    this._pathEquality = [];
    this._pathIndex = -1;

    if (!selection.hasStart() || !selection.hasEnd()) {
      this._plugin.updateView();
      return;
    }

    // find possible paths between start and end
    const paths = this._plugin.pathFinder.findPaths(selection);

    // check for paths that are equal to test cases
    for (let i = 0; i < paths.length; i++) {
      this._pathEquality[i] = this._plugin.testCases.find(testCase => testCase.equals(paths[i])) !== undefined;
    }

    if (paths.length === 0) {
      this._plugin.pathMarker.markError();
    } else {
      this._paths = paths;
      this._pathIndex = 0;

      selection.path = paths[0];

      this._plugin.pathMarker.mark(selection);
    }

    this._plugin.updateView();
  }

  get name() {
    return MODE_SELECTOR;
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
    return this._selection;
  }
}
