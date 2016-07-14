# Indexing

Let's say we want to index `AddressChanged` by restaurant's ID, so that we can
find restaurant's address changes by its ID. In `AddressChanged`, add this simple equality index.

```java
@NonFinal
public static SimpleIndex<AddressChanged, UUID> REFERENCE_ID =
    (addressChanged, queryOptions) -> addressChanged.reference();
```

If you want to index a comparable, such as event's timestamp (it's a very common scenario!), you need to declare index properties:

```java
@NonFinal
@Index({EQ, LT, GT})
public static SimpleIndex<AddressChanged, HybridTimestamp> TIMESTAMP =
    (addressChanged, queryOptions) -> addressChanged.timestamp();
```

Indexing is not limited to producing just one value. In some cases, like
indexing working hours schedule in `WorkingHoursChanged`, an index can produce an `Iterable`:

```java
@NonFinal
@Index({EQ, LT, GT})
public static MultiValueIndex<WorkingHoursChanged, OpeningHoursBoundary> OPENING_AT =
        (workingHoursChanged, queryOptions) ->
                workingHoursChanged.openDuring().stream()
                        .map(openingHours ->
                             new OpeningHoursBoundary(workingHoursChanged.dayOfWeek(),
                                                  openingHours.from()))
                        .collect(Collectors.toList());
```
