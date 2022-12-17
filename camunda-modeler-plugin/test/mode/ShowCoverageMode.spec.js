import chai from "chai";
import spies from "chai-spies";

import { Plugin } from "../helper";

import ShowCoverageMode from "../../main/bpmndt/mode/ShowCoverageMode";
import TestCase from "../../main/bpmndt/TestCase";

chai.use(spies);
const expect = chai.expect;

describe("mode/ShowCoverageMode", () => {
  let plugin;

  beforeEach(() => {
    const testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["c", "d", "e", "f"]}));

    plugin = new Plugin();
    plugin.testCases = testCases;

    chai.spy.on(plugin, ["mark"]);
  });

  it("should define ID", () => {
    const mode = new ShowCoverageMode(plugin);
    expect(mode.id).to.not.be.undefined;
  });

  it("should mark coverage", () => {
    new ShowCoverageMode(plugin);

    expect(plugin.mark).to.have.been.called.once;
    expect(plugin.markers).to.have.lengthOf(6);
  });
});
