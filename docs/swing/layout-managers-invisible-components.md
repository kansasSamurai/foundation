# Layout Managers and Invisible Components

## Overview

This document explains how Java Swing's native layout managers handle components that have been made invisible using `setVisible(false)`.

## General Behavior

**Most native Java layout managers skip invisible components** when calculating sizes and positioning components. Components with `setVisible(false)` are excluded from layout calculations and do not occupy space in the container.

## Standard Implementation Pattern

Layout managers check `Component.isVisible()` before including components in their calculations:

```java
// In preferredLayoutSize() and layoutContainer()
for (Component c : container.getComponents()) {
    if (c.isVisible()) {
        // Include component in size calculation / positioning
    }
    // Invisible components are skipped
}
```

## Native Layout Managers

### BoxLayout

- **Invisible components** (via `setVisible(false)`) are skipped and do not occupy space
- **Box.Filler components** (glue, struts, rigid areas) are transparent but NOT invisible
  - These intentionally do NOT call `setVisible(false)` so they participate in layout
  - Used specifically to create spacing between visible components

### FlowLayout, BorderLayout, GridLayout

These layout managers follow the standard pattern of checking `isVisible()` before including components in layout calculations. Invisible components do not occupy space.

### GridBagLayout

GridBagLayout also checks component visibility when calculating layout. Invisible components are skipped and their grid cells may be collapsed or used by other components depending on constraints.

## Critical: Triggering Layout Updates

**Important:** After calling `setVisible(false)` or `setVisible(true)`, you must trigger a layout update:

```java
component.setVisible(false);
container.revalidate();  // Triggers layout recalculation
container.repaint();     // Ensures visual update
```

Without `revalidate()`, the layout manager may not recalculate the layout until the next layout event, leaving blank space where the invisible component was, or not showing a newly visible component.

## User Experience Considerations

### Hiding vs Disabling Components

**setVisible(false) - Component Hidden:**
- Component does not appear visually
- Component does not occupy layout space (after revalidate)
- Cleaner UI when component is not relevant

**setEnabled(false) - Component Disabled:**
- Component still appears (usually grayed out)
- Component still occupies layout space
- Useful for showing that an action exists but is temporarily unavailable

**Use Case Example (Splash Screen):**
```java
// Dismiss button - hidden until initialization completes
dismissButton.setVisible(false);  // Don't distract user during loading

// Later, when ready...
dismissButton.setVisible(true);
dismissButton.requestFocusInWindow();
progressPanel.revalidate();  // Trigger layout update
progressPanel.repaint();
```

Hiding the button (vs disabling) provides a better user experience by not distracting the user with a grayed-out control while they're reading progress messages.

## Box.Filler vs setVisible(false)

These are fundamentally different concepts:

**Box.Filler (intentional spacing):**
- Transparent components designed to create space
- Always visible (never call setVisible(false) on them)
- Used for glue, struts, rigid areas

**setVisible(false) (conditional hiding):**
- Hides functional components that may be shown later
- Component removed from layout calculations
- Used for dynamic UI where components appear/disappear based on state

## Best Practices

1. **Always call revalidate() after visibility changes**
   ```java
   component.setVisible(false);
   container.revalidate();
   container.repaint();
   ```

2. **Choose visibility vs enabled based on UX intent**
   - Hide: Component is not relevant in current state
   - Disable: Component exists but is temporarily unavailable

3. **Use Box.Filler for intentional spacing**
   - Don't use invisible components for spacing
   - Use `Box.createRigidArea()`, `Box.createHorizontalGlue()`, etc.

4. **Test layout behavior with your specific layout manager**
   - While most managers skip invisible components, custom layout managers may vary
   - Always test visibility changes to ensure expected behavior

## References

- [How to Use BoxLayout - Oracle Java Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html)
- [Java LayoutManager Tutorial](http://www.java2s.com/Tutorials/Java/java.awt/LayoutManager/Java_LayoutManager_preferredLayoutSize_Container_parent_.htm)
- [Practical Layout Managers Article](http://www.claudeduguay.com/articles/layout/LayoutManagerArticle.html)
- [Creating a Custom Layout Manager - Oracle](https://docs.oracle.com/javase/tutorial/uiswing/layout/custom.html)

## Future Enhancements

### Splash Screen Logging (Silver Tier - Future)

A future Silver tier enhancement will provide standardized logging output in splash screens:
- Real-time display of initialization logging messages
- Secondary progress indicator beyond progress bar
- Copy-to-clipboard functionality for troubleshooting/support
- Configurable log level filtering
- Auto-scroll to latest messages

This will make it easier for splash provider implementations to display detailed progress information without custom implementation.
