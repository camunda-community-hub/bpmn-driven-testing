import { PureComponent } from "camunda-modeler-plugin-helpers/react";

import state from "./ModelerTabState";

/**
 * Extension, which runs outside of BPMN JS to be able to subscribe to tab change events.
 */
export default class ModelerExtension extends PureComponent {
  constructor(props) {
    super(props);

    this.props.subscribe("app.activeTabChanged", (event) => {
      const { activeTab } = event;

      if (activeTab.type === "bpmn") {
        state.activeTabId = activeTab.id;
      } else {
        state.disablePlugin();
      }
    });
  }

  render() {
    return null;
  }
}