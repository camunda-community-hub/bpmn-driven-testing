import {
  MARKER,
  MARKER_BRIGHT,
  MARKER_END,
  MARKER_OLD,
  MARKER_OLD_BRIGHT,
  MARKER_START,
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "./constants";

import { selectStartEnd } from "./functions";

import TestCase from "./TestCase";

export default class TestCaseMigration {
  constructor(testCase, problem) {
    this.testCase = testCase;
    this.problem = problem;

    // IDs of flow nodes that are present in the old path
    // used with special markers (MARKER_BRIGHT, MARKER_OLD or MARKER_OLD_BRIGHT)
    this.oldNodeIds = new Set(testCase.path);
  }

  getMarkers(selection) {
    const { problem } = this;

    switch (problem.type) {
      case PROBLEM_START:
        return this._getMarkersStart(selection);
      case PROBLEM_END:
        return this._getMarkersEnd(selection);
      case PROBLEM_PATH:
        return this._getMarkersPath(selection);
      case PROBLEM_UNRESOLVABLE:
        return this._getMarkersUnresolvable(selection);
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }
  }

  getPaths() {
    return this.problem.paths || [];
  }

  /**
   * Returns the selection, used within the SelectMode to handle clicks on BPMN elements and get
   * markers. Depending on the type of problem, the selection may have start and/or end or nothing
   * selected.
   * 
   * @returns The initial selection.
   */
  getSelection() {
    const { problem, testCase } = this;

    const selection = new TestCase();

    switch (problem.type) {
      case PROBLEM_START:
        selection.end = problem.end;
        break;
      case PROBLEM_END:
        selection.start = problem.start;
        break;
      case PROBLEM_PATH:
        selection.start = problem.start;
        selection.startType = testCase.startType;
        selection.end = problem.end;
        selection.endType = testCase.endType;
        selection.path = problem.paths[0];
        break;
      case PROBLEM_UNRESOLVABLE:
        // no initial configuration, since a new path must be selected
        break;
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }

    return selection;
  }

  handleSelection(selection, elementId) {
    const { problem } = this;

    switch (problem.type) {
      case PROBLEM_START:
        this._selectStart(selection, elementId);
        break;
      case PROBLEM_END:
        this._selectEnd(selection, elementId);
        break;
      case PROBLEM_PATH:
        // no selection needed, since start and end node already known
        break;
      case PROBLEM_UNRESOLVABLE:
        selectStartEnd(selection, elementId);
        break;
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }
  }

  migrate(selection) {
    const { problem, testCase } = this;

    let path;
    switch (problem.type) {
      case PROBLEM_START:
        path = this._migrateStart(selection.path);
        break;
      case PROBLEM_END:
        path = this._migrateEnd(selection.path);
        break;
      case PROBLEM_PATH:
        path = this._migratePath(selection.path);
        break;
      case PROBLEM_UNRESOLVABLE:
        path = selection.path;
        break;
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }

    // update test case
    testCase.path = path;
    testCase.removeProblem(problem);
  }

  _getMarkersEnd(selection) {
    const markers = [];
    for (let i = 1; i < selection.path.length - 1; i++) {
      const style = this.oldNodeIds.has(selection.path[i]) ? MARKER_BRIGHT : MARKER;
      markers.push({id: selection.path[i], style: style});
    }
    
    markers.push({id: selection.start, style: MARKER_OLD_BRIGHT});
  
    // end must be last to make markError work, when no paths are found
    if (selection.end) {
      markers.push({id: selection.end, style: MARKER_END});
    }
  
    return markers;
  }
  
  _getMarkersPath(selection) {
    const markers = [];
    for (let i = 1; i < selection.path.length - 1; i++) {
      const style = this.oldNodeIds.has(selection.path[i]) ? MARKER_BRIGHT : MARKER;
      markers.push({id: selection.path[i], style: style});
    }
  
    const pathNodeIds = new Set(selection.path);
    for (const oldNodeId of this.oldNodeIds) {
      if (!pathNodeIds.has(oldNodeId)) {
        markers.push({id: oldNodeId, style: MARKER_OLD});
      }
    }
    
    markers.push({id: selection.start, style: MARKER_OLD_BRIGHT});
    markers.push({id: selection.end, style: MARKER_OLD_BRIGHT});
  
    return markers;
  }
  
  _getMarkersStart(selection) {
    const markers = [];
    for (let i = 1; i < selection.path.length - 1; i++) {
      const style = this.oldNodeIds.has(selection.path[i]) ? MARKER_BRIGHT : MARKER;
      markers.push({id: selection.path[i], style: style});
    }
    
    markers.push({id: selection.end, style: MARKER_OLD_BRIGHT});
  
    // start must be last to make markError work, when no paths are found
    if (selection.start) {
      markers.push({id: selection.start, style: MARKER_START});
    }
  
    return markers;
  }
  
  _getMarkersUnresolvable(selection) {
    const markers = [];
  
    if (selection.start) {
      markers.push({id: selection.start, style: MARKER_START});
    }
  
    for (let i = 1; i < selection.path.length - 1; i++) {
      const style = this.oldNodeIds.has(selection.path[i]) ? MARKER_BRIGHT : MARKER;
      markers.push({id: selection.path[i], style: style});
    }
  
    const pathNodeIds = new Set(selection.path);
    for (const oldNodeId of this.oldNodeIds) {
      if (!pathNodeIds.has(oldNodeId)) {
        markers.push({id: oldNodeId, style: MARKER_OLD});
      }
    }
  
    if (selection.end) {
      markers.push({id: selection.end, style: MARKER_END});
    }
  
    return markers;
  }

  _migrateEnd(path) {
    const { problem, testCase } = this;

    const a = testCase.path.findIndex(flowNode => flowNode === problem.start);

    return testCase.path.slice(0, a).concat(path);
  }

  _migratePath(path) {
    const { problem, testCase } = this;

    const a = testCase.path.findIndex(flowNode => flowNode === problem.start);
    const b = testCase.path.findIndex(flowNode => flowNode === problem.end);

    return testCase.path.slice(0, a).concat(path).concat(testCase.path.slice(b + 1));
  }

  _migrateStart(path) {
    const { problem, testCase } = this;

    const b = testCase.path.findIndex(flowNode => flowNode === problem.end);

    return path.concat(testCase.path.slice(b + 1));
  }

  _selectEnd(selection, elementId) {
    if (selection.end && elementId === selection.end) {
      selection.end = null;
    } else if (elementId === selection.start) {
      selection.end = null;
    } else {
      selection.end = elementId;
    }
  }

  _selectStart(selection, elementId) {
    if (selection.start && elementId === selection.start) {
      selection.start = null;
    } else if (elementId === selection.end) {
      selection.start = null;
    } else {
      selection.start = elementId;
    }
  }
}
