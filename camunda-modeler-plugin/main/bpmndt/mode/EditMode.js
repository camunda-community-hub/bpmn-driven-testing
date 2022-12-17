import { MODE_EDIT } from "../constants";

import BaseMode from "./BaseMode";

export default class EditMode extends BaseMode {
  constructor(plugin, oldMode) {
    super(plugin, MODE_EDIT);
    
    this.testCaseIndex = oldMode.testCaseIndex;

    this.updateMarkers();
  }

  get testCase() {
    const { plugin, testCaseIndex } = this;
    return plugin.testCases[testCaseIndex];
  }

  setDescription(description) {
    this.testCase.description = description;
    this.plugin.markAsChanged();
  }

  setName(name) {
    this.testCase.name = name;
    this.plugin.markAsChanged();
  }

  updateMarkers() {
    const { plugin, testCase } = this;
    plugin.mark(testCase.markers);
  }
}
