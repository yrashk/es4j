# Overview

Evensourcing for Java (*ES4J*) captures [commands](command.md) as its primary input when they are published to a *repository*. A command is *request* for changes in the domain and may produce zero or more [events](event.md). It holds an internal state and may return a value upon its termination (*result*). Every command and every event (collectively "entities") have a unique identifier (UUID) and a [timestamp](http://rfc.eventsourcing.com/spec:6/GLC).

{% plantuml %}

interface Entity<Entity> {
  + UUID uuid()
  + Entity uuid(UUID)
  + HybridTimestamp timestamp()
  + Entity timestamp(HybridTimestamp)
}
interface Command<State, Result> {
  {abstract} EventStream<State> events(Repository)
  {abstract} Result result(State, Repository)
}

interface Event {

}

class EventStream<State> {
 + State getState()
 + Stream<Event> getStream()
}

Command <|- Entity
Command - EventStream : produces >
EventStream - Event : produces 0..n >
Event <|- Entity

package Application {
  class SpecificCommand {
    .. Properties ..
    + name()
    ...
    __ constructor __
    SpecificCommand(name, ...)
  }

  class SpecificEvent {

  }

}

SpecificCommand <|- Command
SpecificEvent <|- Event
{% endplantuml %}

ES4J's main command processing duties include: journalling events and commands,
indexing them, handling event production related exceptions and notifying
application's entity subscribers about newly journalled entities.

{% plantuml %}
@startuml
actor Application
box "ES4J"
  boundary Repository
  database Journal
  database "Index Engine"
end box
Application -> Repository: publish(command)
Repository -> Journal: command
Journal -> Journal: state, events := command.events()
Journal -> Repository: onCommandStateReceived
loop while more events available
  alt successful event production
  Journal -> Journal: journal event
  Journal -> Repository: onEvent
  Repository -> "Index Engine": index event in-memory
  Repository -> Application: any matching subscribers?
  Application -> Repository: [Subscriber]
  else exception thrown
  Journal -> Journal: wipe journalled events out
  Journal -> Journal: CommandTerminatedExceptionally event
  end
end
Journal -> Journal: journal command
Journal -> Journal: commit transaction
Journal -> Repository: onCommit
Repository -> "Index Engine": commit in-memory indices
Repository -> "Index Engine": index command
Repository -> Application: any matching subscribers for the command?
Application -> Repository: [Subscriber]
Repository -> Application: [Event, Command?] for subscribers
Repository -> Application: command.result(state, repository)
@enduml
{% endplantuml %}

The above model facilitates the application of so called [late domain binding](../core_concepts/late_domain_binding.md), a method for reducing the upfront work required to design an application and its domain model.
