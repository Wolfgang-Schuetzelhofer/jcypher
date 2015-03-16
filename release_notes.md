Release Notes
=======

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