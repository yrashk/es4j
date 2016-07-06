# Installation

Currently you can get packages from Bintray. The example below shows configuration for Gradle:

{% if book.isSnapshot %}
<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:v{{ book.version | snapshotVersion }}'
}
</code></pre>

**NB**: This documentation describes a snapshot version.

{% else %}
<pre><code class="lang-groovy">
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven"
    }
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:{{ book.version }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:{{ book.version }}'
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
}

dependencies {
  compile 'com.eventsourcing:eventsourcing-core:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-postgresql:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-cep:v{{ book.version | snapshotVersion }}'
  compile 'com.eventsourcing:eventsourcing-migrations:v{{ book.version | snapshotVersion }}'
}
</code></pre>

{% endif %}
