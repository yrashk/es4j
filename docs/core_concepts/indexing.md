# Indexing

What if we want to find users by their email? The simplest way is to apply `@Index` annotation to `UserCreated#email`'s getter:

```java
private String email;

@Index({EQ})
public String email() { return email; }
public UserCreated email(String email) { this.email = email; return this; }

public static Attribute<UserCreated, String> EMAIL = Indexing.getAttribute(UserCreated.class, "email");
```

If you need to process one or more attributes, you can use a more involved SimpleAttribute and apply `@Index` directly to it
instead.

```
@Index({EQ})
public static Attribute<UserCreated, String> MANGLED_EMAIL = new SimpleAttribute<UserCreated, String>("mangledEmail") {
    @Override
    public String getValue(UserCreated userCreated, QueryOptions queryOptions) {
        return userCreated.email().replace("@"," at ");
    }
  };
```