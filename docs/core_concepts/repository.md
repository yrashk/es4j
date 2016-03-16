# Repository

Repository is an entry point to most of end-user Eventchain functionality.
It puts all components (journalling, indexing, querying) together.

Currently, Repository setup a little bit lengthy (but not too much), but it can also be simplified when used in an OSGi container.

In the future, a more compact API with defaults will be added as well.

```java
Repository repository = Repository.create();

Journal journal = new MemoryJournal();
journal.setRepository(journal);
repository.setJournal(journal);

IndexEngine indexEngine = new MemoryIndexEngine();
indexEngine.setRepository(repository);
indexEngine.setJournal(journal);
repository.setIndexEngine(indexEngine);

// Eventchain should find commands and events in this
// hierarchy of packages:
repository.setPackage(getClass().getPackage());

repository.startAsync().awaitRunning();
```
