# Foundation Framework - Tiered Feature Roadmap

This document outlines the proposed feature distribution across Foundation's tiered architecture (Stone → Bronze → Silver → Gold → Platinum).

## Overview

Foundation's tiered architecture supports a **build-once, deploy-at-complexity** model where users select the JAR matching their feature requirements. Each tier is additive-only: upgrading from a lower tier adds features but never breaks existing code.

## Quick Reference

| Tier | Status | Key Features | Target Use Case |
|------|--------|--------------|-----------------|
| **Stone** | Implemented | LAF setup, single window/desktop, basic IWindow | Simple utilities, calculators |
| **Bronze** | Implemented | Multi-panel registration, multiple JInternalFrames | Multi-document apps, MDI |
| **Silver** | Planned | Window management (cascade/tile), **plugin system** | Professional MDI, extensible apps |
| **Gold** | Planned | Layout persistence, docking, workspaces, plugin UI contribution | IDE-style apps, power tools |
| **Platinum** | Planned | Advanced plugins (updates/security), preferences, resources, notifications | Enterprise platforms, full IDEs |

**Key Decision:** Plugins moved to Silver tier as they are fundamental for flexibility and enable Gold/Platinum features to be implemented as plugins.

---

## Stone (Current Implementation)

**Status:** Implemented
**JAR:** `foundation-stone.jar`

### Core Features

- **Framework initialization** (`_init`) - Stone.java:88
- **Look and Feel setup** - LAF constants, switching logic - Stone.java:59-184
- **Basic window creation** (`useWindow`) - Stone.java:251
- **Basic desktop creation** (`useDesktop`) - Stone.java:205
- **IWindow abstraction** support
- **Single-panel applications**
- **Basic showGUI** with JDesktopPane - Stone.java:350

### Design Rationale

Stone provides the absolute minimum for a functional Swing application: initialization and a single window or desktop environment. This tier is suitable for simple calculator apps, single-window utilities, or minimal GUI tools.

**Example Use Case:** Simple calculator, basic text editor, single-window utility

---

## Bronze (Current Implementation)

**Status:** Implemented
**JAR:** `foundation-bronze.jar`
**Includes:** Stone + Bronze features

### Multi-Panel Features

- **UI registration system** (`registerUI`) - Bronze.java:19
- **Panel tracking** (`HashMap<String, XPanel>`) - Bronze.java:15
- **Multiple JInternalFrame management** - Bronze.java:17
- **initializeOtherWindows** override - Bronze.java:27

### Design Rationale

Bronze adds the ability to manage multiple panels/windows, essential for any multi-document or multi-tool desktop environment. Applications can register multiple UI panels and display them as separate internal frames.

**Example Use Case:** Multi-tool desktop, multi-document application, basic MDI environment

---

## Silver (Proposed Features)

**Status:** Planned
**JAR:** `foundation-silver.jar`
**Includes:** Stone + Bronze + Silver features

### Enhanced Desktop Management

#### 1. Window Arrangement

- `cascadeWindows()` - Arrange internal frames in cascade pattern
- `tileWindows()` - Tile internal frames horizontally/vertically
- `tileWindowsHorizontal()` / `tileWindowsVertical()` - Directional tiling
- `minimizeAll()` / `restoreAll()` - Batch window operations

#### 2. Desktop Navigation

- **Window menu generation** - Auto-populate "Window" menu with open frames
- **Keyboard shortcuts** - Ctrl+Tab for window switching, Ctrl+F4 to close
- **Next/Previous window navigation** - Cycle through open windows
- **Jump to window** - Activate window by name/index

#### 3. Window State Management

- Track window states (minimized, maximized, normal)
- Z-order management (bring to front, send to back)
- Active window tracking with listeners
- Window focus history

#### 4. Basic Desktop Toolbar

- Standard toolbar with window management buttons
- Quick-access buttons for registered UIs
- Window selector dropdown
- Minimize/maximize all buttons

### Plugin Architecture

**Rationale:** Plugin support is fundamental for application flexibility and enables implementation of Gold/Platinum features through plugins.

#### 5. Core Plugin System

- **Plugin discovery** - JAR scanning in `/plugins/` directory
- **Plugin lifecycle** - Load, start, stop, unload operations
- **Plugin metadata** - Version, author info (from `META-INF/foundation-plugin.properties`)
- **Plugin isolation** - Separate classloaders for plugins
- **Plugin registry** - Track loaded plugins and their states
- **Plugin interfaces** - `IPlugin` interface for plugin implementation

#### 6. Basic Plugin API

- `loadPlugins()` - Discover and load all plugins from plugin directory
- `getPlugin(String id)` - Retrieve loaded plugin by ID
- `getLoadedPlugins()` - List all loaded plugins
- **Plugin base classes** - AbstractPlugin with lifecycle methods

### Example API

```java
Silver s = (Silver) Foundation.init();

// Desktop management
s.cascadeWindows();
s.tileWindows();
s.addWindowMenu(menuBar); // Auto-populates with open windows
s.nextWindow(); // Navigate to next window

// Plugin management
s.loadPlugins();
List<IPlugin> plugins = s.getLoadedPlugins();
for (IPlugin plugin : plugins) {
    System.out.println("Loaded: " + plugin.getName());
}
```

### Design Rationale

Silver provides a "mature" desktop manager plus foundational plugin support. The plugin system enables applications to be extended without recompilation, and plugins can be used to implement Gold/Platinum features modularly. This tier makes multi-window applications feel polished and professional while enabling extensibility.

**Example Use Case:** Professional MDI application, multi-document editor, extensible development tool with plugin support, application with third-party extensions

---

## Gold (Proposed Features)

**Status:** Planned
**JAR:** `foundation-gold.jar`
**Includes:** Stone + Bronze + Silver + Gold features

### Advanced Window Management

#### 1. Window Persistence

- **Save/restore window layouts** - Preserve positions, sizes, states
- **Workspace/session management** - Multiple named layouts
- `saveDesktopLayout(String name)` - Persist current layout
- `restoreDesktopLayout(String name)` - Restore saved layout
- `listDesktopLayouts()` - Enumerate saved layouts

#### 2. Advanced Layouts

- **Docking framework integration** - Basic docking zones (left, right, top, bottom)
- **Split pane desktop layouts** - Divide desktop into regions
- **Tabbed window groups** - Multiple frames organized in tabs
- **Floating windows** - Detach windows from main desktop
- **Magnetic edges** - Windows snap to desktop edges/other windows

#### 3. Extended LAF Support

- **Runtime LAF switching** - Change look and feel without restart
- **Per-window LAF override** (if supported by LAF)
- **LAF preference persistence** - Remember user's LAF choice
- **LAF preview mode** - Preview LAF before applying

#### 4. Keyboard Customization

- **Hotkey manager** - User-configurable shortcuts
- **Action binding registry** - Map actions to keys
- **Keyboard shortcut conflict detection**
- **Shortcut editor UI**

#### 5. Multiple Desktop Support

- **Workspace switching** - Multiple virtual desktops (Desktop 1, Desktop 2, etc.)
- **Per-desktop window sets** - Different windows per workspace
- **Workspace quick-switch** - Keyboard shortcuts for workspace navigation
- **Workspace naming** - User-friendly workspace labels

### Plugin UI Contribution

#### 6. Menu/Toolbar Contribution

- **Plugins contribute menu items** dynamically
- **Toolbar button contribution API**
- **Context menu extensibility** - Plugins extend right-click menus
- **Menu merging** - Combine plugin menus intelligently
- **Action registration** - Plugins register actions for UI elements

#### 7. Application Lifecycle Events

- **Startup/shutdown hooks** - Execute code at app lifecycle points
- **Before/after init callbacks**
- **Window open/close event broadcasting**
- **Desktop mode change events**
- **Plugin load/unload events**
- **Event priority system** - Control listener execution order

### Example API

```java
Gold g = (Gold) Foundation.init();

// Layout persistence
g.saveDesktopLayout("my-layout");
g.restoreDesktopLayout("my-layout");

// Workspace management
g.createWorkspace("Development");
g.createWorkspace("Testing");
g.switchWorkspace("Development");

// LAF switching
g.setLookAndFeel("Darcula");
g.setHotkey("save-layout", "Ctrl+Shift+S");

// Plugin UI contribution
g.registerLifecycleListener(new AppLifecycleListener() {
    @Override
    public void onStartup() { /* ... */ }
    @Override
    public void onShutdown() { /* ... */ }
});

// Plugin contributes menu
MenuContribution contrib = new MenuContribution("Tools", "My Plugin Tool");
g.registerMenuContribution(contrib);
```

### Design Rationale

Gold provides power-user features for complex desktop applications (think IDE-level functionality). This tier adds advanced window management, layout persistence, extensive customization, and enables plugins to contribute UI elements and respond to application lifecycle events. Perfect for sophisticated applications requiring deep extensibility.

**Example Use Case:** IDE-style application, CAD tool with multiple viewports, professional audio/video editing software, plugin-extensible desktop environment

---

## Platinum (Proposed Features)

**Status:** Planned
**JAR:** `foundation-platinum.jar`
**Includes:** Stone + Bronze + Silver + Gold + Platinum features

### Enterprise Features & Advanced Plugin System

#### 1. Advanced Plugin Management

- **Plugin manager UI** - Enable/disable plugins, view metadata, manage versions
- **Plugin dependency resolution** - Handle inter-plugin dependencies automatically
- **Plugin update system** - Check for and install plugin updates
- **Plugin marketplace integration** - Browse and install from remote repositories
- **Plugin signing/verification** - Verify plugin authenticity and integrity
- **Plugin sandboxing** - Enhanced security with permission models

#### 2. Resource Management

- **Centralized icon registry** - Register and retrieve icons by name
- **String externalization** - Full i18n support with resource bundles
- **Resource bundles per tier/plugin** - Modular resource organization
- **Theme-aware resource loading** - Icons adapt to dark/light themes
- **Resource versioning** - Handle resource conflicts across plugins
- **Dynamic resource reloading** - Update resources without restart

#### 3. Preferences System

- **User preference storage** - JSON/Properties backend
- **Preference scopes** - User, workspace, application levels
- **Preference UI generation** - Auto-generate settings dialogs from schemas
- **Preference validation** - Type checking and constraints
- **Preference change listeners** - React to preference updates
- **Default preferences** - Per-tier/plugin defaults
- **Preference import/export** - Backup and share configurations
- **Preference search** - Find settings by keyword

#### 4. Advanced Desktop Provider

- **Custom desktop implementations** - Beyond JDesktopPane
- **Desktop decorators/overlays** - Visual enhancements
- **Notification system** - Toast notifications, alerts, badges
- **Status bar framework** - Pluggable status bar components
- **Desktop backgrounds** - Custom background rendering
- **Desktop widgets** - Non-window UI elements on desktop
- **Multi-monitor support** - Window management across displays

#### 5. Toolbar Customization

- **User can rearrange toolbar buttons** - Drag-and-drop customization
- **Toolbar editor UI** - Visual toolbar customization dialog
- **Toolbar profiles** - Save/restore toolbar configurations
- **Context-sensitive toolbars** - Toolbars adapt to active window

### Example API

```java
Platinum p = (Platinum) Foundation.init();

// Advanced plugin management
PluginManager pm = p.getPluginManager();
pm.checkForUpdates();
pm.installPlugin("https://marketplace.example.com/plugin/foo");

// Preferences
PreferenceManager prefs = p.getPreferenceManager();
prefs.set("theme", "dark");
prefs.addListener("theme", event -> updateTheme());
prefs.exportToFile("my-settings.json");

// Resources
IconRegistry icons = p.getIconRegistry();
icons.register("save", new ImageIcon("save.png"));
Icon themed = icons.getThemeAwareIcon("save"); // Adapts to dark/light theme

// Notifications
p.showNotification("Build complete", NotificationType.INFO);

// Status bar
StatusBar status = p.getStatusBar();
status.addComponent(new ProgressIndicatorComponent());

// Toolbar customization
p.showToolbarEditor();
```

### Design Rationale

Platinum provides enterprise-level infrastructure for building sophisticated, plugin-based desktop applications. This tier adds advanced plugin management (updates, dependencies, security), comprehensive resource management, powerful preferences system, and rich desktop enhancements. Designed for applications requiring professional-grade extensibility and user customization.

**Example Use Case:** Full IDE (Eclipse/IntelliJ-style), enterprise application platform, professional content creation suite, extensible tool ecosystem

---

## Implementation Guidelines

### When Adding Features to Tiers

1. **Stone** - Only modify for critical bug fixes or foundational changes that affect all tiers
2. **Bronze** - Keep focused on multi-panel basics; avoid feature creep
3. **Silver+** - New features go here; decide tier based on complexity:
   - User-facing convenience + basic plugins → Silver
   - Power-user/advanced + plugin UI contribution → Gold
   - Enterprise/advanced plugins + resources/preferences → Platinum

### Interface Location

Place new interfaces in the **lowest tier** that needs them:

- **Core abstractions** (IWindow, uContext) → Stone
- **Desktop-related interfaces** → Bronze or Silver
- **Basic plugin APIs** (IPlugin, plugin lifecycle) → Silver
- **Advanced window management interfaces** → Gold
- **Plugin contribution APIs** (menu/toolbar contribution) → Gold
- **Enterprise APIs** (preferences, resources, advanced plugin features) → Platinum

This ensures maximum compatibility: code using lower-tier interfaces works with all higher-tier JARs.

**Important:** Since plugins are in Silver, Gold and Platinum features can be implemented as plugins, maintaining modularity.

### Backwards Compatibility

**Critical Rule:** Never remove or change signatures of public methods in lower tiers.

- **Adding methods:** Safe at any tier
- **Deprecating methods:** Mark as `@Deprecated` but keep functional
- **Changing behavior:** Only if it's a bug fix or doesn't break existing contracts
- **Removing methods:** Never for lower tiers; only acceptable in highest tier with major version bump

### Feature Detection

Consider adding capability detection for applications that need to support multiple tiers:

```java
public interface IFoundationCapabilities {
    boolean supports(String feature);
    String getTierLevel(); // "stone", "bronze", "silver", "gold", "platinum"
    int getTierVersion(); // Numeric version for comparison
}

// Usage
Foundation f = Foundation.init();
if (f.getCapabilities().supports("window-persistence")) {
    ((Gold) f).saveDesktopLayout("default");
}
```

### Dependency Management

Each tier should minimize external dependencies:

- **Stone/Bronze** - Only core Java Swing, no third-party deps
- **Silver** - May add lightweight deps for window management
- **Gold** - Can add deps for docking frameworks
- **Platinum** - Can add deps for plugin systems (classloader utilities, etc.)

Users deploying lower tiers should not be forced to include dependencies only needed by higher tiers.

---

## Migration Path

### For Existing Applications

Applications currently using `foundation.jar` (full version):

1. **Assess feature usage** - Determine which tier features you actually use
2. **Test with lower tier** - Try compiling against lower-tier JAR
3. **Deploy minimal tier** - Use lowest tier that satisfies requirements
4. **Upgrade as needed** - Move to higher tier when new features are required

### For New Applications

1. **Start with Stone** - Begin with minimal tier
2. **Add tiers incrementally** - Move to Bronze when multi-panel needed, etc.
3. **Feature-driven upgrades** - Only upgrade tier when you need specific features

---

## Future Considerations

### Potential Future Tiers

If five tiers prove insufficient, consider:

- **Diamond** - Cloud integration, remote desktop capabilities
- **Titanium** - AI/ML integration, intelligent window management
- **Quantum** - (Reserved for revolutionary features)

### JAR Size Estimates (Approximate)

- `foundation-stone.jar` - ~50KB (core only)
- `foundation-bronze.jar` - ~75KB (Stone + multi-panel)
- `foundation-silver.jar` - ~200KB (+ window management + plugin system)
- `foundation-gold.jar` - ~350KB (+ docking framework + plugin UI)
- `foundation-platinum.jar` - ~550KB (+ preferences + resources + advanced features)

These are rough estimates; actual sizes depend on implementation details and dependencies.

---

## Summary

The tiered architecture provides:

1. **Flexibility** - Users deploy only the complexity they need
2. **Stability** - Lower tiers remain stable; innovation happens in higher tiers
3. **Clear upgrade path** - Features are logically grouped by tier
4. **Backwards compatibility** - Code targeting lower tiers works with all higher tiers
5. **Reduced bloat** - Simple apps stay simple; complex apps get power features

This roadmap balances simplicity for basic apps with power for complex applications, letting users pay (in JAR size/complexity) only for features they use.
