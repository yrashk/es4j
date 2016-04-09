# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:0.2.4'
  compile 'com.eventsourcing:eventsourcing-h2:0.2.4' // for H2 (MVStore) storage
}
```

That said, ES4J is currently evolving quickly and in some cases, having a maven local / private maven repository based snapshot version of ES4J can be a better fit.
