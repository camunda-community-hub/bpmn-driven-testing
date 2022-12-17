import {
  MARKER,
  MARKER_END,
  MARKER_START
} from "./constants";

export function getMarkers(testCase) {
  const { end, path, start } = testCase;

  const markers = [];

  if (start) {
    markers.push({id: start, style: MARKER_START});
  }
  for (let i = 1; i < path.length - 1; i++) {
    markers.push({id: path[i], style: MARKER});
  }
  if (end) {
    markers.push({id: end, style: MARKER_END});
  }

  return markers;
}

export function next(array, currentIndex) {
  if (currentIndex === -1 && array.length === 0) {
    return -1;
  }

  let newIndex = currentIndex + 1;
  if (newIndex > array.length - 1) {
    newIndex = 0;
  }

  return newIndex;
}

export function pathEquals(a, b) {
  if (a.length !== b.length) {
    return false;
  }

  for (let i = a.length - 1; i >= 0; i--) {
    if (a[i] !== b[i]) {
      return false;
    }
  }

  return true;
}

export function prev(array, currentIndex) {
  let newIndex = currentIndex - 1;
  if (newIndex < 0) {
    newIndex = array.length - 1;
  }

  return newIndex;
}

export function selectStartEnd(selection, elementId) {
  if (selection.end && elementId === selection.start) {
    selection.end = null;
    selection.start = null;
  } else if (selection.end && elementId === selection.end) {
    selection.end = null;
  } else if (selection.end) {
    selection.end = elementId;
  } else if (selection.start && elementId === selection.start) {
    selection.start = null;
  } else if (selection.start && !selection.end) {
    selection.end = elementId;
  } else {
    selection.start = elementId;
  }
}
