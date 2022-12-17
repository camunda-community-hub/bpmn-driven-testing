import TestCase from "./TestCase";

import {
  BPMN_COLLABORATION,
  BPMN_EXTENSION_ELEMENTS,
  BPMN_PARTICIPANT,
  BPMN_PROCESS,
  BPMNDT_PATH,
  BPMNDT_TEST_CASE,
  BPMNDT_TEST_CASES
} from "./constants";

export default class TestCaseModdle {
  constructor(elementRegistry, modeling, moddle) {
    this.elementRegistry = elementRegistry;
    this.modeling = modeling;
    this.moddle = moddle;
  }

  enrichProblem(problem) {
    if (problem.start) {
      problem.startType = this.elementRegistry.get(problem.start)?.type;
    }
    if (problem.end) {
      problem.endType = this.elementRegistry.get(problem.end)?.type;
    }
  }

  /**
   * Enriches the test case with additional path information, regarding start and end node.
   * 
   * @param {TestCase} testCase
   */
  enrichTestCase(testCase) {
    const { path } = testCase;

    if (path.length >= 2) {
      testCase.start = path[0];
      testCase.startType = this.elementRegistry.get(testCase.start)?.type;
      testCase.end = path[path.length - 1];
      testCase.endType = this.elementRegistry.get(testCase.end)?.type;
    }
  }

  findProcess() {
    const { elementRegistry } = this;

    const collaboration = elementRegistry.find(element => element.type === BPMN_COLLABORATION);

    if (collaboration) {
      // in case of a collaboration
      const participant = elementRegistry.filter(element => element.type === BPMN_PARTICIPANT).find(element => {
        return element.businessObject.processRef !== undefined;
      });

      if (participant) {
        this.process = participant.businessObject.processRef;
        return true;
      }
    } else {
      // normal process definition
      const process = elementRegistry.find(element => element.type === BPMN_PROCESS);
      if (process) {
        this.process = process.businessObject;
        return true;
      }
    }

    return false;
  }

  getTestCases() {
    const extensionElements = this.process.extensionElements || {};

    // find "bpmndt:TestCases" extension element
    const extension = (extensionElements.values || []).find(extensionElement => extensionElement.$type === BPMNDT_TEST_CASES);

    if (extension) {
      return (extension.testCases || []).map(testCase => this._testCaseFromModdle(testCase));
    } else {
      return [];
    }
  }

  markAsChanged() {
    const { modeling, process } = this;
    modeling.updateProperties(process, {});
  }

  setTestCases(testCases) {
    const { moddle, process } = this;

    const extensionElements = process.extensionElements || moddle.create(BPMN_EXTENSION_ELEMENTS)

    // remove extension
    extensionElements.values = (extensionElements.values || []).filter(extensionElement => {
      return extensionElement.$type !== BPMNDT_TEST_CASES
    });

    if (testCases.length === 0 && extensionElements.values.length === 0) {
      // remove extension elements
      delete process.extensionElements;

      return;
    }

    if (testCases.length !== 0) {
      extensionElements.values.push(moddle.create(BPMNDT_TEST_CASES, {
        testCases: testCases.map(testCase => this._testCaseToModdle(testCase))
      }));
    }

    process.extensionElements = extensionElements;
  }

  /**
   * Converts the extension element to a TestCase instance.
   * 
   * @param {object} testCaseModdle 
   */
  _testCaseFromModdle(testCaseModdle) {
    const path = testCaseModdle.path || {};
    
    // array of flow node IDs
    const node = path.node || [];

    const testCase = new TestCase({
      name: testCaseModdle.name,
      description: testCaseModdle.description,
      path: node
    });

    this.enrichTestCase(testCase);

    return testCase;
  }

  /**
   * Converts the TestCase instance to an extension element.
   * 
   * @param {TestCase} testCase 
   */
  _testCaseToModdle(testCase) {
    const { moddle } = this;

    const path = moddle.create(BPMNDT_PATH, {
      node: testCase.path
    });

    return moddle.create(BPMNDT_TEST_CASE, {
      name: testCase.name,
      description: testCase.description,
      path: path
    });
  }
}
