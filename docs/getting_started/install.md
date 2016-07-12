# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

{% if book.isSnapshot %}
<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
    maven {
        url  "http://dl.bintray.com/unprotocols/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-inmem:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:v{{ book.version | snapshotVersion }}'
}
</code></pre>

Please note that this version of the documentation describes a snapshot version. APIs can and will break, and so can behaviours and guarantees.

{% else %}
<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven"
    }
    maven {
        url  "http://dl.bintray.com/unprotocols/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-inmem:v{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-cep:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-migrations:{{ book.version }}'
}
</code></pre>

That said, ES4J is currently evolving quickly and in some cases, using a snapshot version is preferable.

<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
    maven {
        url  "http://dl.bintray.com/unprotocols/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-inmem:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:v{{ book.version | snapshotVersion }}'
}
</code></pre>

{% endif %}
