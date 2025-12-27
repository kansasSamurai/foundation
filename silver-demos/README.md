# Foundation Silver Tier - Demo Applications

This module contains demo applications showcasing the capabilities of the Foundation Silver tier framework.

## About

Silver tier demos are separated from the framework JAR to:
- Keep the framework lightweight (no logging implementation bundled)
- Allow demos to use SLF4J Simple for visible logging output
- Follow industry best practices for API vs. examples separation

## Running Demos

### Using Maven Exec Plugin

Run the default demo (BronzeTierShowcaseDemo):
```bash
mvn exec:java -pl silver-demos
```

Run a specific demo:
```bash
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.SimpleDesktopDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.MultiPanelDesktopDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.BronzeTierShowcaseDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.CustomDesktopProviderDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.CustomSplashProviderDemo"
mvn exec:java -pl silver-demos -Dexec.mainClass="org.jwellman.foundation.examples.LookAndFeelDemo"
```

### From IDE

Import the `silver-demos` Maven module into your IDE and run any demo's `main()` method directly.

## Available Demos

### Core Demos
- **SimpleWindowDemo** - Minimal window mode example
- **SimpleDesktopDemo** - Minimal desktop mode example
- **MultiPanelDesktopDemo** - Multi-tool desktop with lifecycle events
- **BronzeTierShowcaseDemo** - Comprehensive interactive showcase (inherited from Bronze tier)

### Provider Demos
- **CustomDesktopProviderDemo** - Custom desktop provider implementation
- **CustomSplashProviderDemo** - Custom splash screen provider
- **SplashScreenDemo** - Window mode with splash screen
- **SplashScreenDesktopDemo** - Desktop mode with splash screen

### Utility Demos
- **LookAndFeelDemo** - LAF testing and discovery utility

## Logging

These demos use SLF4J Simple as the logging implementation. Log output will appear in the console showing framework initialization, panel lifecycle events, and other diagnostic information.

To use a different logging implementation (Logback, Log4j2, etc.), replace `slf4j-simple` in `pom.xml`.

## Dependencies

- **foundation-silver** - The Silver tier framework (includes Bronze tier features)
- **slf4j-simple** - Simple logging implementation for demo output

## Notes

Silver tier demos inherit all Bronze tier demos and showcase Silver tier enhancements. As Silver tier features are developed, new demos will be added to this module.
