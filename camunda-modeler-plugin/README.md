# Camunda Modeler Plugin

## Installation

1. Ensure that Camunda Modeler 5.9.0+ is installed
2. [Download](https://github.com/camunda-community-hub/bpmn-driven-testing/releases/latest/download/bpmn-driven-testing-plugin.zip) latest Camunda Modeler plugin release
3. Unpackage downloaded ZIP file to the `resources/plugins/` directory of the Camunda Modeler installation
4. Start Camunda Modeler
5. Verify that the plugin is available within the **Plugins** menu as `BPMN Driven Testing` > `Show / Hide` (shortcut CTRL+T).

## Development
:warning: This and the subsequent sections are only important for plugin development!

For the plugin development a [NodeJS 20.x](https://nodejs.org/download/release/latest-v20.x/) installation is required.

1. Link the `camunda-modeler-plugin/` directory as Camunda Modeler plugin

Under Windows, run as administrator:

```bat
mklink /d <camunda-modeler>\resources\plugins\bpmn-driven-testing <repository>\camunda-modeler-plugin
```

(use `rmdir` to delete the directory link)

Under Unix:

```sh
ln -s <repository>/camunda-modeler-plugin <camunda-modeler>/resources/plugins/bpmn-driven-testing
```

2. Install dependencies

```sh
npm install
```

3. Run webpack to build the plugin on the fly, when code changes are saved to file

```sh
npm run dev
```

4. Start the Camunda Modeler
5. In Camunda Modeler, press **F12** to show developer tools
6. After code changes, press **CTRL+R** in developer tools to reload the plugin within the Camunda Modeler

## Testing
Tests are performed using [mocha](https://mochajs.org/) and [chai](https://www.chaijs.com/).

```sh
npm run test-watch
```

## Releasing
When creating a release, run:

```sh
npm run build && npm run zip
```

This command bundles the Camunda Modeler plugin and creates a ZIP file called `bpmn-driven-testing-plugin.zip`.
This binary asset can now be attached to a **bpmn-driven-testing** [release](https://github.com/camunda-community-hub/bpmn-driven-testing/releases) on Github.
