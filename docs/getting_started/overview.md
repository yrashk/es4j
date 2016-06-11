# Overview

ES4J is a library that helps to define event-sourcing/CQRS domain models as well as record and query them.

One of ideas behind ES4J architecture is to bring the "database layer" of the application closer to the application itself. The domain model is defined entirely in the host language (Java) and, unless a specialized remote storage is used, all storage is operated on locally in the same process (VM). Think of the unikernel concept applied to applications/databases. There is no ES4J server to run, only your application server.
