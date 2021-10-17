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
  return new TestCaseModdle({
    elementRegistry: new ElementRegistry(modelInstance),
    moddle: moddle
  });
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
  // collaboration with two participants
  let simpleCollaboration;

  before(async () => {
    camundaProperties = await moddle.fromXML(readBpmnFile("camundaProperties.bpmn"));
    happyPath = await moddle.fromXML(readBpmnFile("happyPath.bpmn"));
    noTestCases = await moddle.fromXML(readBpmnFile("noTestCases.bpmn"));
    simple = await moddle.fromXML(readBpmnFile("simple.bpmn"));
    simpleCollaboration = await moddle.fromXML(readBpmnFile("simpleCollaboration.bpmn"));
  });

  beforeEach(async () => {
    // reused and modified in different tests
    simple = await moddle.fromXML(readBpmnFile("simple.bpmn"));
  });

  describe("getTestCase", () => {
    it("should get no test cases from noTestCases.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(noTestCases);
      expect(testCaseModdle.findProcess()).to.be.true;

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(0);
    });

    it("should get one test case from simple.bpmn", () => {
      const testCaseModdle = createTestCaseModdle(simple);
      expect(testCaseModdle.findProcess()).to.be.true;

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
      expect(testCaseModdle.findProcess()).to.be.true;

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
      expect(testCaseModdle.findProcess()).to.be.true;

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(1);

      const testCase = testCases[0];
      expect(testCase).to.have.property("path");
      expect(testCase.path).to.be.an("array")
      expect(testCase.path).to.have.lengthOf(3);
      expect(testCase.path[0]).to.equal("startEvent");
      expect(testCase.path[2]).to.equal("endEvent");
    });
  });

  describe("setTestCase", () => {
    describe("if test cases are empty", () => {
      it("should delete the extension and remove the extensionElements", () => {
        const testCaseModdle = createTestCaseModdle(simple);
        expect(testCaseModdle.findProcess()).to.be.true;
  
        testCaseModdle.setTestCases([]);
  
        expect(testCaseModdle.process).to.not.have.property("extensionElements");
      });

      it("should delete the extension and not remove the extensionElements, when there are other extensions", () => {
        const testCaseModdle = createTestCaseModdle(camundaProperties);
        expect(testCaseModdle.findProcess()).to.be.true;
  
        testCaseModdle.setTestCases([]);
  
        expect(testCaseModdle.process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle.process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal("camunda:properties");
      });
    });

    describe("if test cases are specified", () => {
      it("should create the extension", async () => {
        const testCaseModdle = createTestCaseModdle(noTestCases);
        expect(testCaseModdle.findProcess()).to.be.true;
  
        const testCase = new TestCase({
          path: ["a", "b"],
          name: "A name",
          description: "A description",
        });
  
        testCaseModdle.setTestCases([testCase]);
  
        expect(testCaseModdle.process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle.process.extensionElements;
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
        expect(extension.testCases[0].path.node[0]).to.be.equal("a");
        expect(extension.testCases[0].path.node[1]).to.be.equal("b");
        expect(extension.testCases[0].name).to.be.equal("A name");
        expect(extension.testCases[0].description).to.be.equal("A description");
      });

      it("should update the extension", async () => {
        const testCaseModdle = createTestCaseModdle(simple);
        expect(testCaseModdle.findProcess()).to.be.true;

        const testCases = testCaseModdle.getTestCases();
        expect(testCases).to.have.lengthOf(1);
  
        testCases.push(new TestCase({
          path: ["a", "b"]
        }));

        testCaseModdle.setTestCases(testCases);
  
        expect(testCaseModdle.process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle.process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal(BPMNDT_TEST_CASES);
  
        const extension = extensionElements.values[0];
        expect(extension.testCases).to.be.an("array");
        expect(extension.testCases).to.have.lengthOf(2);
        expect(extension.testCases[0].path.node[0]).to.be.equal("startEvent");
        expect(extension.testCases[0].path.node[1]).to.be.equal("endEvent");
        expect(extension.testCases[1].path.node[0]).to.be.equal("a");
        expect(extension.testCases[1].path.node[1]).to.be.equal("b");
      });
    });
  });
});
