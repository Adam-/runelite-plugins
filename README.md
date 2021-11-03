# TODO_PLUGIN_NAME v0.1
TODO_PLUGIN_DESCRIPTION

## How to start writing a new plugin
### Replace `TODO` variables in code
* `TODO_PLUGIN_NAME`
* `TODO_PLUGIN_DESCRIPTION`
* `TODO_PLUGIN_TAGS`

### Edit test run configuration
* Right click on `PluginTest`
* Select `Modify Run Configuration...`
* `Modify options` -> `Add VM options`
  * `VM options:`: `-ea`
  * `Program arguments`: `--debug --developer-mode`

### Start RuneLite
Start RuneLite by running the modified test file