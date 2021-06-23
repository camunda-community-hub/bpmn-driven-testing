import chai from "chai";

import BpmnModdle from "bpmn-moddle";
import { readBpmnFile, ElementRegistry } from "./helper";

import bpmndt from "../main/bpmn-driven-testing.json";

import TestCase from "../main/bpmn-driven-testing/TestCase";
import TestCaseModdle from "../main/bpmn-driven-testing/TestCaseModdle";

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
  let camundaPropertiesXml;
  // no test cases, no extensionElements
  let noTestCasesXml;
  // one test case
  let simpleXml;
  // collaboration with two participants
  let simpleCollaborationXml;

  before(() => {
    camundaPropertiesXml = readBpmnFile("camundaProperties.bpmn");
    noTestCasesXml = readBpmnFile("noTestCases.bpmn");
    simpleXml = readBpmnFile("simple.bpmn");
    simpleCollaborationXml = readBpmnFile("simpleCollaboration.bpmn");
  });

  describe("getTestCase", () => {
    it("should get no test cases from noTestCases.bpmn", async () => {
      const modelInstance = await moddle.fromXML(noTestCasesXml);

      const testCaseModdle = createTestCaseModdle(modelInstance);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(0);
    });

    it("should get one test case from simple.bpmn", async () => {
      const modelInstance = await moddle.fromXML(simpleXml);

      const testCaseModdle = createTestCaseModdle(modelInstance);

      const testCases = testCaseModdle.getTestCases();
      expect(testCases).to.have.lengthOf(1);

      const testCase = testCases[0];
      expect(testCase).to.have.property("path");
      expect(testCase.path).to.be.an("array")
      expect(testCase.path).to.have.lengthOf(2);
      expect(testCase.path[0]).to.equal("startEvent");
      expect(testCase.path[1]).to.equal("endEvent");
    });

    it("should get one test case from simpleCollaboration.bpmn", async () => {
      const modelInstance = await moddle.fromXML(simpleCollaborationXml);

      const testCaseModdle = createTestCaseModdle(modelInstance);

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
      it("should delete the extension and remove the extensionElements", async () => {
        const modelInstance = await moddle.fromXML(simpleXml);
  
        const testCaseModdle = createTestCaseModdle(modelInstance);
  
        testCaseModdle.setTestCases([]);
  
        expect(testCaseModdle._process).to.not.have.property("extensionElements");
      });

      it("should delete the extension and not remove the extensionElements, when there are other extensions", async () => {
        const modelInstance = await moddle.fromXML(camundaPropertiesXml);
  
        const testCaseModdle = createTestCaseModdle(modelInstance);
  
        testCaseModdle.setTestCases([]);
  
        expect(testCaseModdle._process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle._process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal("camunda:properties");
      });
    });

    describe("if test cases are specified", () => {
      it("should create the extension", async () => {
        const modelInstance = await moddle.fromXML(noTestCasesXml);
  
        const testCaseModdle = createTestCaseModdle(modelInstance);
  
        const testCase = new TestCase({
          path: ["a", "b"]
        });
  
        testCaseModdle.setTestCases([testCase]);
  
        expect(testCaseModdle._process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle._process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal("bpmndt:TestCases");
  
        const extension = extensionElements.values[0];
        expect(extension.testCases).to.be.an("array");
        expect(extension.testCases).to.have.lengthOf(1);
        expect(extension.testCases[0].$type).to.equal("bpmndt:TestCase")
        expect(extension.testCases[0]).to.have.property("path");
        expect(extension.testCases[0].path.$type).to.equal("bpmndt:Path");
        expect(extension.testCases[0].path).to.have.property("node");
        expect(extension.testCases[0].path.node).to.be.an("array");
        expect(extension.testCases[0].path.node).to.have.lengthOf(2);
        expect(extension.testCases[0].path.node[0]).to.be.equal("a");
        expect(extension.testCases[0].path.node[1]).to.be.equal("b");
      });

      it("should update the extension", async () => {
        const modelInstance = await moddle.fromXML(simpleXml);
  
        const testCaseModdle = createTestCaseModdle(modelInstance);

        const testCases = testCaseModdle.getTestCases();
  
        testCases.push(new TestCase({
          path: ["a", "b"]
        }));
  
        testCaseModdle.setTestCases(testCases);
  
        expect(testCaseModdle._process).to.have.property("extensionElements");
  
        const extensionElements = testCaseModdle._process.extensionElements;
        expect(extensionElements.values).to.be.an("array");
        expect(extensionElements.values).to.have.lengthOf(1);
        expect(extensionElements.values[0].$type).to.equal("bpmndt:TestCases");
  
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
