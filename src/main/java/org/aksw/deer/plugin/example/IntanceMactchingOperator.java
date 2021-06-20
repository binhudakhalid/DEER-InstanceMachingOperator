package org.aksw.deer.plugin.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class IntanceMactchingOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(IntanceMactchingOperator.class);
	public HashMap<String, String> prefixMap;

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

		Model model = ModelFactory.createDefaultModel();

		dynamicPrefix();

		// File initialFile = new File("001accepted.nt");
		// InputStream targetStream = null;
		String filename = "001accepted.nt";
		File file = new File(filename);
		String content = null;
		try {
			content = FileUtils.readFileToString(file, "UTF-8");
			FileUtils.write(file, content, "UTF-8");
			System.out.println(" a nc d a-2 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Model ourModel = RDFDataMgr.loadModel("001accepted.nt");

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
		conf.setAcceptanceThreshold(0.2);

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
		conf.setOutputFormat("TTL"); // NT or TTL

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
		String outputFormat = config.getOutputFormat();
		ISerializer output = SerializerFactory.createSerializer(outputFormat);

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

		System.out.println(" wo ist das -33 ali2 ");

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

}
