# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-h2:{{ book.version }}' // for H2 (MVStore) storage
}
</code></pre>

That said, ES4J is currently evolving quickly and in some cases, having a maven local / private maven repository based snapshot version of ES4J can be a better fit.
