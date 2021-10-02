import { PATH } from "./PathValidator";

export default class TestCase {
  constructor(data) {
    this._data = data;
  }

  autoResolveProblem() {
    const problem = this.problems.find(problem => problem.type === PATH && problem.autoResolvable);
    if (problem === undefined) {
      return false;
    }

    const path = problem.paths[0];

    const a = this.path.findIndex(flowNode => flowNode === problem.start);
    const b = this.path.findIndex(flowNode => flowNode === problem.end);

    this.path = this.path.slice(0, a).concat(path).concat(this.path.slice(b + 1));

    this.removeProblem(problem);

    return true;
  }

  equals(path) {
    if (this.path.length !== path.length) {
      return false;
    }

    for (let i = path.length - 1; i >= 0; i--) {
      if (this.path[i] !== path[i]) {
        return false;
      }
    }

    return true;
  }

  removeProblem(problem) {
    const index = this._problems.indexOf(problem);
    if (index !== -1) {
      this._problems.splice(index, 1);
    }
  }

  update(oldFlowNodeId, newFlowNodeId) {
    const index = this.path.findIndex(flowNode => flowNode === oldFlowNodeId);
    if (index != -1) {
      this.path[index] = newFlowNodeId
    }
  }

  get description() {
    return this._data.description;
  }

  get name() {
    return this._data.name;
  }

  get path() {
    return this._data.path;
  }

  get problems() {
    return this._problems;
  }

  get valid() {
    return this._problems ? this._problems.length === 0 : true;
  }

  get validation() {
    return this._validation;
  }

  set description(description) {
    this._data.description = description;
  }

  set name(name) {
    this._data.name = name;
  }

  set path(path) {
    this._data.path = path;
  }

  set problems(problems) {
    this._problems = problems;
  }
}
