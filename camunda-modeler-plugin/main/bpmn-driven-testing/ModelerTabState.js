/**
 * Class, which keeps track if the plugin instances and their last state.
 */
class ModelerTabState {
  constructor() {
    this._tabs = {};
  }

  /**
   * Disables the plugin of the active tab, if there is one.
   * This function is called when the active tab is changed to a DMN or CMMN diagram.
   */
  disablePlugin() {
    if (this._hasActiveTab() && this._hasTab(this._activeTabId)) {
      const tab = this._getActiveTab();

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

  unregister(tabId) {
    delete this._tabs[tabId];
  }

  _getActiveTab() {
    return this._tabs[this._activeTabId];
  }

  _getTab(tabId) {
    return this._tabs[tabId];
  }

  _hasActiveTab() {
    return this._activeTabId !== undefined;
  }

  _hasTab(tabId) {
    return this._tabs[tabId] !== undefined;
  }

  set activeTabId(activeTabId) {
    this.disablePlugin();
  
    if (this._hasTab(activeTabId)) {
      const tab = this._getTab(activeTabId);

      if (tab.enabled) {
        tab.plugin.enable();
      }
    }
  
    this._activeTabId = activeTabId;
  }
}

// create shared state
export default new ModelerTabState();
