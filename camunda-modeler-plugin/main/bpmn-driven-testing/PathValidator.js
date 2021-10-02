// problem types
export const START = 1;
export const END = 2;
export const PATH = 3;
export const UNRESOLVABLE = 4;

export default class PathValidator {
  constructor(elementRegistry, pathFinder) {
    this._elementRegistry = elementRegistry;
    this._pathFinder = pathFinder;
  }

  validate(testCase) {
    const problems = [];

    const { path } = testCase;
    if (path.length < 2) {
      // skip empty or incomplete paths
      return;
    }

    const start = path[0];
    const end = path[path.length - 1];

    let a;
    let b;

    // find first existing flow node
    for (let i = 0; i < path.length - 1; i++) {
      if (this._elementRegistry.get(path[i])) {
        a = i;
        break;
      }
    }

    // find last existing flow node
    for (let i = path.length - 1; i > 0; i--) {
      if (this._elementRegistry.get(path[i])) {
        b = i;
        break;
      }
    }

    if (a !== undefined && b !== undefined && a == b) {
      // only one flow node exists, which neither the start nor the end flow node
      problems.push({type: START, end: path[a], missing: start});
      problems.push({type: END, start: path[b], missing: end});
    } else if (a !== undefined && b !== undefined) {
      const paths = this._pathFinder.findPaths(path[a], path[b]);
      if (paths.length == 0) {
        // no path between a and b found
        problems.push({type: UNRESOLVABLE});
      }

      if (a != 0) {
        // a is no the start node
        problems.push({type: START, end: path[a], missing: start});
      }

      if (paths.length > 1) {
        // multiple paths
        problems.push({type: PATH, start: path[a], end: path[b], paths: paths});
      } else if (paths.length == 1 && paths[0].length > 2) {
        // only one possible path between a and b found
        problems.push({type: PATH, start: path[a], end: path[b], paths: paths, autoResolvable: true});
      }

      if (b != path.length - 1) {
        // b is not the end node
        problems.push({type: END, start: path[b], missing: end});
      }
    } else if (a && b === undefined) {
      // only one flow node exists - the start node
      problems.push({type: END, start: path[a], missing: end});
    } else if (b && a === undefined) {
      // only one flow node exists - the end node
      problems.push({type: START, end: path[b], missing: start});
    } else {
      // no flow node exists
      problems.push({type: UNRESOLVABLE});
    }

    for (const problem of problems) {
      if (problem.start) {
        const element = this._elementRegistry.get(problem.start);
        problem.startType = element.type;
      }
      if (problem.end) {
        const element = this._elementRegistry.get(problem.end);
        problem.endType = element.type;
      }
    }

    return problems;
  }
}
