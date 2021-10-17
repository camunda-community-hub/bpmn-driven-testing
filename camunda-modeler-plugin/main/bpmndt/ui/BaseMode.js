export default class BaseMode {
  constructor(controller) {
    this.controller = controller;
    this.state = {};
  }

  setMode(modeId, ctx) {
    this.controller.setMode(modeId, ctx);
  }

  setState(newState) {
    const { controller, state } = this;

    for (const [key, value] of Object.entries(newState)) {
      state[key] = value;
    }

    if ("markers" in newState) {
      controller.mark(newState.markers);
    }
    if (newState.markAsChanged) {
      controller.markAsChanged();
      delete state.markAsChanged;
    }

    controller.update();
  }
}
