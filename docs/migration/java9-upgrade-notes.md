# Java 9+ Migration Notes

This document tracks all Java 8-specific workarounds and compatibility issues that need to be addressed when upgrading Foundation to Java 9+.

## Overview

Foundation currently targets Java 8 to maintain broad compatibility. This document catalogs technical debt and workarounds required for Java 8 compatibility that should be reviewed and potentially removed when upgrading to Java 9+.

## Search Strategy

All Java 8-specific workarounds in the codebase are marked with the comment tag:
```
// JAVA8-COMPAT: <description>
```

To find all marked sections:
```bash
# Search all tiers
grep -r "JAVA8-COMPAT:" --include="*.java" stone/ bronze/ silver/ gold/ platinum/

# Search specific tier
grep -r "JAVA8-COMPAT:" --include="*.java" stone/
```

## Known Compatibility Issues

### 1. Module Descriptor Support (module-info.class)

**Issue Date:** 2025-12-31

**Affected Files:**
- `stone/src/main/java/org/jwellman/foundation/framework/LAFDiscovery.java`
- `bronze/src/main/java/org/jwellman/foundation/framework/LAFDiscovery.java`
- `silver/src/main/java/org/jwellman/foundation/framework/LAFDiscovery.java`

**Problem:**
When scanning Look and Feel JARs, some LAF libraries include `module-info.class` files compiled with Java 9+. When running on Java 8, attempting to load these classes throws `UnsupportedClassVersionError`.

**Current Workaround:**
1. **Explicit filtering** in `scanJarForLAFs()`: Skip `module-info` and `*.module-info` classes before attempting to load them (performance optimization)
2. **Exception handling**: Catch `UnsupportedClassVersionError` for any other Java 9+ classes encountered during class scanning
3. **Exception handling** in `discoverClasspathLAFs()`: Catch `UnsupportedClassVersionError` when checking for known LAF classes

**Code Locations:**
- `LAFDiscovery.scanJarForLAFs()` - Lines ~222-229 (filter) and ~245-251 (exception handler)
- `LAFDiscovery.discoverClasspathLAFs()` - Lines ~296-302 (exception handler)

**Migration Strategy (Java 9+):**
- **Option 1 (Recommended):** Keep the explicit filter for performance (module-info is never a LAF class), but remove or update comments
- **Option 2:** Remove both the filter and `UnsupportedClassVersionError` handlers if Java 9+ is the minimum required version
- **Option 3:** Use `--release` flag during compilation to maintain Java 8 compatibility even when building with newer JDK

**Testing Requirements:**
- Test LAF discovery with multi-release JARs (JARs with `META-INF/versions/` for different Java versions)
- Verify module system compatibility if Foundation itself becomes a module
- Test with both modular and non-modular LAF JARs

**References:**
- [JEP 238: Multi-Release JAR Files](https://openjdk.java.net/jeps/238)
- [JEP 261: Module System](https://openjdk.java.net/jeps/261)

---

## Migration Checklist

When upgrading Foundation to Java 9+, review each item:

- [ ] Review all `JAVA8-COMPAT:` markers in codebase
- [ ] Decide minimum Java version (9, 11 LTS, 17 LTS, 21 LTS, etc.)
- [ ] Test with multi-release JARs
- [ ] Consider modularizing Foundation itself (`module-info.java`)
- [ ] Update Maven compiler plugin configuration
- [ ] Update all documentation (README, CLAUDE.md, etc.)
- [ ] Update CI/CD build configurations
- [ ] Test all LAF integrations with new Java version
- [ ] Review deprecated API usage (if upgrading to Java 11+)
- [ ] Update dependency versions if needed
- [ ] Run full test suite on new Java version

## Future Compatibility Concerns

### Potential Benefits of Java 9+

- **Module system:** Better encapsulation and clearer dependencies
- **Multi-release JARs:** Ship single JAR with optimized code for different Java versions
- **JShell:** Interactive testing during development
- **Improved process API:** Better handling of external processes
- **Collection factory methods:** `List.of()`, `Map.of()`, etc. (cleaner code)
- **var keyword (Java 10+):** Local variable type inference
- **Pattern matching (Java 14+):** Cleaner instanceof checks
- **Records (Java 14+):** Simpler immutable data classes (useful for LAFInfo, PanelRegistration, etc.)
- **Sealed classes (Java 17+):** Better control over inheritance hierarchy

### Compatibility Risks

- **Breaking changes:** Some deprecated APIs removed in Java 11+
- **Third-party dependencies:** LAF libraries may not support newer Java versions
- **Build tool compatibility:** Older Maven plugins may need updates
- **IDE support:** Ensure IntelliJ/Eclipse support target Java version

---

## Document History

| Date       | Author | Change |
|------------|--------|--------|
| 2025-12-31 | System | Initial creation - documented module-info workaround |

