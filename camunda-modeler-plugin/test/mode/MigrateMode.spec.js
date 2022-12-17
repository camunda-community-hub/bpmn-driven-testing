import chai from "chai";
import spies from "chai-spies";

import { Plugin } from "../helper";

import {
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../../main/bpmndt/constants";

import MigrateMode from "../../main/bpmndt/mode/MigrateMode";
import TestCase from "../../main/bpmndt/TestCase";

chai.use(spies);
const expect = chai.expect;

describe("mode/MigrateMode", () => {
  let plugin;

  beforeEach(() => {
    const testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["e", "f", "g"]}));
    testCases.push(new TestCase({path: ["h", "i"]}));

    testCases[0].problems = [
      {type: PROBLEM_START, end: "b", missing: "a"},
      {type: PROBLEM_END, start: "c", missing: "d"},
    ];

    testCases[1].problems = [
      {type: PROBLEM_PATH, start: "e", end: "g", paths: [["e", "x", "g"]]}
    ];

    testCases[2].problems = [
      {type: PROBLEM_UNRESOLVABLE}
    ];

    plugin = new Plugin();
    plugin.testCases = testCases;

    chai.spy.on(plugin, ["mark", "setMode"]);
  });

  afterEach(() => {
    chai.spy.restore(plugin);
  });

  it("should compute initial state", () => {
    const { testCases } = plugin;

    const mode = new MigrateMode(plugin, { testCases, testCaseIndex: 0 });
    expect(mode.id).to.not.be.undefined;

    expect(mode.problems).to.have.lengthOf(2);
    expect(mode.problemIndex).to.equal(0);
    expect(mode.migration).to.not.be.undefined;
    expect(mode.testCase).to.deep.equal(testCases[0]);

    expect(plugin.mark).to.not.have.been.called;
  });

  it("should update problem index and markers, when nextTestCase or prevTestCase is called", () => {
    const { testCases } = plugin;

    const mode = new MigrateMode(plugin, { testCases, testCaseIndex: 0 });

    mode.nextProblem();
    expect(mode.problemIndex).to.equal(1);

    mode.nextProblem();
    expect(mode.problemIndex).to.equal(0);

    mode.prevProblem();
    expect(mode.problemIndex).to.equal(1);

    mode.prevProblem();
    expect(mode.problemIndex).to.equal(0);

    expect(plugin.mark).to.have.been.called.exactly(4);
  });
});
