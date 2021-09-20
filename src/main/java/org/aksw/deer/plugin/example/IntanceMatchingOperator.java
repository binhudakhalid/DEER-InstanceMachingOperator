package org.aksw.deer.plugin.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LimesResult;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.serializer.ISerializer;
import org.aksw.limes.core.io.serializer.SerializerFactory;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class IntanceMatchingOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(IntanceMatchingOperator.class);
	public HashMap<String, String> prefixMap;
	public HashMap<String, Integer> propertyMap;
	public HashMap<String, Double> coverageMap;
	// public HashMap<String, String> propertiesPrefixesSource;
	public List<PrefixEntity> propertiesPrefixesSource;

	// public List<PropertyEntity> propertiesList;
	public List<PropertyEntity> propertiesListSource;
	public List<PropertyEntity> propertiesListTarget;

	public int totalInstances;

	Set<String> entityListFile;

	public static Property Coverage = DEER.property("coverage");
	public static Property MaxLimit = DEER.property("maxLimit");

	public IntanceMatchingOperator() {
		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder().declareProperty(Coverage).declareProperty(MaxLimit)
				.declareValidationShape(getValidationModelFor(IntanceMatchingOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3
		// commiting 123..
		String coverage = getParameterMap().getOptional(Coverage).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("did not able to find coverage");
		System.out.println(" drecipient-d coverage: " + coverage);
		String maxLimit = getParameterMap().getOptional(MaxLimit).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("did not able to find maxLimit param ");
		System.out.println(" drecipient-d maxLimit: " + maxLimit);
		// PrefixUtility PrefixUtility = new PrefixUtility();

		//

		// getEntitiesFromFile("F:\\Newfolder\\LIMES\\t\\lov_linkeddata_es_dataset_lov.nt");
		// test("F:\\Newfolder\\LIMES\\t\\lov_linkeddata_es_dataset_lov.nt");
//		System.exit(0);
		// getEntitiesFromFile("F:\\Newfolder\\LIMES\\t\\dbtune_org_bbc_peel_sparql.nt");
		// System.exit(0);

		propertiesListSource = getPropertiesFromFile("data/data_nobelprize_org.nt");

		propertiesListTarget = getPropertiesFromFile("data/lov_linkeddata_es_dataset_lov.nt");

		System.out.println("alibaba propertiesListSource: " + propertiesListSource);

		System.out.println("alibaba ---------\n : " + propertiesListTarget);

		// getEntitiesFromFile("1");

		// public List<PropertyEntity> getPropertiesFromFile(String entity) {

		System.out
				.println("I out in propertiesListSource:: propertiesList00 :" + propertiesListSource.get(0).toString());
		System.out.println("I out in propertiesListSource:: size  :" + propertiesListSource.size());

		// 9/
		// System.exit(0);
		//
		countEntityPredicate();

		// calculateCoverage
		calculateCoverage();
		System.out.println(" coverageMap1 " + coverageMap);

		/*
		 * int tempTotal = totalInstance("Movie"); System.out.println("THe Count is: " +
		 * tempTotal); countEntityPredicate();
		 */

		// dynamicPrefix(); for sparlEndpoint

		// Querying through Sparql to count the number of predicate of the entity
		// -countEntityPredicate();

		Model model = ModelFactory.createDefaultModel();

		String sourceType = "NT";
		String targetType = "NT";

		// sourceTarget is NT File

		if (sourceType == "NT") {
			// calculateCoverageForNTFile("F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt");
		} else if (sourceType == "SPARQL") {

		}

		if (targetType == "NT") {
			// calculateCoverageForNTFile("F:\\Newfolder\\LIMES\\t\\lov_linkeddata_es_dataset_lov.nt");
		} else if (targetType == "SPARQL") {

		}

		// int abc = totalInstanceTarget("Movie");
		// System.out.println("abcd : " + abc);

		// countEntityPredicateTarget();

		// DOn't need to uncomment these line as you don't want
		// to run as the output is already is saved in 002accepted.nt
		// and it takes atleast 1 hour to execute.

		Configuration con = createLimeConfigurationFile();
		callLimes(con);

		// File initialFile = new File("001accepted.nt");
		// InputStream targetStream = null;

		// 002accepted.nt file contains the output of LIMES
		// between movies from yago and films from Dbpedia
		/*
		 * String filename = "002accepted.nt"; File file = new File(filename); String
		 * content = null; try { content = FileUtils.readFileToString(file, "UTF-8");
		 * FileUtils.write(file, content, "UTF-8"); System.out.println(" a nc d a-2 ");
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		Model ourModel = RDFDataMgr.loadModel("002accepted.nt");

//		Model model1 = ModelFactory.createDefaultModel();
//		RDFDataMgr.read(model1, "accepted.nt", Lang.NT); // RDFDataMgr.read(model, inputStream, ) ;
//		System.out.println(" Model1a " + model1 );

		return List.of(ourModel);
	}

	public Configuration createLimeConfigurationFile() {

		// Creating Limes configuration Object
		Configuration conf = new Configuration();

		dynamicPrefix();

		List<String> sourcePropertylist = new ArrayList<String>();
		List<String> srcPropertylist = new ArrayList<String>();
		List<String> targetPropertylist = new ArrayList<String>();

		for (PropertyEntity list : propertiesListSource) {

			conf.addPrefix(list.key, list.value);
			System.out.println("debug : + " + list.key + " " + list.value);

			srcPropertylist.add(list.key + ":" + list.propertyName);
			System.out.println();
		}

		conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		conf.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		conf.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		KBInfo src = new KBInfo();

		src.setId("sourceId");
		src.setEndpoint("data/data_nobelprize_org.nt");
		src.setVar("?s");
		src.setPageSize(-1);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?s rdf:type xmfo:Person" })));
		// src.addOptionalProperty(sourcePropertylist);
		// srcPropertylist
		System.out.println(" BINHUDA : " + srcPropertylist);
		src.setProperties(srcPropertylist);// Arrays.asList( strAB
		// src.setOptionalProperties(sourcePropertylist);
		src.setType("NT");

		Map<String, String> prefixes = new HashMap<String, String>();

		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixes.put("z1", "http://dbpedia.org/ontology/");
		prefixes.put("z2", "http://xmlns.com/foaf/0.1/");
		prefixes.put("z3", "http://dbpedia.org/property/");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		// http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		// setting prefix for source
		for (PropertyEntity list : propertiesListSource) {
			// adding Prefix
			prefixes.put(list.key, list.value);
			System.out.println("debug new prefixes.put : + " + list.key + " " + list.value);
		}
		System.out.println("prefixMap length : " + prefixMap.size());
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

		src.setPrefixes(prefixes);

		HashMap<String, String> tempHashMap = new HashMap<String, String>();
		tempHashMap.put("rdfs:label", "");
		LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<String, Map<String, String>>();
		functions.put("rdfs:label", tempHashMap);
		src.setFunctions(functions);

		conf.setSourceInfo(src);

		System.out.println("I out in :: size  aliS :" + propertiesListSource.size());

		// sourcePropertylist asd

		Map<String, String> targetPrefixesMap = new HashMap<String, String>();
		targetPrefixesMap.put("owl", "http://www.w3.org/2002/07/owl#");
		targetPrefixesMap.put("z1", "http://dbpedia.org/ontology/");
		targetPrefixesMap.put("z2", "http://xmlns.com/foaf/0.1/");
		targetPrefixesMap.put("z3", "http://dbpedia.org/property/");
		targetPrefixesMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		// setting prefix for target
		for (PropertyEntity list : propertiesListTarget) {

			conf.addPrefix(list.key, list.value);

			targetPrefixesMap.put(list.key, list.value);

			targetPropertylist.add(list.key + ":" + list.propertyName);
			System.out.println(
					"debug new target  : list.key  + " + list.key + " list.propertyName:  " + list.propertyName);
		}

		KBInfo target = new KBInfo();
		target.setId("targetId");
		target.setEndpoint("data/lov_linkeddata_es_dataset_lov.nt");
		target.setVar("?z");
		target.setPageSize(-1);
		target.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?z rdf:type xmfo:Person" })));

		/*
		 * targetPropertylist.remove(1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 * targetPropertylist.remove(targetPropertylist.size() - 1);
		 */
		ArrayList<String> al1 = new ArrayList<String>();
		al1.add("xmfo:name");
		// al1.add("");
		target.setProperties(al1);
		// target.setProperties(al1);
		// target.setOptionalProperties(targetPropertylist);

		// System.out.println("meme12 targetPropertylist : " + targetPropertylist);
		// System.out.println("meme1 srcPropertylist : " + srcPropertylist);
		// Arrays.asList(new String[] { "rdfs:label", "pudc:description","xmfo:name"

		// })); //, "xmfo:name"
		target.setPrefixes(targetPrefixesMap);

		// d
		System.out.println("meme1 targetPrefixesMap : " + targetPrefixesMap);
		// System.out.println("meme1 targetPrefixesMap : " + srcPrefixesMap);

		target.setFunctions(functions);
		target.setType("NT");
		conf.setTargetInfo(target);

		// Set either Metric or MLALGORITHM
		MLImplementationType mlImplementationType = MLImplementationType.UNSUPERVISED;
		conf.setMlAlgorithmName("wombat simple");
		conf.setMlImplementationType(mlImplementationType);

		LearningParameter learningParameter = new LearningParameter();
		learningParameter.setName("max execution time in minutes");
		learningParameter.setValue(60);

		List<LearningParameter> mlAlgorithmParameters = new ArrayList<>();
		mlAlgorithmParameters.add(learningParameter);

		conf.setMlAlgorithmParameters(mlAlgorithmParameters);

		// Acceptance
		conf.setAcceptanceThreshold(0.9);

		conf.setAcceptanceFile("accepted.nt");
		conf.setAcceptanceRelation("owl:sameAs");

		// Review
		conf.setVerificationThreshold(0.1);
		conf.setVerificationFile("reviewme.nt");
		conf.setVerificationRelation("owl:sameAs");

		// EXECUTION
		conf.setExecutionRewriter("default");
		conf.setExecutionPlanner("default");
		conf.setExecutionEngine("default");

		// Output format CSV etc
		conf.setOutputFormat("NT"); // NT or TTL

		System.out.println("lool me conf " + conf);
		return conf;
	}

	public void callLimes(Configuration config) {

		String limesOutputLocation = new File("").getAbsolutePath();

		LimesResult mappings = Controller.getMapping(config);
		String outputFormat = config.getOutputFormat();
		ISerializer output = SerializerFactory.createSerializer(outputFormat);

		output.setPrefixes(config.getPrefixes());

		String workingDir = limesOutputLocation;
		File verificationFile = new File(workingDir, config.getVerificationFile());
		File acceptanceFile = new File(workingDir, config.getAcceptanceFile());

		output.writeToFile(mappings.getVerificationMapping(), config.getVerificationRelation(),
				verificationFile.getAbsolutePath());

		output.writeToFile(mappings.getAcceptanceMapping(), config.getAcceptanceRelation(),
				acceptanceFile.getAbsolutePath());
	}


	public void dynamicPrefix() {

		prefixMap = new HashMap<String, String>();// Creating HashMap

		String prefix, prefixValue;
		try {

			// We will get this data from Ontology team eventually
			File tempDataFile = new File("EntityFileSource.ttl");
			Scanner myReader = new Scanner(tempDataFile);
			// We need this loop to run for every line as we don't know where the prefix can
			// be found in the data file.
			while (myReader.hasNextLine()) {
				String Line = myReader.nextLine();
				if (Line.contains("@prefix")) {
					prefix = Line.substring(Line.indexOf(" "), Line.indexOf(":"));
					System.out.println("prefix:::: " + prefix);

					// this should will done in the new util method
					prefixValue = Line.substring(Line.indexOf("<") + 1, Line.indexOf(">"));// .replaceAll(":",
					System.out.println("prefixValue:::: " + prefixValue);

					prefixMap.put(prefix, prefixValue);
				}
			}

			Scanner myReader2 = new Scanner(tempDataFile);

			while (myReader2.hasNextLine()) {

				String Line = myReader2.nextLine();
				String prefixKey = null, prefixV = null;
				String predicatePrefixValue;

				if (Line.contains("@prefix")) {
					System.out.println("Contains @prefix");
				} else if (Line.contains("<http://")) {
					String subjecturl = Line.substring(Line.indexOf("<") + 1, Line.indexOf(">"));

					// @prefix prov: <http://www.w3.org/ns/prov#>
					if (subjecturl.contains("#")) {

						URL aURL = new URL(subjecturl);
						String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
						prefixV = temp.substring(0, temp.lastIndexOf('/') + 1);

						/// creating prefix key
						// prefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 2);

						if (aURL.getHost().contains("www.")) {
							prefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
						} else {
							prefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
						}

						predicatePrefixValue = subjecturl.substring(0, subjecturl.indexOf("#") + 1);
						String predicatePrefixValue2 = subjecturl.substring(subjecturl.indexOf("#") + 1,
								subjecturl.length());

						System.out.println("RedALret prefixKey  :" + prefixKey);
						System.out.println("RedALret predicatePrefixValue :" + predicatePrefixValue);
						System.out.println("RedALret predicatePrefixValue2 :" + predicatePrefixValue2);

					} else {

						URL aURL = new URL(subjecturl);
						String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
						predicatePrefixValue = temp.substring(0, temp.lastIndexOf('/') + 1);
						/// creating prefix key
						// prefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 2);
						if (aURL.getHost().contains("www.")) {
							prefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
						} else {
							prefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
						}
					}

					prefixMap.put(prefixKey, predicatePrefixValue);
				}

			}
			System.out.println(" let check prefixMap " + prefixMap);

			myReader.close();
			myReader2.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		// return prefixMap;
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// finding the number of records having the specific property
	// save it to hashMap.
	// Query returns the number of times every property is present in all records.
	public void countEntityPredicate() {

		propertyMap = new HashMap<String, Integer>();

		QueryExecution qe = QueryExecutionFactory.sparqlService(

				"http://dbpedia.org/sparql",
				"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n"
						+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
						+ "  ?instance rdf:type url:Movie .\r\n" + "  ?instance ?predicate ?o .\r\n"
						+ "  FILTER(isLiteral(?o)) \r\n" + "} \r\n" + "GROUP BY ?predicate\r\n"
						+ "order by desc ( ?count )\r\n" + "LIMIT 10");

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		resultOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();
			String predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2;
			URL aURL = null;
			if (predicate.contains("#")) {
				// http://www.w3.org/2002/07/owl#sameAs=903475
				System.out.println("****************-URL with Hash********************");
				System.out.println("predicate : " + predicate);

				predicatePrefixValue2 = predicate.substring(predicate.indexOf("#") + 1, predicate.length());

				/// creating prefix key
				aURL = null;
				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					predicatePrefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
				} else {
					predicatePrefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
				}

				predicatePrefixValue = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath() + "#";
				System.out.println("-------------------------------------------------");
			} else {
				System.out.println("****************-URL without Hash********************");
				System.out.println("predicate : " + predicate);

				// predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2;

				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					predicatePrefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
				} else {
					predicatePrefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
				}

				String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
				predicatePrefixValue = temp.substring(0, temp.lastIndexOf('/') + 1);
				predicatePrefixValue2 = predicate.substring(predicate.lastIndexOf("/") + 1, predicate.length());

			}

			propertyMap.put(qsol.getResource("predicate").toString(), qsol.getLiteral("count").getInt());
			System.out.println("propertyMap10 : " + qsol.getResource("predicate").toString());

		});

		System.out.println("Here is the log propertyMap : " + propertyMap);

		resultOne.forEachRemaining(qsol -> System.out.println("khad2 : " + qsol.getLiteral("predicate").getInt()));
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		System.out.println("after countEntityPredicate");
		qe.close();

	}

	// finding the number of records having the specific property
	// save it to hashMap.
	// Query returns the number of times every property is present in all records.
	public void countEntityPredicateTarget() {

		propertiesPrefixesSource = new ArrayList<PrefixEntity>();
		propertyMap = new HashMap<String, Integer>();

		QueryExecution qe = QueryExecutionFactory.sparqlService("https://yago-knowledge.org/sparql/query",
				"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n"
						+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE{\r\n"
						+ "?instance rdf:type url:Movie . \r\n" + "?instance ?predicate ?o .\r\n"
						+ "FILTER(isLiteral(?o))   \r\n" + "} \r\n" + "GROUP BY ?predicate\r\n"
						+ "order by desc ( ?count ) \r\n" + "LIMIT 10");

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		resultOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();
			String predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2;
			URL aURL = null;
			if (predicate.contains("#")) {
				// http://www.w3.org/2002/07/owl#sameAs=903475

				predicatePrefixValue2 = predicate.substring(predicate.indexOf("#") + 1, predicate.length());

				/// creating prefix key
				aURL = null;
				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					predicatePrefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
				} else {
					predicatePrefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
				}

				predicatePrefixValue = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath() + "#";
				PrefixEntity prefix = new PrefixEntity(predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2);

				propertiesPrefixesSource.add(prefix);

			} else {
				// "****************-URL without Hash********************");

				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					predicatePrefixKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
				} else {
					predicatePrefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
				}

				String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
				predicatePrefixValue = temp.substring(0, temp.lastIndexOf('/') + 1);
				predicatePrefixValue2 = predicate.substring(predicate.lastIndexOf("/") + 1, predicate.length());

				System.out.println("predicatePrefixValue : " + predicatePrefixValue);
				System.out.println("predicatePrefixValue2 : " + predicatePrefixValue2);
				System.out.println("predicatePrefixKey : " + predicatePrefixKey);
				PrefixEntity prefix = new PrefixEntity(predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2);
				propertiesPrefixesSource.add(prefix);

			}
			propertyMap.put(qsol.getResource("predicate").toString(), qsol.getLiteral("count").getInt());

		});

		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		System.out.println("after countEntityPredicate");
		qe.close();

	}

	// Calculating coverage and putting it in "coverageMap" hashMap
	public void calculateCoverage() {

		coverageMap = new HashMap<String, Double>();

		double tempTotal = totalInstance("Movie");
		System.out.println("tempTotal : " + tempTotal);
		System.out.println(" H1e1r1e is the log propertyMap : " + propertyMap);

		for (Entry<String, Integer> entry : propertyMap.entrySet()) {
			String prefixName = entry.getKey();
			int prefixValue = entry.getValue();
			// prefixes.put(prefixName, prefixValue);
			// calculate coverage
			Double tempCoverage = (double) prefixValue / tempTotal;
			coverageMap.put(prefixName, tempCoverage);

		}

	}

	// Finding total number of instances of an entity like Movie, Film, Book
	public int totalInstance(String instanceType) {

		QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql",
				"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
						+ "WHERE { ?s rdf:type url:" + instanceType + ". } ");

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		qe.close();

		return totalInstances;
	}

	// Finding total number of instances of an entity like Movie, Film, Book
	public int totalInstanceTarget(String instanceType) {

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

	public Set<String> getEntitiesFromFile(String link) {
		entityListFile = new HashSet<String>();
		double size = 0;

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, link, Lang.NTRIPLES);

		size = model.size();
		System.out.println("size :::: " + size);
		if (size < 1) {
			System.out.println("File is empty. size :" + size);
		}

		Property predicateRDFType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			if (predicate.toString().equals(predicateRDFType.toString())) {
				System.out.println("found enitity : ");
				entityListFile.add(object.toString());
			}
			System.out.println("predicate : " + predicate);
		}
		System.out.println(" entity list : " + entityListFile);

		return entityListFile;
	}

	/*
	 * Takes entity as input return list of properties from file
	 * 
	 */
	public List<PropertyEntity> getPropertiesFromFile(String entity) {

		// if (!checkFileExist(link)) {
		// throw FileNotFoundException;
		// }

		long size = 0;
		List<PropertyEntity> propertiesListTemp = new ArrayList<PropertyEntity>();

		Model model = ModelFactory.createDefaultModel();

		RDFDataMgr.read(model, entity, Lang.NTRIPLES);

		String queryString1 = "PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "PREFIX url: <http://schema.org/>\r\n"
				+ "\r\n" + "PREFIX url1: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
				+ "  ?instance rdf:type url1:Person .\r\n" + "  ?instance ?predicate ?o .\r\n"
				+ "  FILTER(isLiteral(?o)) \r\n" + "} \r\n" + "GROUP BY ?predicate\r\n" + "order by desc ( ?count )\r\n"
				+ "LIMIT 10";

		// JUST FOR DEBUG remove before commit
		Query query1 = QueryFactory.create(queryString1);
		QueryExecution qexec1 = QueryExecutionFactory.create(query1, model);
		ResultSet results = qexec1.execSelect();
		System.out.println("result 009 : " + results);
		ResultSetFormatter.out(System.out, results);
		///

		Query query = QueryFactory.create(queryString1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		// ResultSet results = qexec.execSelect();
		// System.out.println("result 009 : " + results);
		// ResultSetFormatter.out(System.out, results);
		// System.out.println(((Statement) model).getSubject());

		// ADDING HERE
		ResultSet resultsOne = ResultSetFactory.copyResults(qexec.execSelect());

		resultsOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();

			// System.out.println(" lookit : " + predicate);
			PrefixEntity prefixEntity = PrefixUtility.splitPreficFromProperty(predicate);

			double coverage;
			if (size > 0) {
				coverage = PredicateCount / size;
			} else {
				coverage = 0;
			}

			PropertyEntity p1 = new PropertyEntity(prefixEntity.key, prefixEntity.value, prefixEntity.name,
					PredicateCount, coverage);
			propertiesListTemp.add(p1);

		});
		return propertiesListTemp;
	}

}
