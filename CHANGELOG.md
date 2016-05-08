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
