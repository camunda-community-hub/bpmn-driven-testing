import chai from "chai";
import spies from "chai-spies";

import { ElementRegistry, PluginController } from "../helper";

import {
  MODE_SELECT,
  PROBLEM_END,
  PROBLEM_PATH,
  PROBLEM_START,
  PROBLEM_UNRESOLVABLE
} from "../../main/bpmndt/constants";

import TestCase from "../../main/bpmndt/TestCase";

import MigrateMode from "../../main/bpmndt/ui/MigrateMode";

chai.use(spies);
const expect = chai.expect;

describe("ui/MigrateMode", () => {
  let mode;

  let controller;
  let testCases;
  let viewModel;

  beforeEach(() => {
    controller = new PluginController();
    controller.elementRegistry = new ElementRegistry({
      elementsById: {
        a: {$type: "bpmn:StartEvent"},
        b: {$type: "bpmn:ServiceTask"},
        d: {$type: "bpmn:EndEvent"},
        e: {$type: "bpmn:ServiceTask"},
        g: {$type: "bpmn:UserTask"},
      }
    });

    chai.spy.on(controller, ["mark", "setMode", "update"]);

    mode = new MigrateMode(controller);

    testCases = [];
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
  });

  afterEach(() => {
    chai.spy.restore(controller);
  });

  it("should compute initial state and view model", () => {
    expect(mode.id).to.not.be.undefined;

    const state = mode.computeInitialState({testCase: testCases[0], testCases: testCases});
    expect(state.markers).to.have.lengthOf(4);
    expect(state.problems).to.have.lengthOf(2);
    expect(state.problemIndex).to.equal(0);
    expect(state.testCase).to.deep.equal(testCases[0]);
    expect(state.testCases).to.have.lengthOf(3);

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
    expect(viewModel.content.centerTop).to.equal("Start node 'a' is missing");
    expect(viewModel.content.centerBottom).to.equal("Please select a new start node");
    expect(viewModel.content.leftTop).to.equal("?");
    expect(viewModel.content.leftBottom).to.equal("");
    expect(viewModel.content.rightTop).to.equal("b");
    expect(viewModel.content.rightBottom).to.equal("bpmn:ServiceTask");
    expect(viewModel.actionCenter.onClick).to.equal(mode._handleClickResolve);
    expect(viewModel.actionCenter.title).to.equal("Select start node");
  });

  it("should compute initial state and view model, when problem has type PROBLEM_PATH", () => {
    const state = mode.computeInitialState({testCase: testCases[1], testCases: testCases});
    expect(state.markers).to.have.lengthOf(3);
    expect(state.problems).to.have.lengthOf(1);
    expect(state.problemIndex).to.equal(0);
    expect(state.testCase).to.deep.equal(testCases[1]);
    expect(state.testCases).to.have.lengthOf(3);

    mode.setState(state);

    viewModel = mode.computeViewModel();
    expect(viewModel).to.have.property("next");
    expect(viewModel).to.have.property("prev");
    expect(viewModel).to.have.property("content");
    expect(viewModel).to.have.property("actionCenter");

    expect(viewModel.next).to.be.undefined;
    expect(viewModel.prev).to.be.undefined;
    expect(viewModel.content.centerTop).to.equal("Path in between is invalid");
    expect(viewModel.content.centerBottom).to.equal("Please choose one of the possible paths");
    expect(viewModel.content.leftTop).to.equal("e");
    expect(viewModel.content.leftBottom).to.equal("bpmn:ServiceTask");
    expect(viewModel.content.rightTop).to.equal("g");
    expect(viewModel.content.rightBottom).to.equal("bpmn:UserTask");
    expect(viewModel.actionCenter.onClick).to.equal(mode._handleClickResolve);
    expect(viewModel.actionCenter.title).to.equal("Choose path");
  });

  it("should compute initial state and view model, when problem has type PROBLEM_UNRESOLVABLE", () => {
    const state = mode.computeInitialState({testCase: testCases[2], testCases: testCases});
    expect(state.markers).to.have.lengthOf(2);
    expect(state.problems).to.have.lengthOf(1);
    expect(state.problemIndex).to.equal(0);
    expect(state.testCase).to.deep.equal(testCases[2]);
    expect(state.testCases).to.have.lengthOf(3);

    mode.setState(state);

    viewModel = mode.computeViewModel();
    expect(viewModel).to.have.property("next");
    expect(viewModel).to.have.property("prev");
    expect(viewModel).to.have.property("content");
    expect(viewModel).to.have.property("actionCenter");

    expect(viewModel.next).to.be.undefined;
    expect(viewModel.prev).to.be.undefined;
    expect(viewModel.content.centerTop).to.equal("Path problem is unresolvable");
    expect(viewModel.content.centerBottom).to.equal("Please select a new path");
    expect(viewModel.content.leftTop).to.equal("?");
    expect(viewModel.content.leftBottom).to.equal("");
    expect(viewModel.content.rightTop).to.equal("?");
    expect(viewModel.content.rightBottom).to.equal("");
    expect(viewModel.actionCenter.onClick).to.equal(mode._handleClickResolve);
    expect(viewModel.actionCenter.title).to.equal("Select path");
  });

  it("should handle click next or previous", () => {
    mode.state = mode.computeInitialState({testCase: testCases[0], testCases: testCases});

    mode._handleClickNext();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.problemIndex).to.equal(1);

    mode._handleClickNext();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.problemIndex).to.equal(0);

    mode._handleClickPrev();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.problemIndex).to.equal(1);

    mode._handleClickPrev();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.problemIndex).to.equal(0);

    expect(controller.update).to.have.been.called.exactly(4);
  });

  it("should handle click resolve", () => {
    mode.state = mode.computeInitialState({testCase: testCases[0], testCases: testCases});

    mode.state.problemIndex = 1;

    mode._handleClickResolve();
    expect(controller.setMode).to.have.been.called.once;

    expect(controller.modeId).to.equal(MODE_SELECT);
    expect(controller.ctx).to.have.property("migration");
    expect(controller.ctx).to.have.property("testCases");

    expect(controller.ctx.migration.testCase).to.deep.equal(testCases[0]);
    expect(controller.ctx.migration.problem).to.deep.equal(testCases[0].problems[1]);
    expect(controller.ctx.testCases).to.deep.equal(testCases);
  });
});
