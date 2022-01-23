package org.aksw.deer.plugin.kgfusion;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class InstanceCount {

	private int totalInstances;

	public int countInstanceFromFile(String path, Restriction resObj) {

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
		}

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}
		System.out.println("restrictionQueryPrefix: \n" + restrictionQueryPrefix);

		System.out.println(restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
				+ "WHERE { " + restrictionQuery + "}");
		String instanceCountString = restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?" + resObj.variable
				+ ") AS ?totalInstances)\r\n" + "WHERE { " + restrictionQuery + "}";

		System.out.println("instanceCountString : \n" + instanceCountString);

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, path, Lang.NTRIPLES);

		Query query = QueryFactory.create(instanceCountString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		ResultSet resultsOne = ResultSetFactory.copyResults(qexec.execSelect());

		resultsOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qexec.close();

		return totalInstances;
	}

	public int countInstanceFromURL(String url, PrefixEntity prefixEntity, Restriction resObj) {

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
			// ?t w3199:type scMo:Movie
			// System.out.println(" *ali* :" +list);

			// System.out.println(" *ali* :" + list.key + list.value);
		}
		System.out.println("restrictionQuery: \n" + restrictionQuery);

//------------------------

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}
		System.out.println("restrictionQueryPrefix: \n" + restrictionQueryPrefix);

		System.out.println(restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
				+ "WHERE { " + restrictionQuery + "}");
		String instanceCountString = restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?" + resObj.variable
				+ ") AS ?totalInstances)\r\n" + "WHERE { " + restrictionQuery + "}";

		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(Util.getFinalRedirectedUrl(url), instanceCountString);

		System.out.println("urlurl:" + url);

		System.out.println("instanceCountStringinstanceCountString:\n" + instanceCountString);
		/*
		 * QueryExecution qe = null; qe =
		 * QueryExecutionFactory.sparqlService(getFinalRedirectedUrl(url),
		 * restrictionQueryPrefix +
		 * "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
		 * "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" +
		 * "SELECT (COUNT(?s) AS ?totalInstances)\r\n" + "WHERE { " + restrictionQuery +
		 * "}" );
		 */

		// System.exit(0);

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qe.close();

		return totalInstances;
	}

}
