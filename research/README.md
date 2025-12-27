# Foundation Framework - Research

This module contains research, experimentation, and test code used during Foundation framework development.

## Purpose

The research module is:
- **Independent** - No dependencies on Foundation API
- **Pure Swing** - Uses only standard Java Swing components
- **Exploratory** - For investigating Swing behavior, testing assumptions, and prototyping

## Why Separate?

Research code is separated from tier modules because:
1. **No API coupling** - Research explores Swing behavior independent of Foundation abstractions
2. **Clean tier hierarchy** - Tier modules (Stone, Bronze, etc.) contain only production framework code
3. **Clear purpose** - Research is for development/investigation, not end-user consumption
4. **Documentation co-location** - Research findings can be documented alongside test code

## Running Research Tests

Run the default test:
```bash
mvn compile exec:java -pl research
```

Run a specific test:
```bash
mvn compile exec:java -pl research -Dexec.mainClass="org.jwellman.foundation.research.SimpleDesktopTest"
```

## Contents

### Test Files

- **SimpleDesktopTest.java** - Pure Swing test for JDesktopPane sizing behavior

### Documentation

- **jdesktoppane-sizing-behavior.md** - Research findings on JDesktopPane preferred size calculation

## Adding Research

When adding new research:
1. Create test class in `src/main/java/org/jwellman/foundation/research/`
2. Use only standard Java/Swing APIs (no Foundation dependencies)
3. Document findings in `docs/` directory
4. Reference test file in documentation

## Notes

- This module is not included in Foundation framework distributions
- Research findings may inform framework design decisions
- Test code here may be refactored into proper demos if useful for end users
