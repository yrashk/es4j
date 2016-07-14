# Event

Event is a statement of a fact that has occurred once the command that initiated
it, along with other events, has been successfully recorded.

Defining an event is quite similar to command, by subclassing `StandardEvent`:


```java
public class RestaurantRegistered extends StandardEvent {}
```
