import PathSelection from "../PathSelection";

export default class EditorMode {
  constructor(plugin) {
    this._plugin = plugin;

    this._modal = false;
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
    this._plugin.pathMarker.mark(null);
  }

  markNextTestCase() {
    const { testCases } = this._plugin;

    if (this._testCaseIndex >= (testCases.length - 1)) {
      this._testCaseIndex = 0;
    } else {
      this._testCaseIndex++;
    }

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

    this._markTestCase(testCases[this._testCaseIndex]);

    this._plugin.updateView();
  }

  removeTestCase() {
    const { testCases } = this._plugin;

    this._plugin.removeTestCase(this._testCaseIndex);

    if (testCases.length !== 0) {
      this._testCaseIndex--;
      this.markNextTestCase();
    } else {
      this._testCaseIndex = -1;

      this._plugin.pathMarker.mark(null);
      this._plugin.updateView();
    }
  }

  _markTestCase(testCase) {
    this._selection = new PathSelection(testCase.path);
    this._selection.enrich(this._plugin.elementRegistry);

    this._plugin.pathMarker.mark(this._selection);
  }

  get modal() {
    return this._modal;
  }

  get name() {
    return "editor";
  }

  get testCase() {
    return this._plugin.testCases[this._testCaseIndex];
  }
  get testCaseIndex() {
    return this._testCaseIndex;
  }

  get selection() {
    return this._selection;
  }

  set modal(modal) {
    this._modal = modal;
    this._plugin.editTestCase();
    this._plugin.updateView();
  }
}
