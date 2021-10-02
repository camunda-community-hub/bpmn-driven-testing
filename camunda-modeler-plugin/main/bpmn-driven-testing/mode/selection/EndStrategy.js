import { END, PATH, PATH_BRIGHT, EXISTING_BRIGHT } from "../../PathMarker";

import BaseStrategy from "./BaseStrategy";

export default class EndStrategy extends BaseStrategy {
  init() {
    this._selection.start = this._problem.start;
    this._testCaseElements = new Set(this._testCase.path);
  }

  getMarkers() {
    const { selection } = this;

    const markers = [];
    for (let i = 1; i < selection.path.length - 1; i++) {
      let style;
      if (this._testCaseElements.has(selection.path[i])) {
        style = PATH_BRIGHT;
      } else {
        style = PATH;
      }

      markers.push({ id: selection.path[i], style: style });
    }
    
    markers.push({ id: selection.start, style: EXISTING_BRIGHT });

    // end must be last to markError work, when no paths are found
    if (selection.hasEnd()) {
      markers.push({ id: selection.end, style: END });
    }

    return markers;
  }

  handleClick(elementId) {
    const { selection } = this;

    selection.path = [];

    if (selection.hasEnd() && elementId === selection.end) {
      selection.end = null;
    } else if (elementId === selection.start) {
      selection.end = null;
    } else {
      selection.end = elementId;
    }
  }

  migrate(path) {
    const a = this.testCase.path.findIndex(flowNode => flowNode === this.problem.start);

    return this.testCase.path.slice(0, a).concat(path);
  }
}
