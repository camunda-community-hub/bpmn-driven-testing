import { PATH, PATH_BRIGHT, EXISTING_BRIGHT, START } from "../../PathMarker";

import BaseStrategy from "./BaseStrategy";

/**
 * Strategy, used when the start node is missing.
 */
export default class StartStrategy extends BaseStrategy {
  init() {
    this._selection.end = this._problem.end;
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
    
    markers.push({ id: selection.end, style: EXISTING_BRIGHT });

    // start must be last to markError work, when no paths are found
    if (selection.hasStart()) {
      markers.push({ id: selection.start, style: START });
    }

    return markers;
  }

  handleClick(elementId) {
    const { selection } = this;

    selection.path = [];

    if (selection.hasStart() && elementId === selection.start) {
      selection.start = null;
    } else if (elementId === selection.end) {
      selection.start = null;
    } else {
      selection.start = elementId;
    }
  }

  migrate(path) {
    const b = this.testCase.path.findIndex(flowNode => flowNode === this.problem.end);

    return path.concat(this.testCase.path.slice(b + 1));
  }
}
