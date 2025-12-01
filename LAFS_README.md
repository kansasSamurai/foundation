# Look and Feel (LAF) Setup

Foundation can dynamically discover and load Look and Feel implementations at runtime.

## Directory Structure

Create a `lafs` directory in your application root:

```
your-application/
├── lafs/                    ← Create this directory
│   ├── weblaf-1.27.jar
│   ├── napkinlaf-1.2.jar
│   └── flatlaf-3.0.jar
└── your-app.jar
```

## How It Works

Foundation's `LAFDiscovery` class uses three strategies to find LAFs:

### 1. Built-in LAFs
Automatically discovers Java's built-in LAFs (Metal, Nimbus, CDE/Motif, Windows, Mac).

### 2. Directory Scanning (`./lafs/`)
Scans the `lafs` directory for JAR files containing Look and Feel implementations.

### 3. Classpath Scanning
Checks for well-known LAF classes already on the classpath.

## Adding a Custom LAF

### Option 1: With Metadata (Recommended)

1. Create `META-INF/foundation-laf.properties` in your LAF JAR:
   ```properties
   laf.class=com.example.MyLookAndFeel
   laf.name=My Beautiful LAF
   laf.description=A modern look and feel for Swing
   ```

2. Drop the JAR in `./lafs/`

3. Foundation will automatically discover it

### Option 2: Without Metadata

1. Simply drop your LAF JAR in `./lafs/`

2. Foundation will scan the JAR for classes extending `javax.swing.LookAndFeel`

Note: This is slower than using metadata.

## Supported LAFs

Foundation has been tested with:

- **WebLAF** (com.alee.laf.WebLookAndFeel)
- **NapkinLAF** (net.sourceforge.napkinlaf.NapkinLookAndFeel)
- **NimROD** (com.nilo.plaf.nimrod.NimRODLookAndFeel)
- **JTattoo** (com.jtattoo.plaf.acryl.AcrylLookAndFeel)
- **Darcula** (com.bulenkov.darcula.DarculaLaf)
- **FlatLaf** (com.formdev.flatlaf.FlatLightLaf)

## Testing LAF Discovery

Run the discovery test:

```bash
mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.LAFDiscovery"
```

This will list all discovered LAFs.

## Example: Adding WebLAF

1. Download `weblaf-1.27.jar`
2. Create `./lafs/` directory
3. Copy `weblaf-1.27.jar` to `./lafs/`
4. Run your Foundation application
5. WebLAF will be available in the LAF selection dialog

## Troubleshooting

**LAF not discovered?**
- Check that the JAR is in `./lafs/`
- Verify the JAR contains a class extending `LookAndFeel`
- Add metadata file for faster discovery
- Check console output for error messages

**LAF discovered but won't apply?**
- Ensure all dependencies are available
- Some LAFs require additional libraries
- Check console for `ClassNotFoundException` or similar errors

## Future Enhancements

Planned features:
- User preference persistence (remember selected LAF)
- LAF preview/switching without restart
- Hot-reload LAFs when JARs added to directory
- Integration with uContext for LAF selection during init()
