export default class PathSelection {
  constructor(path = []) {
    this._data = {
      path: path
    };

    if (path.length >= 2) {
      this._data.start = path[0];
      this._data.end = path[path.length - 1];
    }
  }

  enrich(elementRegistry) {
    const start = elementRegistry.get(this.start);
    if (start) {
      this.startType = start.type;
    }

    const end = elementRegistry.get(this.end);
    if (end) {
      this.endType = end.type;
    }
  }

  hasEnd() {
    return this._data.end ? true : false;
  }

  hasStart() {
    return this._data.start ? true : false;
  }

  get end() {
    return this._data.end;
  }
  get endType() {
    return this._data.endType;
  }
  get path() {
    return this._data.path;
  }
  get start() {
    return this._data.start;
  }
  get startType() {
    return this._data.startType;
  }

  set end(end) {
    this._data.end = end;
  }
  set endType(endType) {
    this._data.endType = endType;
  }
  set path(path) {
    this._data.path = path;
  }
  set start(start) {
    this._data.start = start;
  }
  set startType(startType) {
    this._data.startType = startType;
  }
}
