package org.aksw.deer.plugin.example;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

public class InstanceCount {
	
	private int totalInstances;
	
	
	public long instanceCount(String instanceType, String knowledgeGraphName) {
		long count = 0; 
		
		if(knowledgeGraphName == "dbpedia") {
			count = totalInstanceDbpedia(instanceType);
		}else if (knowledgeGraphName == "yago") {
			count = totalInstanceYago(instanceType);
		}
		return count;
		
	}
	
	private int totalInstanceYago(String instanceType) {

		QueryExecution qe = QueryExecutionFactory.sparqlService("https://yago-knowledge.org/sparql/query",
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n"
						+ "SELECT (COUNT(?s) AS ?totalInstances)\r\n" + "WHERE { \r\n" + "?s rdf:type url:"
						+ instanceType + " .\r\n" + "}");
		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qe.close();
		return totalInstances;
	}
	
	private int totalInstanceDbpedia(String instanceType) {

		QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql",
				"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
						+ "WHERE { ?s rdf:type url:" + instanceType + ". } ");
		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> totalInstances  = qsol.getLiteral("totalInstances").getInt());
		qe.close();
		return totalInstances;
	}

}