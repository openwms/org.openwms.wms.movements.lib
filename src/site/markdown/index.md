## Purpose
**TMS Movement Service** allows creating manual `Movement` tasks for `TransportUnits` in warehouses. In general a `Movement` always refer to
a `TransportUnit` that needs to be moved from a source `Location` to a target `Location`.

## Features

* Different types of Movements exist: Inbound, Outbound, Replenishment, Reconciliation etc.
* The type of Movement can be given at creation, or it is trying to be resolved from the Movement attributes
* Movements can be grouped into groups

## Release

```
$ mvn deploy -Prelease,gpg
```

### Release Documentation

```
$ mvn package -DsurefireArgs=-Dspring.profiles.active=ASYNCHRONOUS,TEST -Psonar
$ mvn site scm-publish:publish-scm
```
