import {
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "./constants";

import { pathEquals } from "./functions";
import PathFinder from "./PathFinder";

export default class PathValidator {
  constructor(elementRegistry) {
    this.elementRegistry = elementRegistry;
    this.pathFinder = new PathFinder(elementRegistry);
  }

  validate(testCase) {
    const problems = [];

    const { path } = testCase;
    if (path.length < 2) {
      // empty and incomplete paths are unresolvable
      problems.push({type: PROBLEM_UNRESOLVABLE});

      return problems;
    }

    const { elementRegistry, pathFinder } = this;

    const start = path[0];
    const end = path[path.length - 1];

    let a;
    let b;

    // find first existing flow node
    for (let i = 0; i < path.length - 1; i++) {
      if (elementRegistry.get(path[i])) {
        a = i;
        break;
      }
    }

    // find last existing flow node
    for (let i = path.length - 1; i > 0; i--) {
      if (elementRegistry.get(path[i])) {
        b = i;
        break;
      }
    }

    if (a !== undefined && b !== undefined && a === b) {
      // only one flow node exists, which neither the start nor the end flow node
      problems.push({type: PROBLEM_START, end: path[a], missing: start});
      problems.push({type: PROBLEM_END, start: path[b], missing: end});
    } else if (a !== undefined && b !== undefined) {
      const paths = pathFinder.find(path[a], path[b]);
      if (paths.length === 0) {
        // no path between a and b found
        problems.push({type: PROBLEM_UNRESOLVABLE});
      }

      if (paths.find(foundPath => pathEquals(path, foundPath)) !== undefined) {
        // path is still valid
        return [];
      }

      if (a != 0) {
        // a is no the start node
        problems.push({type: PROBLEM_START, end: path[a], missing: start});
      }

      if (paths.length > 1) {
        // multiple paths
        problems.push({type: PROBLEM_PATH, start: path[a], end: path[b], paths: paths});
      } else if (paths.length === 1 && paths[0].length > 2) {
        // only one possible path between a and b found
        problems.push({type: PROBLEM_PATH, start: path[a], end: path[b], paths: paths, autoResolvable: true});
      }

      if (b != path.length - 1) {
        // b is not the end node
        problems.push({type: PROBLEM_END, start: path[b], missing: end});
      }
    } else if (a && b === undefined) {
      // only one flow node exists - the start node
      problems.push({type: PROBLEM_END, start: path[a], missing: end});
    } else if (b && a === undefined) {
      // only one flow node exists - the end node
      problems.push({type: PROBLEM_START, end: path[b], missing: start});
    } else {
      // no flow node exists
      problems.push({type: PROBLEM_UNRESOLVABLE});
    }

    return problems;
  }
}
