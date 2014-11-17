jcypher
=======

## Introduction

**JCypher** provides seamlessly integrated Java access to graph databases (Neo4J) at different levels of abstraction.

- At the bottom level a 'Native Java DSL' in form of a fluent Java API allows to intuitively and comfortably formulate queries against graph databases.
   The DSL (Domain Specific Language) is based on the CYPHER language. Hence the name JCypher.
   (The Cypher Language is developed as part of the Neo4J Graph Database by 'Neo Technology').
   The DSL provides all the power and expressiveness of the Cypher language.

- At the next level of abstraction, access to graph databases is provided based on a generic graph model.
   The model consists of nodes, relations, and paths, together with properties, labels, and types. While simple, the model allows to easily navigate and manipulate graphs.

- At the top level, arbitrarily complex business domains can be mapped to graph databases in a completely non-invasive way (not even annotations invading the business model).

- At this level **'Domain Queries'** provide the power and expressiveness of queries on a graph database, while being formulated on domain objects or on types of domain objects respectively.
   
- Additionally, JCypher provides database access in a uniform way to remote as well as to embedded databases (including in-memory databases).

## Documentation

Please have a look at: https://github.com/Wolfgang-Schuetzelhofer/jcypher/wiki for a more comprehensive documentation. There you will also find code snippets, references to samples, and tips on how
to get started with JCypher. Furthermore you can find a roadmap and you are informed about future directions of JCypher.

For more information about Neo4J have a look at: http://www.neo4j.org/
</br>For more information about the CYPHER language have a look at: http://docs.neo4j.org/chunked/stable/cypher-query-lang.html

## Setup

You may add JCypher as a Maven dependency:

```xml
<dependency>
  <groupId>net.iot-solutions.graphdb</groupId>
   <artifactId>jcypher</artifactId>
   <version>2.0.0</version>
</dependency>
```

## License & Copyright

Copyright (c) 2014 IoT-Solutions e.U.

License:
								Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/