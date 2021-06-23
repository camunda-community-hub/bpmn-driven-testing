export default class TestCase {
  constructor(data) {
    this._data = data;
  }

  equals(path) {
    if (this.path.length !== path.length) {
      return false;
    }

    for (let i = path.length - 1; i >= 0; i--) {
      if (this.path[i] !== path[i]) {
        return false;
      }
    }

    return true;
  }

  update(oldFlowNodeId, newFlowNodeId) {
    const index = this.path.findIndex(flowNode => flowNode === oldFlowNodeId);
    if (index != -1) {
      this.path[index] = newFlowNodeId
    }
  }

  get description() {
    return this._data.description;
  }

  get name() {
    return this._data.name;
  }

  get path() {
    return this._data.path;
  }

  set description(description) {
    this._data.description = description;
  }

  set name(name) {
    this._data.name = name;
  }
}
