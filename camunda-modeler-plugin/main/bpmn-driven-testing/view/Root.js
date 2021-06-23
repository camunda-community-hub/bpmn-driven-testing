import React from "react";

import EditorModeModal from "../mode/EditorModeModal";
import EditorModeView from "../mode/EditorModeView";
import SelectorModeView from "../mode/SelectorModeView";

import Button from "./component/Button";

const modes = {
  coverage: "coverage",
  editor: "editor",
  selector: "selector"
}

export default class Root extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      registered: false
    };

    this._handleClickClose = this._handleClickClose.bind(this);
    this._handleClickModalBackdrop = this._handleClickModalBackdrop.bind(this);

    this._handleToggleSelection = this._toggleMode.bind(this, modes.selector);
    this._handleToggleCoverage = this._toggleMode.bind(this, modes.coverage);
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

    mode.modal = false;
  }

  _toggleMode(modeName) {
    const { mode } = this.props.plugin;

    if (mode.name !== modeName) {
      this.props.plugin.mode = modeName;
    } else {
      this.props.plugin.mode = modes.editor;
    }
  }

  render() {
    if (!this.state.registered) {
      return null;
    }

    const { mode, testCases } = this.props.plugin;

    const active = [
      mode.name === modes.coverage,
      mode.name === modes.editor,
      mode.name === modes.selector
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
            {active[1] ? <EditorModeView mode={mode} testCases={testCases} /> : null}
            {active[2] ? <SelectorModeView mode={mode} /> : null}
          </div>
        </div>

        {active[1] && mode.modal ? <EditorModeModal mode={mode} /> : null}
        {active[1] && mode.modal ? <div className="modal-backdrop" onClick={this._handleClickModalBackdrop}></div> : null}
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
