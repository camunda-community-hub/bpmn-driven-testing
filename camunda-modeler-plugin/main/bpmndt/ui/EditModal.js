import React from "react";

import { MODE_EDIT } from "../constants";

export default class EditModal extends React.Component {
  _handleChangeDescription = (e) => {
    this.props.mode.setDescription(e.target.value);
    this.forceUpdate();
  }

  _handleChangeName = (e) => {
    this.props.mode.setName(e.target.value);
    this.forceUpdate();
  }

  render() {
    const { mode } = this.props;

    if (mode.id !== MODE_EDIT) {
      return null;
    }

    const { description, name } = mode.testCase;

    return (
      <div className="modal-wrapper">
        <div className="container">
          <div className="row">
            <div className="col-1"></div>
            <div className="col-10 box" style={{padding: "10px"}}>
              <div className="container">
                <div className="row" style={{"marginTop": "10px", "marginBottom": "10px"}}>
                  <div className="col">
                    <input className="text-center" type="text" placeholder="Name" value={name || ""} onChange={this._handleChangeName} />
                  </div>
                </div>
                <div className="row" style={{"marginTop": "10px", "marginBottom": "10px"}}>
                  <div className="col">
                    <textarea placeholder="Description" rows="3" value={description || ""} onChange={this._handleChangeDescription} />
                  </div>
                </div>
              </div>
            </div>
            <div className="col-1"></div>
          </div>
        </div>
      </div>
    )
  }
}
