# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/Eventsourcing/org.Eventsourcing"
    }
}

dependencies {
  compile 'org.Eventsourcing:Eventsourcing-core:0.2.0'
  compile 'org.Eventsourcing:Eventsourcing-h2:0.2.0' // for H2 (MVStore) storage
}
```

That said, Eventsourcing is currently evolving quickly and in some cases, having a maven local / private maven repository based snapshot version of Eventsourcing can be a better fit.
