import React from "react";

import {
  MODE_COVERAGE,
  MODE_EDITOR,
  MODE_MIGRATION,
  MODE_SELECTION
} from "./Constants";

import Button from "./component/Button";

import Editor from "./mode/Editor";
import EditorModal from "./mode/EditorModal";
import Migration from "./mode/Migration";
import Selection from "./mode/Selection";

export default class PluginViewRoot extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      registered: false
    };

    this._handleToggleSelection = this._toggleMode.bind(this, MODE_SELECTION);
    this._handleToggleCoverage = this._toggleMode.bind(this, MODE_COVERAGE);
    this._handleToggleMigration = this._toggleMode.bind(this, MODE_MIGRATION);
  }

  componentDidMount() {
    this.props.plugin.registerView(this.forceUpdate.bind(this));
    this.setState({registered: true});
  }

  componentWillUnmount() {
    this.props.plugin.registerView(null);
    this.setState({registered: false});
  }

  _handleClickClose = () => {
    this.props.plugin.disable();
  }
  _handleClickModalBackdrop = () => {
    const { mode } = this.props.plugin;

    mode.showModal(false);
  }

  _toggleMode(modeName) {
    const { mode } = this.props.plugin;

    if (mode.name !== modeName) {
      this.props.plugin.setMode(modeName, true);
    } else if (modeName === MODE_SELECTION && mode.isMigration()) {
      this.props.plugin.setMode(MODE_MIGRATION);
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
      mode.name === MODE_EDITOR,
      mode.name === MODE_SELECTION,
      mode.name === MODE_COVERAGE,
      mode.name === MODE_MIGRATION
    ];

    // hide root to make complete diagram clickable
    // when selector mode is active and no paths have been found yet
    // or coverage mode is active
    const hideRoot = (active[1] && mode.paths.length === 0) || active[2];

    return (
      <div>
        <div className="root-action">
          <div style={{float: "left", marginBottom: "2rem"}}>
            <Button onClick={this._handleClickClose} title="Hide plugin view">
              <i className="fas fa-times" style={{"color": "#dc3545"}}></i>
            </Button>
          </div>

          {this._renderToggleEditorModal(active[0] && mode.isModalShown())}
          {this._renderToggleSelection(active[1])}
          {this._renderToggleCoverage(active[2])}
          {this._renderToggleMigration(active[3] || (active[1] && mode.isMigration()))}
        </div>

        <div className="root" style={hideRoot ? {display: "none"} : {}}>
          <div className="root-container">
            {active[0] ? <Editor mode={mode} testCases={testCases} /> : null}
            {active[1] ? <Selection mode={mode} /> : null}
            {active[3] ? <Migration mode={mode} /> : null}
          </div>
        </div>

        {active[0] && mode.isModalShown() ? <EditorModal mode={mode} /> : null}
        {active[0] && mode.isModalShown() ? <div className="modal-backdrop" onClick={this._handleClickModalBackdrop}></div> : null}
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

  _renderToggleEditorModal(active) {
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

  _renderToggleMigration(active) {
    if (!active) {
      return null;
    }

    return (
      <div style={{float: "left", marginBottom: "0.5rem"}}>
        <Button
          onClick={this._handleToggleMigration}
          style="primary"
          title="Cancel migration"
        >
          <i className="fas fa-tools"></i>
        </Button>
      </div>
    )
  }

  _renderToggleSelection(active) {
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
