import chai from "chai";
import spies from "chai-spies";

import { PluginController } from "../helper";

import { MODE_VIEW, PROBLEM_PATH } from "../../main/bpmndt/constants";
import TestCase from "../../main/bpmndt/TestCase";
import TestCaseMigration from "../../main/bpmndt/TestCaseMigration";

import SelectMode from "../../main/bpmndt/ui/SelectMode";

chai.use(spies);
const expect = chai.expect;

describe("ui/SelectMode", () => {
  let mode;

  let controller;
  let testCases;
  let viewModel;

  beforeEach(() => {
    controller = new PluginController();

    chai.spy.on(controller, ["handleToggleMode", "mark", "markAsChanged", "setMode", "update"]);

    mode = new SelectMode(controller);

    testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["a", "e", "f", "d"]}));
  });

  afterEach(() => {
    chai.spy.restore(controller);
  });

  describe("selection", () => {
    it("should compute initial state and view model, when no paths exist", () => {
      expect(mode.id).to.not.be.undefined;
  
      const state = mode.computeInitialState({ testCases });
      expect(state.markers).to.have.lengthOf(0);
      expect(state.migration).to.be.undefined;
      expect(state.paths).to.have.lengthOf(0);
      expect(state.pathEquality).to.have.lengthOf(0);
      expect(state.pathIndex).to.equal(-1);
      expect(state.selection).to.deep.equal(new TestCase());
  
      mode.setState(state);
      expect(controller.mark).to.have.been.called.once;
      expect(controller.update).to.have.been.called.once;
  
      viewModel = mode.computeViewModel();
      expect(viewModel).to.be.undefined;
    });
  
    it("should compute initial state and view model", () => {
      const state = mode.computeInitialState({ testCases });
      expect(state.markers).to.have.lengthOf(0);
      expect(state.migration).to.be.undefined;
      expect(state.paths).to.have.lengthOf(0);
      expect(state.pathEquality).to.have.lengthOf(0);
      expect(state.pathIndex).to.equal(-1);
      expect(state.selection).to.deep.equal(new TestCase());
  
      mode.setState(state);
      expect(controller.mark).to.have.been.called.once;
      expect(controller.update).to.have.been.called.once;
  
      // simulate selection and path finding
      mode.state.paths = [["a", "x1", "d"], ["a", "y1", "y2", "d"], ["a", "z1", "z2", "z3", "d"]];
      mode.state.pathEquality = [false, false, false];
      mode.state.pathIndex = 2;
      mode.state.selection.start = "a";
      mode.state.selection.startType = "bpmn:StartEvent";
      mode.state.selection.end = "d";
      mode.state.selection.endType = "bpmn:EndEvent";
      mode.state.selection.path = mode.state.paths[2];
  
      viewModel = mode.computeViewModel();
      expect(viewModel).to.have.property("next");
      expect(viewModel).to.have.property("prev");
      expect(viewModel).to.have.property("content");
      expect(viewModel).to.have.property("actionCenter");
  
      expect(viewModel.next.onClick).to.equal(mode._handleClickNext);
      expect(viewModel.prev.onClick).to.equal(mode._handleClickPrev);
      expect(viewModel.content.centerTop).to.equal("Path 3 / 3");
      expect(viewModel.content.centerBottom).to.equal("Length: 5 flow nodes");
      expect(viewModel.content.leftTop).to.equal("a");
      expect(viewModel.content.leftBottom).to.equal("bpmn:StartEvent");
      expect(viewModel.content.onClick).to.equal(mode.actionAdd.onClick);
      expect(viewModel.content.rightTop).to.equal("d");
      expect(viewModel.content.rightBottom).to.equal("bpmn:EndEvent");
      expect(viewModel.content.title).to.equal(mode.actionAdd.title);
      expect(viewModel.actionCenter).to.equal(mode.actionAdd);
    });
  
    it("should handle click next or previous", () => {
      mode.state = mode.computeInitialState({ testCases });
  
      // simulate selection and path finding
      mode.state.paths = [["a", "x1", "d"], ["a", "y1", "y2", "d"], ["a", "z1", "z2", "z3", "d"]];
      mode.state.pathEquality = [false, false, false];
      mode.state.pathIndex = 0;
      mode.state.selection.start = "a";
      mode.state.selection.startType = "bpmn:StartEvent";
      mode.state.selection.end = "d";
      mode.state.selection.endType = "bpmn:EndEvent";
      mode.state.selection.path = mode.state.paths[0];
  
      mode._handleClickNext();
      expect(mode.state.markers).to.have.lengthOf(4);
      expect(mode.state.pathIndex).to.equal(1);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[1]);
  
      mode._handleClickNext();
      expect(mode.state.markers).to.have.lengthOf(5);
      expect(mode.state.pathIndex).to.equal(2);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[2]);
  
      mode._handleClickNext();
      expect(mode.state.markers).to.have.lengthOf(3);
      expect(mode.state.pathIndex).to.equal(0);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[0]);
  
      mode._handleClickPrev();
      expect(mode.state.markers).to.have.lengthOf(5);
      expect(mode.state.pathIndex).to.equal(2);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[2]);
  
      mode._handleClickPrev();
      expect(mode.state.markers).to.have.lengthOf(4);
      expect(mode.state.pathIndex).to.equal(1);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[1]);
  
      mode._handleClickPrev();
      expect(mode.state.markers).to.have.lengthOf(3);
      expect(mode.state.pathIndex).to.equal(0);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[0]);
  
      expect(controller.update).to.have.been.called.exactly(6);
    });
  
    it("should handle click add", () => {
      mode.state = mode.computeInitialState({ testCases });
  
      // simulate selection and path finding
      mode.state.paths = [["a", "x1", "d"], ["a", "y1", "y2", "d"], ["a", "z1", "z2", "z3", "d"]];
      mode.state.pathEquality = [false, false, false];
      mode.state.pathIndex = 0;
      mode.state.selection.start = "a";
      mode.state.selection.startType = "bpmn:StartEvent";
      mode.state.selection.end = "d";
      mode.state.selection.endType = "bpmn:EndEvent";
      mode.state.selection.path = mode.state.paths[0];
  
      mode._handleClickAdd();
      expect(mode.state.markers).to.have.lengthOf(4);
      expect(mode.state.paths).to.have.lengthOf(2);
      expect(mode.state.pathIndex).to.equal(0);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[0]);
      expect(mode.state.testCases).to.have.lengthOf(3);
      expect(mode.state.testCases[2]).to.deep.equal(new TestCase({path: ["a", "x1", "d"]}));
  
      // next
      mode._handleClickNext();
  
      mode._handleClickAdd();
      expect(mode.state.markers).to.have.lengthOf(4);
      expect(mode.state.paths).to.have.lengthOf(1);
      expect(mode.state.pathIndex).to.equal(0);
      expect(mode.state.selection.path).to.deep.equal(mode.state.paths[0]);
      expect(mode.state.testCases).to.have.lengthOf(4);
      expect(mode.state.testCases[3]).to.deep.equal(new TestCase({path: ["a", "z1", "z2", "z3", "d"]}));
  
      mode._handleClickAdd();
      expect(mode.state.markers).to.have.lengthOf(0);
      expect(mode.state.paths).to.have.lengthOf(0);
      expect(mode.state.pathIndex).to.equal(-1);
      expect(mode.state.selection).to.deep.equal(new TestCase());
      expect(mode.state.testCases).to.have.lengthOf(5);
      expect(mode.state.testCases[4]).to.deep.equal(new TestCase({path: ["a", "y1", "y2", "d"]}));
  
      expect(controller.update).to.have.been.called.exactly(3 + 1);
    });
  });

  describe("selection during migration", () => {
    it("should compute initial state and view model", () => {
      testCases[0].startType = "bpmn:StartEvent";
      testCases[0].endType = "bpmn:EndEvent";
      testCases[0].problems = [
        {type: PROBLEM_PATH, start: "a", end: "d", paths: [["a", "x", "d"], ["a", "y", "d"], ["a", "z", "d"]]}
      ];

      const migration = new TestCaseMigration(testCases[0], testCases[0].problems[0]);

      const state = mode.computeInitialState({ migration, testCases });
      expect(state.markers).to.have.lengthOf(5);
      expect(state.migration).to.deep.equal(migration);
      expect(state.paths).to.have.lengthOf(3);
      expect(state.pathEquality).to.deep.equal([false, false, false]);
      expect(state.pathIndex).to.equal(0);
      expect(state.selection.path).to.deep.equal(testCases[0].problems[0].paths[0]);
  
      mode.setState(state);
      expect(controller.mark).to.have.been.called.once;
      expect(controller.update).to.have.been.called.once;
  
      viewModel = mode.computeViewModel();
      expect(viewModel).to.have.property("next");
      expect(viewModel).to.have.property("prev");
      expect(viewModel).to.have.property("content");
      expect(viewModel).to.have.property("actionCenter");
  
      expect(viewModel.next.onClick).to.equal(mode._handleClickNext);
      expect(viewModel.prev.onClick).to.equal(mode._handleClickPrev);
      expect(viewModel.content.centerTop).to.equal("Path 1 / 3");
      expect(viewModel.content.centerBottom).to.equal("Length: 3 flow nodes");
      expect(viewModel.content.leftTop).to.equal("a");
      expect(viewModel.content.leftBottom).to.equal("bpmn:StartEvent");
      expect(viewModel.content.onClick).to.equal(mode.actionMigrate.onClick);
      expect(viewModel.content.rightTop).to.equal("d");
      expect(viewModel.content.rightBottom).to.equal("bpmn:EndEvent");
      expect(viewModel.content.title).to.equal(mode.actionMigrate.title);
      expect(viewModel.actionCenter).to.equal(mode.actionMigrate);
    });

    it("should handle click migrate", () => {
      testCases[0].problems = [
        {type: PROBLEM_PATH, start: "a", end: "d", paths: [["a", "x", "d"], ["a", "y", "d"], ["a", "z", "d"]]}
      ];
  
      const migration = new TestCaseMigration(testCases[0], testCases[0].problems[0]);
  
      const state = mode.computeInitialState({ migration, testCases });

      mode.setState(state);

      mode._handleClickMigrate();
      expect(controller.markAsChanged).to.have.been.called.once;
      expect(controller.handleToggleMode).to.have.been.called.once;
      expect(controller.handleToggleMode).to.have.been.called.with(MODE_VIEW);

      expect(testCases[0].path).to.deep.equal(["a", "x", "d"]);
      expect(testCases[0].problems).to.have.lengthOf(0);
    });
  });
});
