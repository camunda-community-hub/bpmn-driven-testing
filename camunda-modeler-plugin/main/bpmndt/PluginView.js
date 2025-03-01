import React from "react";

import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_SELECT,
  MODE_SHOW_COVERAGE,
  MODE_VIEW
} from "./constants";

import EditModal from "./ui/EditModal";
import EditModalBackdrop from "./ui/EditModalBackdrop";
import Modes from "./ui/Modes";

import Edit from "./mode/Edit";
import Migrate from "./mode/Migrate";
import View from "./mode/View";
import Select from "./mode/Select";

export default class PluginView extends React.Component {
  constructor(props) {
    super(props);

    const { plugin } = props;
    plugin.updateView = this.forceUpdate.bind(this);

    this.hidePlugin = plugin.hide.bind(plugin);
  }

  render() {
    const { plugin } = this.props;

    const { mode } = plugin;
    if (!mode) {
      return null;
    }

    const component = this._createComponent(mode);

    let viewClassName = "view";
    if (mode.id !== MODE_EDIT) {
      viewClassName += " view-lock"; 
    }

    return (
      <div>
        <Modes activeModes={mode.activeModes} hidePlugin={this.hidePlugin} toggleMode={mode.toggle} />

        <EditModal mode={mode} />
        <EditModalBackdrop mode={mode} />

        <div className={viewClassName} style={component ? {} : {display: "none"}}>
          <div className="view-container">
            {component}
          </div>
        </div>
      </div>
    )
  }

  _createComponent(mode) {
    switch (mode.id) {
      case MODE_EDIT:
        return <Edit mode={mode} />
      case MODE_MIGRATE:
        return <Migrate mode={mode} />
      case MODE_SELECT:
        return <Select mode={mode} />
      case MODE_SHOW_COVERAGE:
        return;
      case MODE_VIEW:
        return <View mode={mode} />
      default:
        throw new Error(`Unsupported mode '${mode.id}'`);
    }
  }
}
