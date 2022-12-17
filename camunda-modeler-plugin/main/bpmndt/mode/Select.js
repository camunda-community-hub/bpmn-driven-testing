import React from "react";

import Container from "../ui/Container";

export default class Select extends React.Component {
  constructor(props) {
    super(props);

    this.mode = props.mode;

    this.next = {onClick: this._handleClickNext, title: "Next path"};
    this.prev = {onClick: this._handleClickPrev, title: "Previous path"};

    this.actionAdd = {
      icon: "fas fa-plus",
      onClick: this._handleClickAdd,
      style: "success",
      title: "Add test case"
    };

    this.actionMigrate = {
      icon: "fas fa-check",
      onClick: this._handleClickMigrate,
      style: "primary",
      title: "Migrate test case"
    };

    this.actionPathAlreadyExists = {
      icon: "fas fa-exclamation-triangle",
      style: "danger",
      title: "Path already added"
    };
  }

  _handleClickNext = () => {
    this.mode.nextPath();
    this.forceUpdate();
  }
  _handleClickPrev = () => {
    this.mode.prevPath();
    this.forceUpdate();
  }

  _handleClickAdd = () => {
    this.mode.addTestCase();
    this.forceUpdate();
  }

  _handleClickMigrate = () => {
    this.mode.migrateTestCase();
    this.forceUpdate();
  }

  render() {
    const { migration, paths, pathEquality, pathIndex, selection } = this.mode;

    if (paths.length === 0) {
      return null;
    }

    let actionCenter;
    if (pathEquality[pathIndex]) {
      actionCenter = this.actionPathAlreadyExists;
    } else if (migration) {
      actionCenter = this.actionMigrate;
    } else {
      actionCenter = this.actionAdd;
    }

    const model = {
      next: paths.length > 1 ? this.next : undefined,
      prev: paths.length > 1 ? this.prev : undefined,
      content: {
        centerTop: `Path ${pathIndex + 1} / ${paths.length}`,
        centerBottom: `Length: ${selection.path.length} flow nodes`,
        leftTop: selection.start,
        leftBottom: selection.startType,
        onClick: actionCenter.onClick,
        rightTop: selection.end,
        rightBottom: selection.endType,
        title: actionCenter.title
      },
      actionCenter: pathEquality[pathIndex] || paths.length !== 0 ? actionCenter : undefined,
      
      pathEqual: pathEquality[pathIndex] 
    };

    return <CustomContainer model={model} />
  }
}

class CustomContainer extends Container {
  getBoxClassNames() {
    return this.props.model.pathEqual ? "box-not-allowed" : "box-pointer";
  }

  renderAction(model, className) {
    if (className === "container-center" && this.props.model.pathEqual) {
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
