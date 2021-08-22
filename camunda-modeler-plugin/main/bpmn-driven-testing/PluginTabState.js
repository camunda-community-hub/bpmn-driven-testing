/**
 * Class, which keeps track if the plugin instances and their last state.
 */
class PluginTabState {
  constructor() {
    this._tabs = {};
  }

  /**
   * Disables the plugin of the active tab, if there is one.
   * This function is called when the active tab is changed to a DMN or CMMN diagram.
   */
  disablePlugin() {
    const { _activeTabId, _tabs } = this;

    if (_activeTabId !== undefined && _tabs[_activeTabId] !== undefined) {
      const tab = this._tabs[this._activeTabId];

      // remember plugin state
      tab.enabled = tab.plugin.enabled;

      tab.plugin.disable();
    }
  }

  register(plugin) {
    this._tabs[this._activeTabId] = {
      plugin: plugin
    };

    return this._activeTabId;
  }

  setActiveTabId(activeTabId) {
    const { _tabs } = this;

    this.disablePlugin();
  
    if (_tabs[activeTabId] !== undefined) {
      const tab = _tabs[activeTabId];

      if (tab.enabled) {
        tab.plugin.enable();
      }
    }
  
    this._activeTabId = activeTabId;
  }

  unregister(tabId) {
    delete this._tabs[tabId];
  }
}

// create shared state
export default new PluginTabState();
