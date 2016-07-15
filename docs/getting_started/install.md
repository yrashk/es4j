# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

{% if book.isSnapshot %}
<pre><code class="lang-groovy">
repositories {
    mavenCentral()
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-inmem:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-queries:{{ book.version | snapshotVersion }}'
}
</code></pre>

Please note that this version of the documentation describes a snapshot version. APIs can and will break, and so can behaviours and guarantees.

{% else %}
<pre><code class="lang-groovy">
repositories {
    mavenCentral()
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-inmem:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-cep:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-migrations:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-queries:{{ book.version }}'
}
</code></pre>

That said, ES4J is currently evolving quickly and in some cases, using a snapshot version is preferable.

<pre><code class="lang-groovy">
repositories {
    mavenCentral()
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-inmem:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-queries:{{ book.version | snapshotVersion }}'

}
</code></pre>

{% endif %}
