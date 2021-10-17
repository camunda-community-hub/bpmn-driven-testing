/**
 * Empty helper implementation, used for testing only.
 */
export default class PluginController {
  handleToggleMode() {
  }

  mark() {
  }

  markAsChanged() {
  }

  setMode(modeId, ctx) {
    this.modeId = modeId;
    this.ctx = ctx;
  }

  update() {
  }
}
