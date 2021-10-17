import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_VIEW
} from "../constants";

import { getMarkers } from "../functions";

import BaseMode from "./BaseMode";

export default class ViewMode extends BaseMode {
  constructor(controller) {
    super(controller);

    this.id = MODE_VIEW;

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

  computeInitialState(ctx) {
    const { testCases } = ctx;

    testCases.forEach(testCase => {
      testCase.enrich(this.controller.elementRegistry);
    });

    let testCaseIndex = this.state.testCaseIndex || -1;
    if (testCases.length !== 0 && testCaseIndex === -1) {
      testCaseIndex = 0;
    }

    let markers;
    if (testCaseIndex !== -1) {
      markers = getMarkers(testCases[testCaseIndex]);
    } else {
      markers = [];
    }

    return {markers: markers, testCases: testCases, testCaseIndex: testCaseIndex};
  }

  computeViewModel() {
    const { testCases, testCaseIndex } = this.state;

    if (testCaseIndex === -1) {
      // return undefined to indicate that nothing should be rendered
      return;
    }

    const { testCase } = this;

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

    return {
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
    }
  }

  _handleClickNext = () => {
    this._setTestCaseIndex(this.state.testCaseIndex + 1);
  }
  _handleClickPrev = () => {
    this._setTestCaseIndex(this.state.testCaseIndex - 1);
  }

  _handleClickEdit = () => {
    this.setMode(MODE_EDIT, {testCase: this.testCase});
  }

  _handleClickMigrate = () => {
    this.setMode(MODE_MIGRATE, {testCase: this.testCase, testCases: this.state.testCases});
  }

  _handleClickRemove = () => {
    const { testCases, testCaseIndex } = this.state;

    // remove current test case
    testCases.splice(testCaseIndex, 1);

    let newIndex = testCaseIndex - 1;
    if (testCases.length !== 0 && newIndex === -1) {
      newIndex = 0;
    }

    let markers;
    if (newIndex !== -1) {
      markers = getMarkers(testCases[newIndex]);
    } else {
      markers = [];
    }

    this.setState({testCaseIndex: newIndex, markers: markers, markAsChanged: true});
  }

  _setTestCaseIndex(testCaseIndex) {
    const { testCases } = this.state;

    let newIndex = testCaseIndex;
    if (testCaseIndex > testCases.length - 1) {
      newIndex = 0;
    }
    if (testCaseIndex < 0) {
      newIndex = testCases.length - 1;
    }

    this.setState({markers: getMarkers(testCases[newIndex]), testCaseIndex: newIndex});
  }

  /**
   * Gets the current test case.
   */
  get testCase() {
    const { testCases, testCaseIndex } = this.state;
    return testCases[testCaseIndex];
  }
}
