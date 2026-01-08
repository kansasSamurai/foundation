# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Foundation is a micro-framework for creating Java Swing applications. It simplifies the bootstrapping of Swing applications by providing a clean API for initializing Look and Feel, creating windows (JFrame) or desktop environments (JInternalFrame/JDesktopPane), and managing application UI.

## Design Philosophy

### Deployment-Agnostic Applications

**Core Principle:** Applications built with Foundation should be deployment-agnostic. The same application code (and JAR) should work in multiple contexts without modification:
- **Standalone mode** - Running in its own JFrame with standard windowing behavior
- **Embedded mode** - Loaded into a multi-tool desktop environment (IDE-style, multi-app desktop) as a JInternalFrame

### Interface-First Approach vs JSR 296

Foundation intentionally diverges from JSR 296 (Swing Application Framework) in key ways:

**Why not JSR 296's approach:**
- JSR 296 requires extending framework classes (e.g., SingleFrameApplication), which:
  - Makes migration of existing applications more difficult
  - Locks applications into a single deployment model (standalone windowing)
  - Provides concrete implementations rather than abstractions
  - Cannot easily support embedding into larger composite applications

**Foundation's approach:**
- **Interfaces over inheritance** - Core features use interfaces (IWindow, uiThemeProvider, uiDesktopProvider) rather than requiring subclassing
- **JPanel-based UIs** - Applications build UI in JPanels; the framework wraps them in appropriate containers
- **IWindow abstraction** - Application code never directly manipulates JFrame or JInternalFrame
- **Configuration-driven deployment** - uContext determines standalone vs desktop mode, not code structure

**The benefit:** A tool developer can:
1. Build their application once (using JPanels and IWindow)
2. Distribute as standalone executable (Foundation wraps in JFrame)
3. OR package as a plugin for a larger application (Foundation wraps in JInternalFrame)
4. **Without changing any application code**

This interface-based, container-agnostic design is less intrusive and more flexible than requiring inheritance from framework base classes. While both approaches require some integration work, Foundation's approach maintains cleaner separation between application logic and deployment concerns.

**Note:** JSR 296 compatibility is a possible future consideration but not a requirement. Foundation prioritizes its interface-first philosophy.

## Build System

This project uses Maven. The project targets Java 8.

**Common commands:**
- `mvn clean compile` - Compile the project
- `mvn clean package` - Build JAR file (output: target/foundation-1.0.1-SNAPSHOT.jar)
- `mvn clean install` - Install to local Maven repository

**Selective builds (Maven reactor):**
- `mvn install -pl bronze` - Build only the bronze module
- `mvn install -pl bronze,bronze-demos` - Build bronze and bronze-demos
- `mvn install -pl '!*-demos'` - Build all modules EXCEPT demos (excludes bronze-demos, future silver-demos, etc.)
- `mvn install -pl stone,bronze` - Build only stone and bronze tiers

**Why exclude demos?**
- Demo modules depend on logging implementations (slf4j-simple)
- Framework JARs should not bundle logging implementations
- Excluding demos speeds up builds when only framework changes are needed
- Use `-pl '!*-demos'` for CI/CD builds of framework artifacts

## Developer Workflow Preferences

**Compilation:**
- DO NOT automatically run compile commands (mvn compile, mvn package, etc.) unless explicitly requested
- The developer will handle all compilation and build tasks manually
- After making code changes, simply inform the developer that changes are complete and ready to compile

## Architecture

### Class Hierarchy (Foundation API)

The Foundation API uses a tiered inheritance hierarchy:
```
Stone (base class)
  └── Bronze
      └── Silver
          └── Gold
              └── Platinum
                  └── Foundation (public API)
```

- **Stone** (src/main/java/org/jwellman/foundation/Stone.java:35) - Core initialization logic, Look and Feel setup, and basic window/desktop management
- **Bronze** (src/main/java/org/jwellman/foundation/Bronze.java:26) - Multi-panel registry with namespace:panelId identification, lifecycle events, window positioning, and dynamic visibility management
- **Silver** (src/main/java/org/jwellman/foundation/Silver.java:26) - Preference system with JSON configuration, plugin system, view providers, and card-based UI management
- **Gold/Platinum** - Reserved for future tiered functionality (currently empty)
- **Foundation** - The singleton public API entry point

This tiered design allows for potential future expansion with different feature sets at each tier.

### Tiered Deployment Model

Foundation's tiered architecture supports a **build-once, deploy-at-complexity** model where users select the JAR matching their feature requirements:

**Tier JARs:**
- `foundation-stone.jar` - Minimal framework (single window/desktop, basic LAF)
- `foundation-bronze.jar` - Includes Stone + multi-panel registration
- `foundation-silver.jar` - Includes Bronze + preference system, plugin system, view providers
- `foundation-gold.jar` - Includes Silver + [future: advanced window management]
- `foundation-platinum.jar` - Includes Gold + [future: docking system]
- `foundation.jar` (full) - Complete feature set

**Key Principles:**

1. **Additive Only** - Upgrading tiers adds features but never breaks existing code. Going from `foundation-bronze.jar` to `foundation-silver.jar` has zero negative impact.

2. **JAR Packaging** - Each tier JAR includes all lower tiers (silver.jar contains Silver + Bronze + Stone classes). Users deploy only one JAR.

3. **Interface Stability** - Core interfaces (IWindow, uiThemeProvider, etc.) remain in Stone/Bronze to ensure all tiers support them.

4. **Feature Selection** - Applications requiring only basic windowing use Stone; complex IDE-style desktops use Gold/Platinum.

5. **Transparent Upgrading** - Code written against `foundation-bronze.jar` works identically with `foundation-platinum.jar` but gains access to additional features when needed.

**Example Use Cases:**
- Simple calculator app → `foundation-stone.jar` (minimal footprint)
- Multi-tool desktop → `foundation-bronze.jar` (panel registration)
- Plugin-based application → `foundation-silver.jar` (plugins, preferences, view management)
- IDE-style environment → `foundation-platinum.jar` (docking, advanced desktop)

This model balances simplicity for basic apps with power for complex applications, letting users pay (in JAR size/complexity) only for features they use.

**For detailed tier feature roadmap, see:** `docs/roadmap/tiered-feature-roadmap.md`

### Application Lifecycle

**IMPORTANT:** As of the Bronze tier redesign, `Foundation.init()` now **automatically displays a visible window**. This provides immediate visual feedback that the framework has initialized successfully.

**Critical Design Principle: uContext is NEVER null**
- Foundation **requires** a valid uContext object at all times
- `Foundation.init()` (no-args) creates a default uContext ("foundation.app" namespace, window mode)
- `Foundation.init(uContext)` requires a non-null context parameter
- **No null checks** - If context is null, that's a framework bug and should fail fast with NPE
- This follows the "fail fast" principle - bugs should be immediately apparent

**New Simplified Application Flow:**
1. **Initialize**: `Foundation.init()` or `Foundation.init(uContext)` - Sets up Swing environment, Look and Feel, **and displays window**
   - No-args version: Creates default context ("foundation.app", window mode)
   - Context version: Uses your provided context (must not be null)
   - In window mode: Shows an empty JFrame (ready for content)
   - In desktop mode: Shows a JFrame with empty JDesktopPane (ready for internal frames)
2. **Register Panels**: `registerUI(String namespace, String panelId, JPanel ui)` - Register panels
   - Panels registered after init() are immediately added to the visible desktop
   - In desktop mode, internal frames are created and shown automatically
3. **Interact**: Use visibility management (`showPanel()`, `hidePanel()`) and registry queries as needed

**Legacy Application Flow (deprecated):**
1. **Initialize**: `Foundation.init(uContext)` - Sets up Swing environment and Look and Feel
2. **Register UI**: `registerUI(String namespace, String panelId, JPanel ui)` - Register your JPanel-based UI with namespace and panel ID
3. **Choose Mode**: `useWindow(JPanel)` OR `useDesktop(JPanel)` - Returns IWindow abstraction (deprecated)
4. **Customize**: Modify IWindow properties (title, resizable, maximizable, etc.)
5. **Display**: `showGUI(IWindow)` - Make the UI visible (deprecated)

**Key Changes:**
- `Foundation.init()` now shows a window automatically (unless one is already visible)
- Mode (window vs desktop) must be set in `uContext` BEFORE calling `init()`
- `useWindow()` and `useDesktop()` are deprecated in favor of setting mode in context
- `showGUI()` and `launchWindow()` are deprecated since init() handles window display

### Key Abstractions

**IWindow Interface** (src/main/java/org/jwellman/foundation/swing/IWindow.java:12)
- Abstracts JFrame and JInternalFrame to provide uniform API
- Implementations: XFrame, XInternalFrame
- Allows writing code that works in both window mode and desktop mode

**uContext** (src/main/java/org/jwellman/foundation/uContext.java:13)
- Configuration object for Foundation initialization
- Controls desktop mode, window dimensions, Look and Feel, theme providers
- Created via factory method: `uContext.createContext(String namespace)`
- Each context represents a namespace (tool) and contains its own panel registry
- Hierarchical structure: Bronze → uContext (by namespace) → PanelRegistration (by panelId)

**uiDesktopProvider Interface** (src/main/java/org/jwellman/foundation/interfaces/uiDesktopProvider.java)
- Strategy interface for customizing desktop environment creation
- Follows Strategy pattern for flexible desktop configuration
- Framework provides `DefaultDesktopProvider` as the default implementation
- Applications can provide custom implementations for specialized desktop environments
- **Parameter-free methods** - Supports nested desktops (desktop within desktop)

**Desktop Provider Methods:**
```java
JDesktopPane createDesktop()              // Create and configure desktop (no params!)
JMenuBar createMenuBar()                  // Optional desktop-level menu bar
void onDesktopInitialized(JDesktopPane)   // Post-initialization hook
```

**Why No Parameters?**
- Supports nested desktops (JDesktopPane inside JInternalFrame inside another JDesktopPane)
- Doesn't couple provider to specific container type (XFrame vs IWindow)
- Clean separation of concerns - provider creates desktop, framework handles container
- If provider needs context, inject via constructor (not DI framework, just regular Java)

**Default Desktop Configuration:**
- Drag mode: `OUTLINE_DRAG_MODE` (better performance)
- Background: Light gray
- No menu bar by default
- No post-initialization actions

**Custom Desktop Provider Example (No Context):**
```java
public class MyDesktopProvider implements uiDesktopProvider {
    @Override
    public JDesktopPane createDesktop() {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        desktop.setBackground(new Color(40, 40, 60)); // Dark theme

        // Add custom border
        desktop.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return desktop;  // Framework sets as content pane
    }

    @Override
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);

        JMenu windowMenu = new JMenu("Window");
        windowMenu.add(new JMenuItem("Cascade"));
        windowMenu.add(new JMenuItem("Tile"));
        menuBar.add(windowMenu);

        return menuBar;
    }

    @Override
    public void onDesktopInitialized(JDesktopPane desktop) {
        // Add desktop icons, start background services, etc.
        System.out.println("Desktop initialized with " +
            desktop.getAllFrames().length + " frames");
    }
}

// Usage:
uContext context = uContext.createContext("myapp");
context.setDesktopMode(true);
context.setDesktopProvider(new MyDesktopProvider());
Foundation.init(context);
```

**Custom Provider with Context (Constructor Injection):**
```java
public class ContextAwareDesktopProvider implements uiDesktopProvider {
    private final IWindow containerWindow;
    private final String appName;

    // Inject context via constructor
    public ContextAwareDesktopProvider(IWindow window, String appName) {
        this.containerWindow = window;
        this.appName = appName;
    }

    @Override
    public JDesktopPane createDesktop() {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setName(appName + " Desktop");
        return desktop;
    }

    @Override
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit " + appName));
        menuBar.add(fileMenu);
        return menuBar;
    }

    @Override
    public void onDesktopInitialized(JDesktopPane desktop) {
        containerWindow.setTitle(appName + " - Desktop Ready");
    }
}

// Usage:
IWindow window = // ... obtain window reference
uContext context = uContext.createContext("myapp");
context.setDesktopMode(true);
context.setDesktopProvider(new ContextAwareDesktopProvider(window, "My App"));
```

**Benefits of Desktop Provider Pattern:**
- **Interface-based design** - Promotes flexibility and testability
- **No framework modification** - Custom desktops without changing Foundation code
- **Strategy pattern** - Runtime selection of desktop creation strategy
- **Sensible defaults** - DefaultDesktopProvider works for most applications
- **Supports nested desktops** - Parameter-free methods work with any container
- **Clean separation** - Provider creates, framework manages
- **Extensibility** - Easy to add custom backgrounds, menus, icons, services

### Look and Feel Support

**Current Implementation:**
Foundation uses `LAFDiscovery` for dynamic Look and Feel discovery and selection. The framework automatically discovers LAFs from multiple sources and applies them based on a priority system.

**LAF Selection Priority:**
1. **uContext.lookAndFeel** - LAF class name specified in code via `context.setLookAndFeel(className)`
2. **Config file** - LAF specified in `./lafs/foundation.properties` (`laf.class=com.example.MyLookAndFeel`)
3. **Directory LAFs** - First LAF found in `./lafs/` directory
4. **Nimbus (default)** - Built-in Nimbus Look and Feel (fallback if none specified)

**LAF Discovery Strategies (LAFDiscovery.java):**
The `LAFDiscovery` class discovers LAFs using multiple strategies:

1. **Built-in LAFs** - Checks `UIManager.getInstalledLookAndFeels()` for Java built-ins (Metal, Nimbus, etc.)
2. **Directory scanning** - Scans `./lafs/` folder for JAR files
3. **Classpath scanning** - Checks for well-known LAF classes on the classpath

**LAF JAR Metadata (Optional but Recommended):**
LAF JARs can include `META-INF/foundation-laf.properties`:
```properties
laf.class=com.example.MyLookAndFeel
laf.name=My Beautiful LAF
laf.description=A modern look and feel
```

If no metadata exists, Foundation scans the JAR for classes extending `LookAndFeel` (slower but works).

**Usage Example:**
```java
// Specify LAF in code
uContext context = uContext.createContext();
context.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
Foundation f = Foundation.init(context);

// Or use config file ./lafs/foundation.properties
// laf.class=javax.swing.plaf.nimbus.NimbusLookAndFeel

// Or drop LAF JAR in ./lafs/ folder (auto-discovered)
```

**Deprecated LAF Constants:**
The old hardcoded LAF constants (`LAF_NIMBUS`, `LAF_WEB`, etc.) in Stone.java are deprecated. Use LAF class names instead.

This aligns with Foundation's philosophy of interface-based, pluggable architecture.

### Preference System (Silver Tier)

**Overview:**
Silver tier introduces a comprehensive JSON-based preference system for user configuration. The system supports hierarchical preferences with global defaults and per-namespace overrides, enabling fine-grained control over application behavior.

**Configuration File Requirements (Silver+ Tier):**
- **File location:** `config/foundation.json` (relative to application root)
- **Directory requirement:** `config/` directory MUST exist
- **File requirement:** `config/foundation.json` MUST exist and be well-formed JSON
- **Validation:** If file is missing or malformed, application terminates with error dialog
- **Version:** Configuration file must specify version "1.0"

**Minimal Valid Configuration:**
```json
{
  "version": "1.0",
  "global": {}
}
```

**Full Configuration Example:**
```json
{
  "version": "1.0",
  "global": {
    "splash": {
      "maximizeOnDismiss": true
    },
    "window": {
      "rememberPositions": false
    }
  },
  "namespaces": {
    "tool.calculator": {
      "splash": {
        "maximizeOnDismiss": false
      }
    },
    "tool.editor": {
      "window": {
        "rememberPositions": true
      }
    }
  }
}
```

**uiPreferenceProvider Interface:**
The preference system is accessed via the `uiPreferenceProvider` interface, obtained through `Foundation.getPreferences()`.

**Three Lookup Strategies:**

1. **WithFallback** - Checks namespace → global → default value
   - Use when per-tool customization should fall back to global setting
   - Example: splash.maximizeOnDismiss (most tools use global, some override)

2. **Namespace** - Checks namespace → default value (no global fallback)
   - Use when preference is inherently tool-specific
   - Example: window position (each tool has its own position, no global default makes sense)

3. **Global** - Checks global → default value (no namespace override)
   - Use when preference should be application-wide
   - Example: theme.defaultLAF (consistent across all tools)

**Usage Examples:**

```java
// Get preference provider (available after Foundation.init())
uiPreferenceProvider prefs = Foundation.getPreferences();

// Fallback strategy: namespace → global → default
boolean maximize = prefs.getBooleanWithFallback(namespace, "splash.maximizeOnDismiss", true);

// Namespace-only: no global fallback (tool-specific preference)
int x = prefs.getNamespaceInt(namespace, "window.position.x", 100);
int y = prefs.getNamespaceInt(namespace, "window.position.y", 100);

// Global-only: no namespace override (application-wide setting)
String theme = prefs.getGlobalString("theme.defaultLAF", "Nimbus");

// All type variants available
boolean flag = prefs.getBooleanWithFallback(ns, "path", false);
String text = prefs.getStringWithFallback(ns, "path", "default");
int num = prefs.getIntWithFallback(ns, "path", 0);
double val = prefs.getDoubleWithFallback(ns, "path", 0.0);
```

**Path Format:**
Preferences use dot-separated paths that map to nested JSON structure:
- Path: `"splash.maximizeOnDismiss"` → JSON: `{"splash": {"maximizeOnDismiss": true}}`
- Path: `"window.position.x"` → JSON: `{"window": {"position": {"x": 100}}}`

**Architecture:**

**Interface:** `org.jwellman.foundation.interfaces.uiPreferenceProvider`
- Public API for accessing preferences
- Follows Foundation's interface-first design philosophy

**Implementation Package:** `org.jwellman.foundation.preferences`
- `PreferenceManager` - Implements uiPreferenceProvider, handles lookups
- `PreferenceLoader` - Loads and validates config/foundation.json
- `PreferenceData` - JSON data structure (Jackson-based)

**Initialization Flow:**
1. `Foundation.init(uContext)` called
2. Parent `_init()` runs (LAF, window, splash)
3. `PreferenceManager` loads config/foundation.json
4. If config invalid, shows error dialog and terminates app
5. Preferences available via `Foundation.getPreferences()`

**Built-in Preferences:**

**splash.maximizeOnDismiss** (boolean, default: true)
- Controls whether desktop frame maximizes when splash screen closes
- Only applies in desktop mode (no-op in window mode)
- Supports fallback: namespace override → global → default
- Example: Most tools want maximized desktop, but settings panel might prefer smaller window

**Design Principles:**
- **Fail fast** - Invalid config terminates app immediately (Silver+ requirement)
- **Explicit configuration** - No silent fallbacks to missing files
- **Deployment requirement** - Applications must ship with valid config/foundation.json
- **Type safety** - Preference methods return specific types (boolean, String, int, double)
- **Immutable after load** - Preferences loaded once at startup (no runtime reloading)
- **Extensible** - Easy to add new preferences without framework changes

**Separation from Plugin Registry:**
The preference system (`config/foundation.json`) is intentionally separate from the plugin registry (`config/registry.json`):
- **foundation.json** - User preferences, framework configuration (relatively static, user-edited)
- **registry.json** - Plugin lifecycle state (dynamic, programmatically updated)
- **Isolation** - Buggy plugin corrupting registry.json doesn't affect core preferences
- **Clear separation** - "What do I want?" vs "What's installed?"

**Example Deployment:**
```
myapp/
├── config/
│   ├── foundation.json    # Required for Silver+ tier
│   └── registry.json       # Optional (plugin system)
├── lafs/
│   └── foundation.properties
├── myapp.jar
└── README.md
```

### Package Structure

**Framework Modules:**
- `stone/` - Stone tier (minimal framework, no dependencies)
- `bronze/` - Bronze tier (multi-panel registry, lifecycle, SLF4J API)
- `silver/` - Silver tier (preference system, plugin system, view providers, Jackson JSON)
- `gold/`, `platinum/` - Future tier enhancements

**Demo Modules:**
- `bronze-demos/` - Bronze tier demo applications (depends on bronze + slf4j-simple)

**Framework Packages:**
- `org.jwellman.foundation` - Core Foundation API and tiered classes
- `org.jwellman.foundation.beans` - PropertyChange support utilities
- `org.jwellman.foundation.extend` - Base classes for applications (AbstractSimpleApp, AbstractSimpleMain)
- `org.jwellman.foundation.interfaces` - Provider interfaces (uiThemeProvider, uiDesktopProvider, uiPanelLifecycleListener, uiPreferenceProvider, uiPluginManager, uiViewProvider)
- `org.jwellman.foundation.swing` - Enhanced Swing components (XFrame, XPanel, XButton, etc.) and custom layouts
- `org.jwellman.foundation.utility` - Drag-and-drop and moveable component utilities
- `org.jwellman.foundation.provider` - Example provider implementations (CompanyBrandedDesktopProvider, DefaultViewProvider, etc.)
- `org.jwellman.foundation.preferences` - Preference system implementation (PreferenceManager, PreferenceLoader, PreferenceData)
- `org.jwellman.foundation.plugin` - Plugin system implementation (PluginManager, PluginRegistry, PluginRegistration)

### Desktop vs Window Mode

**Desktop Mode** (useDesktop):
- Creates a JDesktopPane with JInternalFrame(s)
- Supports multiple internal frames/panels
- Main JFrame hosts the desktop pane

**Window Mode** (useWindow):
- Single JFrame containing your JPanel
- Traditional single-window application
- Simpler for basic applications

### Bronze Tier: Multi-Window Management

The Bronze tier provides sophisticated multi-window management capabilities essential for building complex desktop environments and tools with multiple panels.

#### Panel Registry System

**Core Concept:** Panels are registered using a two-part identifier:
- **Namespace** - Tool/application identifier (e.g., "tool.calculator", "tool.editor")
- **Panel ID** - Unique identifier within the namespace (e.g., "main", "settings", "history")

This allows a single tool to have multiple windows, each uniquely identified by `namespace:panelId`.

**Registration API:**
```java
// Basic registration
XPanel panel = foundation.registerUI("tool.calculator", "main", new CalculatorPanel());

// With lifecycle listener
XPanel panel = foundation.registerUI("tool.calculator", "history",
    new HistoryPanel(),
    new PanelLifecycleListener() { /* ... */ });

// With window positioning
XPanel panel = foundation.registerUI("tool.calculator", "settings",
    new SettingsPanel(),
    WindowPosition.center());

// With both lifecycle and positioning
XPanel panel = foundation.registerUI("tool.editor", "main",
    new EditorPanel(),
    lifecycleListener,
    WindowPosition.at(100, 100, 600, 400));
```

**Key Requirements:**
- Both namespace and panelId are **required** (no defaults)
- The `namespace:panelId` combination must be unique across the entire registry
- Attempting to register a duplicate throws `IllegalArgumentException`

#### Panel Lifecycle Events

**PanelLifecycleListener Interface** (src/main/java/org/jwellman/foundation/interfaces/PanelLifecycleListener.java)

Panels can register listeners to receive notifications for key lifecycle events:

```java
public interface PanelLifecycleListener {
    void onCreate(IWindow window);   // Window container created
    void onShow(IWindow window);     // Panel made visible
    void onHide(IWindow window);     // Panel hidden
    void onClose(IWindow window);    // Panel closed/disposed
}
```

**Event Timing:**
- `onCreate()` - Fired when the JFrame or JInternalFrame is created during initialization
- `onShow()` - Fired when `showPanel()` is called (may fire multiple times)
- `onHide()` - Fired when `hidePanel()` is called (may fire multiple times)
- `onClose()` - Fired when `closePanel()` is called (typically once, before removal from registry)

**Use Cases:**
- Lazy loading expensive resources when panel is first shown
- Saving state when panel is hidden
- Cleanup when panel is closed
- Tracking panel usage analytics

#### Window Positioning

**WindowPosition Class** (src/main/java/org/jwellman/foundation/WindowPosition.java)

Provides strategies for positioning panels:

**Positioning Strategies:**
- `CASCADE` - Diagonal cascade (default for desktop mode)
- `CENTER` - Center within desktop or on screen
- `TILE` - Grid tiling (future enhancement)
- `EXPLICIT` - Specific coordinates
- `NONE` - No automatic positioning

**Factory Methods:**
```java
WindowPosition.cascade()                    // Cascade positioning
WindowPosition.center()                     // Center positioning
WindowPosition.at(x, y)                     // Explicit position
WindowPosition.at(x, y, width, height)      // Explicit position and size
```

**Behavior by Mode:**
- **Desktop mode** - Positions JInternalFrame within JDesktopPane
- **Window mode** - Positions JFrame on screen (CENTER uses `setLocationRelativeTo(null)`)

#### Dynamic Panel Management

**Visibility Control:**
```java
// Show a panel
foundation.showPanel("tool.calculator", "history");

// Hide a panel
foundation.hidePanel("tool.calculator", "history");

// Toggle visibility
foundation.togglePanel("tool.calculator", "history");

// Check visibility
boolean visible = foundation.isPanelVisible("tool.calculator", "history");
```

**Registry Queries:**
```java
// Get specific panel
XPanel panel = foundation.getPanel("tool.calculator", "main");

// Get all panels for a namespace
List<XPanel> calcPanels = foundation.getPanels("tool.calculator");

// Get all registered namespaces
List<String> namespaces = foundation.getNamespaces();

// Get panel registration metadata
PanelRegistration reg = foundation.getRegistration("tool.calculator", "main");
```

**Panel Removal:**
```java
// Close and remove a panel
foundation.closePanel("tool.calculator", "history");
// This fires onClose() event, closes the window, and removes from registry
```

#### Multi-Window Scenarios

**Scenario 1: Tool with Multiple Windows**
```java
// Calculator tool with main window and separate history window
foundation.registerUI("tool.calculator", "main", new CalculatorPanel());
foundation.registerUI("tool.calculator", "history", new HistoryPanel());
foundation.registerUI("tool.calculator", "settings", new SettingsPanel());

// User can show/hide history and settings as needed
foundation.showPanel("tool.calculator", "history");
```

**Scenario 2: Multi-Tool Desktop**
```java
// Multiple tools, each with their own panels
foundation.registerUI("tool.calculator", "main", new CalculatorPanel());
foundation.registerUI("tool.editor", "main", new EditorPanel());
foundation.registerUI("tool.editor", "findreplace", new FindReplacePanel());
foundation.registerUI("tool.browser", "main", new BrowserPanel());

// Query all editor panels
List<XPanel> editorPanels = foundation.getPanels("tool.editor"); // Returns 2
```

**Scenario 3: Dynamic Panel Management**
```java
// Control panel that manages other panels
JButton btn = new JButton("Show Calculator History");
btn.addActionListener(e -> {
    if (foundation.isPanelVisible("tool.calculator", "history")) {
        foundation.hidePanel("tool.calculator", "history");
    } else {
        foundation.showPanel("tool.calculator", "history");
    }
});
```

#### Internal Implementation

**PanelRegistration Class** (src/main/java/org/jwellman/foundation/PanelRegistration.java)
- Holds all metadata for a registered panel
- Tracks namespace, panelId, XPanel, window container, visibility state, positioning, and lifecycle listener
- Provides `getFullId()` method returning `"namespace:panelId"`

**Bronze Registry:**
- `Map<String, PanelRegistration>` keyed by `"namespace:panelId"`
- Enables O(1) lookup by composite key
- Supports efficient queries by namespace via stream filtering

#### Future Enhancements (Silver/Gold/Platinum Tiers)

The Bronze tier registry provides the foundation for future features:
- **Frame type switching** - Move panel from JInternalFrame to JFrame (detach from desktop)
- **Z-order management** - Bring panels to front, send to back
- **Window state** - Track minimized/maximized state
- **Position persistence** - Save/restore window positions across sessions
- **Docking** - Dock panels to desktop edges or to each other
- **Tabbed panels** - Multiple panels in tabbed interface

## Creating Applications

### Recommended Pattern (Bronze Tier and Above)

**NEW SIMPLIFIED PATTERN:** With Bronze tier, Foundation handles window creation and display automatically:

```java
public class MyApp extends JPanel {
    public MyApp() {
        super(new BorderLayout());
        // Build your UI here in the constructor
        add(new JLabel("My Application"), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Set up context (optional - defaults to window mode)
        uContext context = uContext.createContext();
        context.setDesktopTitle("My Application");
        // For desktop mode: context.setDesktopMode(true);

        // Initialize - this shows the window immediately
        Foundation f = Foundation.init(context);

        // Register your panel - it's added to the visible window automatically
        f.registerUI("myapp", "main", new MyApp());

        // That's it! Window is already visible with your panel
    }
}
```

**For Desktop Mode (Multi-Panel Applications):**
```java
public static void main(String[] args) {
    // Set desktop mode in context
    uContext context = uContext.createContext();
    context.setDesktopMode(true);
    context.setDesktopTitle("Multi-Tool Desktop");

    // Initialize - shows desktop with empty JDesktopPane
    Foundation f = Foundation.init(context);

    // Register panels - they appear as internal frames automatically
    f.registerUI("tool.calculator", "main", new CalculatorPanel());
    f.registerUI("tool.editor", "main", new EditorPanel());
    f.registerUI("tool.browser", "main", new BrowserPanel());

    // Desktop is visible with all three panels as internal frames
}
```

**Why this pattern?**
- `Foundation.init()` shows a window immediately - instant visual feedback
- Panels registered after init() are automatically added to the visible window
- No need for `useWindow()`, `useDesktop()`, `showGUI()`, or `launchWindow()`
- Clean, simple, minimal boilerplate

### Legacy Pattern (Stone Tier - Deprecated)

The original pattern (still supported but deprecated):

```java
public class MyApp extends JPanel {
    public MyApp() {
        super(new BorderLayout());
        add(new JLabel("My Application"), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        Foundation f = Foundation.init();
        IWindow window = f.useWindow(new MyApp());  // or useDesktop()
        window.setTitle("My Application");
        f.showGUI(window);
    }
}
```

This pattern still works but is deprecated in favor of the simplified Bronze tier pattern.

### Alternative: AbstractSimpleApp

For even less boilerplate, extend AbstractSimpleApp (src/main/java/org/jwellman/foundation/extend/AbstractSimpleApp.java:14):
```java
public class MyApp extends AbstractSimpleApp {
    @Override
    protected JPanel getMainUI() {
        // Return your JPanel UI
    }

    public static void main(String[] args) {
        new MyApp().startup(false, args); // false = desktop mode, true = window mode
    }
}
```

The startup() method handles the entire Foundation lifecycle automatically.

### Example Applications

Demo applications are located in the `bronze-demos` module (sibling to `bronze`).

**Why separate?**
- Framework JARs remain lightweight (no logging implementation bundled)
- Demos use SLF4J Simple for visible console logging output
- Follows industry best practices for API vs. examples separation

**Running demos:**
```bash
# Run default demo (BronzeTierShowcaseDemo)
mvn exec:java -pl bronze-demos

# Run specific demo
mvn exec:java -pl bronze-demos -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"
```

**Core Demos:**

**SimpleWindowDemo** - Minimal window mode example
- Basic Foundation lifecycle
- Single JPanel in a JFrame

**SimpleDesktopDemo** - Minimal desktop mode example
- Same UI as SimpleWindowDemo
- Demonstrates deployment-agnostic design

**MultiPanelDesktopDemo** - Bronze tier comprehensive demo
- Shows namespace:panelId registration (tool.calculator has "main" and "history")
- Demonstrates panel lifecycle events (onCreate, onShow, onHide, onClose)
- Shows window positioning strategies (CASCADE, CENTER, EXPLICIT)
- Includes dynamic panel management (show/hide/toggle via control panel)
- Registry queries (getPanels by namespace, getNamespaces)
- Simulates multi-tool desktop environment

**BronzeTierShowcaseDemo** - Interactive comprehensive showcase
- All Bronze tier features in one interactive demo
- Panel attributes for categorization
- Real-time event logging
- Registry statistics

**Provider Demos:**

**CustomDesktopProviderDemo** - Custom desktop provider implementation
**CustomSplashProviderDemo** - Custom splash screen provider
**SplashScreenDemo** - Window mode with splash screen
**SplashScreenDesktopDemo** - Desktop mode with splash screen

**Utility Demos:**

**LookAndFeelDemo** - LAF testing utility
- Tests all LAF integrations
- Validates LAF dependencies
- Documents LAF discovery architecture

## Dependencies

### Core Framework Dependencies

**Zeus Window Manager (Custom Fork):**
- **Location:** `C:\dev\workspaces\git\zeus\core`
- **Group ID:** `gr.zeus`
- **Artifact ID:** `core`
- **Version:** `8.0.0-SNAPSHOT`
- **Note:** Foundation uses a custom fork of Zeus maintained by the developer. This fork includes bug fixes and enhancements not present in the original Zeus library.
- **Key fixes in fork:**
  - Fixed `selectNext()`/`selectPrevious()` to properly check both `isVisible()` and `isIcon()` status when cycling through frames (invisible frames were incorrectly included in navigation)
- **Access:** If you need to examine or modify Zeus code, use the path above. The developer has permission to modify this fork.

### Look and Feel Libraries

The project depends on several Look and Feel libraries:
- **napkinlaf** (1.2) - Not in Maven Central, requires local repository
- **weblaf** (1.27)
- **nimrod** (1.2d) - Not in Maven Central, requires local repository
- **JTattoo** (1.6.11)
- **jdom** (1.1) - Required for napkinlaf
- **xstream** (1.4.7) - Required for weblaf

## Branch Strategy

- `master` - Main development branch
- `v00_00_01` - Snapshot before LAF dependency restructuring
- `v00_00_02` - Core framework development branch (merges to master when ready)
- `trinity-claude-20251129` - Current working branch

## Important Notes

- Foundation enforces singleton pattern via private constructor
- The _init() method can only be called once; subsequent calls log warnings
- Window mode vs Desktop mode decision must be made on first useWindow()/useDesktop() call
- All user interfaces should be built with JPanel, then wrapped by Foundation
- **CRITICAL:** Application code should never directly reference JFrame or JInternalFrame - always use the IWindow abstraction to maintain deployment-agnostic capability
- When adding features, prefer interfaces over concrete implementations to maintain flexibility
