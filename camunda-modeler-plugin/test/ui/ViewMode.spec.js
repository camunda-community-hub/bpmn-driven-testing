import chai from "chai";
import spies from "chai-spies";

import { ElementRegistry, PluginController } from "../helper";

import { MODE_EDIT, MODE_MIGRATE } from "../../main/bpmndt/constants";
import TestCase from "../../main/bpmndt/TestCase";

import ViewMode from "../../main/bpmndt/ui/ViewMode";

chai.use(spies);
const expect = chai.expect;

describe("ui/ViewMode", () => {
  let mode;

  let controller;
  let testCases;
  let viewModel;

  beforeEach(() => {
    controller = new PluginController();
    controller.elementRegistry = new ElementRegistry({
      elementsById: {
        a: {$type: "bpmn:StartEvent"},
        d: {$type: "bpmn:EndEvent"},
        e: {$type: "bpmn:ServiceTask"},
        g: {$type: "bpmn:UserTask"},
      }
    });

    chai.spy.on(controller, ["mark", "setMode", "update"]);

    mode = new ViewMode(controller);

    testCases = [];
    testCases.push(new TestCase({path: ["a", "b", "c", "d"]}));
    testCases.push(new TestCase({path: ["e", "f", "g"]}));
    testCases.push(new TestCase({path: ["h", "i"]}));
  });

  afterEach(() => {
    chai.spy.restore(controller);
  });

  it("should compute initial state and view model, when there are no test cases", () => {
    expect(mode.id).to.not.be.undefined;

    const state = mode.computeInitialState({ testCases: [] });
    expect(state.markers).to.have.lengthOf(0);
    expect(state.testCases).to.have.lengthOf(0);
    expect(state.testCaseIndex).to.equal(-1);

    mode.setState(state);
    expect(controller.mark).to.have.been.called.once;
    expect(controller.mark).to.have.been.called.with([]);
    expect(controller.update).to.have.been.called.once;

    viewModel = mode.computeViewModel();
    expect(viewModel).to.be.undefined;
  });

  it("should compute initial state and view model, when there is one test case", () => {
    const state = mode.computeInitialState({testCases: [ testCases[1] ]});
    expect(state.markers).to.have.lengthOf(3);
    expect(state.testCases).to.have.lengthOf(1);
    expect(state.testCaseIndex).to.equal(0);

    mode.setState(state);
    expect(controller.mark).to.have.been.called.once;

    viewModel = mode.computeViewModel();
    expect(viewModel).to.have.property("next");
    expect(viewModel).to.have.property("prev");
    expect(viewModel).to.have.property("content");
    expect(viewModel).to.have.property("actionLeft");
    expect(viewModel).to.have.property("actionCenter");
    expect(viewModel).to.have.property("actionRight");

    expect(viewModel.next).to.be.undefined;
    expect(viewModel.prev).to.be.undefined
    expect(viewModel.content.centerTop).to.equal("Test case 1 / 1");
    expect(viewModel.content.centerBottom).to.equal("Length: 3 flow nodes");
    expect(viewModel.content.leftTop).to.equal("e");
    expect(viewModel.content.leftBottom).to.equal("bpmn:ServiceTask");
    expect(viewModel.content.rightTop).to.equal("g");
    expect(viewModel.content.rightBottom).to.equal("bpmn:UserTask");
    expect(viewModel.actionLeft).to.be.undefined;
    expect(viewModel.actionCenter).to.equal(mode.actionCenter);
    expect(viewModel.actionRight).to.equal(mode.actionRight);
  });

  it("should compute initial state and view model", () => {
    const state = mode.computeInitialState({ testCases });
    expect(state.markers).to.have.lengthOf(4);
    expect(state.testCases).to.have.lengthOf(3);
    expect(state.testCaseIndex).to.equal(0);

    mode.setState(state);
    expect(controller.mark).to.have.been.called.once;

    viewModel = mode.computeViewModel();
    expect(viewModel).to.have.property("next");
    expect(viewModel).to.have.property("prev");
    expect(viewModel).to.have.property("content");
    expect(viewModel).to.have.property("actionLeft");
    expect(viewModel).to.have.property("actionCenter");
    expect(viewModel).to.have.property("actionRight");

    expect(viewModel.next.onClick).to.equal(mode._handleClickNext);
    expect(viewModel.prev.onClick).to.equal(mode._handleClickPrev);
    expect(viewModel.content.centerTop).to.equal("Test case 1 / 3");
    expect(viewModel.content.centerBottom).to.equal("Length: 4 flow nodes");
    expect(viewModel.content.leftTop).to.equal("a");
    expect(viewModel.content.leftBottom).to.equal("bpmn:StartEvent");
    expect(viewModel.content.rightTop).to.equal("d");
    expect(viewModel.content.rightBottom).to.equal("bpmn:EndEvent");
    expect(viewModel.actionLeft).to.be.undefined;
    expect(viewModel.actionCenter).to.equal(mode.actionCenter);
    expect(viewModel.actionRight).to.equal(mode.actionRight);
  });

  it("should remember test case index from prior usage", () => {
    mode.state.testCaseIndex = 1;

    const state = mode.computeInitialState({ testCases });
    expect(state.testCaseIndex).to.equal(1);
  });

  it("should handle click next or previous", () => {
    mode.state = mode.computeInitialState({ testCases });

    mode._handleClickNext();
    expect(mode.state.markers).to.have.lengthOf(3);
    expect(mode.state.testCaseIndex).to.equal(1);

    mode._handleClickNext();
    expect(mode.state.markers).to.have.lengthOf(2);
    expect(mode.state.testCaseIndex).to.equal(2);

    mode._handleClickNext();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.testCaseIndex).to.equal(0);

    mode._handleClickPrev();
    expect(mode.state.markers).to.have.lengthOf(2);
    expect(mode.state.testCaseIndex).to.equal(2);

    mode._handleClickPrev();
    expect(mode.state.markers).to.have.lengthOf(3);
    expect(mode.state.testCaseIndex).to.equal(1);

    mode._handleClickPrev();
    expect(mode.state.markers).to.have.lengthOf(4);
    expect(mode.state.testCaseIndex).to.equal(0);

    expect(controller.update).to.have.been.called.exactly(6);
  });

  it("should handle click edit", () => {
    mode.state = mode.computeInitialState({ testCases });

    mode._handleClickEdit();
    expect(controller.setMode).to.have.been.called.once;
    expect(controller.setMode).to.have.been.called.with(MODE_EDIT, {testCase: testCases[0]});
  });

  it("should handle click migrate", () => {
    mode.state = mode.computeInitialState({ testCases });

    mode._handleClickMigrate();
    expect(controller.setMode).to.have.been.called.once;
    expect(controller.setMode).to.have.been.called.with(MODE_MIGRATE, {testCase: testCases[0], testCases: testCases});
  });

  it("should handle click remove", () => {
    mode.state = mode.computeInitialState({ testCases });

    mode._handleClickRemove();
    expect(mode.state.markers).to.have.lengthOf(3);
    expect(mode.state.testCases).to.have.lengthOf(2);
    expect(mode.state.testCaseIndex).to.equal(0);

    mode.state.testCaseIndex = 1;

    mode._handleClickRemove();
    expect(mode.state.markers).to.have.lengthOf(3);
    expect(mode.state.testCases).to.have.lengthOf(1);
    expect(mode.state.testCaseIndex).to.equal(0);
  });
});
