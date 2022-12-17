import { getMarkers } from "./functions";

import TestCaseMigration from "./TestCaseMigration";

export default class TestCase {
  constructor(data = {path: []}) {
    const { description, name, path } = data;

    this.description = description;
    this.name = name;
    this.path = path;
  }

  get markers() {
    return getMarkers(this);
  }

  get valid() {
    const { problems } = this;
    return problems === undefined || problems.length === 0;
  }

  autoResolveProblem() {
    const problem = this.problems.find(problem => problem.autoResolvable);
    if (problem === undefined) {
      return false;
    }

    const migration = new TestCaseMigration(this, problem);
    migration.migrate(new TestCase({path: problem.paths[0]}));

    return true;
  }

  removeProblem(problem) {
    const { problems } = this;

    const index = problems.indexOf(problem);
    if (index !== -1) {
      problems.splice(index, 1);
    }
  }

  updateFlowNodeId(oldFlowNodeId, newFlowNodeId) {
    const index = this.path.findIndex(flowNode => flowNode === oldFlowNodeId);
    if (index != -1) {
      this.path[index] = newFlowNodeId
    }
  }
}
