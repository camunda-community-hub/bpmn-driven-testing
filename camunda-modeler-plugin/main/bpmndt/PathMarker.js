import { MARKER_ERROR } from "./constants";

export default class PathMarker {
  constructor(options) {
    this.canvas = options.canvas;
    this.elementRegistry = options.elementRegistry;

    this._markers = [];
  }

  mark(markers) {
    this.unmark();

    for (const marker of markers) {
      this._markers.push(marker);
      this._add(marker);
    }
  }

  markError() {
    const marker = this._markers[this._markers.length - 1];
    this._remove(marker);
    marker.style = MARKER_ERROR;
    this._add(marker);
  }

  unmark() {
    while (this._markers.length > 0) {
      const marker = this._markers.pop();
      this._remove(marker);
    }
  }

  _add(marker) {
    if (this._has(marker.id)) {
      this.canvas.addMarker(marker.id, marker.style);
    }
  }

  _has(elementId) {
    return this.elementRegistry.get(elementId) !== undefined;
  }

  _remove(marker) {
    if (this._has(marker.id)) {
      this.canvas.removeMarker(marker.id, marker.style);
    }
  }
}
