import PathSelection from "../../PathSelection";

/**
 * Base class for all selection strategies.
 */
export default class BaseStrategy {
  constructor() {
    this._selection = new PathSelection();
  }

  /**
   * Get predefined paths, if a path in between is invalid.
   * Please note: This method can be overwritten by a concrete strategy.
   * 
   * @returns An array with predefined paths.
   */
  getPaths() {
    return [];
  }

  isMigration() {
    return this._testCase !== undefined;
  }

  get problem() {
    return this._problem;
  }

  get selection() {
    return this._selection;
  }

  get testCase() {
    return this._testCase;
  }

  set problem(problem) {
    this._problem = problem;
  }

  set testCase(testCase) {
    this._testCase = testCase;
  }
}
