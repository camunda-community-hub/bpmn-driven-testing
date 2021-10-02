// marker styles
export const PATH = "bpmn-driven-testing-path";
export const PATH_BRIGHT = `${PATH}-bright`;

export const END = `${PATH}-end`;
export const ERROR = `${PATH}-error`;
export const EXISTING = `${PATH}-existing`;
export const EXISTING_BRIGHT = `${PATH}-existing-bright`;
export const START = `${PATH}-start`;

export default class PathMarker {
  constructor(elementRegistry, canvas) {
    this._elementRegistry = elementRegistry;
    this._canvas = canvas;

    this._markers = [];
  }

  mark(markers) {
    this.unmark();

    for (const marker of markers) {
      this._markers.push(marker);
      this._add(marker);
    }
  }

  markAll(elementIds, style = PATH) {
    this.unmark();

    for (const elementId of elementIds) {
      const marker = { id: elementId, style: style };
      this._markers.push(marker);
      this._add(marker);
    }
  }

  markError() {
    const marker = this._markers[this._markers.length - 1];
    this._remove(marker);
    marker.style = ERROR;
    this._add(marker);
  }

  markOne(elementId, style) {
    let marker = this._markers.find(m => m.id === elementId);
    if (marker) {
      this._remove(marker);
    }

    marker = { id: elementId, style: style };
    this._markers.push(marker);
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
