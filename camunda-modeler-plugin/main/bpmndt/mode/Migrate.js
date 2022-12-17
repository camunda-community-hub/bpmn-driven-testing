import React from "react";

import {
  MODE_SELECT,
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../constants";

import Container from "../ui/Container";

export default class Migrate extends React.Component {
  constructor(props) {
    super(props);

    this.mode = props.mode;

    this.next = {onClick: this._handleClickNext, title: "Next problem"};
    this.prev = {onClick: this._handleClickPrev, title: "Previous problem"};
  }

  _handleClickNext = () => {
    this.mode.nextProblem();
    this.forceUpdate();
  }
  _handleClickPrev = () => {
    this.mode.prevProblem();
    this.forceUpdate();
  }

  _handleClickResolve = () => {
    this.mode.toggle(MODE_SELECT);
  }

  render() {
    const { problem, problems } = this.mode;

    const texts = this._getTexts(problem);

    const actionCenter = {
      icon: "far fa-hand-pointer",
      onClick: this._handleClickResolve,
      style: "primary",
      title: texts[2]
    };

    const model = {
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
    };

    return <Container model={model} />
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
}
