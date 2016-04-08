# eventsourcing-layout

This component (eventsourcing-layout) is used for versioning, serialization and deserialization of POJOs. The purpose
behind it is to have a data exchange and storage format for eventsourcing-based systems.

Key features:

* Strong hash-bashed "schema" versioning
* Compact representation (no property names stored)
* Fast encoding and decoding

It has been decided to extract this as an individual component as it will be useful outside of eventsourcing scope.