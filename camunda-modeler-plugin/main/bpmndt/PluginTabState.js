/**
 * Keeps track of the different plugin instances and their last state.
 */
class PluginTabState {
  constructor() {
    this.activeTabId = null;
    this.tabs = {};
  }

  /**
   * Disables the plugin of the active tab, if there is one.
   * This function is also called when the active tab is changed to a DMN or CMMN diagram.
   */
  disablePlugin() {
    const { activeTabId, tabs } = this;

    if (tabs[activeTabId] !== undefined) {
      const tab = tabs[activeTabId];

      // remember plugin state
      tab.shown = tab.plugin.shown;

      if (tab.plugin.shown) {
        tab.plugin.hide();
      }

      this.activeTabId = null;
    }
  }

  register(plugin) {
    const { activeTabId, tabs } = this;

    tabs[activeTabId] = { plugin: plugin };

    return activeTabId;
  }

  /**
   * Sets the active tab.
   * 
   * @param {String} newTabId The ID of the tab that is currently active.
   */
  setActiveTab(newTabId) {
    const { activeTabId, tabs } = this;

    if (activeTabId !== newTabId) {
      this.disablePlugin();
    }
  
    if (tabs[newTabId] !== undefined) {
      const tab = tabs[newTabId];

      if (tab.shown) {
        tab.plugin.show();
      }
    }
  
    this.activeTabId = newTabId;
  }

  unregister(tabId) {
    delete this.tabs[tabId];
  }
}

// create shared state
export default new PluginTabState();
