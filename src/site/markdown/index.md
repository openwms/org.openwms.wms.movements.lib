## Purpose
This library contains the essential functionality of WMS Movements Service to manage and control `Movements` in _manual_ warehouses. It is
built as a library and part of the [WMS Movements Service microservice project](https://github.com/openwms/org.openwms.wms.movements.lib).

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
