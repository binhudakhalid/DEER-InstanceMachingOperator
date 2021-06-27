package org.aksw.deer.plugin.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
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

	public IntanceMactchingOperator() {
		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder()
				.declareValidationShape(getValidationModelFor(IntanceMactchingOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3

		spark();

		Model model = ModelFactory.createDefaultModel();

		// DOn't need to uncomment these line as you don't want
		// to run as the output is already is saved in 002accepted.nt
		// and it takes atleast 1 hour to execute.

		// Configuration con = createLimeConfigurationFile();
		// callLimes(con);

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
		conf.addPrefix("geom", "http://geovocab.org/geometry#");
		conf.addPrefix("geom", "http://geovocab.org/geometry#");
		conf.addPrefix("geos", "http://www.opengis.net/ont/geosparql#");
		conf.addPrefix("lgdo", "http://linkedgeodata.org/ontology/");
		conf.addPrefix("alie", "https://linkedgeodata.org/ontology/");

		conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		conf.addPrefix("url", "http://schema.org/");
		conf.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		conf.addPrefix("dbpo", "http://dbpedia.org/ontology/");
		conf.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

		KBInfo src = new KBInfo();

		src.setId("sourceId");
		src.setEndpoint("http://dbpedia.org/sparql");
		src.setVar("?s");
		src.setPageSize(-1);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?s rdf:type url:Movie" })));
		src.setProperties(Arrays.asList(new String[] { "rdfs:label" }));

		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("dbpo", "http://dbpedia.org/ontology/");
		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixes.put("url", "http://schema.org/");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("dbpo", "http://dbpedia.org/ontology/");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

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
		target.setEndpoint("http://dbpedia.org/sparql");
		target.setVar("?t");
		target.setPageSize(-1);
		target.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?t rdf:type dbpo:Film" })));
		target.setProperties(Arrays.asList(new String[] { "rdfs:label" }));
		target.setPrefixes(prefixes);
		target.setFunctions(functions);
		conf.setTargetInfo(target);

		// Set either Metric or MLALGORITHM
		// conf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");
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
		/*
		 * String sourceEndpoint = config.getSourceInfo().getEndpoint(); String
		 * targetEndpoint = config.getTargetInfo().getEndpoint(); int limit = -1;
		 */

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

		// System.out.println(" __test___mappings.getAcceptanceMapping() : " +
		// mappings.getAcceptanceMapping());
		// System.out.println(" __test___ mappings.getStatistics() : " +
		// mappings.getStatistics());

		System.out.println(" -Completed- ");

	}

	public void dynamicPrefix() {
//https://stackoverflow.com/questions/27745/getting-parts-of-a-url-regex

		prefixMap = new HashMap<String, String>();// Creating HashMap

		String prefix, prefixValue;
		try {

			// We will this data from team eventually
			File tempDataFile = new File("bookEntityFileSource.ttl");
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

				if (Line.contains("<http://")) {
					String subjecturl = Line.substring(Line.indexOf("<") + 1, Line.indexOf(">"));
					URL aURL = new URL(subjecturl);
					String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
					String prefixV = temp.substring(0, temp.lastIndexOf('/') + 1);

					/// creating prefix key
					String prefixKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 2);

					prefixMap.put(prefixKey, prefixV);
				}

			}
			System.out.println(" prefixMap " + prefixMap);

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

	public void spark() {
		
		propertyMap = new  HashMap<String, Integer>();

		QueryExecution qe = QueryExecutionFactory.sparqlService(

				"http://dbpedia.org/sparql",
				"SELECT ?predicate (COUNT(?predicate) as ?count)\r\n" + "WHERE\r\n" + "{\r\n"
						+ "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie> .\r\n"
						+ "  ?s ?predicate ?o .\r\n" + "} \r\n" + "GROUP BY ?predicate\r\n" + "order by desc ( ?count )"
						+ "LIMIT 10");

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		resultOne.forEachRemaining(qsol -> {
		
			propertyMap.put(qsol.getResource("predicate").toString(), qsol.getLiteral("count").getInt());
			System.out.println(
					"khad : " + qsol.getLiteral("count").getInt() + "khad2 : " + qsol.getResource("predicate"));

		});
		
		System.out.println("Here am I : " + propertyMap);

		resultOne.forEachRemaining(qsol -> System.out.println("khad2 : " + qsol.getLiteral("predicate").getInt()));

		// some definitions
		String personURI = "http://somewhere/JohnSmith";
		String givenName = "John";
		String familyName = "Smith";
		String fullName = givenName + " " + familyName;

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// create the resource
		// and add the properties cascading style
		Resource johnSmith = model.createResource(personURI).addProperty(VCARD.FN, fullName).addProperty(VCARD.N,
				model.createResource().addProperty(VCARD.Given, givenName).addProperty(VCARD.Family, familyName));

		model.add(johnSmith, VCARD.Given, "abc");
		// System.out.println(" The Test" + model.getResource("));

		Model abc = RDFOutput.encodeAsModel(resultOne);

		System.out.println(" under me ");
		abc.write(System.out, "N-TRIPLES");

		System.out.println(" tttest : " + RDFOutput.encodeAsRDF(abc, false).toString());

		// Model abc = RDFOutput.asModel(resultOne);
		// RDFOutput.encodeAsRDF(model, resultOne);//(model,
		// booleanResult);//encodeAsModel(resultOne);
		System.out.println("ali haider" + abc.getReader() + "ali haider");

		// String text1 = ResultSetFormatter.to();

		StmtIterator iter = abc.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			System.out.print(" waee : " + subject.toString());
			System.out.print(" waee2 " + predicate.toString() + " s0d-o ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}

		ResultSet result1 = qe.execSelect();
		String text1 = ResultSetFormatter.asText(result1);
		System.out.println("text1;1" + text1);

		ResultSet results = qe.execSelect();

		System.out.println(" bos1 m  " + results.getResourceModel());
		ResultSetFormatter.out(System.out, results);

		System.out.println("me " + results);

		qe.close();
	}

}
