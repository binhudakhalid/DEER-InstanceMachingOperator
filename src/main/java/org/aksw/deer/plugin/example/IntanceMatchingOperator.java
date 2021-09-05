package org.aksw.deer.plugin.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

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
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.vocabulary.VCARD;
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
	//public HashMap<String, String> propertiesPrefixesSource;
	public List<PrefixEntity> propertiesPrefixesSource;
	
	public List<PropertyEntity> propertiesList;
	
	public int totalInstances;

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

		//
		countEntityPredicate();

		// calculateCoverage
		calculateCoverage();
		System.out.println(" coverageMap1 " + coverageMap);

		/*
		 * int tempTotal = totalInstance("Movie"); System.out.println("THe Count is: " +
		 * tempTotal); countEntityPredicate();
		 */

		dynamicPrefix();

		// Querying through Sparql to count the number of predicate of the entity
		// -countEntityPredicate();

		Model model = ModelFactory.createDefaultModel();
		
		String sourceTarget = "NT";
		// sourceTarget is NT File
		if(sourceTarget == "NT") {
			
			
			calculateCoverageForNTFile("F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt");
			
		}
		
		//int abc = totalInstanceTarget("Movie");
		//System.out.println("abcd : " + abc);
		
		//countEntityPredicateTarget();

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

		return List.of(ourModel);
	}

	public Configuration createLimeConfigurationFile() {

		// Creating Limes configuration Object
		Configuration conf = new Configuration();

		// This weeks task add prefix dynamically
		dynamicPrefix();

		for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
			String prefixName = entry.getKey();
			String prefixValue = entry.getValue();
			conf.addPrefix(prefixName, prefixValue);
		}

		// adding prefix
		conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		//conf.addPrefix("zoo", "http://dbpedia.org/ontology/");

		KBInfo src = new KBInfo();

		src.setId("sourceId");   
		src.setEndpoint("F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt");
		src.setVar("?s");
		src.setPageSize(-1);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "" })));
		
		Object[] property = propertyMap.keySet().toArray();
		String[] strArr = Arrays.stream(property).map(Object::toString).toArray(String[]::new);


		//Setting properties or predicates
		//http://dbpedia.org/ontology/abstract
		src.setProperties(Arrays.asList(new String[] { "rdfs:label"//, "rdfs:comment",  "z1:wikiPageLength", "z1:wikiPageID", "z1:wikiPageRevisionID", 
				//"z3:name", "z1:abstract", 
			  })); 
		src.setType("NT");
		//"z3:name ,  error
		//"rdfs:comment", error
		//"z1:abstract" error
		//"z3:country", error
		//"z3:language" error
		Map<String, String> prefixes = new HashMap<String, String>();

		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixes.put("z1", "http://dbpedia.org/ontology/");
		prefixes.put("z2", "http://xmlns.com/foaf/0.1/");
		prefixes.put("z3", "http://dbpedia.org/property/");
		
		 
		
		
		

		System.out.println("prefixMap length : " + prefixMap.size());
		for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
			String prefixName = entry.getKey();
			String prefixValue = entry.getValue();
			prefixes.put(prefixName, prefixValue);
		}

		src.setPrefixes(prefixes);

		// src.setFunctions(functions);

		HashMap<String, String> tempHashMap = new HashMap<String, String>();
		tempHashMap.put("rdfs:label", "");
		LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<String, Map<String, String>>();
		functions.put("rdfs:label", tempHashMap);
		src.setFunctions(functions);

		conf.setSourceInfo(src);

		KBInfo target = new KBInfo();
		target.setId("targetId");
		target.setEndpoint("F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt");
		target.setVar("?t");
		target.setPageSize(1000);
		target.setRestrictions(new ArrayList<String>(
				Arrays.asList(new String[] { "" })));
		target.setProperties(Arrays.asList(new String[] { "rdfs:label" }));
		target.setPrefixes(prefixes);
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

		return conf;
	}

	public void callLimes(Configuration config) {

		// String limesOutputLocation = "F://Newfolder//LIMES//t"; // for output

		String limesOutputLocation = new File("").getAbsolutePath();

		LimesResult mappings = Controller.getMapping(config);
		String outputFormat = config.getOutputFormat();
		ISerializer output = SerializerFactory.createSerializer(outputFormat);

		// mappings.toString();

		output.setPrefixes(config.getPrefixes());

		String workingDir = limesOutputLocation;// "F:\\Newfolder\\LIMES\\t";
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
					
					//@prefix prov:	<http://www.w3.org/ns/prov#> 
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

			QueryExecution qe = QueryExecutionFactory.sparqlService(
					"https://yago-knowledge.org/sparql/query",
					"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
					+ "PREFIX url: <http://schema.org/>\r\n"
					+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n"
					+ "WHERE{\r\n"
					+ "?instance rdf:type url:Movie . \r\n"
					+ "?instance ?predicate ?o .\r\n"
					+ "FILTER(isLiteral(?o))   \r\n"
					+ "} \r\n"
					+ "GROUP BY ?predicate\r\n"
					+ "order by desc ( ?count ) \r\n"
					+ "LIMIT 10");

			ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

			resultOne.forEachRemaining(qsol -> {
				String predicate = qsol.getResource("predicate").toString();
				int PredicateCount = qsol.getLiteral("count").getInt();
				String predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2;
				URL aURL = null;
				if (predicate.contains("#")) {
					// http://www.w3.org/2002/07/owl#sameAs=903475
					// abc
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
					
					
					PrefixEntity prefix = new PrefixEntity(predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2);
					
					propertiesPrefixesSource.add(prefix);
				
				
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

					System.out.println("predicatePrefixValue : " + predicatePrefixValue);
					System.out.println("predicatePrefixValue2 : " + predicatePrefixValue2);
					System.out.println("predicatePrefixKey : " + predicatePrefixKey);
					PrefixEntity prefix = new PrefixEntity(predicatePrefixKey, predicatePrefixValue, predicatePrefixValue2);
					propertiesPrefixesSource.add(prefix);

				}
				
//				propertyMap.put(qsol.getResource("predicate").toString(), qsol.getLiteral("count").getInt());
	//			System.out.println("propertyMap10 : " + qsol.getResource("predicate").toString());
				propertyMap.put(qsol.getResource("predicate").toString(), qsol.getLiteral("count").getInt());
				System.out.println("propertyMap10 : " + qsol.getResource("predicate").toString());
				System.out.println("propertiesPrefixesSource : " + propertiesPrefixesSource);

			});

//			System.out.println("Here is the log propertyMap : " + propertyMap);

	//		resultOne.forEachRemaining(qsol -> System.out.println("khad2 : " + qsol.getLiteral("predicate").getInt()));
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
					+ "PREFIX url: <http://schema.org/>\r\n"
					+ "\r\n"
					+ "\r\n"
					+ "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
					+ "WHERE { \r\n"
					+ "?s rdf:type url:" + instanceType + " .\r\n"
					+ "}");

			ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
			resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
			qe.close();

			return totalInstances;
		}
	
	public void calculateCoverageForNTFile(String link){
		
		propertiesList = new ArrayList<PropertyEntity>();
		long size;
		
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, "F:\\Newfolder\\LIMES\\t\\dbtune_org_magnatune_sparqlCut1.nt", Lang.NTRIPLES);
		
		
		//RDFDataMgr.read(model, inputStream, ) ;

		System.out.println(" meAtIt1 : " + model );
		size = model.size();
		System.out.println(" meAtIt1size  : " + size );
		
		String queryString = "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
                + "PREFIX dbr: <http://dbpedia.org/resource/>" + "PREFIX dbpedia2: <http://dbpedia.org/property/>"
                + "PREFIX dbpedia: <http://dbpedia.org/>" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
                + "PREFIX dbo: <http://dbpedia.org/ontology/>" + "select distinct ?p ?o where {"
                + "dbr:Cristiano_Ronaldo ?p ?o " + "filter(langMatches(lang(?o),'en'))" +

        "}";
		
		
		String queryString1 =	"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
		+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n"
		+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
		//+ "  ?instance rdf:type url:Movie .\r\n" 
		+ "  ?instance ?predicate ?o .\r\n"
		+ "  FILTER(isLiteral(?o)) \r\n" + "} \r\n" + "GROUP BY ?predicate\r\n"
		+ "order by desc ( ?count )\r\n" + "LIMIT 10";
		
		 Query query = QueryFactory.create(queryString1);
		 QueryExecution qexec = QueryExecutionFactory.create(query, model);
		/* ResultSet results = qexec.execSelect();
		 System.out.println("result 007 : " + results);
		 ResultSetFormatter.out(System.out, results);
		//System.out.println(((Statement) model).getSubject());*/
		 
		 ResultSet resultsOne = ResultSetFactory.copyResults(qexec.execSelect());
		 
		 resultsOne.forEachRemaining(qsol -> {
				String predicate = qsol.getResource("predicate").toString();
				int PredicateCount = qsol.getLiteral("count").getInt();
				
				System.out.println(" mmeemee " + predicate );
				System.out.println(" mmeemee PredicateCount1 : " + PredicateCount );
				
				
				
				PropertyEntity p1 = new PropertyEntity("a", "b", "c", PredicateCount);
				propertiesList.add(p1);
				
				
		 });
		 
		 
		 System.out.println("propertiesList01 :" + propertiesList.get(0).toString());
		 
		 System.exit(0);
		 
		
		
	}

	
}