# Overview

Eventsourcing is a library that helps defining event sourcing / CQRS domain models
as well as recording and querying them.

One of ideas behind Eventsourcing architecture is to bring the "database layer" of
the application closer to the application itself. The domain model is defined
entirely in the host language (Java) and, unless a specialized remote storage is used, all storage is operated locally in the same process (VM). Think of the unikernel concept applied to applications/databases. There is no Eventsourcing server to run, only your application server.
