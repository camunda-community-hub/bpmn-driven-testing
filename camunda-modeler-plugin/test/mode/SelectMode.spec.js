import chai from "chai";
import spies from "chai-spies";

import { Plugin } from "../helper";

import { MODE_VIEW, PROBLEM_PATH } from "../../main/bpmndt/constants";
import SelectMode from "../../main/bpmndt/mode/SelectMode";
import TestCase from "../../main/bpmndt/TestCase";
import TestCaseMigration from "../../main/bpmndt/TestCaseMigration";

chai.use(spies);
const expect = chai.expect;

describe("mode/SelectMode", () => {
  let plugin;

  beforeEach(() => {
    const testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["a", "e", "f", "d"]}));

    plugin = new Plugin();
    plugin.testCases = testCases;

    chai.spy.on(plugin, ["enrichTestCase", "mark", "markAsChanged", "setMode"]);
  });

  afterEach(() => {
    chai.spy.restore(plugin);
  });

  it("should define ID", () => {
    const mode = new SelectMode(plugin);
    expect(mode.id).to.not.be.undefined;
  });

  describe("selection", () => {
    it("should compute initial state", () => {
      const mode = new SelectMode(plugin);
  
      expect(mode.paths).to.have.lengthOf(0);
      expect(mode.pathEquality).to.have.lengthOf(0);
      expect(mode.pathIndex).to.equal(-1);
      expect(mode.selection).to.deep.equal(new TestCase());

      expect(plugin.mark).to.have.been.called.once;
    });
  
    it("should update selection, when nextPath or prevPath is called", () => {
      const mode = new SelectMode(plugin);

      // simulate selection and path finding
      mode.paths = [["a", "x1", "d"], ["a", "y1", "y2", "d"], ["a", "z1", "z2", "z3", "d"]];
      mode.pathEquality = [false, false, false];
      mode.pathIndex = 0;
      mode.selection.start = "a";
      mode.selection.startType = "bpmn:StartEvent";
      mode.selection.end = "d";
      mode.selection.endType = "bpmn:EndEvent";
      mode.selection.path = mode.paths[0];
  
      mode.nextPath();
      expect(mode.pathIndex).to.equal(1);
      expect(mode.selection.path).to.deep.equal(mode.paths[1]);
  
      mode.nextPath();
      expect(mode.pathIndex).to.equal(2);
      expect(mode.selection.path).to.deep.equal(mode.paths[2]);
  
      mode.nextPath();
      expect(mode.pathIndex).to.equal(0);
      expect(mode.selection.path).to.deep.equal(mode.paths[0]);
  
      mode.prevPath();
      expect(mode.pathIndex).to.equal(2);
      expect(mode.selection.path).to.deep.equal(mode.paths[2]);
  
      mode.prevPath();
      expect(mode.pathIndex).to.equal(1);
      expect(mode.selection.path).to.deep.equal(mode.paths[1]);
  
      mode.prevPath();
      expect(mode.pathIndex).to.equal(0);
      expect(mode.selection.path).to.deep.equal(mode.paths[0]);
  
      expect(plugin.mark).to.have.been.called.exactly(7);
    });
  
    it("should add test case", () => {
      const mode = new SelectMode(plugin);
  
      // simulate selection and path finding
      mode.paths = [["a", "x1", "d"], ["a", "y1", "y2", "d"], ["a", "z1", "z2", "z3", "d"]];
      mode.pathEquality = [false, false, false];
      mode.pathIndex = 0;
      mode.selection.start = "a";
      mode.selection.startType = "bpmn:StartEvent";
      mode.selection.end = "d";
      mode.selection.endType = "bpmn:EndEvent";
      mode.selection.path = mode.paths[0];
  
      mode.addTestCase();
      expect(mode.paths).to.have.lengthOf(2);
      expect(mode.pathIndex).to.equal(0);
      expect(mode.selection.path).to.deep.equal(mode.paths[0]);
      expect(mode.testCases).to.have.lengthOf(3);
      expect(mode.testCases[2]).to.deep.equal(new TestCase({path: ["a", "x1", "d"]}));

      mode.nextPath();

      mode.addTestCase();
      expect(mode.paths).to.have.lengthOf(1);
      expect(mode.pathIndex).to.equal(0);
      expect(mode.selection.path).to.deep.equal(mode.paths[0]);
      expect(mode.testCases).to.have.lengthOf(4);
      expect(mode.testCases[3]).to.deep.equal(new TestCase({path: ["a", "z1", "z2", "z3", "d"]}));
  
      mode.addTestCase();
      expect(mode.paths).to.have.lengthOf(0);
      expect(mode.pathIndex).to.equal(-1);
      expect(mode.selection).to.deep.equal(new TestCase());
      expect(mode.testCases).to.have.lengthOf(5);
      expect(mode.testCases[4]).to.deep.equal(new TestCase({path: ["a", "y1", "y2", "d"]}));

      expect(plugin.enrichTestCase).to.have.been.called.exactly(3);
      expect(plugin.mark).to.have.been.called.exactly(5);
      expect(plugin.markAsChanged).to.have.been.called.exactly(3);
    });
  });

  describe("selection during migration", () => {
    it("should compute initial state", () => {
      const { testCases } = plugin;

      testCases[0].startType = "bpmn:StartEvent";
      testCases[0].endType = "bpmn:EndEvent";
      testCases[0].problems = [
        {type: PROBLEM_PATH, start: "a", end: "d", paths: [["a", "x", "d"], ["a", "y", "d"], ["a", "z", "d"]]}
      ];

      const migration = new TestCaseMigration(testCases[0], testCases[0].problems[0]);

      const mode = new SelectMode(plugin, { migration });
      expect(mode.paths).to.have.lengthOf(3);
      expect(mode.pathEquality).to.deep.equal([false, false, false]);
      expect(mode.pathIndex).to.equal(0);
      expect(mode.migration).to.deep.equal(migration);
      expect(mode.selection.path).to.deep.equal(testCases[0].problems[0].paths[0]);
  
      expect(plugin.mark).to.have.been.called.once;
    });

    it("should migrate test case", () => {
      const { testCases } = plugin;

      testCases[0].problems = [
        {type: PROBLEM_PATH, start: "a", end: "d", paths: [["a", "x", "d"], ["a", "y", "d"], ["a", "z", "d"]]}
      ];
  
      const migration = new TestCaseMigration(testCases[0], testCases[0].problems[0]);
  
      const mode = new SelectMode(plugin, { migration });

      mode.migrateTestCase();
      expect(plugin.markAsChanged).to.have.been.called.once;
      expect(plugin.setMode).to.have.been.called.once;
      expect(plugin.setMode).to.have.been.called.with(MODE_VIEW);

      expect(testCases[0].path).to.deep.equal(["a", "x", "d"]);
      expect(testCases[0].problems).to.have.lengthOf(0);
    });
  });
});
