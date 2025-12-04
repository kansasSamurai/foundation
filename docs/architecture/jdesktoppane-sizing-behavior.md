# JDesktopPane Sizing Behavior

**Date:** 2025-12-03
**Research:** Claude (Sonnet 4.5)
**Test File:** `src/main/java/org/jwellman/foundation/research/SimpleDesktopTest.java`

---

## Executive Summary

JDesktopPane does not calculate its preferred size based on its contents (JInternalFrames), which creates critical implications for Foundation's desktop mode implementation. When using `pack()` on containers that include a JDesktopPane, the resulting window has virtually no size, making the UI invisible.

---

## The Problem

### Observed Behavior

When building a Swing application with this hierarchy:
```
JFrame
  └── JDesktopPane (content pane)
        └── JInternalFrame
              └── JPanel (with components)
```

Calling `pack()` on the JFrame results in a tiny, nearly invisible window - typically just the window decorations with no visible content area.

### Root Cause

**JDesktopPane is a "canvas" container** that fundamentally differs from standard Swing layout containers:

1. **JDesktopPane returns minimal preferred size** - Unlike JPanel, JScrollPane, or other containers that calculate preferred size based on their children, JDesktopPane typically returns 0x0 or a very small dimension
2. **JInternalFrames are not layout participants** - Internal frames are positioned absolutely within the desktop pane, not arranged by a layout manager
3. **pack() relies on preferred sizes** - The `pack()` method sizes components based on their `getPreferredSize()` traversal up the component tree
4. **Desktop panes expect explicit sizing** - JDesktopPane is designed to be sized by its container, not to self-determine size

### Why This Design Makes Sense

JDesktopPane's sizing behavior is intentional:
- Internal frames can move freely within the desktop
- Internal frames can be resized by the user
- Multiple internal frames may overlap or be positioned anywhere
- There's no meaningful "natural size" for a canvas that can contain arbitrarily positioned windows

---

## Implications for Foundation Framework

### Desktop Mode vs Window Mode Sizing

**Window Mode (useWindow):**
```java
JFrame frame = new XFrame();
frame.setContentPane(userPanel);  // JPanel with components
frame.pack();  // ✅ WORKS - JPanel calculates preferred size correctly
frame.setVisible(true);
```

**Desktop Mode (useDesktop):**
```java
JFrame frame = new XFrame();
JDesktopPane desktop = new JDesktopPane();
JInternalFrame internal = new XInternalFrame();
internal.setContentPane(userPanel);
desktop.add(internal);
frame.setContentPane(desktop);
frame.pack();  // ❌ FAILS - JDesktopPane preferred size is minimal
frame.setVisible(true);  // Invisible or tiny window
```

### Stone.java Implementation Review

Checking the current implementation in Stone.java (lines 205-296):

**useWindow() method:**
- Likely uses explicit sizing via `uContext.getDimension()`
- May call `setSize()` instead of `pack()`

**useDesktop() method:**
- Must use explicit `setSize()` on the JFrame
- Cannot rely on `pack()` for sizing
- Should use `uContext.getDimension()` or similar explicit dimensions

---

## Solutions and Best Practices

### Solution 1: Explicit Sizing (Recommended for Foundation)

Always use explicit dimensions for desktop mode:

```java
public IWindow useDesktop(JPanel ui) {
    XFrame frame = new XFrame(context.getDesktopTitle());
    JDesktopPane desktopPane = new JDesktopPane();

    // Create internal frame with content
    XInternalFrame internalFrame = new XInternalFrame();
    internalFrame.setContentPane(ui);
    desktopPane.add(internalFrame);

    frame.setContentPane(desktopPane);

    // CRITICAL: Use explicit sizing, NOT pack()
    Dimension dim = context.getDimension();
    if (dim != null) {
        frame.setSize(dim);
    } else {
        frame.setSize(900, 500);  // Sensible default
    }

    return frame;
}
```

### Solution 2: Set Preferred Size on Desktop Pane

For cases where pack() is required:

```java
JDesktopPane desktopPane = new JDesktopPane();
desktopPane.setPreferredSize(new Dimension(900, 500));
// Now pack() will work
frame.pack();
```

**Drawback:** This defeats the purpose of `pack()` since you're hardcoding the size anyway.

### Solution 3: Calculate from Internal Frame

More sophisticated approach that sizes desktop based on internal frame:

```java
JInternalFrame internalFrame = new XInternalFrame();
internalFrame.setContentPane(userPanel);
internalFrame.pack();  // Size internal frame to its content

// Give desktop pane some margin around the internal frame
Dimension internalSize = internalFrame.getSize();
int margin = 50;
desktopPane.setPreferredSize(new Dimension(
    internalSize.width + margin,
    internalSize.height + margin
));

frame.pack();  // Now this works
```

**Drawback:** Only works well for single internal frame scenarios; multiple frames complicate this significantly.

---

## Recommendations for Foundation

### 1. Use Explicit Sizing for Desktop Mode

Desktop mode should always use explicit sizing via `uContext.getDimension()`:
- Clear and predictable behavior
- Aligns with desktop paradigm (user expects sizeable canvas)
- Avoids pack() complications

### 2. Document the Distinction

Update documentation (CLAUDE.md and examples) to clearly explain:
- Window mode can use `pack()` because JPanel calculates preferred size
- Desktop mode must use explicit sizing because JDesktopPane does not
- This is not a Framework limitation, but inherent Swing behavior

### 3. Update uContext

Ensure `uContext.getDimension()` always returns sensible defaults:
```java
public Dimension getDimension() {
    if (this.dimension == null) {
        // Desktop mode requires explicit sizing - provide sensible default
        return new Dimension(900, 500);
    }
    return this.dimension;
}
```

### 4. Validate in Examples

Ensure all desktop mode examples (`MultiPanelDesktopDemo.java`, etc.) use explicit sizing, never `pack()`.

### 5. Consider Helper Methods

Add convenience methods to Foundation API:
```java
/**
 * Sets window size using context dimensions or sensible defaults.
 * For desktop mode, explicit sizing is required (JDesktopPane does not
 * calculate preferred size from internal frames).
 */
private void applySizing(IWindow window, boolean isDesktop) {
    Dimension dim = context.getDimension();
    if (isDesktop && dim != null) {
        window.setSize(dim.width, dim.height);
    } else if (isDesktop) {
        window.setSize(900, 500);  // Desktop default
    } else {
        // Window mode can use pack() if no dimension specified
        if (dim != null) {
            window.setSize(dim.width, dim.height);
        } else {
            window.pack();
        }
    }
}
```

---

## Test Case

The test class `SimpleDesktopTest.java` demonstrates the behavior:

**Original (broken):**
```java
frame.setSize(800, 600);  // Explicit sizing - works
```

**Modified (demonstrates issue):**
```java
frame.pack();  // Results in tiny/invisible window
```

To reproduce the issue:
```bash
mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.research.SimpleDesktopTest"
```

With `pack()` commented in (line 39), the window will be nearly invisible.

---

## Conclusion

The inability of JDesktopPane to self-size based on content is fundamental Swing behavior, not a bug. Foundation's desktop mode implementation must account for this by:

1. **Always using explicit sizing** for desktop mode windows
2. **Never calling pack()** on frames containing JDesktopPane as content
3. **Providing sensible defaults** via uContext when dimensions aren't specified
4. **Documenting this distinction** clearly for users

This distinction is part of the fundamental difference between window mode (standard component hierarchy with layout managers) and desktop mode (canvas-based container with absolute positioning).

---

**Related Files:**
- `src/main/java/org/jwellman/foundation/Stone.java` - Core window/desktop creation (lines 205-296)
- `src/main/java/org/jwellman/foundation/uContext.java` - Configuration including dimensions
- `src/main/java/org/jwellman/foundation/research/SimpleDesktopTest.java` - Test case demonstrating behavior
- `docs/architecture/analysis-2025-12-02.md` - Overall architecture analysis

**References:**
- Java Swing documentation: JDesktopPane does not implement Scrollable or calculate preferred size
- Foundation design philosophy: Deployment-agnostic applications require proper abstraction over window/desktop differences
