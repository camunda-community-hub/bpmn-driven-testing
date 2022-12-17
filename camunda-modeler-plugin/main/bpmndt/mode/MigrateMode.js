import {
  MARKER_END,
  MARKER_OLD,
  MARKER_OLD_BRIGHT,
  MARKER_START,
  MODE_MIGRATE,
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../constants";

import { next, prev } from "../functions";

import BaseMode from "./BaseMode";
import TestCaseMigration from "../TestCaseMigration";

export default class MigrateMode extends BaseMode {
  constructor(plugin, oldMode) {
    super(plugin, MODE_MIGRATE);

    let testCase;
    if (oldMode.migration) {
      // select mode
      testCase = oldMode.migration.testCase;
    } else {
      // view mode
      const { testCases, testCaseIndex } = oldMode;

      testCase = testCases[testCaseIndex];
    }

    this.problems = testCase.problems;
    this.problemIndex = 0;
    this.migration = new TestCaseMigration(testCase, testCase.problems[0]);
  }

  /**
   * Gets the current problem.
   */
  get problem() {
    const { problems, problemIndex } = this;
    return problems[problemIndex];
  }

  get testCase() {
    return this.migration.testCase;
  }

  nextProblem() {
    const { problems, problemIndex, testCase } = this;

    this.problemIndex = next(problems, problemIndex);
    this.updateMarkers();

    this.migration = new TestCaseMigration(testCase, this.problem);
  }

  prevProblem() {
    const { problems, problemIndex, testCase } = this;

    this.problemIndex = prev(problems, problemIndex);
    this.updateMarkers();

    this.migration = new TestCaseMigration(testCase, this.problem);
  }

  updateMarkers() {
    const { problem, testCase } = this;

    const markers = [];
    for (const flowNodeId of testCase.path) {
      if (flowNodeId === problem.start || flowNodeId === problem.end) {
        continue;
      }

      markers.push({id: flowNodeId, style: MARKER_OLD});
    }

    switch (problem.type) {
      case PROBLEM_START:
        markers.push({id: problem.end, style: MARKER_END});
        break;
      case PROBLEM_END:
        markers.push({id: problem.start, style: MARKER_START});
        break;
      case PROBLEM_PATH:
        markers.push({id: problem.start, style: MARKER_OLD_BRIGHT});
        markers.push({id: problem.end, style: MARKER_OLD_BRIGHT});
        break;
      case PROBLEM_UNRESOLVABLE:
        // nothing to do here
        break;
      default:
        throw new Error(`Unsupported problem type '${problem.type}'`);
    }

    this.plugin.mark(markers);
  }
}
