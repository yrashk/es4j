# Domain Model

One of the primary ways to interact with data (especially on the query side) within an ES4J application is by using *domain models*. Domain model is
simple a class that encapsulates aggregation of events by querying.

Typically it would have a constructor that takes a `Repository` and an identifier:


```java
public class Restaurant implements Model, NameProtocol, AddressProtocol {
    @Getter
    private final Repository repository;
    @Getter
    private final UUID id;

    @Override public boolean equals(Object obj) {
        return obj instanceof Restaurant && getId().equals(((Restaurant) obj).getId());
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    protected Restaurant(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

```

Typically, one or more static methods are implemented that do initial object
retrieval. It's common to use a combination of `...Created`, `...Changed` and `...Deleted` events for this. For brevity's sake, we will use a simpler definition here:

```java
public static Optional<Restaurant> lookup(Repository repository, UUID id) {
    Optional<RestaurantRegistered> restaurantRegistered =
            ModelQueries.lookup(repository, RestaurantRegistered.class, RestaurantRegistered.ID, id);
    if (restaurantRegistered.isPresent()) {
        return Optional.of(new Restaurant(repository, id));
    } else {
        return Optional.empty();
    }
}
```

In the above example, we query for `RestaurantRegistered` (assuming there can be only zero or one event of this type for any particular restaurant, again, for brevity's sake).

`EntityHandle` is a holder of a reference to entity's UUID and it also has entity retriever `get()`, but we don't need to use it here. The `EntityHandle`'s purpose is to avoid retrieving entities in full unless necessary.

Further information can be either pre-loaded in the static lookup method
or it can be loaded (and potentially cached) on-demand in models' instance methods.

ES4J exposes a simple interface for domain models (`com.eventsourcing.Model`). Although it is not mandatory, it can be useful for further composability, and particularly, [domain protocols](domain_protocol.md).
