# Foundation
A micro framework for creating Java Swing applications.

## Quick Start

**Build the framework:**
```bash
mvn clean install
```

**Build framework only (exclude demos):**
```bash
mvn install -pl '!*-demos'
```

**Run a demo:**
```bash
mvn exec:java -pl bronze-demos
mvn exec:java -pl bronze-demos -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"
```

## Project Structure

- `stone/` - Minimal framework tier (no dependencies)
- `bronze/` - Multi-panel registry, lifecycle events, positioning
- `bronze-demos/` - Demo applications for Bronze tier
- `silver/`, `gold/`, `platinum/` - Future framework tiers

## Maven Reactor Tips

- Build specific module: `mvn install -pl bronze`
- Build multiple modules: `mvn install -pl stone,bronze`
- Exclude demos: `mvn install -pl '!*-demos'` (useful for CI/CD)

## Branches

**v00_00_01** - Snapshot before LAF dependency restructuring

**v00_00_02** - Core framework development branch (merges to master when ready)

**trinity-claude-20251129** - Current working branch
