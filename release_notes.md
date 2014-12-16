Release Notes
=======

## 2.1.0
**New**
- Domain Queries Part 1 (Predicate Expressions) added.
- Mapping of java arrays - Domain Mapping now also supports java arrays

**Fixed**
- Guaranteed uniqueness of node labels - Domain Mapping: If classes in different packages have the same name (e.g. Person), the label of mapped nodes is guaranteed to be unique.