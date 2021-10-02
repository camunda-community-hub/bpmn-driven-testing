import { MODE_COVERAGE } from "../Constants";

export default class CoverageMode {
  constructor(plugin) {
    this._plugin = plugin;
  }

  enable() {
    const { pathMarker, testCases } = this._plugin;

    const elements = new Set();
    for (const testCase of testCases) {
      testCase.path.forEach(element => elements.add(element));
    }

    pathMarker.markAll(elements);
  }

  disable() {
    const { pathMarker } = this._plugin;

    pathMarker.unmark();
  }

  reset() {
    // nothing to do here
  }

  get name() {
    return MODE_COVERAGE;
  }
}
