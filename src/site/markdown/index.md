## Purpose
**TMS Movement Service** allows to create Movement orders for Transport Units in warehouses.

## Release

```
$ mvn deploy -Prelease,gpg
```

### Release Documentation

```
$ mvn package -DsurefireArgs=-Dspring.profiles.active=ASYNCHRONOUS,TEST -Psonar
$ mvn site scm-publish:publish-scm
```
