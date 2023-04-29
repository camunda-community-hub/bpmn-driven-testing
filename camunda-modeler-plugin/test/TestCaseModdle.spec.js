import chai from "chai";

import BpmnModdle from "bpmn-moddle";
import { readBpmnFile, ElementRegistry } from "./helper";

import {
  BPMNDT_PATH,
  BPMNDT_TEST_CASE,
  BPMNDT_TEST_CASES
} from "../main/bpmndt/constants";

import bpmndt from "../main/bpmndt.json";

import TestCase from "../main/bpmndt/TestCase";
import TestCaseModdle from "../main/bpmndt/TestCaseModdle";

const expect = chai.expect;

const moddle = new BpmnModdle({
  bpmndt: bpmndt
});

function createTestCaseModdle(modelInstance) {
  return new TestCaseModdle(new ElementRegistry(modelInstance), null, moddle);
}

describe("TestCaseModdle", () => {
  // other extension
  let camundaProperties;
  // test case with name and description
  let happyPath;
  // no test cases, no extensionElements
  let noTestCases;
  // one test case
  let simple;
  // collaboration with a single process
  let simpleCollaboration;
  // collaboration with multiple processes
  let advancedCollaboration;

  before(async () => {
    camundaProperties = await moddle.fromXML(readBpmnFile("camundaProperties.bpmn"));
    happyPath = await moddle.fromXML(readBpmnFile("happyPath.bpmn"));
    noTestCases = await moddle.fromXML(readBpmnFile("noTestCases.bpmn"));
    simpleCollaboration = await moddle.fromXML(readBpmnFile("simpleCollaboration.bpmn"));
  });

  beforeEach(async () => {
    // reload model instances, since they are modified in different tests
    simple = await moddle.fromXML(readBpmnFile("simple.bpmn"));
    advancedCollaboration = await moddle.fromXML(readBpmnFile("advancedCollaboration.bpmn"));
  });

  describe("getTestCase", () => {
    it("should get no test cases from noTestCases.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(noTestCases);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(0);
    });

    it("should get one test case from simple.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(simple);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(1);

      const testCase = testCases[0];
      expect(testCase).to.have.property("path");
      expect(testCase.path).to.be.an("array")
      expect(testCase.path).to.have.lengthOf(2);
      expect(testCase.path[0]).to.equal("startEvent");
      expect(testCase.path[1]).to.equal("endEvent");
    });

    it("should get one test case with name and description from happyPath.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(happyPath);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(1);

      const testCase = testCases[0];
      expect(testCase).to.have.property("path");
      expect(testCase.path).to.be.an("array")
      expect(testCase.path).to.have.lengthOf(2);
      expect(testCase.path[0]).to.equal("startEvent");
      expect(testCase.path[1]).to.equal("endEvent");
      expect(testCase.name).to.equal("Happy Path");
      expect(testCase.description).to.equal("The happy path");
    });

    it("should get one test case from simpleCollaboration.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(simpleCollaboration);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(1);

      const testCase = testCases[0];
      expect(testCase).to.have.property("path");
      expect(testCase.path).to.be.an("array")
      expect(testCase.path).to.have.lengthOf(3);
      expect(testCase.path[0]).to.equal("startEvent");
      expect(testCase.path[2]).to.equal("endEvent");
    });

    it("should get test cases from a collaboration with multiple processes", () => {
      const testCaseModdle = createTestCaseModdle(advancedCollaboration);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(4);

      expect(testCases[0].path[0]).to.equal("startEventA");
      expect(testCases[0].path[1]).to.equal("subProcessStartA");
      expect(testCases[0].path[2]).to.equal("subProcessEndA");
      expect(testCases[0].path[3]).to.equal("endEventA");

      expect(testCases[1].path[0]).to.equal("startEventC");
      expect(testCases[1].path[1]).to.equal("subProcessStartC");
      expect(testCases[1].path[2]).to.equal("subProcessEndC");
      expect(testCases[1].path[3]).to.equal("endEventC");

      expect(testCases[2].path[0]).to.equal("startEventC");
      expect(testCases[2].path[1]).to.equal("subProcessStartC");
      expect(testCases[2].path[2]).to.equal("subProcessEndC");

      expect(testCases[3].path[0]).to.equal("subProcessStartC");
      expect(testCases[3].path[1]).to.equal("subProcessEndC");
      expect(testCases[3].path[2]).to.equal("endEventC");
    });
  });

  describe("setTestCase", () => {
    describe("if test cases are empty", () => {
      it("should delete the extension and remove the extensionElements", () => {
        const testCaseModdle = createTestCaseModdle(simple);

        testCaseModdle.setTestCases([]);

        const process = testCaseModdle._findProcesses()[0];
        expect(process).to.not.have.property("extensionElements");
      });

      it("should delete the extension and not remove the extensionElements, when there are other extensions", () => {
        const testCaseModdle = createTestCaseModdle(camundaProperties);

        testCaseModdle.setTestCases([]);

        const process = testCaseModdle._findProcesses()[0];
        expect(process).to.have.property("extensionElements");
  
        const extensionElements = process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal("camunda:properties");
      });
    });

    describe("if test cases are specified", () => {
      it("should create the extension", async () => {
        const testCaseModdle = createTestCaseModdle(noTestCases);

        const testCases = testCaseModdle.getTestCases();
        expect(testCases).to.have.lengthOf(0);

        const testCase = new TestCase({
          path: ["startEvent", "endEvent"],
          name: "A name",
          description: "A description",
        });

        testCaseModdle.setTestCases([testCase]);

        const process = testCaseModdle._findProcesses()[0];
        expect(process).to.have.property("extensionElements");

        const extensionElements = process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal(BPMNDT_TEST_CASES);

        const extension = extensionElements.values[0];
        expect(extension.testCases).to.be.an("array");
        expect(extension.testCases).to.have.lengthOf(1);
        expect(extension.testCases[0].$type).to.equal(BPMNDT_TEST_CASE)
        expect(extension.testCases[0]).to.have.property("path");
        expect(extension.testCases[0].path.$type).to.equal(BPMNDT_PATH);
        expect(extension.testCases[0].path).to.have.property("node");
        expect(extension.testCases[0].path.node).to.be.an("array");
        expect(extension.testCases[0].path.node).to.have.lengthOf(2);
        expect(extension.testCases[0].path.node[0]).to.be.equal("startEvent");
        expect(extension.testCases[0].path.node[1]).to.be.equal("endEvent");
        expect(extension.testCases[0].name).to.be.equal("A name");
        expect(extension.testCases[0].description).to.be.equal("A description");
      });

      it("should update the extension", async () => {
        const testCaseModdle = createTestCaseModdle(simple);

        const testCases = testCaseModdle.getTestCases();
        expect(testCases).to.have.lengthOf(1);

        testCases.push(new TestCase({
          path: ["startEvent", "endEvent"]
        }));

        testCaseModdle.setTestCases(testCases);

        const process = testCaseModdle._findProcesses()[0];
        expect(process).to.have.property("extensionElements");

        const extensionElements = process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal(BPMNDT_TEST_CASES);

        const extension = extensionElements.values[0];
        expect(extension.testCases).to.be.an("array");
        expect(extension.testCases).to.have.lengthOf(2);
        
        expect(extension.testCases[0].path.node[0]).to.be.equal("startEvent");
        expect(extension.testCases[0].path.node[1]).to.be.equal("endEvent");
        expect(extension.testCases[1].path.node[0]).to.be.equal("startEvent");
        expect(extension.testCases[1].path.node[1]).to.be.equal("endEvent");
      });
    });

    it("should set test case at the corresponding processes of a collaboration", async () => {
      const testCaseModdle = createTestCaseModdle(advancedCollaboration);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(4);

      testCases.shift();

      testCases.push(new TestCase({
        path: ["startEventB", "subProcessStartB", "subProcessEndB", "endEventB"]
      }));

      const processes = testCaseModdle._findProcesses();
      // workaround for testing, since parent property of elements is not set
      testCases[3].process = processes[1];

      testCaseModdle.setTestCases(testCases);

      expect(processes).to.have.lengthOf(3);
      expect(processes[0]).to.not.have.property("extensionElements");
      expect(processes[1]).to.have.property("extensionElements");
      expect(processes[2]).to.have.property("extensionElements");

      // processB
      const extensionElements = processes[1].extensionElements;
      expect(extensionElements.values).to.be.an("array");
      expect(extensionElements.values).to.have.lengthOf(1);
      expect(extensionElements.values[0].$type).to.equal(BPMNDT_TEST_CASES);

      const extension = extensionElements.values[0];
      expect(extension.testCases).to.be.an("array");
      expect(extension.testCases).to.have.lengthOf(1);
      
      expect(extension.testCases[0].path.node[0]).to.be.equal("startEventB");
      expect(extension.testCases[0].path.node[1]).to.be.equal("subProcessStartB");
      expect(extension.testCases[0].path.node[2]).to.be.equal("subProcessEndB");
      expect(extension.testCases[0].path.node[3]).to.be.equal("endEventB");

      // processC
      expect(processes[2].extensionElements.values).to.be.an("array");
      expect(processes[2].extensionElements.values).to.have.lengthOf(1);
      expect(processes[2].extensionElements.values[0].$type).to.equal(BPMNDT_TEST_CASES);
      expect(processes[2].extensionElements.values[0].testCases).to.have.lengthOf(3);
    });
  });
});
