export default class TestCase {
  constructor(data = {path: []}) {
    const { description, name, path } = data;

    this.description = description;
    this.name = name;
    this.path = path;
  }

  /**
   * Enriches the test case with additional path information, regarding start and end node.
   * 
   * @param {object} elementRegistry 
   */
  enrich(elementRegistry) {
    const { path } = this;

    if (path.length >= 2) {
      this.start = path[0];
      this.startType = elementRegistry.get(this.start)?.type;
      this.end = path[path.length - 1];
      this.endType = elementRegistry.get(this.end)?.type;
    }
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

  get valid() {
    const { problems } = this;
    return problems === undefined || problems.length === 0;
  }
}
