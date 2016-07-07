# Layout

ES4J uses a concept of layout to give a deterministic shape to any class. Layout allows to specify what class properties are included into serialized object's representation and unique class identifier (hash). Other
ES4J components use it to serialize, deserialize and identify classes according to their layout.


Lets start with an example:

```java
public class User {
  private final String email;
  public String getEmail() { return email; }
  public User(String email) {
    this.email = email;
  }
}
```

This class defines one property (`email`) by a one-parameter constructor and a getter (JavaBean, chain and fluent naming conventions are supported).

Implemented in the `eventsourcing-layout` package, `com.eventsourcing.layout.Layout` is the main class used to create layouts:

```java
Layout<User> layout = Layout.forClass(User.class);
```

In this `layout` object, two main methods are available: `getProperties()`
that will return a list of all properties and `getHash()` that will return
layout hash.

Layout defines following rules for property inclusion:

* Every property must have a getter
* Property must be of a supported type (see below)
* Property must have a matching parameter in the constructor (same parameter name or same name through `@PropertyName` parameter annotation)

Additionally, inherited properties from parent classes will be admitted based on this criteria:

* Every inherited property must have a getter and a setter
* Property must be of a supported type (see below)
* Property must have a matching parameter in any of the parent's constructors (same parameter name or same name through `@PropertyName` parameter annotation)


Note: Since ES4J requires the use of accessors, using code generation tools like Lombok is advisable.

## Hash

Every layout has a hash which is a unique fingerprint of such a class. It is computed using a SHA-1 digest (as a compromise between the hash size and chances of collision) in the following way:

1. Full class name is encoded to bytes platform's default charset and digested.
1. All properties are sorted lexicographically
1. For each property:
  1. Property name encoded to bytes using platform's default charset and digested
  1. Property type's fingerprint (short byte sequence) is digested.

This process is described in [RFC1/ELF](http://rfc.eventsourcing.com/spec:1/ELF)

## Supported Property Types

Currently, ES4J supports a rather limited set of types, but this is going
to be improved.

* Byte/byte
* Byte[]/byte[]
* Short/short
* Integer/int
* Long/long
* Float/float
* Double/double
* BigDecimal
* Boolean/boolean
* java.lang.String
* java.util.UUID
* java.util.Date
* Enum (Java enumerations)
* List<?> (Java lists)
* Optional<?>

All other types will be handled through Layout.

## Serialization

Due to properties sorting and strict typing, ES4J's serialization format
is fairly compact. All property information is stripped and only the actual data is stored, in the same lexicographical order. ES4J doesn't currently use
variable length integers or any other compression methods so it is not extremely compact (this might change in the future).

You can get a serializer and a deserializer very easily:

```java
Serialization serialization = BinarySerialization.getInstance();
Serializer<User> serializer = serialization.getSerializer(User.class);
Deserializer<User> deserializer = serialization.getDeserializer(User.class);
```


### `null` values

It is important to note that ES4J does not support a notion of a `null`
property value. While the instances you pass for serialization *can* contain
nulls, they will be treated as "empty" values (for example, empty String, nil UUID, zero number, false boolean, etc.). The default values for these are
specified in [RFC1/ELF](http://rfc.eventsourcing.com/spec:1/ELF/)
