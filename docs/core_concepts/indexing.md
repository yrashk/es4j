# Indexing
What if we want to find users by their email? Let's add an index to `UserCreated`:

```java
@Index({EQ, UNIQUE})
public static SimpleAttribute<UserCreated, String> EMAIL =
  new SimpleAttribute<UserCreated, String>() {
    @Override
    public String getValue(UserCreated userCreated, QueryOptions queryOptions) {
        return userCreated.email();
    }
  };
```
