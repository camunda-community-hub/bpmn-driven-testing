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
  let linkEvent;
  let linkEventSubProcess;

  let simple;
  let simpleCollaboration;
  let simpleLoop;

  let subProcessErrorEscalation;
  let subProcessErrorEscalationNegative;

  before(async () => {
    const moddle = new BpmnModdle();

    linkEvent = await moddle.fromXML(readBpmnFile("linkEvent.bpmn"));
    linkEventSubProcess = await moddle.fromXML(readBpmnFile("linkEventSubProcess.bpmn"));

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

  it("should find path through link events", () => {
    const pathFinder = createPathFinder(linkEvent);

    const paths = pathFinder.find("forkA", "joinB");
    expect(paths).to.have.lengthOf(2);

    let path;

    path = paths[0];
    expect(path).to.have.lengthOf(5);
    expect(path[0]).to.equal("forkA");
    expect(path[1]).to.equal("linkThrowEventA");
    expect(path[2]).to.equal("linkCatchEventA");
    expect(path[3]).to.equal("joinA");
    expect(path[4]).to.equal("joinB");

    path = paths[1];
    expect(path).to.have.lengthOf(5);
    expect(path[0]).to.equal("forkA");
    expect(path[1]).to.equal("forkB");
    expect(path[2]).to.equal("linkThrowEventB");
    expect(path[3]).to.equal("linkCatchEventB");
    expect(path[4]).to.equal("joinB");
  });

  it("should find path through link events of same scope", () => {
    const pathFinder = createPathFinder(linkEventSubProcess);

    const paths = pathFinder.find("startEvent", "endEvent");
    expect(paths).to.have.lengthOf(2);

    let path;

    path = paths[0];
    expect(path).to.have.lengthOf(8);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("fork");
    expect(path[2]).to.equal("startEventA");
    expect(path[3]).to.equal("linkThrowEventA");
    expect(path[4]).to.equal("linkCatchEventA");
    expect(path[5]).to.equal("endEventA");
    expect(path[6]).to.equal("join");
    expect(path[7]).to.equal("endEvent");

    path = paths[1];
    expect(path).to.have.lengthOf(8);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("fork");
    expect(path[2]).to.equal("startEventB");
    expect(path[3]).to.equal("linkThrowEventB");
    expect(path[4]).to.equal("linkCatchEventB");
    expect(path[5]).to.equal("endEventB");
    expect(path[6]).to.equal("join");
    expect(path[7]).to.equal("endEvent");
  });

  it("should append link catch event, when path ends at link throw event", () => {
    const pathFinder = createPathFinder(linkEvent);

    const paths = pathFinder.find("forkA", "linkThrowEventA");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.have.lengthOf(3);
    expect(path[0]).to.equal("forkA");
    expect(path[1]).to.equal("linkThrowEventA");
    expect(path[2]).to.equal("linkCatchEventA");
  });

  it("should append link catch event of same scope, when path ends at link throw event", () => {
    const pathFinder = createPathFinder(linkEventSubProcess);

    const paths = pathFinder.find("startEvent", "linkThrowEventA");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.have.lengthOf(5);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("fork");
    expect(path[2]).to.equal("startEventA");
    expect(path[3]).to.equal("linkThrowEventA");
    expect(path[4]).to.equal("linkCatchEventA");
  });
});
