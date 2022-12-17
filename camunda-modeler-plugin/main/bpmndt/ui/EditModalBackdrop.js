import React from "react";

import { MODE_EDIT } from "../constants";

export default class EditModalBackdrop extends React.Component {
  _handleClick = () => {
    this.props.mode.toggle(MODE_EDIT);
  }

  render() {
    const { mode } = this.props;

    if (mode.id !== MODE_EDIT) {
      return null;
    }

    return <div className="modal-backdrop" onClick={this._handleClick}></div>
  }
}
