/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.samples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrPath;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.writer.Format;
import iot.jcypher.result.JcError;
import iot.jcypher.result.Util;

/**
 * This JCypher sample is constructing and querying the 'Movie Database'.
 * It is based on the 'Movie Database' sample, provided in the Neo4j Manual in the Tutorials section
 */
public class MovieDatabase_Experimental {

	private static IDBAccess dbAccess;
	
	public static void main(String[] args) {
		
		/** initialize connection to a Neo4j database */
		initDBConnection();
		
		/** execute queries against the database */
		createMovieDatabase();
		createAdditionalNodes();
//		queryNodeCount();
//		queryLikes();
//		queryMovies();
//		queryMovieGraph();
//		queryMovieGraph_error_01();
//		queryMovieGraph_02();
		queryMovieGraph_modify();
		
		/** close the connection to a Neo4j database */
		closeDBConnection();
	}
	
	/**
	 * Create the movie database
	 */
	static void createMovieDatabase() {
		JcNode matrix1 = new JcNode("matrix1");
		JcNode matrix2 = new JcNode("matrix2");
		JcNode matrix3 = new JcNode("matrix3");
		JcNode keanu = new JcNode("keanu");
		JcNode laurence = new JcNode("laurence");
		JcNode carrieanne = new JcNode("carrieanne");
		
		/**
		 * -----------------------------------------------------------------
		 * Create the movie database
		 */
		String queryTitle = "CREATE MOVIE DATABASE";
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(matrix1).label("Movie")
						.property("title").value("The Matrix")
						.property("year").value("1999-03-31"),
				CREATE.node(matrix2).label("Movie")
						.property("title").value("The Matrix Reloaded")
						.property("year").value("2003-05-07"),
				CREATE.node(matrix3).label("Movie")
						.property("title").value("The Matrix Revolutions")
						.property("year").value("2003-10-27"),
				CREATE.node(keanu).label("Actor")
						.property("name").value("Keanu Reeves")
						.property("like").value(8.5)
						.property("numbers").value(1, 2, 3),
				CREATE.node(laurence).label("Actor")
						.property("name").value("Laurence Fishburne")
						.property("like").value(7),
				CREATE.node(carrieanne).label("Actor")
						.property("name").value("Carrie-Anne Moss")
						.property("like").value(8.3),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix1),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix2),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix3),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix1),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix2),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix3),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix1),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix2),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix3)
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
		     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		JcQueryResult result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
	}
	
	/**
	 * Create additional nodes in the movie database
	 */
	static void createAdditionalNodes() {
		JcNode escape_plan = new JcNode("escape_plan");
		JcNode arnie = new JcNode("arnie");
		JcNode sylvester = new JcNode("sylvester");
		
		/**
		 * -----------------------------------------------------------------
		 * Create the movie database
		 */
		String queryTitle = "CREATE ADDITIONAL NODES";
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(escape_plan).label("Movie")
						.property("title").value("Escape Plan")
						.property("year").value("2013-03-31"),
				CREATE.node(arnie).label("Actor")
						.property("name").value("Arnold Schwarzenegger")
						.property("like").value(9.0),
				CREATE.node(sylvester).label("Actor")
						.property("name").value("Sylvester Stalone")
						.property("like").value(9.0),
				CREATE.node(arnie).relation().out().type("ACTS_IN").property("role").value("Prisoner").node(escape_plan),
				CREATE.node(sylvester).relation().out().type("ACTS_IN").property("role").value("Prisoner").node(escape_plan)
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
		     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		JcQueryResult result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
	}
	
	/**
	 * Check how many nodes we have
	 */
	static void queryNodeCount() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "COUNT NODES";
		JcNode n = new JcNode("n");
		JcNumber nCount = new JcNumber("nCount");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(n),
				RETURN.count().value(n).AS(nCount)
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		List<BigDecimal> nr = result.resultOf(nCount);
		return;
	}
	
	static void queryLikes() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "GATHER LIKES";
		JcNode n = new JcNode("n");
		JcNumber like = new JcNumber("like");
		JcCollection nums = new JcCollection("nums");
		JcString nm = new JcString("nm");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(n).label("Actor"),
				RETURN.value(n.property("like")).AS(like),
				RETURN.value(n.property("numbers")).AS(nums),
				RETURN.value(n.property("name")).AS(nm),
				RETURN.value(n.property("name"))
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		List<BigDecimal> likes = result.resultOf(like);
		List<List<?>> numbers = result.resultOf(nums);
		List<?> names = result.resultOf(n.property("name"));
		List<String> nms = result.resultOf(nm);
		return;
	}
	
	/**
	 * Query all movies
	 */
	static void queryMovies() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "MOVIES";
		JcNode movie = new JcNode("movie");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(movie).label("Movie"),
				RETURN.value(movie)
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
	}
	
	/**
	 * Query the entire graph
	 */
	static void queryMovieGraph() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "MOVIE_GRAPH";
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		query = new JcQuery();
//		query.setClauses(new IClause[] {
//				MATCH.path(p).node(n).relation(r).out().node(),
//				//RETURN.value(n.property("name"))
//				RETURN.ALL()
//		});
		query.setClauses(new IClause[] {
				MATCH.path(p).node().relation().out().type("ACTS_IN").node()
						.relation().in().type("ACTS_IN").node(),
				RETURN.ALL()
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		GrNode sNode;
		GrNode eNode;
		
		Graph graph = result.getGraph();
		List<GrPath> pr = result.resultOf(p);
		// TODO extract as unit test, check states
		for (GrPath path : pr) {
			List<GrRelation> rels = path.getRelations();
			for (GrRelation rel : rels) {
				List<GrProperty> props = rel.getProperties();
				String typ = rel.getType();
				sNode = rel.getStartNode();
				GrProperty prop = sNode.addProperty("loaded", true);
				prop.setValue(false);
				prop.remove();
				eNode = rel.getEndNode();
				GrLabel lab = eNode.addLabel("Superstar");
				List<GrLabel> sLabels = sNode.getLabels();
				List<GrLabel> eLabels = eNode.getLabels();
				for (GrLabel el : eLabels) {
					el.remove();
				}
				props = props;
			}
			sNode = path.getStartNode();
			eNode = path.getEndNode();
			List<GrProperty> sprops = sNode.getProperties();
			List<GrProperty> eprops = eNode.getProperties();
			List<GrLabel> sLabels = sNode.getLabels();
			List<GrLabel> eLabels = eNode.getLabels();
			String tst = null;
		}
		
//		List<GrNode> nr = result.resultOf(n);
		List<GrRelation> rr = result.resultOf(r);
		for (GrRelation rel : rr) {
			List<GrProperty> rprops = rel.getProperties();
			sNode = rel.getStartNode();
			eNode = rel.getEndNode();
			List<GrProperty> sprops = sNode.getProperties();
			List<GrProperty> eprops = eNode.getProperties();
			String tst = null;
		}
		
		return;
	}
	
	static void queryMovieGraph_error_01() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "MOVIE_GRAPH";
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.node(n).byId(4),
				DO.DELETE(n)
		});
//		query.setClauses(new IClause[] {
//				MATCH.path(p).node().relation().out().type("ACTS_IN").node()
//						.relation().in().type("ACTS_IN").node(),
//				RETURN.ALL()
//		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		GrNode sNode;
		GrNode eNode;
		
		Graph graph = result.getGraph();
		List<GrPath> pr = result.resultOf(p);
		for (GrPath path : pr) {
			sNode = path.getStartNode();
			sNode.remove();
			String tst = null;
		}
		
		return;
	}
	
	/**
	 * Query the entire graph
	 */
	static void queryMovieGraph_02() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "MOVIE_GRAPH_02";
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		query = new JcQuery();
//		query.setClauses(new IClause[] {
//				MATCH.path(p).node(n).relation(r).out().node(),
//				//RETURN.value(n.property("name"))
//				RETURN.ALL()
//		});
		query.setClauses(new IClause[] {
				MATCH.path(p).node().relation().out().type("ACTS_IN").node()
						.relation().in().type("ACTS_IN").node(),
				RETURN.ALL()
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		GrNode sNode;
		GrNode eNode;
		GrRelation relat;
		
		Graph graph = result.getGraph();
		sNode = graph.createNode();
		eNode = graph.createNode();
		relat = graph.createRelation("LocalRelation", sNode, eNode);
		
		GrProperty prop = sNode.addProperty("state", "NEW");
		GrLabel lab = eNode.addLabel("Person");
		List<GrProperty> props = sNode.getProperties();
		List<GrLabel> labs = eNode.getLabels();
		
		prop.remove();
		lab.remove();
		props = sNode.getProperties();
		labs = eNode.getLabels();

		sNode.remove();
		eNode.remove();
		relat.remove();
		
		return;
	}
	
	static void queryMovieGraph_modify() {
		JcQuery query;
		JcQueryResult result;
		
		String queryTitle = "MOVIE_GRAPH";
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		query = new JcQuery();
//		query.setClauses(new IClause[] {
//				MATCH.path(p).node(n).relation(r).out().node(),
//				//RETURN.value(n.property("name"))
//				RETURN.ALL()
//		});
		query.setClauses(new IClause[] {
				MATCH.path(p).node().relation().out().type("ACTS_IN").node(),
						//.relation().in().type("ACTS_IN").node(),
				RETURN.ALL()
		});
		/** map to CYPHER statements and map to JSON, print the mapping results to System.out.
	     This will show what normally is created in the background when accessing a Neo4j database*/
		print(query, queryTitle, Format.PRETTY_3);
		
		/** execute the query against a Neo4j database */
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		
		/** print the JSON representation of the query result */
		print(result, queryTitle);
		
		GrNode sNode;
		GrNode eNode;
		GrNode sNode_2;
		GrNode eNode_2;
		GrNode eNode_3;
		
		List<GrPath> pr = result.resultOf(p);
		GrPath path = pr.get(0);
		List<GrRelation> rels = path.getRelations();
		GrRelation rel = rels.get(0);
		List<GrProperty> props = rel.getProperties();
		String typ = rel.getType();
		sNode = rel.getStartNode();
		sNode_2 = sNode;
		sNode.addProperty("loaded", true);
		sNode.addLabel("Superstar");
		eNode = rel.getEndNode();
		eNode.addLabel("BestMovie");
		GrProperty eProp = eNode.getProperties().get(0);
		eProp.remove();
		List<GrLabel> sLabels = sNode.getLabels();
		List<GrProperty> sProps = sNode.getProperties();
		List<GrLabel> eLabels = eNode.getLabels();
		List<GrProperty> eProps = eNode.getProperties();
		
		List<String> actorNames = new ArrayList<String>();
		GrRelation rel1 = null;
		GrRelation rel2 = null;
		for (GrPath pth : pr) {
			for (GrRelation rl : pth.getRelations()) {
				GrNode sn = rl.getStartNode();
				for (GrProperty prp : sn.getProperties()) {
					if (prp.getName().equals("name")) {
						String actorName = prp.getValue().toString();
						if (!actorNames.contains(actorName))
							actorNames.add(actorName);
					}
					if (prp.getName().equals("name") && prp.getValue().equals("Arnold Schwarzenegger")) {
						rel1 = rl;
						break;
					}
					if (prp.getName().equals("name") && prp.getValue().equals("Sylvester Stalone")) {
						rel2 = rl;
						break;
					}
				}
			}
		}
		
		sNode = rel1.getStartNode();
		eNode = rel1.getEndNode();
		sProps = sNode.getProperties();
		eProps = eNode.getProperties();
		GrNode s2Node = rel2.getStartNode();
		GrNode e2Node = rel2.getEndNode();
		List<GrProperty> s2Props = s2Node.getProperties();
		List<GrProperty> e2Props = e2Node.getProperties();
		rel1.remove();
		rel2.remove();
		sNode.remove();
		eNode.remove();
		s2Node.remove();
		e2Node.remove();
		
		Graph graph = result.getGraph();
		
		eNode_2 = graph.createNode();
		eNode_2.addLabel("Shop");
		eNode_2.addProperty("name", "Metro");
		GrRelation relNew = graph.createRelation("SHOPS_AT", sNode_2, eNode_2);
		relNew.addProperty("frequency", "daily");
		
		eNode_3 = graph.createNode();
		eNode_3.addLabel("Restaurant");
		eNode_3.addProperty("name", "Steak House");
		GrRelation relNew_2 = graph.createRelation("EATS_AT", sNode_2, eNode_3);
		relNew_2.addProperty("frequency", "weekly");
		
		if (graph.isModified()) {
			List<JcError> errs = graph.store();
			if (!errs.isEmpty())
				printErrors(errs);
		}
		
		boolean modified = graph.isModified();

//		sNode = path.getStartNode();
//		eNode = path.getEndNode();
//		sProps = sNode.getProperties();
//		eProps = eNode.getProperties();
//		sLabels = sNode.getLabels();
//		eLabels = eNode.getLabels();
//		String tst = null;
		
		return;
	}
	
	/**
	 * initialize connection to a Neo4j database
	 */
	private static void initDBConnection() {
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		// have a look at the DBProperties interface
		// the appropriate database access class will pick the properties it needs
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		/** connect to an in memory database (no properties are required) */
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		
		/** connect to remote database via REST (SERVER_ROOT_URI property is needed) */
		//dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
		
		/** connect to an embedded database (DATABASE_DIR property is needed) */
		//dbAccess = DBAccessFactory.createDBAccess(DBType.EMBEDDED, props);
	}
	
	/**
	 * close the connection to a Neo4j database
	 */
	private static void closeDBConnection() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
	}
	
	/**
	 * map to CYPHER statements and map to JSON, print the mapping results to System.out
	 * @param query
	 * @param title
	 * @param format
	 */
	private static void print(JcQuery query, String title, Format format) {
		System.out.println("QUERY: " + title + " --------------------");
		// map to Cypher
		String cypher = iot.jcypher.samples.Util.toCypher(query, format);
		System.out.println("CYPHER --------------------");
		System.out.println(cypher);
		
		// map to JSON
		String json = iot.jcypher.samples.Util.toJSON(query, format);
		System.out.println("");
		System.out.println("JSON   --------------------");
		System.out.println(json);
		
		System.out.println("");
	}
	
	/**
	 * print the JSON representation of the query result
	 * @param queryResult
	 */
	private static void print(JcQueryResult queryResult, String title) {
		System.out.println("RESULT OF QUERY: " + title + " --------------------");
		String resultString = Util.writePretty(queryResult.getJsonResult());
		System.out.println(resultString);
	}
	
	/**
	 * print errors to System.out
	 * @param result
	 */
	private static void printErrors(JcQueryResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------General Errors:");
		appendErrorList(result.getGeneralErrors(), sb);
		sb.append("\n---------------DB Errors:");
		appendErrorList(result.getDBErrors(), sb);
		sb.append("\n---------------end Errors:");
		String str = sb.toString();
		System.out.println("");
		System.out.println(str);
	}
	
	/**
	 * print errors to System.out
	 * @param result
	 */
	private static void printErrors(List<JcError> errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------Errors:");
		appendErrorList(errors, sb);
		sb.append("\n---------------end Errors:");
		String str = sb.toString();
		System.out.println("");
		System.out.println(str);
	}
	
	private static void appendErrorList(List<JcError> errors, StringBuilder sb) {
		int num = errors.size();
		for (int i = 0; i < num; i++) {
			JcError err = errors.get(i);
			sb.append('\n');
			if (i > 0) {
				sb.append("-------------------\n");
			}
			sb.append("codeOrType: ");
			sb.append(err.getCodeOrType());
			sb.append("\nmessage: ");
			sb.append(err.getMessage());
			if (err.getAdditionalInfo() != null) {
				sb.append("\nadditional info: ");
				sb.append(err.getAdditionalInfo());
			}
		}
	}
}
