jcypher
=======

JCypher aims to provide seamlessly integrated Java access to graph databases (Neo4J) at different levels of abstraction.

- At the bottom level a 'Native Java DSL' in form of a fluent Java API allows to intuitively and comfortably formulate queries against graph databases.
   The DSL (Domain Specific Language) is based on the Cypher language. Hence the name JCypher.
   (The Cypher Language is developed as part of the Neo4J Graph Database by 'Neo Technology').
   The DSL provides all the power and expressiveness of the Cypher language.

- At the next level of abstraction, access to graph databases is provided based on a generic graph model.
   The model consists of nodes, relations, and paths, together with properties, labels, and types. While simple, the model allows to easily manipulate graphs.

- At the top level, arbitrarily complex business domains can be mapped to graph databases in a completely non-invasive way (not even annotations invading the business model).
   Additionally, JCypher provides database access in a uniform way to remote as well as to embedded databases (including in-memory databases).
