# Bronze Tier Test Cases

This document captures test cases that validate Bronze tier functionality, API consistency, and proper behavior across different deployment modes.

## Purpose

- Document expected behavior for manual testing
- Capture regression test scenarios
- Validate API abstraction effectiveness
- Record "it just works" scenarios that demonstrate API maturity

## Test Environment

**Primary Test Application:** `BronzeTierShowcaseDemo`
- Location: `bronze-demos/src/main/java/org/jwellman/foundation/examples/BronzeTierShowcaseDemo.java`
- Run with: `mvn exec:java -pl bronze-demos`

---

## Panel Visibility Management Across Frame Types

**Test Case ID:** BRONZE-001
**Feature:** Panel visibility management (show/hide/toggle)
**Significance:** Validates that IWindow abstraction works transparently across JFrame and JInternalFrame

### Description

Panel visibility operations (show, hide, toggle) should work identically whether a panel is currently in an internal frame (desktop mode) or external frame (detached to standalone window). This demonstrates proper abstraction through the `IWindow` interface and `PanelRegistration.show()/hide()` methods.

### Test Steps

1. Launch `BronzeTierShowcaseDemo`
2. Create a dynamic panel using "Create Panel with Selected Strategy"
3. **Test with Internal Frame:**
   - Verify panel is visible in internal frame within desktop
   - Click "Hide All Dynamic Panels" from control panel
   - Verify panel becomes invisible (internal frame hidden)
   - Click "Show All Panels" from control panel
   - Verify panel becomes visible again
4. **Test with External Frame:**
   - Click "Detach/Attach" button on the panel
   - Verify panel moves to standalone external frame (JFrame)
   - Click "Hide All Dynamic Panels" from control panel
   - Verify panel becomes invisible (external frame hidden)
   - Click "Show All Panels" from control panel
   - Verify panel becomes visible again in its standalone window
5. **Test Panel List Toggle:**
   - Click panel entry in "Panel Registry" list (control panel, section 3)
   - Verify panel visibility toggles (works in both internal and external frame modes)

### Expected Results

✅ Hide/show/toggle operations work identically for both internal frames and external frames
✅ No special handling required in application code for different frame types
✅ `PanelRegistration.show()` and `hide()` properly delegate to the correct window type
✅ Panel visibility state is correctly tracked via `PanelRegistration.isVisible()`
✅ Registry statistics correctly reflect visible/hidden counts regardless of frame type

### Why This Matters

This validates that:
- The `IWindow` interface successfully abstracts JFrame vs JInternalFrame differences
- The Bronze tier panel management API is deployment-agnostic
- Developers can write panel management code once and it works across all deployment modes
- The detach/attach feature integrates seamlessly with existing visibility management

### Code Zero Test

**No code changes were required** to support hide/show operations after implementing the detach/attach feature. The existing visibility management code worked immediately with external frames, demonstrating mature API design.

---

## Panel Detach/Attach Operations

**Test Case ID:** BRONZE-002
**Feature:** Panel detach/attach (toggle between internal and external frames)
**Significance:** Validates frame type switching and state preservation

### Description

Panels can be dynamically moved between internal frames (embedded in desktop) and external frames (standalone windows) while preserving window state (size, position, visibility).

### Test Steps

1. Launch `BronzeTierShowcaseDemo`
2. Create a dynamic panel with specific positioning (e.g., EXPLICIT with size 200,150,400,300)
3. Note the panel's position and size
4. **Detach to External Frame:**
   - Click "Detach/Attach" button on the panel
   - Verify panel moves to standalone window (JFrame)
   - Verify window appears at approximately same screen position (desktop coords → screen coords)
   - Verify window size is preserved
   - Verify panel content is intact and functional
5. **Move and Resize External Frame:**
   - Move the standalone window to a different screen location
   - Resize the window
6. **Attach Back to Desktop:**
   - Click "Detach/Attach" button again
   - Verify panel moves back to internal frame in desktop
   - Verify position is preserved relative to desktop (screen coords → desktop coords)
   - Verify size is preserved
   - Verify panel content is still intact and functional
7. **Repeat Multiple Times:**
   - Perform detach/attach cycle 3-5 times
   - Verify operation remains stable and consistent

### Expected Results

✅ Panel moves seamlessly between internal and external frames
✅ Window size is preserved during transitions
✅ Window position is preserved (with coordinate space translation)
✅ Visibility state is preserved
✅ Panel content remains intact (no re-initialization needed)
✅ All panel controls remain functional after transition
✅ Multiple detach/attach cycles work without degradation
✅ Panel's "Detach/Attach" button continues working regardless of current frame type

### Why This Matters

This demonstrates:
- Robust container switching capability
- Proper state management during transitions
- Foundation for Silver tier features (docking, multi-monitor support)
- IDE-like window management behavior

---

## Window Positioning Strategies

**Test Case ID:** BRONZE-003
**Feature:** Window positioning (CASCADE, CENTER, EXPLICIT)
**Significance:** Validates WindowPosition strategies work correctly in desktop mode

### Description

Panels can be created with different positioning strategies that control initial placement within the desktop.

### Test Steps

1. Launch `BronzeTierShowcaseDemo`
2. **Test CASCADE positioning:**
   - Select "CASCADE (diagonal offset)" radio button
   - Click "Create Panel with Selected Strategy" 3 times
   - Verify panels appear in diagonal cascade pattern
   - Verify each subsequent panel offsets from previous
3. **Test CENTER positioning:**
   - Select "CENTER (centered)" radio button
   - Click "Create Panel with Selected Strategy"
   - Verify panel appears centered in desktop pane
4. **Test EXPLICIT positioning (position only):**
   - Select "EXPLICIT (100,100)" radio button
   - Click "Create Panel with Selected Strategy"
   - Verify panel appears at position (100, 100) in desktop
5. **Test EXPLICIT positioning (position + size):**
   - Select "EXPLICIT (with size 200,150,400,300)" radio button
   - Click "Create Panel with Selected Strategy"
   - Verify panel appears at position (200, 150) with size 400x300

### Expected Results

✅ CASCADE creates diagonal offset pattern
✅ CENTER places panel in center of desktop
✅ EXPLICIT(x,y) positions panel at specified coordinates
✅ EXPLICIT(x,y,w,h) positions and sizes panel as specified
✅ Positioning is consistent and repeatable

---

## Panel Lifecycle Events

**Test Case ID:** BRONZE-004
**Feature:** Panel lifecycle listener (onCreate, onShow, onHide, onClose)
**Significance:** Validates lifecycle events fire correctly

### Description

Panels can register lifecycle listeners that receive notifications for key events (creation, show, hide, close).

### Test Steps

1. Launch `BronzeTierShowcaseDemo`
2. Open Event Log panel (right side, top)
3. **Test onCreate event:**
   - Click "Create Panel with Selected Strategy"
   - Verify "onCreate" event appears in event log for the new panel
4. **Test onShow event:**
   - Hide a panel using "Hide All Dynamic Panels"
   - Click "Show All Panels"
   - Verify "onShow" event appears in event log
5. **Test onHide event:**
   - Click "Hide All Dynamic Panels"
   - Verify "onHide" events appear for each hidden panel
6. **Test onClose event:**
   - Create a panel
   - Click "Close This Panel" button on the panel
   - Verify "onClose" event appears in event log
7. **Test with Detached Panels:**
   - Create a panel and detach it to external frame
   - Perform hide/show operations
   - Verify events still fire correctly for detached panel

### Expected Results

✅ onCreate fires when panel's window container is first created
✅ onShow fires when panel becomes visible
✅ onHide fires when panel becomes hidden
✅ onClose fires when panel is closed and removed from registry
✅ Events fire in correct order: onCreate → onShow → [onHide/onShow]* → onClose
✅ Events fire correctly for both internal and external frames
✅ Event log shows timestamp, event type, and panel identification

---

## Registry Statistics and Queries

**Test Case ID:** BRONZE-005
**Feature:** Panel registry queries (getPanels, getRegistrations, getNamespaces)
**Significance:** Validates registry introspection capabilities

### Description

The Bronze tier maintains a registry of all panels organized by namespace, supporting queries and statistics.

### Test Steps

1. Launch `BronzeTierShowcaseDemo`
2. Open Registry Statistics panel (right side, bottom)
3. **Test Initial State:**
   - Verify shows 3 permanent panels (control, eventlog, stats)
   - Verify all shown as visible
4. **Test Panel Creation:**
   - Create 3 dynamic panels
   - Click "Refresh Statistics"
   - Verify total panel count increases to 6
   - Verify all panels listed with correct visibility state
5. **Test Visibility Tracking:**
   - Hide all dynamic panels
   - Click "Refresh Statistics"
   - Verify visible count decreases
   - Verify hidden count increases
   - Verify individual panel states show [HIDDEN]
6. **Test Panel Removal:**
   - Close a dynamic panel
   - Click "Refresh Statistics"
   - Verify total count decreases
   - Verify closed panel no longer appears in list
7. **Test Panel List (Section 3):**
   - Verify all panels show with visibility indicator (● visible, ○ hidden)
   - Verify list updates in real-time as panels are created/shown/hidden/closed

### Expected Results

✅ Registry accurately tracks all panels in namespace
✅ Visible/hidden counts are accurate
✅ Panel list shows real-time status
✅ Closed panels are removed from registry
✅ Statistics update correctly after all operations
✅ Panel attributes (permanent vs dynamic) are correctly maintained

---

## Notes for Future Test Cases

### Areas to Document

- Multi-panel namespace management (tool.calculator:main, tool.calculator:history, etc.)
- Panel attributes system (permanent, dynamic, custom attributes)
- Window mode vs desktop mode behavior differences
- Splash screen integration with Bronze tier
- Edge cases (closing master panel, disposing desktop, etc.)

### Testing Best Practices

- Always test with both internal frames and external frames
- Verify state preservation across operations
- Check event log for correct lifecycle event sequencing
- Validate registry statistics after each operation
- Test with multiple panels simultaneously
- Test edge cases (rapid clicking, repeated operations, etc.)

---

## Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2024-12-26 | 1.0 | Initial test cases document created |
| | | - BRONZE-001: Panel visibility across frame types |
| | | - BRONZE-002: Panel detach/attach operations |
| | | - BRONZE-003: Window positioning strategies |
| | | - BRONZE-004: Panel lifecycle events |
| | | - BRONZE-005: Registry statistics and queries |
