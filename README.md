# Eventchain

Eventchain is an **event sourcing** framework for Java. Instead of mutating data in a database, it stores all changes
(events) and causes (commands). This facilitates rapid application development and evolution by mitigating the inability
to predict how future needs will drive data shape requirements as all the causal information is persisted. It also provides a foundation
for deep analytics, data recovery, audit trails and other associated benefits.
