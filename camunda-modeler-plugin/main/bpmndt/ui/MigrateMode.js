import {
  MARKER_END,
  MARKER_OLD,
  MARKER_OLD_BRIGHT,
  MARKER_START,
  MODE_MIGRATE,
  MODE_SELECT,
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../constants";

import BaseMode from "./BaseMode";
import TestCaseMigration from "../TestCaseMigration";

export default class MigrateMode extends BaseMode {
  constructor(controller) {
    super(controller);

    this.id = MODE_MIGRATE;

    this.next = {onClick: this._handleClickNext, title: "Next problem"};
    this.prev = {onClick: this._handleClickPrev, title: "Previous problem"};
  }

  computeInitialState(ctx) {
    const { testCase, testCases } = ctx;

    for (const problem of testCase.problems) {
      this._enrich(problem);
    }

    return {
      markers: this._getMarkers(testCase, testCase.problems[0]),
      problems: testCase.problems,
      problemIndex: 0,
      testCase: testCase,
      testCases: testCases
    };
  }

  computeViewModel() {
    const { problems } = this.state;

    const problem = this.problem;
    const texts = this._getTexts(problem);

    const actionCenter = {
      icon: "far fa-hand-pointer",
      onClick: this._handleClickResolve,
      style: "primary",
      title: texts[2]
    };

    return {
      next: problems.length > 1 ? this.next : undefined,
      prev: problems.length > 1 ? this.prev : undefined,
      content: {
        centerTop: texts[0],
        centerBottom: texts[1],
        leftTop: problem.start || "?",
        leftBottom: problem.startType || "",
        rightTop: problem.end || "?",
        rightBottom: problem.endType || ""
      },
      actionCenter: actionCenter
    }
  }

  _enrich(problem) {
    const { elementRegistry } = this.controller;

    if (problem.start) {
      problem.startType = elementRegistry.get(problem.start)?.type;
    }
    if (problem.end) {
      problem.endType = elementRegistry.get(problem.end)?.type;
    }
  }

  _getMarkers(testCase, problem) {
    const markers = [];
    for (const flowNodeId of testCase.path) {
      if (flowNodeId === problem.start || flowNodeId === problem.end) {
        continue;
      }

      markers.push({id: flowNodeId, style: MARKER_OLD});
    }

    switch (problem.type) {
      case PROBLEM_START:
        markers.push({id: problem.end, style: MARKER_END});
        break;
      case PROBLEM_END:
        markers.push({id: problem.start, style: MARKER_START});
        break;
      case PROBLEM_PATH:
        markers.push({id: problem.start, style: MARKER_OLD_BRIGHT});
        markers.push({id: problem.end, style: MARKER_OLD_BRIGHT});
        break;
      case PROBLEM_UNRESOLVABLE:
        // nothing to do here
        break;
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }

    return markers;
  }

  _getTexts(problem) {
    switch (problem.type) {
      case PROBLEM_START:
        return [`Start node '${problem.missing}' is missing`, "Please select a new start node", "Select start node"];
      case PROBLEM_END:
        return [`End node '${problem.missing}' is missing`, "Please select a new end node", "Select end node"];
      case PROBLEM_PATH:
        return ["Path in between is invalid", "Please choose one of the possible paths", "Choose path"];
      case PROBLEM_UNRESOLVABLE:
        return ["Path problem is unresolvable", "Please select a new path", "Select path"];
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }
  }

  _handleClickNext = () => {
    this._setProblemIndex(this.state.problemIndex + 1);
  }
  _handleClickPrev = () => {
    this._setProblemIndex(this.state.problemIndex - 1);
  }

  _handleClickResolve = () => {
    const { testCase, testCases } = this.state;

    this.setMode(MODE_SELECT, {migration: new TestCaseMigration(testCase, this.problem), testCases: testCases});
  }

  _setProblemIndex(problemIndex) {
    const { problems, testCase } = this.state;

    let newIndex = problemIndex;
    if (problemIndex > problems.length - 1) {
      newIndex = 0;
    }
    if (problemIndex < 0) {
      newIndex = problems.length - 1;
    }

    this.setState({markers: this._getMarkers(testCase, problems[newIndex]), problemIndex: newIndex});
  }

  /**
   * Gets the current problem.
   */
  get problem() {
    const { problems, problemIndex } = this.state;
    return problems[problemIndex];
  }
}
