import { END, EXISTING, PATH, PATH_BRIGHT, START } from "../../PathMarker";

import BaseStrategy from "./BaseStrategy";

/**
 * Default strategy, used when a path for new a test case is selected or an unresolvable problem is
 * handled.
 */
export default class StartEndStrategy extends BaseStrategy {
  init() {
    if (this.isMigration()) {
      this._testCaseElements = new Set(this._testCase.path);
    }
  }

  getMarkers() {
    return this.isMigration() ? this._getMigrationMarkers() : this._getMarkers();
  }

  handleClick(elementId) {
    const { selection } = this;

    selection.path = [];

    if (selection.hasEnd() && elementId === selection.start) {
      selection.end = null;
      selection.start = null;
    } else if (selection.hasEnd() && elementId === selection.end) {
      selection.end = null;
    } else if (selection.hasEnd()) {
      selection.end = elementId;
    } else if (selection.hasStart() && elementId === selection.start) {
      selection.start = null;
    } else if (selection.hasStart() && !selection.hasEnd()) {
      selection.end = elementId;
    } else {
      selection.start = elementId;
    }
  }

  migrate(path) {
    return path;
  }

  _getMarkers() {
    const { selection } = this;

    const markers = [];
    if (selection.hasStart()) {
      markers.push({ id: selection.start, style: START });
    }

    for (let i = 1; i < selection.path.length - 1; i++) {
      markers.push({ id: selection.path[i], style: PATH });
    }

    if (selection.hasEnd()) {
      markers.push({ id: selection.end, style: END });
    }

    return markers;
  }

  _getMigrationMarkers() {
    const { selection } = this;

    const markers = [];
    if (selection.hasStart()) {
      markers.push({ id: selection.start, style: START });
    }

    for (let i = 1; i < selection.path.length - 1; i++) {
      let style;
      if (this._testCaseElements.has(selection.path[i])) {
        style = PATH_BRIGHT;
      } else {
        style = PATH;
      }

      markers.push({ id: selection.path[i], style: style });
    }

    const pathElements = new Set(selection.path);
    for (const elementId of this._testCase.path) {
      if (!pathElements.has(elementId)) {
        markers.push({ id: elementId, style: EXISTING });
      }
    }

    if (selection.hasEnd()) {
      markers.push({ id: selection.end, style: END });
    }

    return markers;
  }
}
