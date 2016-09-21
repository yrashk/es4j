# 0.4.2

**Features**

* Map (associative array) type has been added ([#152](https://github.com/eventsourcing/es4j/pull/152))
* Index discovery mechanism is no longer hardcoded ([#154](https://github.com/eventsourcing/es4j/pull/154))

# 0.4.1

**Backwards-incompatible changes**

* Index definition syntax is simplified for most of cases (when query
  options are not necessary) ([#144](https://github.com/eventsourcing/es4j/pull/144))

**Bugfixes**

* Command#events can no longer block publishing commands through the repository ([#147](https://github.com/eventsourcing/es4j/pull/147))
* RFC 3/CEP event layout names were incorrect ([#148](https://github.com/eventsourcing/es4j/pull/148))
* PostgreSQL is no longer leaking ResultSet when deserializing arrays ([#150](https://github.com/eventsourcing/es4j/pull/150))

**Upgrades**

* cqengine 2.7.1 ([#143](https://github.com/eventsourcing/es4j/pull/143))

**Misc**

* A process manager implementation example is provided ([#146](https://github.com/eventsourcing/es4j/pull/146))

0.4.0
=====

**Backwards-incompatible changes**

* [Core] `Entity`, `Command` and `Event` are now interfaces, the classes were moved to to `StandardEntity`, `StandardCommand` and `StandardEvent` ([#67](https://github.com/eventsourcing/es4j/pull/67))
* [Core] `Model#id()` has been renamed to `Model#getId()` for consistency of
the interface ([#114](https://github.com/eventsourcing/es4j/pull/114))
* [Core] Entity layouts are now constructor + getters driven, allowing
for immutable entities ([#83](https://github.com/eventsourcing/es4j/pull/83), [#87](https://github.com/eventsourcing/es4j/pull/87))
* [Layout] `new Layout<>(klass)` has been replaced with `Layout.forClass(klass)` ([#83](https://github.com/eventsourcing/es4j/pull/83))
* [Core] `Command<Result>` signature has been changed to `Command<State,Result>`, `Command#events` return type has changed to `EventStream` to hold the state and `Command#onCompletion` has been renamed to `#result` ([#77](https://github.com/eventsourcing/es4j/pull/77), [#84](https://github.com/eventsourcing/es4j/pull/84))
* [Core] New index definition syntax via `SimpleIndex` and `MultiValue` index
([#133](https://github.com/eventsourcing/es4j/pull/133))
* [Core] Most of repository implementation related code was moved to `com.eventsourcing.repository` ([#64](https://github.com/eventsourcing/es4j/pull/64))
* [Core] `new StandardRepository()` now will use localhost NTP server ([#76](https://github.com/eventsourcing/es4j/pull/66), [#66](https://github.com/eventsourcing/es4j/pull/76))
* [Core] Renamed `MemoryLockProvider` to `LocalLockProvider` ([#69](https://github.com/eventsourcing/es4j/pull/69), [#70](https://github.com/eventsourcing/es4j/pull/70))
* [Core] `Journal#commandEventsIterator` has been removed ([#69](https://github.com/eventsourcing/es4j/pull/69))
* [Core] `MemoryJournal` has been moved to `eventsourcing-inmem` ([#69](https://github.com/eventsourcing/es4j/pull/69))
* [Layout] Layout serialization/deserialization framework
can be extended to other byte-oriented encodings ([#74](https://github.com/eventsourcing/es4j/pull/74))

**Bugfixes**

* [Core] Plugging in indices did not work ([#94](https://github.com/eventsourcing/es4j/pull/94))
* [Core] Entity static initialization can no longer hang the current thread ([#65](https://github.com/eventsourcing/es4j/pull/65))
* [H2] MVStoreJournal#journal will no longer into an infinite recursion on
having an exception during `CommandTerminatedExceptionally` journalling
([#82](https://github.com/eventsourcing/es4j/pull/82))
* [Core] OSGi `StandardRepository#activate()` now waits until repository is started ([#92](https://github.com/eventsourcing/es4j/pull/92))
* [Core] byte[] equality did not work in MemoryIndexEngine ([#100](https://github.com/eventsourcing/es4j/pull/100))
* [HLC] Fixed initialization of HybridTimestamp and updating with another
HybridTimestamp ([#112](https://github.com/eventsourcing/es4j/pull/112))

**Features**

* [CEP] Migrated es4j-cep into the project to maintain version parity
([#60](https://github.com/eventsourcing/es4j/pull/60))
* [Migrations] Implemented [8/EMT](http://rfc.eventsourcing.com/spec:8/EMT) and `LayoutMigration` to standardize migrations ([#69](https://github.com/eventsourcing/es4j/pull/69))
* [Core] `EventCausalityEstablished` event-command causality indexing has been implemented ([#69](https://github.com/eventsourcing/es4j/pull/69))
* [Core] Better support for Kotlin in `Repository` ([#72](https://github.com/eventsourcing/es4j/pull/72))
* [Queries] Composable model collection queries ([#123](https://github.com/eventsourcing/es4j/pull/123))
* [Queries] `LatestAssociatedEntryQuery` query has been added ([#115](https://github.com/eventsourcing/es4j/pull/115))
* [Queries] `IsLatestEntity` query has been added ([#117](https://github.com/eventsourcing/es4j/pull/117))
* [Queries] `ModelQueries.lookup` helper for looking up an entity by ID
has been added ([#116](https://github.com/eventsourcing/es4j/pull/116))
* [PostgreSQL] Implemented PostgreSQL journal ([#79](https://github.com/eventsourcing/es4j/pull/79))
* [PostgreSQL] Implemented PostgreSQL equality and navigable indices ([#94](https://github.com/eventsourcing/es4j/pull/94), [#104](https://github.com/eventsourcing/es4j/pull/104))
* [PostgreSQL] Implemented PostgreSQL-based lock provider `PostgreSQLLockProvider` ([#98](https://github.com/eventsourcing/es4j/pull/98))
* [Core] Expose CascadingIndexEngine index decisions through JMX ([#94](https://github.com/eventsourcing/es4j/pull/94))
* [Kotlin] Add basic support for Kotlin ([#88](https://github.com/eventsourcing/es4j/pull/88), [#99](https://github.com/eventsourcing/es4j/pull/99))

**Specification compliance**

* [Core] Layout and Property are now [7/LDL](http://rfc.eventsourcing.com/spec:7/LDL) compliant ([#61](https://github.com/eventsourcing/es4j/pull/61))
* [Repository] `AbstractRepository` now adheres to [8/EMT](http://rfc.eventsourcing.com/spec:8/EMT) and records
`EntityLayoutIntroduced` ([#91](https://github.com/eventsourcing/es4j/pull/91))
* [Repository] `AbstractRepository` now adheres to the exception reporting
specified in [9/RIG](http://rfc.eventsourcing.com/spec:9/RIG) ([#102](https://github.com/eventsourcing/es4j/pull/102))

**Upgrades**

* cqengine 2.7.0 ([#135](https://github.com/eventsourcing/es4j/pull/135))

**Misc**

* Source and javadoc jars are now included into distribution ([#78](https://github.com/eventsourcing/es4j/pull/78))


0.3.2
=====

**Features**

* [Core] Added an API to iterate over command's events ([#46](https://github.com/eventsourcing/es4j/pull/46))
* [Core] If a command had a timestamp prior to publishing, the timestamp will not be overriden and repository's timestamp will be updated ([#55](https://github.com/eventsourcing/es4j/pull/55))
* [Core] If an event had a timestamp prior to journalling, the timestamp will not be overriden and repository's timestamp will be updated ([#56](https://github.com/eventsourcing/es4j/pull/56))
* [Core] `Repository#getTimestamp()` was added ([#55](https://github.com/eventsourcing/es4j/pull/55))

**Bugfixes**

* [HLC] HybridTimestamp now follows the layout prescribed in RFC6/HLC ([#47](https://github.com/eventsourcing/es4j/pull/47))
* [H2] Current H2 version doesn't export org.h2.mvstore.db in OSGi, making es4j undeployable in OSGi. Own build with a fix is used now ([#49](https://github.com/eventsourcing/es4j/pull/49))
* [Core] CascadingIndexEngine's self-referencing in OSGi was fixed ([#50](https://github.com/eventsourcing/es4j/pull/50))
* [Core] MemoryJournal still recorded normal events even when the command terminated exceptionally ([#51](https://github.com/eventsourcing/es4j/pull/51))
* [H2] MVStoreJournal command hash keys were wrongfully overwritten ([#54](https://github.com/eventsourcing/es4j/pull/54))
* Outstanding failing tests were fixed ([#52](https://github.com/eventsourcing/es4j/pull/52), [#53](https://github.com/eventsourcing/es4j/pull/53))


0.3.1
=====

**Backwards-incompatible changes**

* [Layout] Character type removed as RFC1/ELF no longer defines it ([#30](https://github.com/eventsourcing/es4j/pull/30))

**Bugfixes**

* [Layout] UnknownTypeHandler was losing layout type information, according to RFC1/ELF, it should be kept ([#42](https://github.com/eventsourcing/es4j/pull/42))

**Features**

* [Layout] Added support for RFC1/ELF's Timestamp ([#41](https://github.com/eventsourcing/es4j/pull/41))

**Improvements**

* [H2] Slightly faster MVStoreJournal by avoiding extra reads (`tryPut` vs `put`) ([#32](https://github.com/eventsourcing/es4j/pull/32))

**Upgrades**

* cqengine 2.6.0 ([#29](https://github.com/eventsourcing/es4j/pull/29))
* h2 1.4.192 ([#33](https://github.com/eventsourcing/es4j/pull/33))



0.3.0
=====

**Backwards-incompatible changes**

* EntityHandle is now an interface ([#23](https://github.com/eventsourcing/es4j/pull/23))

**Features**

* New EntitySubscriber interface to subscribe to subsets of
  journaled entities. ([#22](https://github.com/eventsourcing/es4j/pull/22))

0.2.7
=====

Relicensed under MPL 2.0

**Backwards-incompatible changes**

* All type handler fingerprints were changed to become human-readable ([#10](https://github.com/eventsourcing/es4j/issues/10))

**Bugfixes**

* Enum type handler fingerprint is based on enum's shape now (previously,
  all enums were considered the same with regards to their fingerprint)

**Upgrades**

* cqengine 2.5.0

0.2.6
=====

This release is a quick fix for 0.2.5 removing accidentally
added premature code (MVStore-based NavigableIndex)

0.2.5
=====

**Backwards-incompatible changes**

* If a command results in exception (during #events() call or
  during stream generation), all events will be replaced
  with a CommandTerminatedExceptionally event so that
  the exception does not get lost.
* EntityHandle#get() has been split into get() and getOptional()
  for usability reasons.

**Bugfixes**

* MVStoreJournal entity iterator was iterating over non-matching keys

0.2.4
=====

The project has been renamed to Eventsourcing for Java

**Backwards-incompatible changes**

* org.eventchain package got renamed to com.eventsourcing

**Features**

* [Layout] Introduce serialization of comparable values

0.2.3
=====

**Bugfixes**

* [Layout] Fixed layout hasing for parametrized types (Optional and List)

**Features**

* [Layout] Add support for BigDecimal
* [Layout] Allow non-readonly layouts with setterless properties with matching constructors
