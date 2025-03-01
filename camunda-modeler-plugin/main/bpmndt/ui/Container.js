import React from "react";

import Button from "./Button";

export default class Container extends React.Component {
  getBoxClassNames() {
    return "";
  }

  render() {
    const { model } = this.props;

    return (
      <div className="container h-100">
        <div className="row h-100">
          <div className="col-1 h-100">
            <div className="v-center-l">
              {this.renderPrev(model.prev)}
            </div>
          </div>
          <div className={`col-10 box ${this.getBoxClassNames()}`}>
            {this.renderContent(model.content)}

            {this.renderAction(model.actionLeft, "container-left")}
            {this.renderAction(model.actionCenter, "container-center")}
            {this.renderAction(model.actionRight, "container-right")}
          </div>
          <div className="col-1 h-100">
            <div className="v-center-r">
              {this.renderNext(model.next)}
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
            <div className="text-overflow" title={content.leftTop}>
              <b>{content.leftTop}</b>
            </div>
            <div className="text-overflow" title={content.leftBottom}>{content.leftBottom}</div>
          </div>
          <div className="col-6 text-center">
            <div className="text-overflow" title={content.rightTop}>
              <b>{content.rightTop}</b>
            </div>
            <div className="text-overflow" title={content.rightBottom}>{content.rightBottom}</div>
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
