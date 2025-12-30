# Hello World Plugin

A simple demonstration plugin for the Foundation framework plugin system.

## Building the Plugin

### Option 1: Using Maven (Recommended)

The plugin is automatically built when you compile the silver-demos module.

```bash
cd foundation
mvn clean package -pl silver-demos
```

The plugin JAR will be created at:
```
silver-demos/target/hello-world-plugin.jar
```

### Option 2: Manual Compilation

```bash
# Compile
javac -d bin src/main/java/org/jwellman/foundation/plugins/HelloWorldPlugin.java

# Create JAR with manifest
echo "Main-Class: org.jwellman.foundation.plugins.HelloWorldPlugin" > MANIFEST.MF
jar cfm hello-world-plugin.jar MANIFEST.MF -C bin .
```

## Installing the Plugin

1. Create the plugins directory structure:
```
plugins/
  └── hello-world/
      ├── hello-world-plugin.jar
      └── plugin.properties
```

2. Copy the plugin files:
```bash
# Copy JAR
cp silver-demos/target/hello-world-plugin.jar plugins/hello-world/

# Copy descriptor
cp silver-demos/src/main/resources/sample-plugins/hello-world/plugin.properties plugins/hello-world/
```

## Running with Foundation

The plugin will be automatically discovered when you run SilverTierShowcaseDemo:

```bash
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.SilverTierShowcaseDemo"
```

## Plugin Features

- **Simple UI**: Displays a greeting window with information about the plugin system
- **Isolated Execution**: Runs in its own classloader (ISOLATED_JVM mode)
- **Self-Contained**: No external dependencies required
- **Multiple Instances**: Allows launching multiple instances simultaneously

## Plugin Descriptor

The `plugin.properties` file describes the plugin metadata:

- **name**: Display name shown in the UI
- **version**: Plugin version number
- **mainClass**: Entry point class with main() method
- **launchMode**: How the plugin should be executed (ISOLATED_JVM, EXTERNAL_PROCESS, etc.)
- **allowMultipleInstances**: Whether multiple instances can run simultaneously

## Testing Different Launch Modes

You can edit `plugin.properties` to test different launch modes:

- **ISOLATED_JVM**: Separate classloader, same JVM (default)
- **EXTERNAL_PROCESS**: Separate JVM process
- **SHARED_JVM**: Shared classloader (not recommended for 3rd party plugins)
- **AUTO**: Framework decides based on plugin characteristics
