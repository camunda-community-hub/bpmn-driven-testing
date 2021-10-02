import { MODE_MIGRATION } from "../Constants";
import { END as END_MARKER, EXISTING, EXISTING_BRIGHT, START as START_MARKER } from "../PathMarker";
import { START, END, PATH, UNRESOLVABLE } from "../PathValidator";

import EndStrategy from "./selection/EndStrategy";
import PathStrategy from "./selection/PathStrategy";
import StartEndStrategy from "./selection/StartEndStrategy";
import StartStrategy from "./selection/StartStrategy";

export default class MigrationMode {
  constructor(plugin) {
    this._plugin = plugin;

    this._problemIndex = 0;
  }

  enable() {
    this._markProblem();
  }

  disable() {
    this._plugin.pathMarker.unmark();
  }

  reset() {
    // nothing to do here
  }

  hasMultipleProblems() {
    return this._testCase.problems.length > 1;
  }

  nextProblem() {
    if (this._problemIndex >= (this._testCase.problems.length - 1)) {
      this._problemIndex = 0;
    } else {
      this._problemIndex++;
    }

    // update
    this._markProblem();
    this._plugin.updateView();
  }

  prevProblem() {
    if (this._problemIndex <= 0) {
      this._problemIndex = this._testCase.problems.length - 1;
    } else {
      this._problemIndex--;
    }

    // update
    this._markProblem();
    this._plugin.updateView();
  }

  resolveProblem() {
    const { problem, testCase } = this;

    let strategy;
    switch (problem.type) {
      case START:
        strategy = new StartStrategy();
        break;
      case END:
        strategy = new EndStrategy();
        break;
      case PATH:
        strategy = new PathStrategy();
        break;
      case UNRESOLVABLE:
        strategy = new StartEndStrategy();
        break;
    }

    // initialize strategy
    strategy.problem = problem;
    strategy.testCase = testCase;
    strategy.init();

    this._plugin.resolveProblem(strategy);
  }

  _markProblem() {
    const { problem, testCase } = this;
    const { pathMarker } = this._plugin;

    pathMarker.markAll(testCase.path, EXISTING);

    switch (problem.type) {
      case START:
        pathMarker.markOne(problem.end, END_MARKER);
        break;
      case END:
        pathMarker.markOne(problem.start, START_MARKER);
        break;
      case PATH:
        pathMarker.markOne(problem.start, EXISTING_BRIGHT);
        pathMarker.markOne(problem.end, EXISTING_BRIGHT);
        break;
      case UNRESOLVABLE:
        // nothing to do here
    }
  }

  get name() {
    return MODE_MIGRATION;
  }

  get problem() {
    return this._testCase.problems[this._problemIndex];
  }

  get problemIndex() {
    return this._problemIndex;
  }

  get testCase() {
    return this._testCase;
  }

  set testCase(testCase) {
    this._testCase = testCase;
  }
}
