# Camunda Modeler Plugin

1. Ensure that Camunda Modeler 4.4.0 or higher is installed
2. [Download](https://github.com/camunda-community-hub/bpmn-driven-testing/releases/latest/download/bpmn-driven-testing-plugin.zip) latest Camunda Modeler plugin release
3. Unpackage downloaded ZIP file to the `resources/plugins/` directory of the Camunda Modeler installation
4. Start Camunda Modeler
5. Verify that the plugin is available within the **Plugins** menu as `BPMN Driven Testing` > `Show / Hide` (shortcut CTRL+T).

## Development
For the plugin development `npm` (a Node.js installation) is required.

1. (Only once) Link the camunda-modeler-plugin directory of the GIT repository as "bpmn-driven-testing" modeler plugin

Under Windows, run as administrator

```
cd <camunda-modeler-installation>
mklink /d resources\plugins\bpmn-driven-testing <repository>\camunda-modeler-plugin
```

If you want to remove the link:

Under Windows, run as administrator

```
cd <camunda-modeler-installation>
rd resources\plugins\bpmn-driven-testing
```

2. Start the Camunda Modeler
3. Press **F12** to show developer tools
4. Run webpack to build `dist/client.js` on the fly, when saving changes

```
npm run dev
```

5. To reload the plugin within the Camunda Modeler, press **CTRL+R** in developer tools

## Testing
Tests are performed using [mocha](https://mochajs.org/) and [chai](https://www.chaijs.com/).

```
npm run test-watch
```

## Releasing
When creating a release, run:

```
npm run build && npm run zip
```

This command bundles the Camunda Modeler plugin and creates a ZIP file called `bpmn-driven-testing-plugin.zip`.
This binary asset can now be attached to a **bpmn-driven-testing** [release](https://github.com/camunda-community-hub/bpmn-driven-testing/releases) on Github.
