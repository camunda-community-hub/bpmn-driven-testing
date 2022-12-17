import chai from "chai";
import spies from "chai-spies";

import { Plugin } from "../helper";

import TestCase from "../../main/bpmndt/TestCase";
import ViewMode from "../../main/bpmndt/mode/ViewMode";

chai.use(spies);
const expect = chai.expect;

describe("mode/ViewMode", () => {
  let plugin;

  beforeEach(() => {
    const testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["e", "f", "g"]}));
    testCases.push(new TestCase({path: ["h", "i"]}));

    plugin = new Plugin();
    plugin.testCases = testCases;

    chai.spy.on(plugin, ["mark", "markAsChanged"]);
  });

  afterEach(() => {
    chai.spy.restore(plugin);
  });

  it("should define ID", () => {
    const mode = new ViewMode(plugin, null);
    expect(mode.id).to.not.be.undefined;
  });

  it("should compute initial state, when there are no test cases", () => {
    plugin.testCases = [];

    const mode = new ViewMode(plugin, null);
    expect(mode.testCaseIndex).to.equal(-1);

    expect(plugin.mark).to.not.have.been.called;
  });

  it("should compute initial state, when there are test cases", () => {
    const mode = new ViewMode(plugin, null);
    expect(mode.testCaseIndex).to.equal(0);

    expect(plugin.mark).to.have.been.called.once;
  });

  it("should use test case index from old mode", () => {
    const mode = new ViewMode(plugin, { testCaseIndex: 1 });
    expect(mode.testCaseIndex).to.equal(1);

    expect(plugin.mark).to.have.been.called.once;
  });

  it("should update test case index and markers, when nextTestCase or prevTestCase is called", () => {
    const mode = new ViewMode(plugin, null);

    mode.nextTestCase();
    expect(mode.testCaseIndex).to.equal(1);
    mode.nextTestCase();
    expect(mode.testCaseIndex).to.equal(2);
    mode.nextTestCase();
    expect(mode.testCaseIndex).to.equal(0);

    mode.prevTestCase();
    expect(mode.testCaseIndex).to.equal(2);
    mode.prevTestCase();
    expect(mode.testCaseIndex).to.equal(1);
    mode.prevTestCase();
    expect(mode.testCaseIndex).to.equal(0);

    expect(plugin.mark).to.have.been.called.exactly(7);
  });

  it("should remove test case", () => {
    const mode = new ViewMode(plugin, { testCaseIndex: 1 });

    mode.removeTestCase();
    expect(mode.testCaseIndex).to.equal(0);

    expect(plugin.testCases).to.have.lengthOf(2);
    expect(plugin.testCases[0].path[0]).to.equal("a");
    expect(plugin.testCases[1].path[0]).to.equal("h");

    expect(plugin.mark).to.have.been.called.exactly(2);
    expect(plugin.markAsChanged).to.have.been.called.once;
  });
});
