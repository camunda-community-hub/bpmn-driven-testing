// BPMN element types
export const BPMN_BOUNDARY_EVENT = "bpmn:BoundaryEvent";
export const BPMN_COLLABORATION = "bpmn:Collaboration";
export const BPMN_END_EVENT = "bpmn:EndEvent";
export const BPMN_ERROR_EVENT_DEFINITION = "bpmn:ErrorEventDefinition";
export const BPMN_ESCALATION_EVENT_DEFINITION = "bpmn:EscalationEventDefinition";
export const BPMN_EXTENSION_ELEMENTS = "bpmn:ExtensionElements";
export const BPMN_PARTICIPANT = "bpmn:Participant";
export const BPMN_PROCESS = "bpmn:Process";
export const BPMN_SEQUENCE_FLOW = "bpmn:SequenceFlow";
export const BPMN_START_EVENT = "bpmn:StartEvent";
export const BPMN_SUB_PROCESS = "bpmn:SubProcess";

// BPMNDT extension element types
export const BPMNDT_PATH = "bpmndt:Path";
export const BPMNDT_TEST_CASE = "bpmndt:TestCase";
export const BPMNDT_TEST_CASES = "bpmndt:TestCases";

// marker styles
export const MARKER = "bpmndt-marker";
export const MARKER_BRIGHT = `${MARKER}-bright`;
export const MARKER_END = `${MARKER}-end`;
export const MARKER_ERROR = `${MARKER}-error`;
export const MARKER_OLD = `${MARKER}-old`;
export const MARKER_OLD_BRIGHT = `${MARKER}-old-bright`;
export const MARKER_START = `${MARKER}-start`;

// modes
export const MODE_EDIT = 1;
export const MODE_MIGRATE = 2;
export const MODE_SELECT = 3;
export const MODE_SHOW_COVERAGE = 4;
export const MODE_VIEW = 5;

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

// problem types
export const PROBLEM_START = 1;
export const PROBLEM_END = 2;
export const PROBLEM_PATH = 3;
export const PROBLEM_UNRESOLVABLE = 4;

// element types, that are not supported for during path selection
export const UNSUPPORTED_ELEMENT_TYPES = new Set([
  BPMN_COLLABORATION,
  BPMN_PARTICIPANT,
  BPMN_PROCESS,
  BPMN_SEQUENCE_FLOW,
  BPMN_SUB_PROCESS
]);
