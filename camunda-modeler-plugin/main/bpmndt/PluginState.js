/**
 * Cache that holds information about the plugin instances.
 * If a tab is changed, the cache is used to save and restore the plugin's view state.
 */
export default class PluginState {
  constructor() {
    this.cache = [];
  }

  hidePlugin() {
    const { cache } = this;

    const entry = cache.find(e => e.active);
    if (!entry) {
      return;
    }

    const { plugin } = entry;
    entry.active = false;
    entry.visible = plugin.visible;

    if (plugin.visible) {
      plugin.hide();
    }
  }

  registerPlugin(id, plugin) {
    const { cache } = this;

    const entry = cache.find(e => e.id === id);
    if (entry) {
      entry.plugin = plugin;
    } else {
      cache.push({ id, plugin });
    }
  }

  showPlugin(id) {
    this.hidePlugin();

    const { cache } = this;

    const entry = cache.find(e => e.id === id);
    if (!entry) {
      cache.push({ id, active: true });
      return;
    }

    const { plugin, visible } = entry;
    if (visible) {
      plugin.show();
    }

    entry.active = true;
  }

  unregisterPlugin(id) {
    const { cache } = this;

    const index = cache.findIndex(e => e.id === id);
    if (index > -1) {
      cache.splice(index, 1);
    }
  }
}
