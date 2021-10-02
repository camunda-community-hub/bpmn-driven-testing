import {
  BPMN_BOUNDARY_EVENT,
  BPMN_END_EVENT,
  BPMN_ERROR_EVENT_DEFINITION,
  BPMN_ESCALATION_EVENT_DEFINITION,
  BPMN_START_EVENT,
  BPMN_SUB_PROCESS
} from "./Constants";

/**
 * Class to find possible paths through a BPMN process.
 */
export default class PathFinder {
  constructor(elementRegistry) {
    this._elementRegistry = elementRegistry;
  }

  /**
   * Finds all paths between a given start and end element.
   */
  findPaths(start, end) {
    const paths = [];

    const stack = [];
    stack.push({ id: start, path: [] });

    while (stack.length !== 0) {
      const current = stack.pop();
      if (this._isLoop(current)) {
        // stop branch, if loop is detected
        continue;
      }

      current.path.push(current.id);
      if (current.id === end) {
        // stop branch, if end is reached
        paths.push(current.path);
        continue;
      }

      const element = this._elementRegistry.get(current.id);
      if (element === undefined) {
        // stop branch, if element does not exist anymore
        continue;
      }

      const next = this._getOutgoing(element).concat(this._getBoundary(element.id));
      for (const id of next) {
        stack.push({ id: id, path: current.path.slice() });
      }
    }

    return paths.reverse();
  }

  _filter(elementType) {
    return this._elementRegistry.filter(element => element.type === elementType);
  }

  /**
   * Finds all elements with a sequence flow that is coming from the given embedded sub process.
   * 
   * @param {String} subProcessId ID of an embedded sub process.
   * 
   * @returns The found elements.
   */
  _findIncoming(subProcessId) {
    return this._elementRegistry.find(element => {
      const { incoming } = element.businessObject;

      if (incoming) {
        return incoming.find(sequenceFlow => sequenceFlow.sourceRef.id === subProcessId);
      } else {
        return false;
      }
    });
  }

  _findStartEvent(subProcessId) {
    return this._filter(BPMN_START_EVENT).find(element => {
      return element.businessObject.$parent.id === subProcessId;
    });
  }

  _getBoundary(elementId) {
    const next = [];
    
    this._filter(BPMN_BOUNDARY_EVENT).forEach(element => {
      if (element.businessObject.attachedToRef.id === elementId) {
        next.push(element.id);
      }
    });

    return next;
  }

  _getErrorBoundary(elementId, expectedErrorCode) {
    const next = [];

    this._filter(BPMN_BOUNDARY_EVENT).forEach(element => {
      if (element.businessObject.attachedToRef.id !== elementId) {
        return;
      }

      const eventDefinitions = element.businessObject.eventDefinitions;
      if (!this._isErrorEventDefinition(eventDefinitions)) {
        return;
      }

      const errorCode = eventDefinitions[0]?.errorRef?.errorCode;
      if (expectedErrorCode && expectedErrorCode === errorCode) {
        next.push(element.id);
      }
    });

    return next;
  }

  _getEscalationBoundary(elementId, expectedEscalationCode) {
    const next = [];

    this._filter(BPMN_BOUNDARY_EVENT).forEach(element => {
      if (element.businessObject.attachedToRef.id !== elementId) {
        return;
      }

      const eventDefinitions = element.businessObject.eventDefinitions;
      if (!this._isEscalationEventDefinition(eventDefinitions)) {
        return;
      }

      const escalationCode = eventDefinitions[0]?.escalationRef?.escalationCode;
      if (expectedEscalationCode && expectedEscalationCode === escalationCode) {
        next.push(element.id);
      }
    });

    return next;
  }

  _getOutgoing(element) {
    const next = [];

    if (element.type === BPMN_END_EVENT) {
      const { eventDefinitions, $parent } = element.businessObject;

      if ($parent.$type === BPMN_SUB_PROCESS) {
        if (this._isErrorEventDefinition(eventDefinitions)) {
          // handle error end events of embedded sub processes
          return this._getErrorBoundary($parent.id, eventDefinitions[0].errorRef?.errorCode);
        } else if (this._isEscalationEventDefinition(eventDefinitions)) {
          // handle escalation end events of embedded sub processes
          return this._getEscalationBoundary($parent.id, eventDefinitions[0].escalationRef?.escalationCode);
        } else {
          // handle end events of embedded sub processes
          const incoming = this._findIncoming($parent.id);
          return incoming ? [ incoming.id ] : [];
        }
      }
    }

    (element.businessObject.outgoing || []).forEach(sequenceFlow => {
      const target = this._elementRegistry.get(sequenceFlow.targetRef.id);

      let id;
      if (target.type === BPMN_SUB_PROCESS) {
        id = (this._findStartEvent(target.id) || {}).id;
      } else {
        id = target.id;
      }

      if (id) {
        next.push(id);
      }
    });

    return next;
  }

  _isErrorEventDefinition(eventDefinitions) {
    return eventDefinitions && eventDefinitions.length !== 0 && eventDefinitions[0].$type === BPMN_ERROR_EVENT_DEFINITION;
  }

  _isEscalationEventDefinition(eventDefinitions) {
    return eventDefinitions && eventDefinitions.length !== 0 && eventDefinitions[0].$type === BPMN_ESCALATION_EVENT_DEFINITION;
  }

  _isLoop(state) {
    return state.path.find(elementId => elementId === state.id) ? true : false
  }
}
