import React from "react";

import { START, END, PATH, UNRESOLVABLE } from "../PathValidator";

import Button from "../component/Button";

const getTexts = (problem) => {
  switch (problem.type) {
    case START:
      return [`Start node '${problem.missing}' is missing`, "Please select a new start node", "Select start node"];
    case END:
      return [`End node '${problem.missing}' is missing`, "Please select a new end node", "Select end node"];
    case PATH:
      return ["Path in between is invalid", "Please choose one of the possible paths", "Choose path"];
    case UNRESOLVABLE:
      return ["Path problem is unresolvable", "Please select a new path", "Select path"];
  }
};

export default class Migration extends React.Component {
  _handleClickNext = () => {
    this.props.mode.nextProblem();
  }
  _handleClickPrev = () => {
    this.props.mode.prevProblem();
  }

  _handleClickResolveProblem = () => {
    this.props.mode.resolveProblem();
  }

  render() {
    const { mode } = this.props;

    const texts = getTexts(mode.problem);

    return (
      <div className="container h-100">
        <div className="row h-100">
          <div className="col-1 h-100">
            <div className="v-center-l">
              {this._renderPrevButton(mode.hasMultipleProblems())}
            </div>
          </div>
          <div className="col-10 box">
            {this._renderProblem(mode, texts)}
            <div className="container-center">
              {this._renderActionCenter(texts)}
            </div>
          </div>
          <div className="col-1 h-100">
            <div className="v-center-r">
              {this._renderNextButton(mode.hasMultipleProblems())}
            </div>
          </div>
        </div>
      </div>
    )
  }

  _renderNextButton(visible) {
    return visible ? (
      <Button onClick={this._handleClickNext} title="Next problem">
        <i className="fas fa-angle-right"></i>
      </Button>
    ) : null
  }
  _renderPrevButton(visible) {
    return visible ? (
      <Button onClick={this._handleClickPrev} title="Previous problem">
        <i className="fas fa-angle-left"></i>
      </Button>
    ) : null
  }

  _renderProblem(mode, texts) {
    const { problem } = mode;

    return (
      <div className="container" onClick={this._handleClickAdd}>
        <div className="row">
          <div className="col text-center">
            <span style={{display: "inline-block", marginBottom: "0.5rem"}}>{texts[0]}</span>
            <br />
            <span>{texts[1]}</span>
          </div>
        </div>
        <div className="row">
          <div className="col-6 text-center">
            <h3>{problem.start || "?"}</h3>
            <span>{problem.startType || ""}</span>
          </div>
          <div className="col-6 text-center">
            <h3>{problem.end || "?"}</h3>
            <span>{problem.endType || ""}</span>
          </div>
        </div>
      </div>
    )
  }

  _renderActionCenter(texts) {
    return (
      <Button
        onClick={this._handleClickResolveProblem}
        small
        style="primary"
        title={texts[2]}
      >
        <i className="far fa-hand-pointer"></i>
      </Button>
    )
  }
}
