import React from "react";

import { MODE_SELECT, MODE_SHOW_COVERAGE } from "./constants";

import Container from "./ui/component/Container";

import EditModal from "./ui/EditModal";
import EditModalBackdrop from "./ui/EditModalBackdrop";
import ModeButtonGroup from "./ui/ModeButtonGroup";
import Select from "./ui/Select";

export default class PluginView extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      registered: false
    };
  }

  componentDidMount() {
    this.props.controller.updateView = this.forceUpdate.bind(this);
    this.setState({registered: true});
  }

  componentWillUnmount() {
    this.props.controller.updateView = null;
    this.setState({registered: false});
  }

  render() {
    if (!this.state.registered) {
      return null;
    }

    const { controller } = this.props;
    
    if (controller.state === undefined) {
      return null;
    }

    return (
      <div>
        <ModeButtonGroup controller={controller} />

        <div className="view" style={controller.state.showView ? {} : {display: "none"}}>
          <div className="view-container">
            {this.renderContainer(controller)}
          </div>
        </div>

        <EditModal controller={controller} />
        <EditModalBackdrop controller={controller} />
      </div>
    )
  }

  renderContainer(controller) {
    const { mode } = controller;

    if (mode.id === MODE_SELECT) {
      return <Select controller={controller} />
    } else if (mode.id === MODE_SHOW_COVERAGE) {
      return null;
    } else {
      return <Container controller={controller} />
    }
  }
}
