# Domain Model

One of the primary ways to interact with data (especially on the query side) within an Eventchain application is by using *domain models*. Domain model is
simple a POJO class that encapsulates aggregation of events by querying.

Typically it would have a protected constructor that takes a `Repository`:


```java
@Accessors(fluent = true)
class User {
  @Getter @Setter
  private UUID uuid;
  @Getter @Setter
  private String email;

  protected final Repository repository;
  protected User(Repository repository) {
    this.repository = repository;
  }
}
```

Typically, one or more static methods are implemented that do initial object
retrieval. It's common to use a combination of `...Created`, `...Changed` and `...Deleted` events for this. For brevity's sake, we will use a simpler definition here:

```java
public static Optional<User> lookup(Repository repository, String email) {
  try (ResultSet<EntityHandle<UserCreated>> resultSet =
  repository.query(UserCreated.class, equal(UserCreated.EMAIL, email))) {
    if (resultSet.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(
        new User(repository).uuid(resultSet.uniqueResult().uuid()).email(email)
      );
    }
  }
}
```

In the above example, we query for `UserCreated` (assuming there can be only zero or one event of this type for any particular user, again, for brevity's sake).

`EntityHandle` is a holder of a reference to entity's UUID and it also has entity retriever `get()`, but we don't need to use it here. The `EntityHandle`'s purpose is to avoid retrieving entities in full unless necessary.

Further user information can be either pre-loaded in the static lookup method
or it can be loaded (and potentially cached) on-demand in models' instance methods.
