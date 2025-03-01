import {
  BPMN_BOUNDARY_EVENT,
  BPMN_END_EVENT,
  BPMN_ERROR_EVENT_DEFINITION,
  BPMN_ESCALATION_EVENT_DEFINITION,
  BPMN_INTERMEDIATE_CATCH_EVENT,
  BPMN_INTERMEDIATE_THROW_EVENT,
  BPMN_LINK_EVENT_DEFINITION,
  BPMN_START_EVENT,
  BPMN_SUB_PROCESS
} from "./constants";

/**
 * Class to find possible paths through a BPMN process.
 */
export default class PathFinder {
  constructor(elementRegistry) {
    this.elementRegistry = elementRegistry;
  }

  /**
   * Finds all paths between a given start and end element.
   */
  find(start, end) {
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

      const element = this.elementRegistry.get(current.id);
      if (element === undefined) {
        // stop branch, if element does not exist anymore
        continue;
      }

      const next = this._getOutgoing(element).concat(this._getBoundary(element.id));
      for (const id of next) {
        stack.push({ id: id, path: current.path.slice() });
      }
    }

    // when end element is a link throw event
    // find the related link catch event and append it
    paths.forEach(path => {
      const endId = path[path.length - 1];
      const endElement = this.elementRegistry.get(endId);

      if (this._isLinkThrowEvent(endElement)) {
        const next = this._getOutgoing(endElement);
        if (next.length === 1) {
          path.push(next[0]);
        }
      }
    });

    return paths.reverse();
  }

  _filterElementsByType(elementType) {
    return this.elementRegistry.filter(element => element.type === elementType);
  }

  /**
   * Finds all elements with a sequence flow that is coming from the given embedded sub process.
   * 
   * @param {String} subProcessId ID of an embedded sub process.
   * 
   * @returns The found elements.
   */
  _findIncoming(subProcessId) {
    return this.elementRegistry.filter(element => {
      const { incoming } = element.businessObject;

      if (incoming) {
        return incoming.find(sequenceFlow => sequenceFlow.sourceRef.id === subProcessId) !== undefined;
      } else {
        return false;
      }
    });
  }

  _findStartEvent(subProcessId) {
    return this._filterElementsByType(BPMN_START_EVENT).find(element => {
      return element.businessObject.$parent.id === subProcessId;
    });
  }

  _getBoundary(elementId) {
    const next = [];
    
    this._filterElementsByType(BPMN_BOUNDARY_EVENT).forEach(element => {
      if (element.businessObject.attachedToRef.id === elementId) {
        next.push(element.id);
      }
    });

    return next;
  }

  _getErrorBoundary(elementId, expectedErrorCode) {
    const next = [];

    this._filterElementsByType(BPMN_BOUNDARY_EVENT).forEach(element => {
      const { attachedToRef, eventDefinitions } = element.businessObject;

      if (attachedToRef.id !== elementId) {
        return;
      }

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

    this._filterElementsByType(BPMN_BOUNDARY_EVENT).forEach(element => {
      const { attachedToRef, eventDefinitions } = element.businessObject;

      if (attachedToRef.id !== elementId) {
        return;
      }

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

  _getLinkCatch(parentId, expectedLinkName) {
    const next = [];

    this._filterElementsByType(BPMN_INTERMEDIATE_CATCH_EVENT).forEach(element => {
      const { eventDefinitions, $parent } = element.businessObject;

      if ($parent.id !== parentId) {
        // ensure same scope
        return;
      }

      if (!this._isLinkEventDefinition(eventDefinitions)) {
        return;
      }

      const linkName = eventDefinitions[0].name;
      if (expectedLinkName && expectedLinkName === linkName) {
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
          // handle error end events of sub process
          return this._getErrorBoundary($parent.id, eventDefinitions[0].errorRef?.errorCode);
        } else if (this._isEscalationEventDefinition(eventDefinitions)) {
          // handle escalation end events of sub process
          return this._getEscalationBoundary($parent.id, eventDefinitions[0].escalationRef?.escalationCode);
        } else {
          // handle end events of sub process
          this._findIncoming($parent.id).map(element => {
            if (element.type === BPMN_SUB_PROCESS) {
              // try to find start event of sub process
              const startEvent = this._findStartEvent(element.id);
              return startEvent !== undefined ? startEvent.id : undefined;
            } else if (element.type === "label") {
              return; // ignore label of subsequent gateway
            } else {
              return element.id;
            }
          }).filter(id => id !== undefined).forEach(id => next.push(id));

          return next;
        }
      }
    }

    if (this._isLinkThrowEvent(element)) {
      // handle link events of same scope
      const { eventDefinitions, $parent } = element.businessObject;
      return this._getLinkCatch($parent.id, eventDefinitions[0].name);
    }

    (element.businessObject.outgoing || []).forEach(sequenceFlow => {
      const target = this.elementRegistry.get(sequenceFlow.targetRef.id);

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

  _isLinkEventDefinition(eventDefinitions) {
    return eventDefinitions && eventDefinitions.length !== 0 && eventDefinitions[0].$type === BPMN_LINK_EVENT_DEFINITION;
  }

  _isLinkThrowEvent(element) {
    return element.type === BPMN_INTERMEDIATE_THROW_EVENT && this._isLinkEventDefinition(element.businessObject.eventDefinitions);
  }

  _isLoop(state) {
    return state.path.find(elementId => elementId === state.id) !== undefined;
  }
}
