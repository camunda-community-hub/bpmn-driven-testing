import TestCase from "./TestCase";

export default class TestCaseModdle {
  constructor(options) {
    this._modeling = options.modeling;
    this._moddle = options.moddle;

    // find process element
    this._process = this._findProcess(options.elementRegistry);
  }

  getTestCases() {
    const extensionElements = this._process.extensionElements || {};

    // find "bpmndt:TestCases" extension element
    const extension = (extensionElements.values || []).find(extensionElement => extensionElement.$type === "bpmndt:TestCases");

    if (extension) {
      return (extension.testCases || []).map(testCase => this._testCaseFromModdle(testCase));
    } else {
      return [];
    }
  }

  markAsChanged() {
    this._modeling.updateProperties(this._process, {});
  }

  setTestCases(testCases) {
    const extensionElements = this._process.extensionElements || this._moddle.create("bpmn:ExtensionElements")

    // remove extension
    extensionElements.values = (extensionElements.values || []).filter(extensionElement => {
      return extensionElement.$type !== "bpmndt:TestCases"
    });

    if (testCases.length === 0 && extensionElements.values.length === 0) {
      // remove extension elements
      delete this._process.extensionElements;

      return;
    }

    if (testCases.length !== 0) {
      extensionElements.values.push(this._moddle.create("bpmndt:TestCases", {
        testCases: testCases.map(testCase => this._testCaseToModdle(testCase))
      }));
    }

    this._process.extensionElements = extensionElements;
  }

  _findProcess(elementRegistry) {
    const process = elementRegistry.find(element => element.type === "bpmn:Process");
    if (process) {
      return process.businessObject;
    }

    // in case of a collaboration
    const participant = elementRegistry.filter(element => element.type === "bpmn:Participant").find(element => {
      return element.businessObject.processRef !== undefined;
    });

    return participant.businessObject.processRef;
  }

  /**
   * Converts the test case from the extension element to TestCase instance.
   * 
   * @param {object} testCase 
   */
  _testCaseFromModdle(testCase) {
    const path = testCase.path || {};

    return new TestCase({
      name: testCase.name,
      description: testCase.description,
      path: path.node || []
    });
  }

  /**
   * Converts the test case to a BPMN moddle object.
   * 
   * @param {TestCase} testCase 
   */
  _testCaseToModdle(testCase) {
    const path = this._moddle.create("bpmndt:Path", {
      node: testCase.path
    });

    return this._moddle.create("bpmndt:TestCase", {
      name: testCase.name,
      description: testCase.description,
      path: path
    });
  }
}
