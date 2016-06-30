# Entity

Entity is an interface (`com.eventsourcing.core.Entity`) that defines `uuid`
and `timestamp` properties. It is not very useful on its own and is rarely used
by end users, but it is an important building block for [Commands](command.md) and [Events](event.md) and can be often seen in type signatures throughout the API, so the end user should be familiar with it.
