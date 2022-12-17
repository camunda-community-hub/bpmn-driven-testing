import { MARKER_ERROR } from "./constants";

export default class PathMarker {
  constructor(canvas, elementRegistry) {
    this.canvas = canvas;
    this.elementRegistry = elementRegistry;

    this.markers = [];
  }

  mark(markers) {
    this.removeAll();

    for (const marker of markers) {
      this.markers.push(marker);
      this._add(marker);
    }
  }

  markError() {
    const marker = this.markers[this.markers.length - 1];
    this._remove(marker);

    // in case of migration, a light blue marker may already exist
    // it must be removed first, otherwise the error marker will not become visible
    this._remove(this.markers.find(m => m.id == marker.id));

    marker.style = MARKER_ERROR;
    this._add(marker);
  }

  removeAll() {
    while (this.markers.length > 0) {
      const marker = this.markers.pop();
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
