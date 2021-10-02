import { END, EXISTING, EXISTING_BRIGHT, PATH, PATH_BRIGHT } from "../../PathMarker";

import BaseStrategy from "./BaseStrategy";

export default class PathStrategy extends BaseStrategy {
  init() {
    this._selection.start = this._problem.start;
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

    const pathElements = new Set(selection.path);
    for (const elementId of this._testCase.path) {
      if (!pathElements.has(elementId)) {
        markers.push({ id: elementId, style: EXISTING });
      }
    }
    
    markers.push({ id: selection.start, style: EXISTING_BRIGHT });
    markers.push({ id: selection.end, style: EXISTING_BRIGHT });

    // end must be last to markError work, when no paths are found
    if (selection.hasEnd()) {
      markers.push({ id: selection.end, style: END });
    }

    return markers;
  }

  getPaths() {
    return this._problem.paths;
  }

  handleClick() {
    // nothing to do here
  }

  migrate(path) {
    const a = this.testCase.path.findIndex(flowNode => flowNode === this.problem.start);
    const b = this.testCase.path.findIndex(flowNode => flowNode === this.problem.end);

    return this.testCase.path.slice(0, a).concat(path).concat(this.testCase.path.slice(b + 1));
  }
}
