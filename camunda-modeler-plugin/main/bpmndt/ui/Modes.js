import React from "react";

import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_SELECT,
  MODE_SHOW_COVERAGE
} from "../constants";

import Button from "./Button";

export default class Modes extends React.Component {
  constructor(props) {
    super(props);

    this.modes = [
      {id: MODE_SELECT,        renderOnlyIfActive: false, title: ["Enable path selection", "Disable path selection"], icon: "far fa-hand-pointer"},
      {id: MODE_SHOW_COVERAGE, renderOnlyIfActive: false, title: ["Show coverage", ,"Hide coverage"],                 icon: "fas fa-tasks"},
      {id: MODE_EDIT,          renderOnlyIfActive: true,  title: [null, null],                                        icon: "fas fa-pencil-alt"},
      {id: MODE_MIGRATE,       renderOnlyIfActive: true,  title: [null, "Cancel migration"],                          icon: "fas fa-tools"}
    ];

    this.modes.forEach(mode => {
      mode.toggle = this._handleToggleMode.bind(this, mode.id);
    });
  }

  _handleToggleMode(modeId) {
    this.props.toggleMode(modeId);
  }

  render() {
    let modesClassName = "modes";
    if (!this.props.activeModes.has(MODE_EDIT)) {
      modesClassName += " modes-lock";
    }

    return (
      <div className={modesClassName}>
        <div style={{float: "left", marginBottom: "2rem"}}>
          <Button onClick={this.props.hidePlugin} title="Hide plugin view">
            <i className="fas fa-times" style={{"color": "#dc3545"}}></i>
          </Button>
        </div>

        {this.modes.map(mode => {
          const active = this.props.activeModes.has(mode.id);

          return this._renderMode(mode, active);
        })}
      </div>
    )
  }

  _renderMode(mode, active) {
    if (!active && mode.renderOnlyIfActive) {
      return null;
    }

    return (
      <div key={mode.id} style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={mode.toggle}
          style={active ? "primary" : "secondary"}
          title={active ? mode.title[1] : mode.title[0]}
        >
          <i className={mode.icon}></i>
        </Button>
      </div>
    )
  }
}
