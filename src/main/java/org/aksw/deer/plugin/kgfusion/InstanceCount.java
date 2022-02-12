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


/**
 * This class is use to count the instance of an entity
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class InstanceCount {

	private int totalInstances;

	/**
	 * @param path the path of the file
	 * @param resObj the restrictions
	 * @return
	 */
	public int countInstanceFromFile(String path, Restriction resObj) {

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
		}

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}

		 String instanceCountString = restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?" + resObj.variable
				+ ") AS ?totalInstances)\r\n" + "WHERE { " + restrictionQuery + "}";

		System.out.println("instanceCountString : " + instanceCountString);

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, path, Lang.NTRIPLES);

		Query query = QueryFactory.create(instanceCountString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		ResultSet resultsOne = ResultSetFactory.copyResults(qexec.execSelect());

		resultsOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qexec.close();

		return totalInstances;
	}
	
	

	/**
	 * It count the number of instance from URL end-point.
	 * 
	 * @param url the url of path
	 * @param prefixEntity the prefix of entity
	 * @param resObj the restriction object
	 * @return count the count of instances
	 */
	public int countInstanceFromURL(String url, PrefixEntity prefixEntity, Restriction resObj) {

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
		}

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}

		String instanceCountString = restrictionQueryPrefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "\r\n" + "SELECT (COUNT(?" + resObj.variable
				+ ") AS ?totalInstances)\r\n" + "WHERE { " + restrictionQuery + "}";

		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(Util.returnRedirectedURL(url), instanceCountString);

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qe.close();

		return totalInstances;
	}

}
