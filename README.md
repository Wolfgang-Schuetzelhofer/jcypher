![](https://github.com/Wolfgang-Schuetzelhofer/jcypher/blob/master/src/test/resources/docs/jcypher_logo_3_long_1.png)
=======

[**JCypher Project Homepage**](http://wolfgang-schuetzelhofer.github.io/jcypher/)
## Introduction

**JCypher** provides seamlessly integrated Java access to graph databases (Neo4J) at different levels of abstraction. 
Starting top-down:

- At the topmost level of abstraction, JCypher allows to map **complex business domains** to graph databases.
   You can take an arbitrarily complex graph of domain objects (pojos (plain old java objects)) and store it
   in a straight forward way into a graph database for later retrieval.
   You do not need to modify your domain object classes in any way. You even do not add annotations.
   Moreover JCypher provides a default mapping so you don't have to write a single line of mapping code or of mapping configuration.

- At the same level of abstraction **'Domain Queries'** provide the power and expressiveness of queries on a graph database,
   while being formulated on **domain objects** or on types of domain objects respectively.
   The true power of Domain Queries comes from the fact, that the graph of domain objects is backed by a graph database.

- At the next lower level of abstraction, access to graph databases is provided based on a **generic graph model**.
  The model consists of nodes, relations, and paths, together with properties, labels, and types.
  While simple, the model allows to easily navigate and manipulate graphs.

- At the bottom level of abstraction, a **'Native Java DSL'** in form of a fluent Java API allows to intuitively
  and comfortably formulate queries against graph databases.
  The DSL (Domain Specific Language) is based on the CYPHER language. Hence the name JCypher.
  (The Cypher Language is developed as part of the Neo4J Graph Database by 'Neo Technology').
  The DSL provides all the power and expressiveness of the Cypher language.

- Additionally, JCypher provides **database access in a uniform way** to remote as well as to embedded databases (including in-memory databases).

## Documentation

Please have a look at: https://github.com/Wolfgang-Schuetzelhofer/jcypher/wiki for a more comprehensive documentation. There you will also find code snippets, references to samples, and tips on how
to get started with JCypher. Furthermore you can find a roadmap and you are informed about future directions of JCypher.

Additionally, a distinct samples project can be found at: https://github.com/Wolfgang-Schuetzelhofer/jcypher_samples.

For more information about Neo4J have a look at: http://www.neo4j.org/
</br>For more information about the CYPHER language have a look at: http://docs.neo4j.org/chunked/stable/cypher-query-lang.html

## Setup

You may add JCypher as a Maven dependency:

```xml
<dependency>
  <groupId>net.iot-solutions.graphdb</groupId>
   <artifactId>jcypher</artifactId>
   <version>2.7.1</version>
</dependency>
```
## JCypher on Maven Central
<a href="http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22net.iot-solutions.graphdb%22%20AND%20a%3A%22jcypher%22">Can be found here.</a>
## Release Notes (since 2.1.0)
<a href="https://github.com/Wolfgang-Schuetzelhofer/jcypher/blob/master/release_notes.md">Please look here.</a>

## License & Copyright

Copyright (c) 2014-2015 IoT-Solutions e.U.

License:
								Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/