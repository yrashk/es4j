# Indexing

What if we want to find an entity by value of a property? This is done by adding
a non-final, public static index field:

```java
@Getter
private String name;

public static SimpleIndex<MyEvent, String> NAME = (o, queryOptions) -> o.name(); ```
