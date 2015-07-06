Release Notes
=======

## 2.6.0
**New**
- Works with Neo4j 2.2.2.
- Support for basic authentication and authorization.

## 2.5.0
**New**
- Domain Queries - Query Concatenation.

**Fixed**
- Problem with list parameters in embedded database access.

## 2.4.0
**New**
- Domain Queries Part 3 (Collection Expressions - UNION, INTERSECTION) added.
- Improved support for multiple domains in a single database.

## 2.3.0
**New**
- Domain Queries Part 3 (Collection Expressions - SELECT, REJECT, COLLECT) added.

**Fixed**
- Support for Date subclasses (java.sql.Date, java.sql.Time, java.sql.Timestamp)
- Fixed bug accessing embedded databases

## 2.2.0
**New**
- Domain Queries Part 2 (Traversal Expressions) added.
- Mapping of inner classes - Domain Mapping now also supports inner classes

## 2.1.0
**New**
- Domain Queries Part 1 (Predicate Expressions) added.
- Mapping of java arrays - Domain Mapping now also supports java arrays

**Fixed**
- Guaranteed uniqueness of node labels - Domain Mapping: If classes in different packages have the same name (e.g. Person), the label of mapped nodes is guaranteed to be unique.