import chai from "chai";

import BpmnModdle from "bpmn-moddle";
import { readBpmnFile, ElementRegistry } from "./helper";

import PathFinder from "../main/bpmn-driven-testing/PathFinder";
import PathSelection from "../main/bpmn-driven-testing/PathSelection";

const expect = chai.expect;

const moddle = new BpmnModdle();

describe("PathFinder", () => {
  it("should find simple path", async () => {
    const modelInstance = await moddle.fromXML(readBpmnFile("simple.bpmn"));

    const pathFinder = new PathFinder(new ElementRegistry(modelInstance));

    const paths = pathFinder.findPaths(new PathSelection(["startEvent", "endEvent"]));
    expect(paths).to.be.an("array");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.be.an("array");
    expect(path).to.have.lengthOf(2);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("endEvent");
  });

  it("should find simple path within participant of collaboration", async () => {
    const modelInstance = await moddle.fromXML(readBpmnFile("simpleCollaboration.bpmn"));

    const pathFinder = new PathFinder(new ElementRegistry(modelInstance));

    const paths = pathFinder.findPaths(new PathSelection(["startEvent", "endEvent"]));
    expect(paths).to.be.an("array");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.be.an("array");
    expect(path).to.have.lengthOf(3);
    expect(path[0]).to.equal("startEvent");
    expect(path[2]).to.equal("endEvent");
  });

  it("should not find path, when end node does not exist", async () => {
    const modelInstance = await moddle.fromXML(readBpmnFile("simple.bpmn"));

    const pathFinder = new PathFinder(new ElementRegistry(modelInstance));

    const paths = pathFinder.findPaths(new PathSelection(["startEvent", "notExisting"]));
    expect(paths).to.be.an("array");
    expect(paths).to.have.lengthOf(0);
  });

  it("should detect and ignore loops", async () => {
    const modelInstance = await moddle.fromXML(readBpmnFile("simpleLoop.bpmn"));

    const pathFinder = new PathFinder(new ElementRegistry(modelInstance));

    const paths = pathFinder.findPaths(new PathSelection(["startEvent", "endEvent"]));
    expect(paths).to.be.an("array");
    expect(paths).to.have.lengthOf(1);

    const path = paths[0];
    expect(path).to.be.an("array");
    expect(path).to.have.lengthOf(4);
    expect(path[0]).to.equal("startEvent");
    expect(path[1]).to.equal("exclusiveGatewayFork");
    expect(path[2]).to.equal("exclusiveGatewayJoin");
    expect(path[3]).to.equal("endEvent");
  });
});
