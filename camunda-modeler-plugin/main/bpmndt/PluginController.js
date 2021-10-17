import {
  MODE_EDIT,
  MODE_MIGRATE,
  MODE_SELECT,
  MODE_SHOW_COVERAGE,
  MODE_VIEW,
  UNSUPPORTED_ELEMENT_TYPES
} from "./constants";

import { getMarkers } from "./functions";

import PathFinder from "./PathFinder";
import PathMarker from "./PathMarker";
import PathValidator from "./PathValidator";
import TestCase from "./TestCase";
import TestCaseMigration from "./TestCaseMigration";
import TestCaseModdle from "./TestCaseModdle";

import EditMode from "./ui/EditMode";
import MigrateMode from "./ui/MigrateMode";
import SelectMode from "./ui/SelectMode";
import ShowCoverageMode from "./ui/ShowCoverageMode";
import ViewMode from "./ui/ViewMode";

export default class PluginController {
  constructor(options) {
    this.bpmnModelChanged = false;
    this.elementRegistry = options.elementRegistry;
    this.enabled = false;
    this.hidePlugin = options.hidePlugin;
    this.pathFinder = new PathFinder(options);
    this.pathMarker = new PathMarker(options);
    this.pathValidator = new PathValidator(options);
    this.testCases = [];
    this.testCaseModdle = new TestCaseModdle(options);

    // modes
    this._editMode = new EditMode(this);
    this._migrateMode = new MigrateMode(this);
    this._selectMode = new SelectMode(this);
    this._showCoverageMode = new ShowCoverageMode(this);
    this._viewMode = new ViewMode(this);
  }

  disable() {
    this.enabled = false;
    this.pathMarker.unmark();
  }

  enable() {
    const { mode, testCases } = this;

    if (mode !== undefined) {
      this.pathMarker.mark(mode.state.markers);
    } else if (testCases.length !== 0) {
      this.setMode(MODE_VIEW, this);
    } else {
      this.setMode(MODE_SELECT, this);
    }

    this._validateTestCases();
    this.enabled = true;
  }

  findPaths(start, end) {
    return this.pathFinder.find(start, end);
  }

  handleBpmnElementChanged(oldProperties, properties) {
    if (!oldProperties.id) {
      return;
    }

    // when element ID was changed, update the path of each test case
    this.testCases.forEach(testCase => testCase.updateFlowNodeId(oldProperties.id, properties.id));
  }

  handleBpmnElementClicked(event) {
    const { element } = event;

    if (UNSUPPORTED_ELEMENT_TYPES.has(element.type)) {
      // skip click on unsupported element
      return;
    }

    const { mode } = this;

    if (mode.id === MODE_SELECT) {
      mode.handleSelection(element.id);
    }
  }

  handleBpmnModelChanged() {
    this.bpmnModelChanged = true;
  }

  handleLoadTestCases() {
    const { testCaseModdle } = this;

    if (!testCaseModdle.findProcess()) {
      // if process element could not be found
      return;
    }

    this.testCases = testCaseModdle.getTestCases();

    // allow initial validation
    this.bpmnModelChanged = true;

    this._validateTestCases();
  }

  handleSaveTestCases() {
    const { testCases, testCaseModdle } = this;
    testCaseModdle.setTestCases(testCases);
  }

  handleToggleMode(modeId) {
    const { mode, testCases } = this;

    if (mode.id === MODE_SELECT && modeId == MODE_MIGRATE) {
      // special case
      this.setMode(MODE_VIEW, this);
    } else if (mode.id !== modeId) {
      // enable mode
      this.setMode(modeId, this);
    } else if (modeId === MODE_SELECT && mode.isMigration()) {
      // special case
      this.setMode(MODE_MIGRATE, {testCase: mode.state.migration.testCase, testCases: testCases});
    } else {
      // disable current mode and enable view mode
      this.setMode(MODE_VIEW, this);
    }
  }

  mark(markers) {
    this.pathMarker.mark(markers);
  }

  markAsChanged() {
    this.testCaseModdle.markAsChanged();
  }

  markError() {
    this.pathMarker.markError();
  }

  setMode(modeId, ctx) {
    let mode;
    switch (modeId) {
      case MODE_EDIT:
        mode = this._editMode;
        break;
      case MODE_MIGRATE:
        mode = this._migrateMode;
        break;
      case MODE_SELECT:
        mode = this._selectMode
        break;
      case MODE_SHOW_COVERAGE:
        mode = this._showCoverageMode;
        break;
      case MODE_VIEW:
        mode = this._viewMode;
        break;
      default:
        throw new Error(`Unsupported mode '${modeId}'`);
    }

    this.mode = mode;

    // triggers update
    mode.setState(mode.computeInitialState(ctx));
  }

  update() {
    this.state = this._computeState();
    this.updateView();
  }

  _autoResolveProblem(testCase) {
    const problem = testCase.problems.find(problem => problem.autoResolvable);
    if (problem === undefined) {
      return;
    }

    const migration = new TestCaseMigration(testCase, problem);

    migration.migrate(new TestCase({path: problem.paths[0]}));

    const { enabled, mode, pathMarker } = this;

    this.markAsChanged();

    if (enabled && mode.id === MODE_VIEW && mode.testCase === testCase) {
      pathMarker.mark(getMarkers(testCase));
    }
  }

  _computeState() {
    const { mode } = this;

    const isMigration = mode.id === MODE_SELECT && mode.isMigration();

    const activeModes = {};
    activeModes[MODE_EDIT] = mode.id === MODE_EDIT;
    activeModes[MODE_SELECT] = mode.id === MODE_SELECT;
    activeModes[MODE_SHOW_COVERAGE] = mode.id === MODE_SHOW_COVERAGE;

    // special case
    activeModes[MODE_MIGRATE] = mode.id === MODE_MIGRATE || isMigration;

    // hide view to make diagram completely clickable
    let hideView = mode.id === MODE_SHOW_COVERAGE;
    hideView = hideView || (mode.id === MODE_SELECT && mode.state.paths.length === 0);
    hideView = hideView || (mode.id == MODE_VIEW && mode.state.testCases.length === 0);

    return {
      activeModes: activeModes,
      showView: !hideView,
      viewModel: mode.computeViewModel()
    };
  }

  _validateTestCases() {
    const { bpmnModelChanged, pathValidator, testCases } = this;

    if (!bpmnModelChanged || testCases.length === 0) {
      // nothing to validate
      return;
    }

    this.bpmnModelChanged = false;

    setTimeout(() => {
      testCases.forEach((testCase) => {
        testCase.problems = pathValidator.validate(testCase);

        this._autoResolveProblem(testCase);
      });

      if (this.enabled) {
        this.update();
      }
    }, 1000);
  }
}
