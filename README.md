[![Build Status](https://travis-ci.org/eventchain/eventchain.svg?branch=master)](https://travis-ci.org/eventchain/eventchain)
[ ![Download](https://api.bintray.com/packages/eventchain/org.eventchain/eventchain-core/images/download.svg) ](https://bintray.com/eventchain/org.eventchain/eventchain-core/_latestVersion)

# Eventchain

[![Join the chat at https://gitter.im/eventchain/eventchain](https://badges.gitter.im/eventchain/eventchain.svg)](https://gitter.im/eventchain/eventchain?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Eventchain is an **event sourcing** framework for Java. Instead of mutating data in a database, it stores all changes
(events) and causes (commands). This facilitates rapid application development and evolution by mitigating the inability
to predict how future needs will drive data shape requirements as all causal information is persisted. It also provides a foundation
for deep analytics, data recovery, audit trails and other associated benefits.

*This is an early version. That said, it's a rewrite of another
library that has been used in real projects, so a lot was learned and incorporated in this incarnation.*

## Key benefits

* Flexibility of data aggregation and representation
* Persistence of causal information
* Succinctly mapped application functionality
* Undo/redo functionality
* Audit trail logging

## Key features

* Clean, succinct Command/Event model
* Compact data storage layout
* Using [Disruptor](https://lmax-exchange.github.io/disruptor/) for fast message processing
* Using [CQengine](https://github.com/npgall/cqengine) for fast indexing and querying
* In-memory and on-disk (*more persistent indices coming soon*) storage
* Causality-preserving [Hybrid Logical Clocks](http://www.cse.buffalo.edu/tech-reports/2014-04.pdf)
* Locking synchronization primitive

# Documentation

Documentation is available at [doc.eventchain.org](http://doc.eventchain.org)

# Downloading

You can get packages from Bintray:

(Gradle)

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/eventchain/org.eventchain"
    }
}

dependencies {
  compile 'org.eventchain:eventchain-core:0.2.3'
  compile 'org.eventchain:eventchain-h2:0.2.3' // for H2 storage
}
```

That said, Eventchain is evolving quickly and in some cases, having Eventchain from the master branch can be a better fit.

## Repository

First of all, the repository has to be configured. Currently, the procedure is
a little bit lengthy, but it can also be simplified when used in an OSGi container.

```java
Repository repository = Repository.create();

PhysicalTimeProvider timeProvider = new NTPServerTimeProvider();
repository.setPhysicalTimeProvider(timeProvider);

Journal journal = new MemoryJournal();
repository.setJournal(journal);

IndexEngine indexEngine = new MemoryIndexEngine();
repository.setIndexEngine(indexEngine);

LockProvider lockProvider = new MemoryLockProvider();
repository.setLockProvider(lockProvider);

repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{getClass().getPackage()}));
repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{getClass().getPackage()});

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
@Index
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
