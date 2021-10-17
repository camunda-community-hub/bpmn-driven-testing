import chai from "chai";

import { PluginController } from "../helper";

import TestCase from "../../main/bpmndt/TestCase";

import ShowCoverageMode from "../../main/bpmndt/ui/ShowCoverageMode";

const expect = chai.expect;

describe("ui/ShowCoverageMode", () => {
  let mode;

  let controller;
  let testCases;

  beforeEach(() => {
    controller = new PluginController();

    mode = new ShowCoverageMode(controller);

    testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["c", "d", "e", "f"]}));
  });

  it("should compute initial state", () => {
    expect(mode.id).to.not.be.undefined;

    const state = mode.computeInitialState({ testCases });
    expect(state.markers).to.have.lengthOf(6);
  });
});
