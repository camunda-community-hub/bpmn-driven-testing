// BPMN model constants
export const BPMN_BOUNDARY_EVENT = "bpmn:BoundaryEvent";
export const BPMN_COLLABORATION = "bpmn:Collaboration";
export const BPMN_END_EVENT = "bpmn:EndEvent";
export const BPMN_EXTENSION_ELEMENTS = "bpmn:ExtensionElements";
export const BPMN_PARTICIPANT = "bpmn:Participant";
export const BPMN_PROCESS = "bpmn:Process";
export const BPMN_SEQUENCE_FLOW = "bpmn:SequenceFlow";
export const BPMN_START_EVENT = "bpmn:StartEvent";
export const BPMN_SUB_PROCESS = "bpmn:SubProcess";

export const BPMNDT_PATH = "bpmndt:Path";
export const BPMNDT_TEST_CASE = "bpmndt:TestCase";
export const BPMNDT_TEST_CASES = "bpmndt:TestCases";

// marker styles
export const MARKER = "bpmn-driven-testing-path";
export const MARKER_END = `${MARKER}-end`;
export const MARKER_ERROR = `${MARKER}-error`;
export const MARKER_START = `${MARKER}-start`;

// modes
export const MODE_COVERAGE = "coverage";
export const MODE_EDITOR = "editor";
export const MODE_SELECTOR = "selector";

// class name of the plugin view's parent DOM node
export const PLUGIN_VIEW_PARENT_CLASS_NAME = "bjs-container";

// style to apply when the plugin view is shown
export const PLUGIN_VIEW_STYLE = `
  .djs-element {cursor: pointer}
  .djs-minimap { display: none; }
  .djs-overlay-context-pad { display: none; }
  .djs-palette { display: none; }
  .properties { display: none; }
  .secondary.links { display: none; }
`;

// element types, that are not supported for during path selection
export const UNSUPPORTED_ELEMENT_TYPES = new Set([
  BPMN_COLLABORATION,
  BPMN_PARTICIPANT,
  BPMN_PROCESS,
  BPMN_SEQUENCE_FLOW,
  BPMN_SUB_PROCESS
]);
