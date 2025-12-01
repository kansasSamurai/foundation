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
- **Bronze** (src/main/java/org/jwellman/foundation/Bronze.java:13) - UI registration and multi-panel management
- **Silver/Gold/Platinum** - Reserved for future tiered functionality (currently empty)
- **Foundation** - The singleton public API entry point

This tiered design allows for potential future expansion with different feature sets at each tier.

### Application Lifecycle

Standard Foundation application flow:
1. **Initialize**: `Foundation.init(uContext)` - Sets up Swing environment and Look and Feel
2. **Register UI**: `registerUI(String name, JPanel ui)` - Register your JPanel-based UI
3. **Choose Mode**: `useWindow(JPanel)` OR `useDesktop(JPanel)` - Returns IWindow abstraction
4. **Customize**: Modify IWindow properties (title, resizable, maximizable, etc.)
5. **Display**: `showGUI(IWindow)` - Make the UI visible

### Key Abstractions

**IWindow Interface** (src/main/java/org/jwellman/foundation/swing/IWindow.java:12)
- Abstracts JFrame and JInternalFrame to provide uniform API
- Implementations: XFrame, XInternalFrame
- Allows writing code that works in both window mode and desktop mode

**uContext** (src/main/java/org/jwellman/foundation/uContext.java:13)
- Configuration object for Foundation initialization
- Controls desktop mode, window dimensions, Look and Feel, theme providers
- Created via factory method: `uContext.createContext()`

### Look and Feel Support

**Current Implementation (Temporary):**
Foundation currently supports multiple Look and Feel options via hardcoded LAF constants in Stone.java:
- `LAF_NIMBUS` (1)
- `LAF_WEB` (2) - WebLAF
- `LAF_NAPKIN` (3) - NapkinLAF
- `LAF_SYSTEM` (4)
- `LAF_NIMROD` (5)
- `LAF_JTATTOO` (6)
- `LAF_DARCULA` (7)

**Future Architecture Goal:**
The hardcoded LAF approach is temporary. The planned architecture will support:
- **Dynamic LAF discovery** - LAF JARs placed in a predefined folder (e.g., `./lafs/`) are auto-detected at startup
- **Runtime LAF selection** - Users can specify/change LAF without recompiling
- **User preference persistence** - Selected LAF is saved and restored across sessions
- **Multiple LAF support** - If multiple LAFs detected, prompt user for selection
- **No recompilation required** - LAFs added by dropping JARs, not code changes

This aligns with Foundation's philosophy of interface-based, pluggable architecture. When implementing LAF features, design with this future direction in mind.

### Package Structure

- `org.jwellman.foundation` - Core Foundation API and tiered classes
- `org.jwellman.foundation.beans` - PropertyChange support utilities
- `org.jwellman.foundation.extend` - Base classes for applications (AbstractSimpleApp, AbstractSimpleMain)
- `org.jwellman.foundation.interfaces` - Provider interfaces (uiThemeProvider, uiDesktopProvider)
- `org.jwellman.foundation.swing` - Enhanced Swing components (XFrame, XPanel, XButton, etc.) and custom layouts
- `org.jwellman.foundation.utility` - Drag-and-drop and moveable component utilities

### Desktop vs Window Mode

**Desktop Mode** (useDesktop):
- Creates a JDesktopPane with JInternalFrame(s)
- Supports multiple internal frames/panels
- Main JFrame hosts the desktop pane

**Window Mode** (useWindow):
- Single JFrame containing your JPanel
- Traditional single-window application
- Simpler for basic applications

## Creating Applications

### Quick Start with AbstractSimpleApp

Extend AbstractSimpleApp (src/main/java/org/jwellman/foundation/extend/AbstractSimpleApp.java:14) to bootstrap simple single-panel applications:
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

The `org.jwellman.foundation.examples` package contains working demonstrations:

**SimpleWindowDemo.java** - Minimal window mode example
- Basic Foundation lifecycle
- Single JPanel in a JFrame
- Run: `mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"`

**SimpleDesktopDemo.java** - Minimal desktop mode example
- Same UI as SimpleWindowDemo
- Demonstrates deployment-agnostic design
- Run: `mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SimpleDesktopDemo"`

**MultiPanelDesktopDemo.java** - Multiple panels in desktop
- Shows registerUI() with multiple panels
- Simulates multi-tool desktop environment
- Demonstrates Bronze tier functionality
- Run: `mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.MultiPanelDesktopDemo"`

**LookAndFeelDemo.java** - LAF testing utility
- Tests all 7 LAF integrations
- Validates LAF dependencies
- Documents future dynamic LAF architecture
- Run: `mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.LookAndFeelDemo"`

## Dependencies

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
