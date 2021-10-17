import React from "react";

import Container from "./component/Container";

export default class Select extends Container {
  getBoxClassNames() {
    const { pathEquality, pathIndex } = this.props.controller.mode.state;

    return pathEquality[pathIndex] ? "box-not-allowed" : "box-pointer";
  }

  renderAction(model, className) {
    const { pathEquality, pathIndex } = this.props.controller.mode.state;

    if (className === "container-center" && pathEquality[pathIndex]) {
      return (
        <div className={className}>
          <div className="icon-danger" title={model.title}>
            <i className={model.icon} />
          </div>
        </div>
      )
    } else {
      return super.renderAction(model, className);
    }
  }
}
