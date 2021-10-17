import { MODE_EDIT } from "../constants";
import { getMarkers } from "../functions";

import BaseMode from "./BaseMode";

export default class ViewMode extends BaseMode {
  constructor(controller) {
    super(controller);

    this.id = MODE_EDIT;
  }

  computeInitialState(ctx) {
    const { testCase } = ctx;

    return {testCase: testCase, markers: getMarkers(testCase)};
  }

  computeViewModel() {
    const { testCase } = this.state;

    return {
      content: {
        centerTop: "Edit test case",
        centerBottom: `Length: ${testCase.path.length} flow nodes`,
        leftTop: testCase.start,
        leftBottom: testCase.startType,
        rightTop: testCase.end,
        rightBottom: testCase.endType
      }
    }
  }

  handleChangeDescription = (e) => {
    const { testCase } = this.state;

    testCase.description = e.target.value;
    this.setState({markAsChanged: true});
  }

  handleChangeName = (e) => {
    const { testCase } = this.state;

    testCase.name = e.target.value;
    this.setState({markAsChanged: true});
  }
}
