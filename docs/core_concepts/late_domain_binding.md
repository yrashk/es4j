# Late Domain Binding

In *classic* event sourcing systems, events are processed as they come and are applied to the domain model. However, this requires one to decide how events map onto the application's domain before the event can be processed. It also requires to create a number of separate aggregating message handlers that will handle events that should be grouped according to the model. Let's call it an "eager domain binding" method.

Late domain binding, on the other hand, postpones binding to the domain until a later point in time. This method allows to postpone the domain model decisions, as well as produce multiple, even conflicting, domain models over journalled events. ES4J's particular method of enabling it is *entity indexing*.

*Technically speaking, even the eager domain binding is in fact an extreme case of late one, with its delay between event message processing and binding being very short*

Also, ES4J has a method to enable eager domain binding by registering so called *entity subscribers* to the repository.
