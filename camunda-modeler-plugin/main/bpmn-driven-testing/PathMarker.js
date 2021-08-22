import {
  MARKER,
  MARKER_END,
  MARKER_ERROR,
  MARKER_START
} from "./Constants";

export default class PathMarker {
  constructor(elementRegistry, canvas) {
    this._elementRegistry = elementRegistry;
    this._canvas = canvas;

    this._markers = [];
  }

  mark(selection) {
    while (this._markers.length > 0) {
      const marker = this._markers.pop();
      this._remove(marker);
    }

    if (selection === null) {
      return;
    }

    if (selection.hasStart()) {
      const marker = { id: selection.start, style: MARKER_START };
      this._markers.push(marker);
      this._add(marker);
    }

    for (let i = 1; i < selection.path.length - 1; i++) {
      const marker = { id: selection.path[i], style: MARKER };
      this._markers.push(marker);
      this._add(marker);
    }

    if (selection.hasEnd()) {
      const marker = { id: selection.end, style: MARKER_END };
      this._markers.push(marker);
      this._add(marker);
    }
  }

  markAll(elements) {
    while (this._markers.length > 0) {
      const marker = this._markers.pop();
      this._remove(marker);
    }

    for (const element of elements) {
      const marker = { id: element, style: MARKER };
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

  _add(marker) {
    if (this._has(marker.id)) {
      this._canvas.addMarker(marker.id, marker.style);
    }
  }

  _has(elementId) {
    return this._elementRegistry.get(elementId) ? true : false;
  }

  _remove(marker) {
    if (this._has(marker.id)) {
      this._canvas.removeMarker(marker.id, marker.style);
    }
  }
}
