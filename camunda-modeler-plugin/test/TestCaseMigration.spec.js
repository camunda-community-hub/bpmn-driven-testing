import chai from "chai";

import {
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../main/bpmndt/constants";

import TestCase from "../main/bpmndt/TestCase";

import TestCaseMigration from "../main/bpmndt/TestCaseMigration";

const expect = chai.expect;

describe("TestCaseMigration", () => {
  let migration;

  let testCase;
  let problem;

  beforeEach(() => {
    testCase = new TestCase({path: ["a", "b", "c"]});
    testCase.problems = [
      {type: PROBLEM_START, end: "b", missing: "a"},
      {type: PROBLEM_END, start: "b", missing: "c"},
      {type: PROBLEM_PATH, start: "a", end: "c", paths: [["a", "x", "c"], ["a", "y", "c"], ["a", "z", "c"]]},
      {type: PROBLEM_UNRESOLVABLE}
    ];
  });

  describe("PROBLEM_START", () => {
    beforeEach(() => {
      problem = testCase.problems[0];

      migration = new TestCaseMigration(testCase, problem);
    });

    it("getMarkers", () => {
      const selection = migration.getSelection();

      const markers = migration.getMarkers(selection);
      expect(markers).to.have.lengthOf(1);
      expect(markers[0].id).to.equal("b");
    });

    it("getPaths", () => {
      expect(migration.getPaths()).to.have.lengthOf(0);
    });

    it("getSelection", () => {
      const selection = migration.getSelection();
      expect(selection.start).to.be.undefined;
      expect(selection.end).to.equal("b");
    });

    it("handleSelection", () => {
      const selection = migration.getSelection();

      migration.handleSelection(selection, "x");
      expect(selection.start).to.equal("x");
      migration.handleSelection(selection, "x");
      expect(selection.start).to.be.null;

      migration.handleSelection(selection, "x");
      migration.handleSelection(selection, "b");
      expect(selection.start).to.be.null;
    });

    it("migrate", () => {
      migration.migrate(new TestCase({path: ["x", "y", "b"]}));
      expect(testCase.path).to.deep.equal(["x", "y", "b", "c"]);
      expect(testCase.problems.indexOf(problem)).to.equal(-1);
    });
  });

  describe("PROBLEM_END", () => {
    beforeEach(() => {
      problem = testCase.problems[1];

      migration = new TestCaseMigration(testCase, problem);
    });

    it("getMarkers", () => {
      const selection = migration.getSelection();

      const markers = migration.getMarkers(selection);
      expect(markers).to.have.lengthOf(1);
      expect(markers[0].id).to.equal("b");
    });

    it("getPaths", () => {
      expect(migration.getPaths()).to.have.lengthOf(0);
    });

    it("getSelection", () => {
      const selection = migration.getSelection();
      expect(selection.start).to.equal("b");
      expect(selection.end).to.be.undefined;
    });

    it("handleSelection", () => {
      const selection = migration.getSelection();

      migration.handleSelection(selection, "x");
      expect(selection.end).to.equal("x");
      migration.handleSelection(selection, "x");
      expect(selection.end).to.be.null;

      migration.handleSelection(selection, "x");
      migration.handleSelection(selection, "b");
      expect(selection.end).to.be.null;
    });

    it("migrate", () => {
      migration.migrate(new TestCase({path: ["b", "x", "y"]}));
      expect(testCase.path).to.deep.equal(["a", "b", "x", "y"]);
      expect(testCase.problems.indexOf(problem)).to.equal(-1);
    });
  });

  describe("PROBLEM_PATH", () => {
    beforeEach(() => {
      problem = testCase.problems[2];

      migration = new TestCaseMigration(testCase, problem);
    });

    it("getMarkers", () => {
      const selection = migration.getSelection();

      const markers = migration.getMarkers(selection);
      expect(markers).to.have.lengthOf(4);
      expect(markers[0].id).to.equal("x");
      expect(markers[1].id).to.equal("b");
      expect(markers[2].id).to.equal("a");
      expect(markers[3].id).to.equal("c");
    });

    it("getPaths", () => {
      expect(migration.getPaths()).to.have.lengthOf(3);
    });

    it("getSelection", () => {
      const selection = migration.getSelection();
      expect(selection.start).to.equal("a");
      expect(selection.end).to.equal("c");
      expect(selection.path).to.deep.equal(["a", "x", "c"]);
    });

    it("handleSelection", () => {
      const selection = migration.getSelection();

      // should not affect selection
      migration.handleSelection(selection, "z");
      expect(selection.start).to.equal("a");
      expect(selection.end).to.equal("c");
    });

    it("migrate", () => {
      migration.migrate(new TestCase({path: ["a", "z", "c"]}));
      expect(testCase.path).to.deep.equal(["a", "z", "c"]);
      expect(testCase.problems.indexOf(problem)).to.equal(-1);
    });
  });

  describe("PROBLEM_UNRESOLVABLE", () => {
    beforeEach(() => {
      problem = testCase.problems[3];

      migration = new TestCaseMigration(testCase, problem);
    });

    it("getMarkers", () => {
      const selection = migration.getSelection();

      const markers = migration.getMarkers(selection);
      expect(markers).to.have.lengthOf(3);
      expect(markers[0].id).to.equal("a");
      expect(markers[1].id).to.equal("b");
      expect(markers[2].id).to.equal("c");
    });

    it("getPaths", () => {
      expect(migration.getPaths()).to.have.lengthOf(0);
    });

    it("getSelection", () => {
      const selection = migration.getSelection();
      expect(selection.start).to.be.undefined;
      expect(selection.end).to.be.undefined;
    });

    it("handleSelection", () => {
      const selection = migration.getSelection();

      migration.handleSelection(selection, "x");
      expect(selection.start).to.equal("x");
      migration.handleSelection(selection, "z");
      expect(selection.end).to.equal("z");
    });

    it("migrate", () => {
      migration.migrate(new TestCase({path: ["x", "y", "z"]}));
      expect(testCase.path).to.deep.equal(["x", "y", "z"]);
      expect(testCase.problems.indexOf(problem)).to.equal(-1);
    });
  });
});
