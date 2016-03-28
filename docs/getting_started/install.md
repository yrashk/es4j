# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/eventchain/org.eventchain"
    }
}

dependencies {
  compile 'org.eventchain:eventchain-core:0.2.0'
  compile 'org.eventchain:eventchain-h2:0.2.0' // for H2 (MVStore) storage
}
```

That said, Eventchain is currently evolving quickly and in some cases, having a maven local / private maven repository based snapshot version of Eventchain can be a better fit.
