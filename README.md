# Eventchain

Eventchain is an **event sourcing** framework for Java. Instead of mutating data in a database, it stores all changes
(events) and causes (commands). This facilitates rapid application development and evolution by mitigating the inability
to predict how future needs will drive data shape requirements as all the causal information is persisted. It also provides a foundation
for deep analytics, data recovery, audit trails and other associated benefits.

# Getting Started

First of all, the repository has to be configured. Currently, the procedure is
a little bit lengthy, but it can also be simplified when used in an OSGi container.

```java
Repository repository = Repository.create();

PhysicalTimeProvider timeProvider = new NTPServerTimeProvider();
repository.setPhysicalTimeProvider(timeProvider);

Journal journal = new MemoryJournal();
journal.setRepository(journal);
repository.setJournal(journal);

IndexEngine indexEngine = new MemoryIndexEngine();
indexEngine.setRepository(repository);
indexEngine.setJournal(journal);
repository.setIndexEngine(indexEngine);

LockProvider lockProvider = new MemoryLockProvider();
repository.setLockProvider(lockProvider);

repository.setPackage(getClass().getPackage()); // application's package

repository.startAsync().awaitRunning();
```

## Commands

Command is a request for changes in the domain. Unlike an event, it is not a statement of fact as it might be rejected. Commands are defined by subclassing
`Command<T>`. Below is an example of such a command:

```java
@Accessors(fluent = true)
public class Login extends Command<Optional<User>> {
  @Getter @Setter
  private String login;
  @Getter @Setter
  private String password;

  private succeeded = true;

  public Stream<Event> events(Repository repository) {
    if (login().contentEquals("admin") && password().contentEquals("admin")) {
      return Stream.of(new UserLoggedIn().login(login()));
    } else {
      succeeded = false;
      return Stream.of(new UserFailedLoggingIn().login(login()));
    }
  }

  public Optional<User> onCompletion() {
    if (succeeded) {
      return User.lookup(login());
    } else {
      return Optional.empty();
    }
  }

}
```

In the above example, the `Login` command describes an attempt to login. Depending on the provided credentials, either `UserLoggedIn` or `UserFailedLoggingIn` events (defined below) will be generated.

Also, `onCompletion` is used to return an optional instance of `User` if the
authentication attempt was successful.

## Events

Event is a statement of a fact that has occurred once written to a journal. Events are defined by subclassing `Event`. Below is an example:

```java
@Accessors(fluent = true)
public class UserLoggedIn extends Event {
  @Getter @Setter
  private String login;
}

@Accessors(fluent = true)
public class UserFailedLoggingIn extends Event {
  @Getter @Setter
  private String login;
}
```

## Indexing

What if we want to find unsuccessful login attempts? Let's add an index to `UserFailedLoggingIn`:

```java
public static SimpleAttribute<UserFailedLoggingIn, String> LOGIN =
  new SimpleAttribute<UserFailedLoggingIn, String>() {
    @Override
    public String getValue(UserFailedLoggingIn object, QueryOptions queryOptions) {
        return object.login();
    }
  };
```

## Publishing

Now we're ready to publish the command:

```java
Optional<User> result = repository.publish(new Login().login("admin").password("badpassword")).get();
```

In this example, `result` should be an empty optional as the password supplied was incorrect.

We can now find how many failed login attempts this user had:

```java
import static com.googlecode.cqengine.query.QueryFactory.*;

// ...

repository.getIndexEngine().
   getIndexedCollection(UserFailedLoggingIn.class).
   retrieve(equal(UserFailedLoggingIn.LOGIN, "admin")).size();
```
