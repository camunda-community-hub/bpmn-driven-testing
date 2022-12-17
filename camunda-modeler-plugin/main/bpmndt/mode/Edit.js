import React from "react";

import Container from "../ui/Container";

export default class Edit extends React.Component {
  constructor(props) {
    super(props);

    this.mode = props.mode;
  }

  render() {
    const { testCase } = this.mode;

    const model = {
      content: {
        centerTop: "Edit test case",
        centerBottom: `Length: ${testCase.path.length} flow nodes`,
        leftTop: testCase.start,
        leftBottom: testCase.startType,
        rightTop: testCase.end,
        rightBottom: testCase.endType
      }
    };

    return <Container model={model} />
  }
}
