import { MODE_SELECT, MODE_VIEW } from "../constants";
import { getMarkers, pathEquals, selectStartEnd } from "../functions";

import BaseMode from "./BaseMode";
import TestCase from "../TestCase";

export default class SelectMode extends BaseMode {
  constructor(controller) {
    super(controller);

    this.id = MODE_SELECT;

    this.next = {onClick: this._handleClickNext, title: "Next path"};
    this.prev = {onClick: this._handleClickPrev, title: "Previous path"};

    this.actionAdd = {
      icon: "fas fa-plus",
      onClick: this._handleClickAdd,
      style: "success",
      title: "Add test case"
    };

    this.actionMigrate = {
      icon: "fas fa-check",
      onClick: this._handleClickMigrate,
      style: "primary",
      title: "Migrate test case"
    };

    this.actionPathAlreadyExists = {
      icon: "fas fa-exclamation-triangle",
      style: "danger",
      title: "Path already added"
    };
  }

  computeInitialState(ctx) {
    const { migration, testCases } = ctx;

    if (migration === undefined) {
      return {
        markers: [],
        migration: undefined,
        paths: [],
        pathEquality: [],
        pathIndex: -1,
        selection: new TestCase(),
        testCases: testCases
      };
    }

    // handle migration
    const paths = migration.getPaths();
    const selection = migration.getSelection();

    return {
      markers: migration.getMarkers(selection),
      migration: migration,
      paths: paths,
      pathEquality: this._determinePathEquality(testCases, paths),
      pathIndex: paths.length !== 0 ? 0 : -1,
      selection: selection,
      testCases: testCases
    };
  }

  computeViewModel() {
    const { paths, pathEquality, pathIndex, selection } = this.state;

    if (paths.length === 0) {
      return;
    }

    let actionCenter;
    if (pathEquality[pathIndex]) {
      actionCenter = this.actionPathAlreadyExists;
    } else if (this.isMigration()) {
      actionCenter = this.actionMigrate;
    } else {
      actionCenter = this.actionAdd;
    }

    return {
      next: paths.length > 1 ? this.next : undefined,
      prev: paths.length > 1 ? this.prev : undefined,
      content: {
        centerTop: `Path ${pathIndex + 1} / ${paths.length}`,
        centerBottom: `Length: ${selection.path.length} flow nodes`,
        leftTop: selection.start,
        leftBottom: selection.startType,
        onClick: actionCenter.onClick,
        rightTop: selection.end,
        rightBottom: selection.endType,
        title: actionCenter.title
      },
      actionCenter: pathEquality[pathIndex] || paths.length !== 0 ? actionCenter : undefined
    }
  }

  handleSelection(elementId) {
    const { migration, selection, testCases } = this.state;

    // reset
    selection.path = [];

    if (migration) {
      migration.handleSelection(selection, elementId);
    } else {
      selectStartEnd(selection, elementId);
    }

    const { controller } = this;

    controller.mark(this._getMarkers());

    if (!selection.start || !selection.end) {
      this.setState({paths: [], pathIndex: -1});
      return;
    }

    selection.enrich(controller.elementRegistry);

    // find possible paths between start and end node
    const paths = controller.findPaths(selection.start, selection.end);

    // determine paths that are equal to paths of existing test cases
    const pathEquality = this._determinePathEquality(testCases, paths);

    if (paths.length === 0) {
      controller.markError();
      this.setState({paths: [], pathIndex: -1});
    } else {
      selection.path = paths[0];
      this.setState({markers: this._getMarkers(), paths: paths, pathEquality: pathEquality, pathIndex: 0});
    }
  }

  isMigration() {
    return this.state.migration !== undefined;
  }

  _determinePathEquality(testCases, paths) {
    const pathEquality = [];
    for (const path of paths) {
      pathEquality.push(testCases.find(testCase => pathEquals(testCase.path, path)) !== undefined);
    }
    return pathEquality;
  }

  _getMarkers() {
    const { migration, selection } = this.state;

    if (migration) {
      return migration.getMarkers(selection);
    } else {
      return getMarkers(selection);
    }
  }

  _handleClickNext = () => {
    this._setPathIndex(this.state.pathIndex + 1);
  }
  _handleClickPrev = () => {
    this._setPathIndex(this.state.pathIndex - 1);
  }

  _handleClickAdd = () => {
    const { paths, pathIndex, selection, testCases } = this.state;

    const testCase = new TestCase({path: selection.path});

    // add test case
    testCases.push(testCase);

    // remove path
    paths.splice(pathIndex, 1);

    if (paths.length !== 0) {
      const newIndex = pathIndex === 0 ? 0 : (pathIndex - 1);

      selection.path = paths[newIndex];

      this.setState({
        markers: this._getMarkers(),
        markAsChanged: true,
        pathIndex: newIndex
      });
    } else {
      // reset selection
      this.setState({
        markers: [],
        markAsChanged: true,
        paths: [],
        pathIndex: -1,
        selection: new TestCase()
      });
    }
  }

  _handleClickMigrate = () => {
    const { migration, selection } = this.state;

    migration.migrate(selection);

    this.controller.markAsChanged();
    this.controller.handleToggleMode(MODE_VIEW);
  }

  _setPathIndex(pathIndex) {
    const { paths, selection } = this.state;

    let newIndex = pathIndex;
    if (pathIndex > paths.length - 1) {
      newIndex = 0;
    }
    if (pathIndex < 0) {
      newIndex = paths.length - 1;
    }

    selection.path = paths[newIndex];

    this.setState({markers: this._getMarkers(), pathIndex: newIndex});
  }
}
