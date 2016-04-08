# Introduction

Eventsourcing is an **event sourcing** framework for Java. Instead of mutating data in a database, it stores all changes
(events) and causes (commands). This facilitates rapid application development and evolution by mitigating the inability
to predict how future needs will drive data shape requirements as all the causal information is persisted. It also provides a foundation
for deep analytics, data recovery, audit trails and other associated benefits.

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
