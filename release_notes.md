Release Notes
=======

## 4.1.1
**Fixed**
- Issue with labels retrieval when using AbstractEmbeddedDBAccess and forcing PlannerStrategy.COST.

## 4.1.0
**New**
- Collections in the Query API are now aware of their component type. This allows expressions like: **WHERE.valueOf(path.relations().last().type()).EQUALS("Some_Type")** or: **WHERE.has(path.nodes().head().label("Some_Label"))**

## 4.0.1
**Fixed**
- minHops(0) now works correctly. Previously it was translated to cypher *..  or * respectively for specifying a link's min cardinality to be zero. However in practice this returns a min cardinality of one. Now minHops(0) is translated to cypher *0.. which works correctly. This also changes the behaviour of Domain Query expression DISTANCE(min, max) with a min of 0. This works correctly now but has changed it's behaviour, so you might need to adopt some of your queries.

## 4.0.0
**New**
- Support for Neo4J 3.4.1.

## 3.9.0
**New**
- Support for Neo4J 3.3.1.
- Set planner strategy globally or individually on a per query basis.
- New - ...SET(...).byExpression() - to allow formulating the expression in form of Clauses (e.g. CASE .. WHEN ..)

## 3.8.0
**New**
- Support for Neo4J 3.2.2.
- Shutdown hooks in IDBAccess optional
- Public constructors for IDBAccess implementations allow more flexibly customizable configurations.  Although you are highly encouraged to use DBAccessFactory, you are no longer forced to do so.

## 3.7.0
**New**
- Support for Neo4J 3.1.1.
- Extended Authentication according to Neo4J's Bolt Driver specification

## 3.6.0
**New**
- Some improvements like simple construction of literal lists.
- Code compatibility with Java 1.7 (JCypher since 3.5.0 however is built for Java 1.8. This is required, so that JCypher- embedded and- in-memory db access works with Neo4J 3.0.x ).

## 3.5.0
**New**
- Support for Neo4J 3.0.x including the BOLT protocol

## 3.4.1
**Fixed**
- JC.coalesce(...) now returns a JcValue to be applicable in RETURN and WITH clauses.

## 3.4.0
**New**

- Store / retrieve Domain Queries to / from the graph db.
- JcQueryParameter to be used with Query DSL expressions.

## 3.4.0-M01
**New**

Extensions to the Query-DSL API
- JcCollection: add(...), addAll(...), get(...)
- JcValue: asNumber(), asString(), asBoolean()
- Property: values(...)

**Changed**

There is one API change which is not backward compatible:

In class **Property** there where two implementations of method value(...):
public &lt;E&gt; T **value(E value)**, and public &lt;E&gt; T **value(E... value)**.
This was ambiguous in case of a single argument and some language compilers like e.g. for Scala had problems with that.
This is now solved by using different method names:
public &lt;E&gt; T **value(E value)**, and public &lt;E&gt; T **values(E... value)**.
If you are using this API you are required to change the method name(s) in your code.


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