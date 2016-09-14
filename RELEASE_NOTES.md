0.4.1
=====

This release simplifies index definitions for most of scenarios, dropping the need to
declare `queryOptions` attribute.

Also, this release has improvemed support for long-running commands, they no longer
block the repository. 

0.4.0
=====

A relatively significant update to 0.3.x.

Most notably, a few core APIs have changed.

`Entity`, `Command` and `Event` are now interfaces, and the classes were moved to to `StandardEntity`, `StandardCommand` and `StandardEvent`, respectively. For most of standard cases,
commands and events should subclass those. Also, `Model#id()` has been renamed to `Model#getId()`.

Index definition syntax has changed considerably, `SimpleIndex` and `MultiValueIndex` non-final public static fields should now be used, preferably using a lambda syntax to avoid the verbosity.

PostgreSQL journal, index engine and a lock provider have been implemented and are now suggested to be the default choice (instead of H2/MVStore).

Basic, experimental support for [Kotlin](https://kotlinlang.org) has been added.

For more details, check out the [change log](CHANGELOG.md#040).
