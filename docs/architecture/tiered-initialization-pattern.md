# Tiered Polymorphic Initialization Pattern

## Overview

Foundation uses a **strict polymorphic initialization chain** across all tiers. This document explains the critical design pattern that MUST be followed when adding features to any tier.

## The Pattern

### Core Principle: Always Call Super First

Every tier's `_init()` method MUST:
1. Call `super._init(c)` **FIRST**
2. Perform tier-specific initialization
3. Return the context

```java
// CORRECT - Every tier follows this pattern
@Override
protected uiContext _init(uiContext c) {
    // ALWAYS call parent first
    super._init(c);

    // Then do tier-specific work
    // ...

    return c;
}
```

### Why This Matters

The initialization chain ensures:
1. **Stone initializes Look and Feel FIRST** - Before ANY Swing components are created
2. **Each tier builds on previous tiers** - Bronze needs Stone, Silver needs Bronze, etc.
3. **Predictable initialization order** - Always Stone → Bronze → Silver → Gold → Platinum

### The Complete Chain

```
Foundation.init(context)
  └─> Platinum._init(c)
       └─> Gold._init(c)
            └─> Silver._init(c)
                 └─> Bronze._init(c)
                      └─> Stone._init(c)
                           • Initialize LAF ← CRITICAL: Must happen first
                           • Create external frame
                           • Call afterLookAndFeelInitialization() hook
                           • Display window/splash
                      ← Bronze-specific initialization
                 ← Silver-specific initialization
            ← Gold-specific initialization
       ← Platinum-specific initialization
```

## Hook Methods

### Purpose

Hook methods allow tiers to inject behavior at specific points in the initialization and launch sequences **without breaking the chain** and **without contaminating lower tiers with upper tier concepts**.

**Critical Design Principle:** Lower tiers must NEVER reference concepts from upper tiers. Hook methods solve this by providing injection points where upper tiers can add behavior without modifying lower tier code.

### Pattern for Hook Methods

Hook methods follow the SAME pattern as `_init()`:

```java
// CORRECT - Hook method pattern
@Override
protected void someHook() {
    // ALWAYS call parent first
    super.someHook();

    // Then do tier-specific work
    // ...
}
```

### Example 1: afterLookAndFeelInitialization()

This hook is called AFTER LAF initialization but BEFORE window display.

**Stone (base implementation):**
```java
protected void afterLookAndFeelInitialization() {
    // Empty in Stone - higher tiers can override
}
```

**Bronze (pass-through):**
```java
@Override
protected void afterLookAndFeelInitialization() {
    super.afterLookAndFeelInitialization(); // Call Stone
    // Bronze has no specific logic here (yet)
}
```

**Silver (adds view provider):**
```java
@Override
protected void afterLookAndFeelInitialization() {
    super.afterLookAndFeelInitialization(); // Call Bronze → Stone

    // Create view provider AFTER LAF but BEFORE window display
    if (masterContext.getViewProvider() == null) {
        masterContext.setViewProvider(new DefaultViewProvider());
    }
}
```

### Example 2: prepareLaunch()

This hook is called during launch BEFORE the main window is displayed. It demonstrates how to avoid contaminating lower tiers with upper tier concepts.

**Problem it solves:** Menu bar attachment needs to be synchronized with card switching (a Silver tier feature). If we put `uiViewProvider` references in Stone._launch(), Stone tier becomes dependent on Silver tier concepts and cannot be backported.

**Solution:** Hook method in Stone that Silver overrides.

**Stone (base implementation):**
```java
protected void prepareLaunch() {
    // Empty in Stone - higher tiers can override
}

protected void _launch(uiContext ctx) {
    prepareLaunch(); // Hook for upper tiers
    _initializeAndShowWindow(ctx);
}
```

**Bronze (pass-through):**
```java
@Override
protected void prepareLaunch() {
    super.prepareLaunch(); // Call Stone
    // Bronze has no specific logic here (yet)
}
```

**Silver (registers card listener):**
```java
@Override
protected void prepareLaunch() {
    super.prepareLaunch(); // Call Bronze → Stone

    // Register listener to attach menu bar when "main" card is shown
    uiViewProvider viewProvider = masterContext.getViewProvider();
    if (viewProvider != null && isDesktop()) {
        viewProvider.addCardListener("main", () -> {
            attachMenuBarToExternalFrame();
        });
    }
}
```

**Benefits:**
- Stone tier has NO knowledge of `uiViewProvider` (can be backported to stone project)
- Bronze tier has NO knowledge of `uiViewProvider` (can be backported to bronze project)
- Silver tier adds the behavior where it belongs
- Polymorphic chain is maintained

## Critical Timing Requirements

### LAF Initialization Timing

**RULE:** Look and Feel MUST be initialized before ANY Swing components are created.

**Why:** Swing components inherit styling from the current LAF. Creating components before LAF initialization results in incorrect styling that cannot be fixed later.

**Where:** Stone._init() line ~142 initializes LAF

**Enforcement:**
- View provider creation uses `afterLookAndFeelInitialization()` hook (called AFTER LAF init)
- Custom desktop providers should create components in `createDesktop()` (called AFTER LAF init)
- Menu bars created during LAUNCH phase (AFTER LAF init)

### Initialization vs Launch

Foundation has TWO phases:

**INIT Phase (`_init()`):**
- Initialize Look and Feel
- Create external frame (empty)
- Show splash screen (if splash provider exists)
- Returns immediately

**LAUNCH Phase (`_launch()`):**
- Close splash (if auto-dismiss)
- Attach menu bar
- Show master panel
- Application is fully visible and interactive

**Why separate?** This allows splash screen to show BEFORE heavy initialization work (plugin loading, panel creation, etc.).

## Common Mistakes

### ❌ WRONG: Calling super at the end

```java
@Override
protected uiContext _init(uiContext c) {
    // Creating Swing components BEFORE super._init()
    JPanel panel = new JPanel(); // ← LAF NOT INITIALIZED YET!

    super._init(c); // ← TOO LATE!
    return c;
}
```

**Problem:** Components created before LAF initialization will have wrong styling.

### ❌ WRONG: Not calling super at all

```java
@Override
protected uiContext _init(uiContext c) {
    // Do tier-specific work
    // ...

    // Missing super._init(c) !
    return c;
}
```

**Problem:** Breaks the entire initialization chain. Parent tiers never initialize.

### ❌ WRONG: Not implementing hook in intermediate tier

```java
// Bronze.java
// Missing afterLookAndFeelInitialization() implementation!

// Silver.java
@Override
protected void afterLookAndFeelInitialization() {
    super.afterLookAndFeelInitialization(); // ← Calls Stone, skips Bronze!
    // ...
}
```

**Problem:** Breaks polymorphic chain. If Bronze later needs the hook, it won't work correctly.

### ✅ CORRECT: Always implement hooks, even if empty

```java
// Bronze.java
@Override
protected void afterLookAndFeelInitialization() {
    super.afterLookAndFeelInitialization(); // Call Stone
    // Bronze has no specific logic here (yet)
    // But hook is present for future use and maintains chain
}

// Silver.java
@Override
protected void afterLookAndFeelInitialization() {
    super.afterLookAndFeelInitialization(); // Calls Bronze → Stone
    // Silver-specific logic
}
```

## Adding New Tiers (Gold, Platinum)

When implementing Gold or Platinum tiers:

1. **Always implement `_init()`:**
   ```java
   @Override
   protected uiContext _init(uiContext c) {
       super._init(c); // FIRST
       // Gold/Platinum-specific init
       return c;
   }
   ```

2. **Always implement ALL hook methods** (even if empty):
   ```java
   @Override
   protected void afterLookAndFeelInitialization() {
       super.afterLookAndFeelInitialization(); // FIRST
       // Gold/Platinum-specific hook logic
   }
   ```

3. **Document any new hooks** you create in this file

## Checklist for New Features

When adding a new feature to any tier:

- [ ] Does it create Swing components? → Use hook AFTER LAF initialization
- [ ] Does it need initialization? → Add to tier's `_init()` method
- [ ] Did you call `super._init(c)` FIRST?
- [ ] Did you call `super.hookMethod()` FIRST in all hooks?
- [ ] Did you implement hooks in ALL intermediate tiers (even if empty)?
- [ ] Did you return the context from `_init()`?

## Summary

**Golden Rule:** ALWAYS call `super` FIRST, then do your work.

This applies to:
- `_init()` methods
- `_launch()` methods
- All hook methods (`afterLookAndFeelInitialization()`, `prepareLaunch()`, etc.)
- Any polymorphic method in the tier hierarchy

**Critical Design Principle:** Lower tiers must NEVER reference concepts from upper tiers. Use hook methods to inject upper tier behavior without contaminating lower tier code. This ensures all tiers remain independently backportable.

When in doubt, follow the pattern in existing code. Every tier follows this pattern without exception.
