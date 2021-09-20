package org.aksw.deer.plugin.example;

import java.io.File;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class Coverage {

	public int calculate(String type, String link) {

		if (type == "sparql") {
			return endpointSparql(link);
		} else if (type == "NT") {
			return endpointNTFile(link);
		} else if (type == "Turtle") {
		}
		return 3;
	}

	private int endpointSparql(String link) {
		
		if (!checkFileExist(link)) {
			//throw FileNotFoundException; 
		}

		long size;

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, "F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt", Lang.NTRIPLES);

		// RDFDataMgr.read(model, inputStream, ) ;

		System.out.println(" meAtIt1 : " + model);
		size = model.size();
		System.out.println(" meAtIt1size  : " + size);

		String queryString1 = "PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "PREFIX url: <http://schema.org/>\r\n"
				+ "\r\n" + "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
				// + " ?instance rdf:type url:Movie .\r\n"
				+ "  ?instance ?predicate ?o .\r\n" + "  FILTER(isLiteral(?o)) \r\n" + "} \r\n"
				+ "GROUP BY ?predicate\r\n" + "order by desc ( ?count )\r\n" + "LIMIT 10";

		Query query = QueryFactory.create(queryString1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		System.out.println("result 007 : " + results);
		ResultSetFormatter.out(System.out, results);
		// System.out.println(((Statement) model).getSubject());
		System.exit(0);
		return 3;
	}

	private int endpointNTFile(String link) {

		return 3;
	}

	private boolean checkFileExist(String link) {
		File file = new File(link);
		if (!file.isDirectory())
			file = file.getParentFile();

		if (file.exists()) {
			return true;
		}
		return true;
	}

}
