import { MARKER, MODE_SHOW_COVERAGE } from "../constants";

import BaseMode from "./BaseMode";

export default class ShowCoverageMode extends BaseMode {
  constructor(controller) {
    super(controller);

    this.id = MODE_SHOW_COVERAGE;
  }

  computeInitialState(ctx) {
    const { testCases } = ctx;

    const flowNodeIds = new Set();
    for (const testCase of testCases) {
      testCase.path.forEach(flowNodeId => flowNodeIds.add(flowNodeId));
    }

    const markers = [];
    flowNodeIds.forEach(flowNodeId => {
      markers.push({id: flowNodeId, style: MARKER});
    });

    return {markers: markers};
  }

  computeViewModel() {
    // nothing to do here
  }
}
