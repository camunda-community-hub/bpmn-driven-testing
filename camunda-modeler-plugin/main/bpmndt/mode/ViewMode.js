import { MODE_VIEW } from "../constants";
import { next, prev } from "../functions"

import BaseMode from "./BaseMode";

export default class ViewMode extends BaseMode {
  constructor(plugin, oldMode) {
    super(plugin, MODE_VIEW);

    this.activeModes = new Set();

    const { testCases } = this;

    let testCaseIndex;
    if (oldMode?.testCaseIndex) {
      // show test case of previous mode
      testCaseIndex = oldMode.testCaseIndex;
    } else {
      testCaseIndex = next(testCases, -1);
    }

    this.testCaseIndex = testCaseIndex;

    this.updateMarkers();
  }

  get testCases() {
    return this.plugin.testCases;
  }

  nextTestCase() {
    const { testCases, testCaseIndex } = this;

    this.testCaseIndex = next(testCases, testCaseIndex);
    this.updateMarkers();
  }

  prevTestCase() {
    const { testCases, testCaseIndex } = this;

    this.testCaseIndex = prev(testCases, testCaseIndex);
    this.updateMarkers();
  }

  removeTestCase() {
    const { plugin, testCases, testCaseIndex } = this;

    testCases.splice(testCaseIndex, 1);
    plugin.markAsChanged();

    this.testCaseIndex = prev(testCases, testCaseIndex);
    this.updateMarkers();
  }

  updateMarkers() {
    const { plugin, testCases, testCaseIndex } = this;

    if (testCaseIndex === -1) {
      plugin.mark([]);
    } else {
      plugin.mark(testCases[testCaseIndex].markers);
    }
  }
}
