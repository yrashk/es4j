# Indexing

Let's say we want to index `AddressChanged` by restaurant's ID, so that we can
find restaurant's address changes by its ID. In `AddressChanged`, add this simple equality index.

```java
@NonFinal
public static SimpleIndex<AddressChanged, UUID> REFERENCE_ID = SimpleIndex.as(AddressChanged::reference);
```

If you want to index a comparable, such as event's timestamp (it's a very common scenario!), you need to declare index properties:

```java
@NonFinal
@Index({EQ, LT, GT})
public static SimpleIndex<AddressChanged, HybridTimestamp> TIMESTAMP = SimpleIndex.as(StandardEntity::timestamp);
```

Indexing is not limited to producing just one value. In some cases, like
indexing working hours schedule in `WorkingHoursChanged`, an index can produce an `Iterable`:

```java
@NonFinal
@Index({EQ, LT, GT})
public static MultiValueIndex<WorkingHoursChanged, OpeningHoursBoundary> OPENING_AT =
        SimpleIndex.as((workingHoursChanged) ->
                workingHoursChanged.openDuring().stream()
                        .map(openingHours ->
                             new OpeningHoursBoundary(workingHoursChanged.dayOfWeek(),
                                                  openingHours.from()))
                        .collect(Collectors.toList()));
```
