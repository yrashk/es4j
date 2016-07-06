# Domain Protocol


The idea behind protocols is to re-use "polymorphic" events to perform common operations on various types of entities.

Consider, for example, that we have models called Product, Widget and Company. All of these models have a name.

Instead of assigning name or renaming each of these models in individual command/event pairs (RenameProduct/ProductRenamed, RenameWidget/WidgetRenamed, RenameCompany/CompanyRemained) and having similar yet different name retrieval methods in the respective model, the following approach is used:


There is only one command/event pair (Rename/NameChanged) that takes and stores a "polymorphic" reference to any model (its UUID) and a new name. Now, since renaming any supported model is done the same way, we can implement a common protocol for name retrieval:

```java
public interface NameProtocol extends Protocol {
    public String name() {
        try (ResultSet<EntityHandle<NameChanged>> resultSet =
             repository.query(NameChanged.class, equal(NameChanged.REFERENCE_ID, getId()),
                              queryOptions(orderBy(descending(attribute)),
                                           applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
             if (resultSet.isEmpty()) {
                 return null;
             }
             return resultSet.iterator().next().get().name();
       }
    }
}
```

The above protocol implements a `name()` function that retrieves the last `NameChange` for the particular model referenced by its UUID (`getId()`).

Now, all we have to do is to make every model implement this interface:

```java
public class Product implements Model, NameProtocol { ... }
public class Widget implements Model, NameProtocol { ... }
public class Company implements Model, NameProtocol { ... }
```

This approach helps reducing the number of commands and events that needs to exist to serve similar purposes.
