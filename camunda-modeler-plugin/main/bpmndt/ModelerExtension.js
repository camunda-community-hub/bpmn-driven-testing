import { PureComponent } from "camunda-modeler-plugin-helpers/react";

import pluginTabState from "./PluginTabState";

/**
 * Extension, which runs outside of BPMN JS to be able to subscribe to tab change events.
 */
export default class ModelerExtension extends PureComponent {
  constructor(props) {
    super(props);

    this.props.subscribe("app.activeTabChanged", (event) => {
      const { activeTab } = event;

      if (activeTab.type === "bpmn") {
        pluginTabState.setActiveTab(activeTab.id);
      } else {
        pluginTabState.disablePlugin();
      }
    });
  }

  render() {
    // not needed for this kind of extension
    return null;
  }
}