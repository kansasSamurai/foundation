20190702 - branch origin/master can be used going forward;
this is a reminder of why... I started with three applications
where I had copied the API from app to app and could not remember
the development history:  mmdealv2, jclock, and editor
So, I started this repo with the contents of mmdealv2.
I then created branches for the jclock and editor versions.
When realizing how similar they all were, I chose to abandon
the jclock branch and merged the editor branch back to master.

---

## Java 8 Compatibility Tracking

20251231 - Foundation currently targets Java 8 for broad compatibility.
All Java 8-specific workarounds are marked with `JAVA8-COMPAT:` comments
in the source code for easy identification when upgrading to Java 9+.

**Quick search for all Java 8 compatibility workarounds:**
```bash
# Search all tiers
grep -r "JAVA8-COMPAT:" --include="*.java" stone/ bronze/ silver/ gold/ platinum/

# Count occurrences
grep -r "JAVA8-COMPAT:" --include="*.java" stone/ bronze/ silver/ gold/ platinum/ | wc -l
```

**Documentation:**
See `docs/migration/java9-upgrade-notes.md` for comprehensive migration guide.
