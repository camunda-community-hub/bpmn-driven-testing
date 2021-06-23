/**
 * Helper class that stubs the BPMN JS element registry.
 */
export default class ElementRegistry {
  constructor(modelInstance) {
    this._elements = [];
    this._elementMap = {};

    for (const [id, flowElement] of Object.entries(modelInstance.elementsById)) {
      const element = {
        id: id,
        type: flowElement.$type,
        businessObject: flowElement
      };

      this._elements.push(element);
      this._elementMap[id] = element;
    }
  }

  filter(fn) {
    return this._elements.filter(fn);
  }

  find(fn) {
    return this._elements.find(fn);
  }

  get(elementId) {
    return this._elementMap[elementId];
  }
}
