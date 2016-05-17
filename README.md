[![Build Status](https://travis-ci.org/eventsourcing/es4j.svg?branch=master)](https://travis-ci.org/eventsourcing/es4j)
[ ![Download](https://api.bintray.com/packages/eventsourcing/maven/eventsourcing-core/images/download.svg) ](https://bintray.com/eventsourcing/maven/eventsourcing-core/_latestVersion)
[ ![Download](https://api.bintray.com/packages/eventsourcing/maven-snapshots/eventsourcing-core/images/download.svg) ](https://bintray.com/eventsourcing/maven-snapshots/eventsourcing-core/_latestVersion)
[![Join the chat at https://gitter.im/eventsourcing/eventsourcing](https://badges.gitter.im/eventsourcing/eventsourcing.svg)](https://gitter.im/eventsourcing/eventsourcing?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Eventsourcing for Java

Instead of mutating data in a database, it stores all changes
(events) and causes (commands). This facilitates rapid application development and evolution by mitigating the inability
to predict how future needs will drive data shape requirements as all causal information is persisted. It also provides a foundation
for deep analytics, data recovery, audit trails and other associated benefits.

*This is an early version. That said, it's a rewrite of another
library that has been used in real projects, so a lot was learned and incorporated in this incarnation.*

## Key benefits

* Flexibility of data aggregation and representation
* Persistence of causal information
* Succinctly mapped application functionality
* Undo/redo functionality
* Audit trail logging

## Key features

* Clean, succinct Command/Event model
* Compact data storage layout
* Using [Disruptor](https://lmax-exchange.github.io/disruptor/) for fast message processing
* Using [CQengine](https://github.com/npgall/cqengine) for fast indexing and querying
* In-memory and on-disk (*more persistent indices coming soon*) storage
* Causality-preserving [Hybrid Logical Clocks](http://www.cse.buffalo.edu/tech-reports/2014-04.pdf)
* Locking synchronization primitive

# Documentation

Installation instructions and documentation can be found at [es4j-doc.eventsourcing.com](http://es4j-doc.eventsourcing.com)

# Snapshot versions

Every successful build is published into a [separate Maven repository on Bintray](https://bintray.com/eventsourcing/maven-snapshots) (using a `git describe`
version), you can find the last snapshot version mentioned in a badge at the top of this file.

Gradle configuration:

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/eventsourcing/maven-snapshots"
    }
}
```

Maven configuration:

```xml
<?xml version='1.0' encoding='UTF-8'?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd' xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
<profiles>
	<profile>
		<repositories>
			<repository>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
				<id>bintray-eventsourcing-maven-snapshots</id>
				<name>bintray</name>
				<url>http://dl.bintray.com/eventsourcing/maven-snapshots</url>
			</repository>
		</repositories>
		<pluginRepositories>
			<pluginRepository>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
				<id>bintray-eventsourcing-maven-snapshots</id>
				<name>bintray-plugins</name>
				<url>http://dl.bintray.com/eventsourcing/maven-snapshots</url>
			</pluginRepository>
		</pluginRepositories>
		<id>bintray</id>
	</profile>
</profiles>
<activeProfiles>
	<activeProfile>bintray</activeProfile>
</activeProfiles>
</settings>
```

# Related projects

* [es4j-graphql](https://github.com/eventsourcing/es4j-graphql) A Relay.js/GraphQL adaptor for ES4J-based applications.

# Contributing

Contributions of all kinds (code, documentation, testing, artwork, etc.) are highly encouraged. Please open a GitHub issue if you want to suggest an idea or
ask a question. We use ZeroMQ's [C4 process](C4.md).

For more details, please refer to [CONTRIBUTING](CONTRIBUTING.md)
