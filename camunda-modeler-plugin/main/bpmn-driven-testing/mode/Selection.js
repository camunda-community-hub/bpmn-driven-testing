import React from "react";

import Button from "../component/Button";

export default class Selection extends React.Component {
  _handleClickNext = () => {
    this.props.mode.nextPath();
  }
  _handleClickPrev = () => {
    this.props.mode.prevPath();
  }

  _handleClickAddTestCase = () => {
    this.props.mode.addTestCase();
  }

  _handleClickMigrateTestCase = () => {
    this.props.mode.migrateTestCase();
  }

  render() {
    const { mode } = this.props;

    if (mode.paths.length === 0) {
      return null;
    }

    return (
      <div className="container h-100">
        <div className="row h-100">
          <div className="col-1 h-100">
            <div className="v-center-l">
              {this._renderPrevButton(mode.paths.length > 1)}
            </div>
          </div>
          <div className={`col-10 box ${mode.pathEquality ? "box-not-allowed" : "box-pointer"}`}>
            {this._renderSelectedPath(mode)}
            <div className="container-center">
              {this._renderActionCenter(mode)}
            </div>
          </div>
          <div className="col-1 h-100">
            <div className="v-center-r">
              {this._renderNextButton(mode.paths.length > 1)}
            </div>
          </div>
        </div>
      </div>
    )
  }

  _renderNextButton(visible) {
    if (!visible) {
      return null;
    }

    return (
      <Button onClick={this._handleClickNext} title="Next path">
        <i className="fas fa-angle-right"></i>
      </Button>
    )
  }

  _renderPrevButton(visible) {
    if (!visible) {
      return null;
    }

    return (
      <Button onClick={this._handleClickPrev} title="Previous path">
        <i className="fas fa-angle-left"></i>
      </Button>
    )
  }

  _renderSelectedPath(mode) {
    const { paths, pathIndex, selection } = mode;

    let onClick;
    let title;
    if (mode.pathEquality) {
      onClick = null;
      title = "Path already added";
    } else if (mode.isMigration()) {
      onClick = this._handleClickMigrateTestCase;
      title = "Migrate test case";
    } else {
      onClick = this._handleClickAddTestCase;
      title = "Add test case";
    }

    return (
      <div className="container" onClick={onClick} title={title}>
        <div className="row">
          <div className="col text-center">
            <span style={{display: "inline-block", marginBottom: "0.5rem"}}>Path {pathIndex + 1} / {paths.length}</span>
            <br />
            <span>Length: {selection.path.length} Flow nodes</span>
          </div>
        </div>
        <div className="row">
          <div className="col-6 text-center">
            <h3>{selection.start}</h3>
            <span>{selection.startType}</span>
          </div>
          <div className="col-6 text-center">
            <h3>{selection.end}</h3>
            <span>{selection.endType}</span>
          </div>
        </div>
      </div>
    )
  }

  _renderActionCenter(mode) {
    if (mode.pathEquality) {
      return (
        <div className="icon-danger" title="Path already added">
          <i className="fas fa-exclamation-triangle" />
        </div>
      )
    } else if (mode.isMigration()) {
      return (
        <Button small onClick={this._handleClickMigrateTestCase} style="primary" title="Migrate test case">
          <i className="fas fa-check" />
        </Button>
      )
    } else {
      return (
        <Button small onClick={this._handleClickAddTestCase} style="success" title="Add test case">
          <i className="fas fa-plus" />
        </Button>
      )
    }
  }
}
