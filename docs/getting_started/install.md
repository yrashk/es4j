# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/eventchain/org.eventchain"
    }
}

dependencies {
  compile 'org.eventchain:eventchain-core:0.1.0'
  compile 'org.eventchain:eventchain-h2:0.1.0' // for H2 (MVStore) storage
}
```

That said, Eventchain is currently evolving quickly and in some cases, having Eventchain from the master branch as a git submodule can be a better fit. In fact, this documentation describes version `0.2.0-SNAPSHOT` so if you want
to be able to use it to the full extent, this approach should be used. Snapshot
versions are not currently published.
