# Indexing

What if we want to find an entity by value of a property The simplest way is to apply `@Index` annotation to a propety's getter:

```java
private String name;

@Index({EQ})
public String name() { return name; }

public static Attribute<MyEvent, String> NAME = Indexing.getAttribute(MyEvent.class, "name");
```

If you need to process one or more attributes, you can use a more involved SimpleAttribute and apply `@Index` directly to it
instead.

```java
@Index({EQ})
public static Attribute<MyEvent, String> LOWERCASE_NAME = new SimpleAttribute<MyEvent, String>("name") {
    @Override
    public String getValue(MyEvent object, QueryOptions queryOptions) {
        return object.name().toLowerCase();
    }
  };
```
