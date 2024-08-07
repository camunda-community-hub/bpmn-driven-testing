import { PureComponent } from "camunda-modeler-plugin-helpers/react";

import { TAB_TYPE_BPMN, TAB_TYPE_CLOUD_BPMN } from "./constants";

import PluginState from "./PluginState";

/**
 * Extension, which runs outside of BPMN JS to be able to manage the different plugin instances.
 */
export default class ModelerExtension extends PureComponent {
  constructor(props) {
    super(props);

    this.pluginState = new PluginState();

    // handle tab changes
    props.subscribe("app.activeTabChanged", (event) => {
      const { activeTab } = event;
      if (activeTab.type === TAB_TYPE_BPMN || activeTab.type === TAB_TYPE_CLOUD_BPMN) {
        this.pluginState.showPlugin(activeTab.id);
      } else {
        this.pluginState.hidePlugin();
      }
    });

    // register plugin, when BPMN modeler was created
    props.subscribe("bpmn.modeler.created", (event) => {
      const { modeler, tab } = event;

      const plugin = modeler.get("bpmndt");
      plugin.type = tab.type; // "bpmn" (Camunda Platform 7) or "cloud-bpmn" (Camunda Platform 8)
      plugin.unregister = this.pluginState.unregisterPlugin.bind(this.pluginState, tab.id);

      this.pluginState.registerPlugin(tab.id, plugin);
    });
  }

  render() {
    // currently not needed
    return null;
  }
}
