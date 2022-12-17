/**
 * Empty helper implementation, used for testing only.
 */
export default class Plugin {

  enrichTestCase(testCase) {
    this.testCase = testCase;
  }

  mark(markers) {
    this.markers = markers;
  }

  markAsChanged() {
  }

  setMode() {
  }
}
