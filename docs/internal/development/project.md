A note about shadowing and libraries.
--------------------------------------
The real plugins themselves all have gradle compileOnly dependencies to their 3rd party libs,
to avoid bundling copies of all libraries in each plugin jar.
Instead, dependencies are bundled in a separate plugin, the Cubematic-Runtime plugin. 
The plugins also have a "plugin dependency" on the runtime plugin.
This is important so the server loads the plugins in the correct order so the libs are available
on the classpath before the other plugins are loaded.

## Why?
- Smaller plugin jars
- Faster compilation

## Add new dependencies
To add a new dependency to any of the plugins, you must add it as a `compileOnly()` dependency in the
plugin's `build.gradle.kts` file, and then also add it to the `runtime` project as an `implementation()` dependency.

## Adding new plugins
Adding new plugins can be done by using dependencies the same way, just remember
to add the `Cubematic-Runtime` plugin as a plugin dependency in the new plugin's bukkit manifest.

## Running the server
When running the server, the Cubematic-Runtime plugin must be present.