[![Build Status](https://travis-ci.org/eventsourcing/es4j.svg?branch=master)](https://travis-ci.org/eventsourcing/es4j)
[ ![Download](https://api.bintray.com/packages/eventsourcing/maven/eventsourcing-core/images/download.svg) ](https://bintray.com/eventsourcing/maven/eventsourcing-core/_latestVersion)

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

# Contributing

Contributions of all kinds (code, documentation, testing, artwork, etc.) are highly encouraged. Please open a GitHub issue if you want to suggest an idea or
ask a question.
