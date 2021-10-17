import React from "react";

import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_SELECT,
  MODE_SHOW_COVERAGE
} from "../constants";

import Button from "./component/Button";

export default class ModeButtonGroup extends React.Component {
  constructor(props) {
    super(props);

    this._handleToggleMigrate = this._handleToggleMode.bind(this, MODE_MIGRATE);
    this._handleToggleSelect = this._handleToggleMode.bind(this, MODE_SELECT);
    this._handleToggleShowCoverage = this._handleToggleMode.bind(this, MODE_SHOW_COVERAGE);
  }

  _handleHidePlugin = () => {
    this.props.controller.hidePlugin();
  }

  _handleToggleMode(modeId) {
    this.props.controller.handleToggleMode(modeId);
  }

  render() {
    const { activeModes } = this.props.controller.state;

    return (
      <div className="view-modes">
        <div style={{float: "left", marginBottom: "2rem"}}>
          <Button onClick={this._handleHidePlugin} title="Hide plugin view">
            <i className="fas fa-times" style={{"color": "#dc3545"}}></i>
          </Button>
        </div>

        {this._renderSelect(activeModes[MODE_SELECT])}
        {this._renderShowCoverage(activeModes[MODE_SHOW_COVERAGE])}
        {this._renderEdit(activeModes[MODE_EDIT])}
        {this._renderMigrate(activeModes[MODE_MIGRATE])}
      </div>
    )
  }

  _renderEdit(active) {
    if (!active) {
      return null;
    }

    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button style="primary">
          <i className="fas fa-pencil-alt"></i>
        </Button>
      </div>
    )
  }

  _renderMigrate(active) {
    if (!active) {
      return null;
    }

    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleMigrate}
          style="primary"
          title="Cancel migration"
        >
          <i className="fas fa-tools"></i>
        </Button>
      </div>
    )
  }

  _renderSelect(active) {
    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleSelect}
          style={active ? "primary" : "secondary"}
          title={`${active ? "Disable" : "Enable"} path selection`}
        >
          <i className="far fa-hand-pointer"></i>
        </Button>
      </div>
    )
  }

  _renderShowCoverage(active) {
    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleShowCoverage}
          style={active ? "primary" : "secondary"}
          title={`${active ? "Hide" : "Show"} coverage`}
        >
          <i className="fas fa-tasks"></i>
        </Button>
      </div>
    )
  }
}
