import chai from "chai";

import BpmnModdle from "bpmn-moddle";
import { readBpmnFile, ElementRegistry } from "./helper";

import bpmndt from "../main/bpmndt.json";

import {
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../main/bpmndt/constants";

import PathValidator from "../main/bpmndt/PathValidator";
import TestCaseModdle from "../main/bpmndt/TestCaseModdle";

const expect = chai.expect;

const moddle = new BpmnModdle({
  bpmndt: bpmndt
});

function createTestCaseModdle(modelInstance) {
  const testCaseModdle = new TestCaseModdle(new ElementRegistry(modelInstance), null, moddle);
  testCaseModdle.findProcess();
  return testCaseModdle;
}

function createPathValidator(modelInstance) {
  const elementRegistry = new ElementRegistry(modelInstance);
  return new PathValidator(elementRegistry);
}

describe("PathValidator", () => {
  let validator;
  let testCases;

  describe("simple", () => {
    before(async () => {
      const xml = readBpmnFile("validationSimple.bpmn");
  
      const modelInstance = await moddle.fromXML(xml);
      validator = createPathValidator(modelInstance);
  
      const testCaseModdle = createTestCaseModdle(modelInstance);
      testCases = testCaseModdle.getTestCases();
    });
  
    it("should detect missing start node issue", () => {
      const problems = validator.validate(testCases[0]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({type: PROBLEM_START, end: "intermediateEvent", missing: "messageStartEvent"});
    });
  
    it("should detect missing end node issue", () => {
      const problems = validator.validate(testCases[1]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({type: PROBLEM_END, start: "intermediateEvent", missing: "messageEndEvent"});
    });
  
    it("should detect renamed flow node issue", () => {
      const problems = validator.validate(testCases[2]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({
        type: PROBLEM_PATH, start: "startEvent", end: "endEvent", autoResolvable: true,
        paths: [["startEvent", "intermediateEvent", "endEvent"]]
      });
    });
  
    it("should detect missing flow node issue", () => {
      const problems = validator.validate(testCases[3]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({
        type: PROBLEM_PATH, start: "startEvent", end: "endEvent", autoResolvable: true,
        paths: [["startEvent", "intermediateEvent", "endEvent"]]
      });
    });
  
    it("should detect unresolvable issue, when no flow node exists", () => {
      const problems = validator.validate(testCases[4]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({type: PROBLEM_UNRESOLVABLE});
    });

    it("should detect issues", () => {
      const problems = validator.validate(testCases[5]);
      expect(problems).to.have.lengthOf(2);
      expect(problems[0]).to.deep.equal({type: PROBLEM_START, end: "intermediateEvent", missing: "messageStartEvent"});
      expect(problems[1]).to.deep.equal({type: PROBLEM_END, start: "intermediateEvent", missing: "messageEndEvent"});
    });

    it("should detect no issues, when path is still valid", () => {
      const problems = validator.validate(testCases[6]);
      expect(problems).to.have.lengthOf(0);
    });
  });

  describe("advanced", () => {
    let validator;
    let testCases;

    before(async () => {
      const xml = readBpmnFile("validationAdvanced.bpmn");
  
      const modelInstance = await moddle.fromXML(xml);
      validator = createPathValidator(modelInstance);
  
      const testCaseModdle = createTestCaseModdle(modelInstance);
      testCases = testCaseModdle.getTestCases();
    });

    it("should detect unresolvable issue, when no path exists", () => {
      const problems = validator.validate(testCases[0]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.deep.equal({type: PROBLEM_UNRESOLVABLE});
    });

    it("should detect missing path issue", () => {
      const problems = validator.validate(testCases[1]);
      expect(problems).to.have.lengthOf(1);
      expect(problems[0]).to.include.keys("paths");
      expect(problems[0]).to.deep.include({type: PROBLEM_PATH, start: "startEvent", end: "endEvent"});
    });

    it("should detect no issues, when path is still valid", () => {
      const problems = validator.validate(testCases[2]);
      expect(problems).to.have.lengthOf(0);
    });
  });
});
