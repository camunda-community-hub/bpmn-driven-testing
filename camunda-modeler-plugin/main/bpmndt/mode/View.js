import React from "react";

import {
  MODE_EDIT,
  MODE_MIGRATE
} from "../constants";

import Container from "../ui/Container";

export default class View extends React.Component {
  constructor(props) {
    super(props);

    this.mode = props.mode;

    this.next = {onClick: this._handleClickNext, title: "Next test case"};
    this.prev = {onClick: this._handleClickPrev, title: "Previous test case"};

    this.actionCenter = {
      icon: "fas fa-trash-alt",
      onClick: this._handleClickRemove,
      style: "danger",
      title: "Remove test case"
    };
    this.actionRight = {
      icon: "fas fa-pencil-alt",
      onClick: this._handleClickEdit,
      style: "primary",
      title: "Edit test case"
    };
  }

  _handleClickNext = () => {
    this.mode.nextTestCase();
    this.forceUpdate();
  }
  _handleClickPrev = () => {
    this.mode.prevTestCase();
    this.forceUpdate();
  }

  _handleClickEdit = () => {
    this.mode.toggle(MODE_EDIT);
  }
  _handleClickMigrate = () => {
    this.mode.toggle(MODE_MIGRATE);
  }
  _handleClickRemove = () => {
    this.mode.removeTestCase();
    this.forceUpdate();
  }

  render() {
    const { testCases, testCaseIndex } = this.mode;
    if (testCaseIndex === -1) {
      return null;
    }

    const testCase = testCases[testCaseIndex];

    let actionLeft;
    if (!testCase.valid) {
      const problemCount = testCase.problems.length;

      actionLeft = {
        icon: "fas fa-exclamation",
        onClick: this._handleClickMigrate,
        style: "warning",
        title: `Path is invalid - click to resolve ${problemCount} problem${problemCount > 1 ? "s" : ""}`
      };
    }

    const model = {
      next: testCases.length > 1 ? this.next : undefined,
      prev: testCases.length > 1 ? this.prev : undefined,
      content: {
        centerTop: `Test case ${testCaseIndex + 1} / ${testCases.length}`,
        centerBottom: testCase.name || `Length: ${testCase.path.length} flow nodes`,
        leftTop: testCase.start,
        leftBottom: testCase.startType,
        rightTop: testCase.end,
        rightBottom: testCase.endType
      },
      actionLeft: actionLeft,
      actionCenter: this.actionCenter,
      actionRight: this.actionRight
    };

    return <Container model={model} />
  }
}
