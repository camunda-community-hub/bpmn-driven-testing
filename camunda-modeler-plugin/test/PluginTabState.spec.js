import chai from "chai";

import pluginTabState from "../main/bpmndt/PluginTabState";

const expect = chai.expect;

class Plugin {
  constructor() {
    this.shown = false;
  }

  hide() {
    this.shown = false;
  }

  show() {
    this.shown = true;
  }
}

describe("PluginTabState", () => {
  let plugin1;
  let plugin2;

  before(() => {
    plugin1 = new Plugin();
    plugin2 = new Plugin();
  });

  describe("setActiveTab", () => {
    it("simulate roundtrip", () => {
      pluginTabState.setActiveTab("tab1");
      expect(pluginTabState.activeTabId).to.have.equal("tab1");

      pluginTabState.register(plugin1);
      expect(pluginTabState.tabs).to.have.property("tab1");
      expect(pluginTabState.tabs["tab1"].plugin === plugin1).to.be.true;

      plugin1.show();

      // switch to DMN tab
      pluginTabState.disablePlugin();
      expect(pluginTabState.activeTabId).to.be.null;
      expect(pluginTabState.tabs).to.have.property("tab1");
      expect(pluginTabState.tabs["tab1"].plugin === plugin1).to.be.true;
      expect(pluginTabState.tabs["tab1"].shown).to.be.true;
      expect(plugin1.shown).to.be.false;

      // switch back to tab1
      pluginTabState.setActiveTab("tab1");
      expect(pluginTabState.tabs).to.have.property("tab1");
      expect(plugin1.shown).to.be.true;

      // switch to tab2
      pluginTabState.setActiveTab("tab2");
      expect(pluginTabState.activeTabId).to.have.equal("tab2");
      expect(plugin1.shown).to.be.false;

      pluginTabState.register(plugin2);
      expect(pluginTabState.tabs).to.have.property("tab2");
      expect(pluginTabState.tabs["tab2"].plugin === plugin2).to.be.true;

      // switch back to tab1
      pluginTabState.setActiveTab("tab1");
      expect(pluginTabState.activeTabId).to.have.equal("tab1");
      expect(pluginTabState.tabs["tab2"].plugin === plugin2).to.be.true;
      expect(pluginTabState.tabs["tab2"].shown).to.be.false;
      expect(plugin1.shown).to.be.true;
      expect(plugin2.shown).to.be.false;

      // switch back to tab2
      pluginTabState.setActiveTab("tab2");
      expect(pluginTabState.activeTabId).to.have.equal("tab2");
      expect(plugin1.shown).to.be.false;
      expect(plugin2.shown).to.be.false;
    });
  });
});
