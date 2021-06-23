import React from "react";

import Button from "../view/component/Button";

export default class SelectorModeView extends React.Component {
  constructor(props) {
    super(props);

    this._handleClickAdd = this._handleClickAdd.bind(this);

    this._handleClickNext = this._handleClickNext.bind(this);
    this._handleClickPrev = this._handleClickPrev.bind(this);
  }

  _handleClickAdd() {
    const { mode } = this.props;

    if (mode.pathEquality) {
      return;
    }

    mode.addPath();
  }

  _handleClickNext() {
    this.props.mode.markNextPath();
  }
  _handleClickPrev() {
    this.props.mode.markPrevPath();
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
            {this._renderActionCenter(mode)}
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

    return (
      <div className="container" onClick={this._handleClickAdd} title={mode.pathEquality ? "Path already added" : "Add test case"}>
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
        <div className="container-center">
          <div className="icon-danger" title="Path already added">
            <i className="fas fa-exclamation-triangle" />
          </div>
        </div>
      )
    } else {
      return (
        <div className="container-center">
          <Button small onClick={this._handleClickAdd} style="success" title="Add test case">
            <i className="fas fa-plus" />
          </Button>
        </div>
      )
    }
  }
}
