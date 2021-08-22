import React from "react";

import Button from "../component/Button";

export default class Editor extends React.Component {
  constructor(props) {
    super(props);

    this._handleClickNext = this._handleClickNext.bind(this);
    this._handleClickPrev = this._handleClickPrev.bind(this);

    this._handleClickEdit = this._handleClickEdit.bind(this);
    this._handleClickRemove = this._handleClickRemove.bind(this);
  }

  _handleClickNext() {
    this.props.mode.markNextTestCase();
  }
  _handleClickPrev() {
    this.props.mode.markPrevTestCase();
  }

  _handleClickEdit() {
    this.props.mode.showModal(true);
  }
  _handleClickRemove() {
    this.props.mode.removeTestCase();
  }

  render() {
    const { mode, testCases } = this.props;

    if (testCases.length === 0) {
      return null;
    }

    return (
      <div className="container h-100">
        <div className="row h-100">
          <div className="col-1 h-100">
            <div className="v-center-l">
              {this._renderPrevButton(testCases.length > 1)}
            </div>
          </div>
          <div className="col-10 box">
            {this._renderTestCase(mode, testCases)}
            {this._renderActionCenter(mode)}
            {this._renderActionRight(mode)}
          </div>
          <div className="col-1 h-100">
            <div className="v-center-r">
              {this._renderNextButton(testCases.length > 1)}
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
      <Button onClick={this._handleClickNext} title="Next test case">
        <i className="fas fa-angle-right"></i>
      </Button>
    )
  }

  _renderPrevButton(visible) {
    if (!visible) {
      return null;
    }

    return (
      <Button onClick={this._handleClickPrev} title="Previous test case">
        <i className="fas fa-angle-left"></i>
      </Button>
    )
  }

  _renderTestCase(mode, testCases) {
    const { testCaseIndex, selection } = mode;

    const testCase = testCases[testCaseIndex];

    return (
      <div className="container" onClick={this._handleClickAdd}>
        <div className="row">
          <div className="col text-center">
            <span style={{display: "inline-block", marginBottom: "0.5rem"}}>Test case {testCaseIndex + 1} / {testCases.length}</span>
            <br />
            <span>{testCase.name || `Length: ${testCase.path.length} Flow nodes`}</span>
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
    if (mode.isModalShown()) {
      return null;
    }

    return (
      <div className="container-center">
        <Button
          onClick={this._handleClickRemove}
          small
          style="danger"
          title="Remove test case"
        >
          <i className="fas fa-trash-alt"></i>
        </Button>
      </div>
    )
  }
  _renderActionRight(mode) {
    if (mode.isModalShown()) {
      return null;
    }

    return (
      <div className="container-right">
        <Button
          onClick={this._handleClickEdit}
          small
          style="primary"
          title="Edit test case"
        >
          <i className="fas fa-pencil-alt"></i>
        </Button>
      </div>
    )
  }
}
