import { MODE_MIGRATE, MODE_SELECT, MODE_VIEW } from "../constants";
import { getMarkers, next, pathEquals, prev, selectStartEnd } from "../functions";

import BaseMode from "./BaseMode";
import TestCase from "../TestCase";

export default class SelectMode extends BaseMode {
  constructor(plugin, oldMode) {
    super(plugin, MODE_SELECT);

    if (oldMode?.migration === undefined) {
      this.paths = [];
      this.pathEquality = [];
      this.pathIndex = -1;
      this.selection = new TestCase();
    } else {
      const { migration } = oldMode;

      const paths = migration.getPaths();
      const selection = migration.getSelection();

      this.paths = paths
      this.pathEquality = this._determinePathEquality(paths);
      this.pathIndex = paths.length !== 0 ? 0 : -1;
      this.migration = migration;
      this.selection = selection;

      this.activeModes = new Set([ MODE_SELECT, MODE_MIGRATE ]);
    }

    this.updateMarkers();
  }

  get testCases() {
    return this.plugin.testCases;
  }

  addTestCase() {
    const { paths, pathIndex, selection, testCases } = this;

    const testCase = new TestCase({path: selection.path});
    this.plugin.enrichTestCase(testCase);

    // add test case
    testCases.push(testCase);
    // remove path
    paths.splice(pathIndex, 1);

    if (paths.length !== 0) {
      const newIndex = pathIndex === 0 ? 0 : (pathIndex - 1);

      selection.path = paths[newIndex];

      this.pathIndex = newIndex;
    } else {
      // reset selection
      this.pathIndex = -1;
      this.selection = new TestCase();
    }

    this.plugin.markAsChanged();
    this.updateMarkers();
  }

  handleClickElement(element) {
    this.handleSelection(element.id);
    this.plugin.updateView();
  }

  handleSelection(elementId) {
    const { migration, selection } = this;

    // reset
    selection.path = [];

    if (migration) {
      migration.handleSelection(selection, elementId);
    } else {
      selectStartEnd(selection, elementId);
    }

    this.updateMarkers();

    if (!selection.start || !selection.end) {
      this.paths = [];
      this.pathIndex = -1;
      return;
    }

    const { plugin } = this;

    // find possible paths between start and end node
    const paths = plugin.pathFinder.find(selection.start, selection.end);

    // determine paths that are equal to paths of existing test cases
    const pathEquality = this._determinePathEquality(paths);

    if (paths.length === 0) {
      plugin.markError();

      this.paths = [];
      this.pathIndex = -1;
    } else {
      selection.path = paths[0];

      plugin.enrichTestCase(selection);

      this.paths = paths;
      this.pathEquality = pathEquality;
      this.pathIndex = 0;

      this.updateMarkers();
    }
  }

  migrateTestCase() {
    const { migration, selection } = this;

    migration.migrate(selection);

    this.plugin.markAsChanged();
    this.toggle(MODE_VIEW);
  }

  nextPath() {
    const { paths, pathIndex, selection } = this;

    let newIndex = next(paths, pathIndex);

    selection.path = paths[newIndex];

    this.pathIndex = newIndex;
    this.updateMarkers();
  }

  prevPath() {
    const { paths, pathIndex, selection } = this;

    let newIndex = prev(paths, pathIndex);

    selection.path = paths[newIndex];

    this.pathIndex = newIndex;
    this.updateMarkers();
  }

  updateMarkers() {
    const { migration, selection } = this;

    let markers;
    if (migration) {
      markers = migration.getMarkers(selection);
    } else {
      markers = getMarkers(selection);
    }

    this.plugin.mark(markers);
  }

  _determinePathEquality(paths) {
    const { testCases } = this;

    const pathEquality = [];
    for (const path of paths) {
      pathEquality.push(testCases.find(testCase => pathEquals(testCase.path, path)) !== undefined);
    }
    return pathEquality;
  }
}
