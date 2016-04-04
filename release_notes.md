Release Notes
=======

## 3.3.1
**Fixed**
- Illegal use of parameter-sets with MERGE, now using simple parameters.
- Added support for all Collection sub classes in domain model.

## 3.3.0
**New**
- Query DSL - DETACH DELETE
- JSONDBFacade, JSONDomainFacade

**Fixed**
- Bug in adding super interface to generic interface model.

## 3.2.1
**Fixed**
- NullpointerException in ResultHandler in certain query scenarios.

## 3.2.0
**New**
- Concurrency support - thread-safe invocation of 'IDomainAccess' and domain queries.
- Query DSL and Domain Query - String Operators: STARTS_WITH, CONTAINS (CONTAINS_string), ENDS_WITH
- Query DSL - Support for Literal Maps
- Upgrade to Neo4j 2.3.1

## 3.1.0
**New**
- Concurrency support - multi client access to the database, optimistic locking.
- Query DSL - MERGE, ON_CREATE, ON_MATCH
- Query DSL - CASE, WHEN, ELSE, END

**Fixed**
- Bug in close transaction when working with generic graph model.

## 3.0.0
**New**
- Generic Domain Model.

**Fixed**
- Domain Query - Select Expression on abstract types produced (in rare scenarios) wrong results.
- Domain Query - Count Expression within Select Expression on abstract types produced (in rare scenarios) exceptions.
- Unnecessary rounding of milliseconds when mapping Date instances removed.

## 2.7.1
**Fixed**
- Properties of nodes in generic graph model which are of type short were not stored to the graph database (fix in JSONWriter).
- Changed signature of store(List<Object> ..) to store(List<?> ..) in IDBAccess. Calls to store(..) with lists of types other than Object were not dispatched correctly.

## 2.7.0
**New**
- Transaction API. 

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