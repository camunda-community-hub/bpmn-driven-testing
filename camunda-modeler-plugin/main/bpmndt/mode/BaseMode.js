import { MODE_VIEW } from "../constants";

export default class BaseMode {
  constructor(plugin, id) {
    this.plugin = plugin;
    this.id = id;

    this.activeModes = new Set([ id ]);
  }

  handleClickElement() {
    // empty default implenentation
  }

  toggle = (newModeId) => {
    const { id, plugin } = this;

    if (id !== newModeId) {
      // enable mode
      plugin.setMode(newModeId, this);
    } else {
      // disable current mode and enable view mode
      plugin.setMode(MODE_VIEW, this);
    }
  }

  updateMarkers() {
    // empty default implenentation
  }
}
