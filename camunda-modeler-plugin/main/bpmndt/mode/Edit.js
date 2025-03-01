import React from "react";

import { MODE_EDIT } from "../constants";

import Container from "../ui/Container";

export default class Edit extends React.Component {
  constructor(props) {
    super(props);

    this.mode = props.mode;

    this.actionToggle = {
      icon: "fas fa-times",
      onClick: () => this.mode.toggle(MODE_EDIT),
      style: "secondary",
      title: "Close modal"
    };
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
      },
      actionRight: this.actionToggle,
    };

    return <Container model={model} />
  }
}
