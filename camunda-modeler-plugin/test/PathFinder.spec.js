import chai from "chai";

import BpmnModdle from "bpmn-moddle";
import { readBpmnFile, ElementRegistry } from "./helper";

import PathFinder from "../main/bpmndt/PathFinder";

const expect = chai.expect;

function createPathFinder(modelInstance) {
  const elementRegistry = new ElementRegistry(modelInstance);
  return new PathFinder(elementRegistry);
}

describe("PathFinder", () => {
  let simple;
  let simpleCollaboration;
  let simpleLoop;

  let subProcessErrorEscalation;
  let subProcessErrorEscalationNegative;

  before(async () => {
    const moddle = new BpmnModdle();

    simple = await moddle.fromXML(readBpmnFile("simple.bpmn"));
    simpleCollaboration = await moddle.fromXML(readBpmnFile("simpleCollaboration.bpmn"));
    simpleLoop = await moddle.fromXML(readBpmnFile("simpleLoop.bpmn"));
    
    subProcessErrorEscalation = await moddle.fromXML(readBpmnFile("subProcessErrorEscalation.bpmn"));
    subProcessErrorEscalationNegative = await moddle.fromXML(readBpmnFile("subProcessErrorEscalationNegative.bpmn"));
  });

  it("should find simple path", () => {
    const pathFinder = createPathFinder(simple);

    const paths = pathFinder.find("startEvent", "endEvent");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.have.lengthOf(2);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("endEvent");
  });

  it("should find simple path within participant of collaboration", () => {
    const pathFinder = createPathFinder(simpleCollaboration);

    const paths = pathFinder.find("startEvent", "endEvent");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.have.lengthOf(3);
    expect(path[0]).to.equal("startEvent");
    expect(path[2]).to.equal("endEvent");
  });

  it("should not find path, when end node does not exist", () => {
    const pathFinder = createPathFinder(simple);

    const paths = pathFinder.find("startEvent", "notExisting");
    expect(paths).to.have.lengthOf(0);
  });

  it("should detect and ignore loops", () => {
    const pathFinder = createPathFinder(simpleLoop);

    const paths = pathFinder.find("startEvent", "endEvent");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.have.lengthOf(4);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("exclusiveGatewayFork");
    expect(path[2]).to.equal("exclusiveGatewayJoin");
    expect(path[3]).to.equal("endEvent");
  });

  it("should find path through embedded sub process error and escalation end events", () => {
    const pathFinder = createPathFinder(subProcessErrorEscalation);

    const paths = pathFinder.find("startEvent", "altEndEvent");
    expect(paths).to.have.lengthOf(4);

    expect(paths[0]).to.have.lengthOf(7);
    expect(paths[0]).to.deep.equal(["startEvent", "subProcessStartEvent", "g1", "errorEndEventA", "errorBoundaryEventA", "g2", "altEndEvent"]);

    expect(paths[1]).to.have.lengthOf(7);
    expect(paths[1]).to.deep.equal(["startEvent", "subProcessStartEvent", "g1", "escalationEndEventA", "escalationBoundaryEventA", "g2", "altEndEvent"]);

    expect(paths[2]).to.have.lengthOf(7);
    expect(paths[2]).to.deep.equal(["startEvent", "subProcessStartEvent", "g1", "errorEndEventB", "errorBoundaryEventB", "g2", "altEndEvent"]);

    expect(paths[3]).to.have.lengthOf(7);
    expect(paths[3]).to.deep.equal(["startEvent", "subProcessStartEvent", "g1", "escalationEndEventB", "escalationBoundaryEventB", "g2", "altEndEvent"]);
  });

  it("should not find path through embedded sub process error and escalation end events", () => {
    const pathFinder = createPathFinder(subProcessErrorEscalationNegative);

    const paths = pathFinder.find("startEvent", "altEndEvent");
    expect(paths).to.have.lengthOf(0);
  });
});
