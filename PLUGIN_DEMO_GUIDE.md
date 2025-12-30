# Foundation Plugin System Demo Guide

This guide explains how to use the sample plugin and SilverTierShowcaseDemo to explore the plugin system.

## Quick Start

### 1. Build the Project

```bash
cd foundation
mvn clean install -pl silver
```

### 2. Create Plugin Directory Structure

```bash
# From foundation root
mkdir -p plugins/hello-world
mkdir -p config
```

### 3. Package the Sample Plugin

The HelloWorld plugin needs to be compiled and packaged as a JAR. You can do this manually:

```bash
# Compile the plugin
cd silver-demos
mkdir -p target/classes
javac -d target/classes src/main/java/org/jwellman/foundation/plugins/HelloWorldPlugin.java

# Create JAR with manifest
cd target/classes
echo "Main-Class: org.jwellman.foundation.plugins.HelloWorldPlugin" > MANIFEST.MF
jar cfm ../../hello-world-plugin.jar MANIFEST.MF org/jwellman/foundation/plugins/HelloWorldPlugin.class
cd ../..

# Copy to plugins directory
cp target/hello-world-plugin.jar ../../plugins/hello-world/
cp src/main/resources/sample-plugins/hello-world/plugin.properties ../../plugins/hello-world/
```

### 4. Run the Showcase Demo

```bash
# From foundation root
cd silver-demos
mvn exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SilverTierShowcaseDemo"
```

## Plugin System Features Demonstrated

### In the Showcase Demo UI

The showcase demo includes a **"3. Plugin System"** section with:

1. **Plugin Status Panel** - Shows:
   - Number of registered plugins
   - Number of loaded plugins
   - Number of discovered plugins
   - Plugins directory path
   - Config directory path

2. **Plugin Management Buttons**:
   - **Show Plugin List** - Displays all registered and discovered plugins
   - **Register All Actions** - Creates Swing Actions for all enabled plugins
   - **Show Plugin Menu** - Demonstrates plugin menu creation
   - **Rescan for Plugins** - Re-scans the plugins directory for new plugins

### Event Log

Watch the Event Log panel for plugin-related events:
- `PLUGIN` - Plugin system initialization
- `PLUGIN` - Plugin discovery results
- `PLUGIN` - Plugin registration events
- `PLUGIN` - Plugin launch events

## Testing the Plugin System

### Test 1: Plugin Discovery

1. Place the hello-world plugin in `plugins/hello-world/`
2. Run the showcase demo
3. Check the event log - you should see:
   ```
   [timestamp] PLUGIN | Plugin system initialized successfully
   [timestamp] PLUGIN | Discovered 1 new plugin(s)
   [timestamp] PLUGIN | Found: Hello World Plugin v1.0.0
   ```

### Test 2: Plugin Registration

1. Click **"Show Plugin List"**
2. You should see the HelloWorld plugin in the "Discovered Plugins" section
3. To register it programmatically, you can use:
   ```java
   PluginManager pm = Foundation.getPluginManager();
   List<UnregisteredPlugin> discovered = pm.getDiscoveredPlugins();
   for (UnregisteredPlugin plugin : discovered) {
       pm.registerPlugin(plugin, LaunchMode.ISOLATED_JVM, true);
   }
   ```

### Test 3: Plugin Action Registry

1. Register the plugin (see Test 2)
2. Click **"Register All Actions"**
3. Click **"Show Plugin Menu"**
4. A new window will appear with a "Plugins" menu
5. Click the plugin name in the menu to launch it

### Test 4: Plugin Launch

When you launch the HelloWorld plugin:
- In **ISOLATED_JVM** mode: A new window appears with the plugin UI
- In **EXTERNAL_PROCESS** mode: A separate JVM process launches
- In **SHARED_JVM** mode: Runs in same classloader (not recommended)

## Plugin Descriptor Properties

The `plugin.properties` file supports:

```properties
# Metadata
name=Plugin Name
version=1.0.0
vendor=Your Company
description=What the plugin does

# Launch Configuration
jar=plugin.jar
mainClass=com.example.Main
launchMode=ISOLATED_JVM  # or EXTERNAL_PROCESS, SHARED_JVM, AUTO

# Dependencies (optional)
classpath=lib/dependency1.jar,lib/dependency2.jar

# JVM Args (optional, for EXTERNAL_PROCESS)
jvmArgs=-Xmx512m,-Dapp.mode=production

# App Args (optional)
appArgs=--config,app.properties

# UI Integration (optional)
icon=icon.png
windowTitle=My Plugin

# Features (optional)
allowMultipleInstances=true
```

## Creating Your Own Plugin

### Minimal Plugin Example

```java
package com.example.myplugin;

import javax.swing.*;

public class MyPlugin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Plugin");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JLabel("Hello from my plugin!"));
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
```

### Plugin Directory Structure

```
plugins/
  └── my-plugin/
      ├── my-plugin.jar
      ├── plugin.properties
      ├── icon.png (optional)
      └── lib/ (optional dependencies)
          └── dependency.jar
```

### Packaging

```bash
# Compile
javac -d bin MyPlugin.java

# Create JAR with manifest
echo "Main-Class: com.example.myplugin.MyPlugin" > MANIFEST.MF
jar cfm my-plugin.jar MANIFEST.MF -C bin .

# Create descriptor
cat > plugin.properties << EOF
name=My Plugin
version=1.0.0
jar=my-plugin.jar
mainClass=com.example.myplugin.MyPlugin
launchMode=ISOLATED_JVM
EOF
```

## Troubleshooting

### Plugin Not Discovered

- Check that `plugins/` directory exists
- Verify `plugin.properties` exists in plugin directory
- Check that JAR filename matches `jar=` property
- Look for errors in the event log

### Plugin Won't Launch

- Verify `mainClass` is correct
- Check that JAR contains the main class
- Ensure JAR has proper MANIFEST.MF with Main-Class
- Try different launch modes (ISOLATED_JVM vs EXTERNAL_PROCESS)

### ClassNotFoundException

- Verify all dependencies are in the `lib/` subdirectory
- Add dependencies to `classpath=` property
- For EXTERNAL_PROCESS mode, ensure all JARs are accessible

## Advanced Topics

### Custom Launch Modes

You can programmatically control launch mode:

```java
PluginManager pm = Foundation.getPluginManager();
LoadedPlugin plugin = pm.launchPlugin("my-plugin");
plugin.run(new String[] {"--arg1", "value1"});
```

### Plugin Lifecycle

1. **Discovery** - Scan plugins directory for unregistered plugins
2. **Registration** - Add to registry.json with launch mode and preferences
3. **Action Creation** - Create Swing Actions for UI integration
4. **Launch** - Load via classloader or external process
5. **Execution** - Call main() method with arguments
6. **Cleanup** - Close classloader or terminate process

### Memory Considerations

- **ISOLATED_JVM**: ~5-10MB per medium plugin
- **EXTERNAL_PROCESS**: ~50-100MB per plugin (full JVM)
- **SHARED_JVM**: Minimal overhead but risk of conflicts

For details, see the design discussion in `/convo.tmp`.

## Next Steps

1. Try creating your own simple plugin
2. Experiment with different launch modes
3. Add plugin icons and custom metadata
4. Integrate plugin menus into your desktop applications
5. Explore plugin lifecycle listeners (future feature)

## Resources

- Plugin implementation: `silver/src/main/java/org/jwellman/foundation/plugin/`
- Sample plugin: `silver-demos/src/main/java/org/jwellman/foundation/plugins/HelloWorldPlugin.java`
- Showcase demo: `silver-demos/src/main/java/org/jwellman/foundation/examples/SilverTierShowcaseDemo.java`
- Plugin descriptor template: `silver-demos/src/main/resources/sample-plugins/hello-world/plugin.properties`
