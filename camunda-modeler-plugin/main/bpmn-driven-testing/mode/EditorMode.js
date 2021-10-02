import { MODE_EDITOR } from "../Constants";
import { END, PATH, START } from "../PathMarker";

import PathSelection from "../PathSelection";

export default class EditorMode {
  constructor(plugin) {
    this._plugin = plugin;

    this._showModal = false;
    this._testCaseIndex = -1;
  }

  enable() {
    const { testCases } = this._plugin;

    if (testCases.length === 0) {
      return;
    }

    if (this._testCaseIndex === -1) {
      this._testCaseIndex = 0;
    }

    this._markTestCase(testCases[this._testCaseIndex]);
  }

  disable() {
    this._plugin.pathMarker.unmark();
  }

  reset() {
    // nothing to do here
  }

  isModalShown() {
    return this._showModal;
  }

  markNextTestCase() {
    const { testCases } = this._plugin;

    if (this._testCaseIndex >= (testCases.length - 1)) {
      this._testCaseIndex = 0;
    } else {
      this._testCaseIndex++;
    }

    // update
    this._markTestCase(testCases[this._testCaseIndex]);
    this._plugin.updateView();
  }

  markPrevTestCase() {
    const { testCases } = this._plugin;

    if (this._testCaseIndex <= 0) {
      this._testCaseIndex = testCases.length - 1;
    } else {
      this._testCaseIndex--;
    }

    // update
    this._markTestCase(testCases[this._testCaseIndex]);
    this._plugin.updateView();
  }

  migrateTestCase() {
    const { testCases } = this._plugin;

    this._plugin.migrateTestCase(testCases[this._testCaseIndex]);
  }

  removeTestCase() {
    const { testCases } = this._plugin;

    this._plugin.removeTestCase(this._testCaseIndex);

    if (testCases.length !== 0) {
      this._testCaseIndex--;
      this.markNextTestCase();
    } else {
      this._testCaseIndex = -1;

      this._plugin.pathMarker.unmark();
      this._plugin.updateView();
    }
  }

  showModal(show) {
    this._showModal = show;
    this._plugin.editTestCase();
    this._plugin.updateView();
  }

  _markTestCase(testCase) {
    const selection = new PathSelection(testCase.path);
    
    selection.enrich(this._plugin.elementRegistry);

    const markers = [];
    if (selection.hasStart()) {
      markers.push({ id: selection.start, style: START });
    }
    for (let i = 1; i < selection.path.length - 1; i++) {
      markers.push({ id: selection.path[i], style: PATH });
    }
    if (selection.hasEnd()) {
      markers.push({ id: selection.end, style: END });
    }

    this._plugin.pathMarker.mark(markers);
    this._selection = selection;
  }

  get name() {
    return MODE_EDITOR;
  }

  get selection() {
    return this._selection;
  }

  get testCase() {
    return this._plugin.testCases[this._testCaseIndex];
  }

  get testCaseIndex() {
    return this._testCaseIndex;
  }
}
