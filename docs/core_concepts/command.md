# Command

Command is a request for changes in the domain. Unlike an [event](event.md), it is not a statement of fact as it might be rejected. For example, `RegisterRestaurant` command may or may not result in relevant events being produced.

Defining a command is pretty straightforward, through subclassing `StandardCommand<State, Result>`:

```java
@Value
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class RegisterRestaurant extends StandardCommand<RestaurantRegistered, Restaurant> {

    private String name;
    private Address address;
    private OpeningHours openDuring;
```

The type parameter signifies an optional "result" type that can be returned
once the command is successfully executed, by overriding the `result()`
method:

```java
@Override public Restaurant result(RestaurantRegistered restaurantRegistered, Repository repository) {
    return Restaurant.lookup(repository, restaurantRegistered.uuid()).get();
}
```

A more important part of any command is being able to generate events. This is done by overriding the `events()` method that returns a stream of events:

```java
@Override public EventStream<RestaurantRegistered> events() throws Exception {
    RestaurantRegistered restaurantRegistered = new RestaurantRegistered();
    NameChanged nameChanged = new NameChanged(restaurantRegistered.uuid(), name);
    AddressChanged addressChanged = new AddressChanged(restaurantRegistered.uuid(), address);
    Stream<WorkingHoursChanged> workingHoursChangedStream =
            Arrays.asList(DayOfWeek.values()).stream()
                  .map(dayOfWeek -> new WorkingHoursChanged(restaurantRegistered.uuid(),
                                                            dayOfWeek, Collections.singletonList(openDuring)));
    return EventStream.ofWithState(restaurantRegistered,
                                   Stream.concat(
                                     Stream.of(restaurantRegistered, nameChanged, addressChanged),
                                     workingHoursChangedStream
                                   ));
}
```
