import React from "react";

import { MODE_EDIT } from "../constants";

export default class EditModalBackdrop extends React.Component {
  _handleClick = () => {
    this.props.controller.handleToggleMode(MODE_EDIT);
  }

  render() {
    const { state } = this.props.controller;

    if (!state.activeModes[MODE_EDIT]) {
      return null;
    }

    return <div className="modal-backdrop" onClick={this._handleClick}></div>
  }
}
