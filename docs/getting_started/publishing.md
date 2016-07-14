# Publishing

Repository is an entry point to most of end-user ES4J functionality.
It puts all components (journalling, indexing, querying) together and allows to publish commands.

```java
Repository repository = StandardRepository.builder()
                        .journal(new MemoryJournal())
                        .indexEngine(new MemoryIndexEngine()).
                        build();

// ES4J should find commands and events in this
// hierarchy of packages:
repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{getClass().getPackage()}));
repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{getClass().getPackage()}));

repository.startAsync().awaitRunning();
```

StandardRepository also supports OSGi glue all components together.

Now we're ready to publish the command:

```java
Address restaurantAddress = new Address("559 W Pender St, Vancouver, BC",
                                        "Canada", "Vancouver",
                                        "V6B 1V5", 49.2837512, -123.1134196);
Restaurant restaurant = repository.publish(new RegisterRestaurant("Kyzock",
                                         restaurantAddress,
                                         new OpeningHours(11, 30, 19, 00))).get();
```
