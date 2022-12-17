import { MARKER, MODE_SHOW_COVERAGE } from "../constants";

import BaseMode from "./BaseMode";

export default class ShowCoverageMode extends BaseMode {
  constructor(plugin) {
    super(plugin, MODE_SHOW_COVERAGE);

    this.updateMarkers();
  }

  updateMarkers() {
    const { plugin } = this;

    const flowNodeIds = new Set();
    for (const testCase of plugin.testCases) {
      testCase.path.forEach(flowNodeId => flowNodeIds.add(flowNodeId));
    }

    const markers = [];
    flowNodeIds.forEach(flowNodeId => {
      markers.push({id: flowNodeId, style: MARKER});
    });

    plugin.mark(markers);
  }
}
