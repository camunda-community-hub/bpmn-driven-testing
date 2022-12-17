import chai from "chai";

import PluginState from "../main/bpmndt/PluginState";

const expect = chai.expect;

class Plugin {
  constructor() {
    this.visible = false;
  }

  hide() {
    this.visible = false;
  }

  show() {
    this.visible = true;
  }
}

describe("PluginState", () => {
  let pluginState;

  let plugin1;
  let plugin2;

  before(() => {
    pluginState = new PluginState();

    plugin1 = new Plugin();
    plugin2 = new Plugin();
  });

  it("simulate roundtrip", () => {
    pluginState.showPlugin("tab1"); // called before the plugin is created

    pluginState.registerPlugin("tab1", plugin1);
    expect(pluginState.cache).to.have.lengthOf(1);
    expect(pluginState.cache[0].plugin).to.be.equal(plugin1);
    expect(pluginState.cache[0].id).to.be.equal("tab1");

    plugin1.show();

    // switch to DMN tab
    pluginState.hidePlugin();
    expect(pluginState.cache[0].active).to.be.false;
    expect(pluginState.cache[0].visible).to.be.true;
    expect(plugin1.visible).to.be.false;

    // switch back to tab1
    pluginState.showPlugin("tab1");
    expect(pluginState.cache[0].active).to.be.true;
    expect(plugin1.visible).to.be.true;

    // switch to tab2
    pluginState.showPlugin("tab2");
    expect(pluginState.cache[0].active).to.be.false;
    expect(pluginState.cache[0].visible).to.be.true;
    expect(plugin1.visible).to.be.false;

    pluginState.registerPlugin("tab2", plugin2);
    expect(pluginState.cache).to.have.lengthOf(2);
    expect(pluginState.cache[1].plugin).to.be.equal(plugin2);
    expect(pluginState.cache[1].id).to.be.equal("tab2");

    // switch back to tab1
    pluginState.showPlugin("tab1");
    expect(pluginState.cache[0].active).to.be.true;
    expect(pluginState.cache[0].visible).to.be.true;
    expect(pluginState.cache[1].active).to.be.false;
    expect(pluginState.cache[1].visible).to.be.false;
    expect(plugin1.visible).to.be.true;
    expect(plugin2.visible).to.be.false;

    // switch back to tab2
    pluginState.showPlugin("tab2");
    expect(pluginState.cache[0].active).to.be.false;
    expect(pluginState.cache[0].visible).to.be.true;
    expect(pluginState.cache[1].active).to.be.true;
    expect(pluginState.cache[1].visible).to.be.false;
    expect(plugin1.visible).to.be.false;
    expect(plugin2.visible).to.be.false;
  });
});
