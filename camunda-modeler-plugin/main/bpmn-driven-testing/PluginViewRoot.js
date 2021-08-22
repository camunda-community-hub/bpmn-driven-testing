import React from "react";

import {
  MODE_COVERAGE,
  MODE_EDITOR,
  MODE_SELECTOR
} from "./Constants";

import Button from "./component/Button";

import Editor from "./mode/Editor";
import EditorModal from "./mode/EditorModal";
import Selector from "./mode/Selector";

export default class PluginViewRoot extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      registered: false
    };

    this._handleClickClose = this._handleClickClose.bind(this);
    this._handleClickModalBackdrop = this._handleClickModalBackdrop.bind(this);

    this._handleToggleSelection = this._toggleMode.bind(this, MODE_SELECTOR);
    this._handleToggleCoverage = this._toggleMode.bind(this, MODE_COVERAGE);
  }

  componentDidMount() {
    this.props.plugin.registerView(this.forceUpdate.bind(this));
    this.setState({registered: true});
  }

  componentWillUnmount() {
    this.props.plugin.registerView(null);
    this.setState({registered: false});
  }

  _handleClickClose() {
    this.props.plugin.disable();
  }
  _handleClickModalBackdrop() {
    const { mode } = this.props.plugin;

    mode.showModal(false);
  }

  _toggleMode(modeName) {
    const { mode } = this.props.plugin;

    if (mode.name !== modeName) {
      this.props.plugin.setMode(modeName);
    } else {
      this.props.plugin.setMode(MODE_EDITOR);
    }
  }

  render() {
    if (!this.state.registered) {
      return null;
    }

    const { mode, testCases } = this.props.plugin;

    const active = [
      mode.name === MODE_COVERAGE,
      mode.name === MODE_EDITOR,
      mode.name === MODE_SELECTOR
    ];

    return (
      <div>
        <div className="root-action">
          <div style={{float: "left", marginBottom: "2rem"}}>
            <Button onClick={this._handleClickClose} title="Hide plugin view">
              <i className="fas fa-times" style={{"color": "#dc3545"}}></i>
            </Button>
          </div>

          {this._renderToggleSelector(active[2])}
          {this._renderToggleCoverage(active[0])}
        </div>

        <div className="root">
          <div className="root-container">
            {active[1] ? <Editor mode={mode} testCases={testCases} /> : null}
            {active[2] ? <Selector mode={mode} /> : null}
          </div>
        </div>

        {active[1] && mode.isModalShown() ? <EditorModal mode={mode} /> : null}
        {active[1] && mode.isModalShown() ? <div className="modal-backdrop" onClick={this._handleClickModalBackdrop}></div> : null}
      </div>
    )
  }

  _renderToggleCoverage(active) {
    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleCoverage}
          style={active ? "primary" : "secondary"}
          title={`${active ? "Hide" : "Show"} coverage`}
        >
          <i className="fas fa-tasks"></i>
        </Button>
      </div>
    )
  }

  _renderToggleSelector(active) {
    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleSelection}
          style={active ? "primary" : "secondary"}
          title={`${active ? "Disable" : "Enable"} path selection`}
        >
          <i className="far fa-hand-pointer"></i>
        </Button>
      </div>
    )
  }
}
