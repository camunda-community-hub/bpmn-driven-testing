import React from "react";

export default class EditorModeModal extends React.Component {
  constructor(props) {
    super(props);

    this._handleChangeDescription = this._handleChangeDescription.bind(this);
    this._handleChangeName = this._handleChangeName.bind(this);
  }

  _handleChangeDescription(e) {
    const { testCase } = this.props.mode;

    testCase.description = e.target.value;
    this.forceUpdate();
  }

  _handleChangeName(e) {
    const { testCase } = this.props.mode;

    testCase.name = e.target.value;
    this.forceUpdate();
  }

  render() {
    return (
      <div className="modal-wrapper">
        <div className="container">
          <div className="row">
            <div className="col-1"></div>
            <div className="col-10 box" style={{padding: "10px"}}>
              {this._renderForm()}
            </div>
            <div className="col-1"></div>
          </div>
        </div>
      </div>
    )
  }

  _renderForm() {
    const { testCase } = this.props.mode;

    return (
      <div className="container">
        <div className="row" style={this._getMargin()}>
          <div className="col">
            <input className="text-center" type="text" placeholder="Name" value={testCase.name || ""} onChange={this._handleChangeName} />
          </div>
        </div>
        <div className="row" style={this._getMargin()}>
          <div className="col">
            <textarea placeholder="Description" rows="3" value={testCase.description || ""} onChange={this._handleChangeDescription} />
          </div>
        </div>
      </div>
    )
  }

  _getMargin() {
    return {"marginTop": "10px", "marginBottom": "10px"};
  }
}
