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

    this._rootElement = null;
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

  getTestCases() {
    const testCases = [];

    for (const process of this._findProcesses()) {
      // get extension elements
      const extensionElements = process.extensionElements || {};

      // find "bpmndt:TestCases" extension element
      const extension = this._findExtension(extensionElements);
      if (!extension) {
        continue;
      }

      (extension.testCases || []).map(testCase => this._testCaseFromModdle(testCase)).forEach(testCase => {
        testCases.push(testCase);

        // workaround for testing, since parent property of elements is not set
        testCase.process = process;
      });
    }

    return testCases;
  }

  markAsChanged() {
    const { modeling, _rootElement } = this;

    if (_rootElement !== null) {
      modeling.updateProperties(_rootElement, {});
    }
  }

  setTestCases(testCases) {
    const { moddle } = this;

    const processes = this._findProcesses();
    this._resetExtensionElements(processes);

    for (const testCase of testCases) {
      if (testCase.path.length === 0) {
        continue;
      }

      let process = processes.length === 1 ? processes[0] : this._getProcess(testCase.path[0]);
      if (!process) {
        // workaround for testing, since parent property of elements is not set
        process = testCase.process;
      }
      if (!process) {
        continue;
      }

      // get or create extension elements
      let extensionElements = process.extensionElements;
      if (!extensionElements) {
        extensionElements = moddle.create(BPMN_EXTENSION_ELEMENTS);
        process.extensionElements = extensionElements;
      }
      if (!extensionElements.values) {
        extensionElements.values = [];
      }

      // find or create "bpmndt:TestCases" extension element
      let extension = this._findExtension(extensionElements);
      if (!extension) {
        extension = moddle.create(BPMNDT_TEST_CASES, { testCases: [] });
        extensionElements.values.push(extension);
      }

      extension.testCases.push(this._testCaseToModdle(testCase));
    }
  }

  _findExtension(extensionElements) {
    return (extensionElements.values || []).find(extensionElement => extensionElement.$type === BPMNDT_TEST_CASES);
  }

  /**
   * Finds a single process or all processes of collaboration.
   * 
   * @returns {Array} An array with all defined processes - can be empty.
   */
  _findProcesses() {
    const { elementRegistry } = this;

    const collaboration = elementRegistry.find(element => element.type === BPMN_COLLABORATION);
    if (collaboration) {
      this._rootElement = collaboration;

      return elementRegistry
          .filter(element => element.type === BPMN_PARTICIPANT)
          .filter(element => element.businessObject.processRef !== undefined)
          .map(element => element.businessObject.processRef);
    }

    const process = elementRegistry.find(element => element.type === BPMN_PROCESS);
    if (process) {
      this._rootElement = process;

      return [ process.businessObject ];
    } else {
      return [];
    }
  }

  /**
   * Gets the process, which is a direct or indirect parent of the element with the given ID.
   * 
   * @param {String} elementId ID of a test case's start element.
   * 
   * @returns The corresponding process or undefined.
   */
  _getProcess(elementId) {
    const { elementRegistry } = this;

    let element = elementRegistry.get(elementId);
    while (element.parent) {
      element = element.parent;

      if (element.type === BPMN_PROCESS) {
        return element.businessObject;
      }
      if (element.type === BPMN_PARTICIPANT) {
        return element.businessObject.processRef;
      }
    }
  }

  /**
   * Resets the extension elements of all processes by deleting the "bpmndt:TestCases" element.
   * If the "bpmn:extensionElements" element is empty, it will also be deleted.
   * 
   * @param {Array} processes
   */
  _resetExtensionElements(processes) {
    for (const process of processes) {
      // get extension elements
      const extensionElements = process.extensionElements || {};

      // find "bpmndt:TestCases" extension element
      const extension = this._findExtension(extensionElements);
      if (!extension) {
        continue;
      }

      // remove extension
      extensionElements.values = (extensionElements.values || []).filter(extensionElement => {
        return extensionElement.$type !== BPMNDT_TEST_CASES
      });

      if (extensionElements.values.length === 0) {
        // remove extension elements
        delete process.extensionElements;
      }
    }
  }

  /**
   * Converts the extension element to a TestCase instance.
   * 
   * @param {object} testCaseModdle
   * 
   * @returns {TestCase} Mapped test case.
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
   * 
   * @returns {object} Test case element, created by moddle.
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
