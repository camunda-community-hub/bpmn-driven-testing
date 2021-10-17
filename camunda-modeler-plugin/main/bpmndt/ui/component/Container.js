import React from "react";

import Button from "./Button";

export default class Container extends React.Component {
  getBoxClassNames() {
    return "";
  }

  render() {
    const { state } = this.props.controller;

    const { viewModel } = state;
    if (viewModel === undefined) {
      return null;
    }

    return (
      <div className="container h-100">
        <div className="row h-100">
          <div className="col-1 h-100">
            <div className="v-center-l">
              {this.renderPrev(viewModel.prev)}
            </div>
          </div>
          <div className={`col-10 box ${this.getBoxClassNames()}`}>
            {this.renderContent(viewModel.content)}

            {this.renderAction(viewModel.actionLeft, "container-left")}
            {this.renderAction(viewModel.actionCenter, "container-center")}
            {this.renderAction(viewModel.actionRight, "container-right")}
          </div>
          <div className="col-1 h-100">
            <div className="v-center-r">
              {this.renderNext(viewModel.next)}
            </div>
          </div>
        </div>
      </div>
    )
  }

  renderNext(next) {
    if (next === undefined) {
      return null;
    }

    return (
      <Button onClick={next.onClick} title={next.title}>
        <i className="fas fa-angle-right"></i>
      </Button>
    )
  }
  renderPrev(prev) {
    if (prev === undefined) {
      return null;
    }

    return (
      <Button onClick={prev.onClick} title={prev.title}>
        <i className="fas fa-angle-left"></i>
      </Button>
    )
  }

  renderContent(content) {
    return (
      <div className="container" onClick={content.onClick} title={content.title}>
        <div className="row">
          <div className="col text-center">
            <span style={{display: "inline-block", marginBottom: "0.5rem"}}>{content.centerTop}</span>
            <br />
            <span>{content.centerBottom}</span>
          </div>
        </div>
        <div className="row">
          <div className="col-6 text-center">
            <h3>{content.leftTop}</h3>
            <span>{content.leftBottom}</span>
          </div>
          <div className="col-6 text-center">
            <h3>{content.rightTop}</h3>
            <span>{content.rightBottom}</span>
          </div>
        </div>
      </div>
    )
  }

  renderAction(model, className) {
    if (model === undefined) {
      return null;
    }

    return (
      <div className={className}>
        <Button
          onClick={model.onClick}
          small
          style={model.style}
          title={model.title}
        >
          <i className={model.icon}></i>
        </Button>
      </div>
    )
  }
}
