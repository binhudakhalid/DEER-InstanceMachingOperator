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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
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
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.vocabulary.VCARD;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class IntanceMactchingOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(IntanceMactchingOperator.class);
	public HashMap<String, String> prefixMap;
	public HashMap<String, Integer> propertyMap;
	public int totalInstances;

	public static Property Coverage = DEER.property("coverage");
	public static Property MaxLimit = DEER.property("maxLimit");

	public IntanceMactchingOperator() {
		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder().declareProperty(Coverage).declareProperty(MaxLimit)
				.declareValidationShape(getValidationModelFor(IntanceMactchingOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3

		String coverage = getParameterMap().getOptional(Coverage).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("World");
		System.out.println(" drecipient-d coverage: " + coverage);

		String maxLimit = getParameterMap().getOptional(MaxLimit).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("none");
		System.out.println(" drecipient-d maxLimit: " + maxLimit);

		int tempTotal = totalInstance("Movie");
		System.out.println("THe Count is: " + tempTotal);

		// - dynamicPrefix();

		// Querying through Sparql to count the number of predicate of the entity
		// -countEntityPredicate();

		Model model = ModelFactory.createDefaultModel();

		// DOn't need to uncomment these line as you don't want
		// to run as the output is already is saved in 002accepted.nt
		// and it takes atleast 1 hour to execute.

		// -Configuration con = createLimeConfigurationFile();
		// -callLimes(con);

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

		KBInfo src = new KBInfo();

		src.setId("sourceId");
		src.setEndpoint("http://dbpedia.org/sparql");
		src.setVar("?s");
		src.setPageSize(-1);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?s rdf:type url:Movie" })));

		Object[] property = propertyMap.keySet().toArray();
		String[] strArr = Arrays.stream(property).map(Object::toString).toArray(String[]::new);

		src.setProperties(Arrays.asList(new String[] { "rdfs:label" }));

		Map<String, String> prefixes = new HashMap<String, String>();

		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");

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
		target.setEndpoint("https://yago-knowledge.org/sparql/query");
		target.setVar("?t");
		target.setPageSize(1000);
		target.setRestrictions(new ArrayList<String>(
				Arrays.asList(new String[] { "?t rdf:type url:Movie", " ?t  url:actor yago:Jennifer_Aniston" })));
		target.setProperties(Arrays.asList(new String[] { "rdfs:label" }));
		target.setPrefixes(prefixes);
		target.setFunctions(functions);
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

		System.out.println("mappings-khd- : " + mappings.getStatistics());
		System.out.println("mappings-khd- : " + mappings.getClass());
		System.out.println("mappings-kh- : " + mappings.toString());

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

		System.out.println(" -Completed- ");

	}

	public void dynamicPrefix() {

		prefixMap = new HashMap<String, String>();// Creating HashMap

		String prefix, prefixValue;
		try {

			// We will this data from team eventually
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

				// System.out.println("khan Red alert subjecturl" + Line);
				if (Line.contains("@prefix")) {

				} else if (Line.contains("<http://")) {
					String subjecturl = Line.substring(Line.indexOf("<") + 1, Line.indexOf(">"));

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

	public void countEntityPredicate() {

		Model mop = ModelFactory.createDefaultModel();

		addStatement("sss", "ppp", "ooo", mop);
		System.out.println("Ali Header  " + mop);

		propertyMap = new HashMap<String, Integer>();

		QueryExecution qe = QueryExecutionFactory.sparqlService(

				"http://dbpedia.org/sparql",
				"SELECT ?predicate (COUNT(?predicate) as ?count)\r\n" + "WHERE\r\n" + "{\r\n"
						+ "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie> .\r\n"
						+ "  ?s ?predicate ?o .\r\n" + "} \r\n" + "GROUP BY ?predicate\r\n" + "order by desc ( ?count )"
						+ "LIMIT 10");

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

		});

		System.out.println("Here is the log propertyMap : " + propertyMap);

		resultOne.forEachRemaining(qsol -> System.out.println("khad2 : " + qsol.getLiteral("predicate").getInt()));
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		qe.close();
	}

	public int totalInstance(String instanceType) {

		QueryExecution qe = QueryExecutionFactory.sparqlService(

				"http://dbpedia.org/sparql",
				"PREFIX dbpo: <http://dbpedia.org/ontology/>\r\n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n" + "SELECT (COUNT(?s) AS ?totalInstances)\r\n"
						+ "WHERE { ?s rdf:type url:" + instanceType + ". } ");

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		// totalInstance = resultOne.forEachRemaining(action);
		// qsol.getLiteral("totalInstances").getInt();

		System.out.println("Here is the log propertyMap : " + propertyMap);

		resultOne.forEachRemaining(qsol -> totalInstances = qsol.getLiteral("totalInstances").getInt());
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		qe.close();

		return totalInstances;
	}

	public void addStatement(String s, String p, String o, Model model) {

		Resource subject = model.createResource(s);
		Property predicate = model.createProperty(p);
		RDFNode object = model.createResource(o);
		Statement stmt = model.createStatement(subject, predicate, object);
		model.add(stmt);
	}

}

/*
 * ------------------------------------------------ predicate :
 * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
 * 
 * 
 * prefixKey: w3199 predicatePrefixValue
 * http://www.w3.org/1999/02/22-rdf-syntax-ns# predicatePrefixValue2 1: #type
 * 
 * 
 * w3199:type ------------------------------------------------------------
 * 
 * ns5=http://dbpedia.org/resource/Think
 * 
 * key = ns5 value = http://dbpedia.org/resource/Think
 *
 */