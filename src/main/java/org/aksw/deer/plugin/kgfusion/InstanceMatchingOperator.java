package org.aksw.deer.plugin.kgfusion;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;
import org.jgap.gp.terminal.False;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class InstanceMatchingOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(InstanceMatchingOperator.class);
	public HashMap<String, String> prefixMap;
	public HashMap<String, Integer> propertyMap;
	public HashMap<String, Double> coverageMap;
	// public HashMap<String, String> propertiesPrefixesSource;
	public List<PrefixEntity> propertiesPrefixesSource;

	// public List<PropertyEntity> propertiesList;
	// public List<PropertyEntity> propertiesListSource;
	// public List<PropertyEntity> propertiesListTarget;

	public List<PropertyEntity> propertiesListSource1;
	public List<PropertyEntity> propertiesListTarget1;

	public int totalInstances;
	private static boolean debug;
	private static boolean debugLogs;

	Set<String> entityListFile;

	private HashMap<String, Resource> tabuSourceProperty = new HashMap<String, Resource>();
	private HashMap<Integer, Double> tabuTargetProperty;

	public static Property COVERAGE = DEER.property("coverage");
	public static Property MAX_LIMIT = DEER.property("maxLimit");
	public static Property TEST = DEER.property("test");
	public static Property TYPE = DEER.property("type");
	public static Property SOURCE = DEER.property("source");
	public static Property TARGET = DEER.property("target");
	public static Property SOURCE_RESTRICTION = DEER.property("sourceRestriction");
	public static Property TARGET_RESTRICTION = DEER.property("targetRestriction");
	public static Property TABU_SOURCE_PROPERTY = DEER.property("tabuSourceProperty");
	public static Property TABU_TARGET_PROPERTY = DEER.property("tabuTargetProperty");
	public static Property DEBUG_LOGS = DEER.property("debugLogs");

	public static final Property PROPERTY_URI = DEER.property("propertyURI");

	List<Model> outputList = new ArrayList<>();;

	public InstanceMatchingOperator() {
		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder().declareProperty(COVERAGE).declareProperty(MAX_LIMIT)
				.declareProperty(TEST).declareProperty(TYPE).declareProperty(SOURCE).declareProperty(TARGET)
				.declareProperty(SOURCE_RESTRICTION).declareProperty(TARGET_RESTRICTION)
				.declareProperty(TABU_SOURCE_PROPERTY).declareProperty(TABU_TARGET_PROPERTY).declareProperty(DEBUG_LOGS)
				.declareValidationShape(getValidationModelFor(InstanceMatchingOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3
		

		// tam();
		// System.out.println();
		// System.exit(0);
		// Setting DEER Parameters
		double coverage = getParameterMap().getOptional(COVERAGE).map(RDFNode::asLiteral).map(Literal::getDouble)
				.orElse(0.90);
		int maxLimit = getParameterMap().getOptional(MAX_LIMIT).map(RDFNode::asLiteral).map(Literal::getInt).orElse(3);

		String test = getParameterMap().getOptional(TEST).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("false");

		String type = getParameterMap().getOptional(TYPE).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("fileType");
		String source = getParameterMap().getOptional(SOURCE).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("sampleFalse");
		String target = getParameterMap().getOptional(TARGET).map(RDFNode::asLiteral).map(Literal::getString)
				.orElse("smapleTarget");
		debugLogs = getParameterMap().getOptional(DEBUG_LOGS).map(RDFNode::asLiteral).map(Literal::getBoolean)
				.orElse(false);

		final String sourceRestriction = getParameterMap().getOptional(SOURCE_RESTRICTION).map(RDFNode::asResource)
				.map(Resource::getURI).orElse("asdasd");
		// .asResource(). .orElse("sampleSourceRestriction");

		if (debugLogs)
			System.out.println("Zidane 10");

		// String targetRestriction =
		// getParameterMap().getOptional(TARGET_RESTRICTION).map(RDFNode::asLiteral)
		// .map(Literal::getString).orElse("sampleTargetRestriction");

		// ValidatableParameterMap parameters = getParameterMap();
		// String targetRestriction =
		// getParameterMap().getOptional(TARGET_RESTRICTION).asResource().getURI();
		final String targetRestriction = getParameterMap().getOptional(TARGET_RESTRICTION).map(RDFNode::asResource)
				.map(Resource::getURI).orElse("asdasd tat");

		getParameterMap().listPropertyObjects(TABU_SOURCE_PROPERTY).map(RDFNode::asResource).forEach(op -> {

			final Resource propertyUri = op.getPropertyResourceValue(PROPERTY_URI).asResource();
			// final String abc =
			// op.getPropertyResourceValue(PROPERTY_URI).asResource().getURI();
			// System.out.println("op1 : " + abc);
			System.out.println("op1 abcd : " + propertyUri);
			// System.out.println("Count :" + a);

			tabuSourceProperty.put(propertyUri.toString(), propertyUri);

		});
		System.out.println("tabuSourceProperty : " + tabuSourceProperty);
		printOut("tabuSourceProperty tsp : " + tabuSourceProperty);

		getParameterMap().listPropertyObjects(TABU_TARGET_PROPERTY).map(RDFNode::asResource).forEach(op -> {
			final String abc = op.getPropertyResourceValue(PROPERTY_URI).asResource().getURI();
			System.out.println("op2 : " + abc);
		});
		
		

		System.out.println(" drecipient-dc coverage: " + coverage);
		System.out.println(" drecipient-dm maxLimit: " + maxLimit);
		System.out.println(" drecipient-dc test: " + test);
		System.out.println(" drecipient-dc type: " + type);
		System.out.println(" drecipient-dc source: " + source);
		System.out.println(" drecipient-dc target: " + target);
		System.out.println(" drecipient-dc sourceRestriction: " + sourceRestriction);
		// System.out.println(" drecipient-dc sourceRestriction: " +
		// sourceRestriction.get().getClass());

		// String fromEndpoint = parameters.get(FROM_ENDPOINT).asResource().getURI();

		System.out.println(" drecipient-dc targetRestriction: " + targetRestriction);
		// System.out.println(" drecipient-dc tabuProperty: " + tabuProperty);
		// System.exit(0);
		// double coverage = Double.valueOf(coverage);

		// Getting input from previous operator
		// There parameter will be set by the output from previous operator
		// Setting the parameter manually until the ontology operator is integrated with
		// it.

		String inputEndpoint = "fileType";
		String sourceFilePath = "data/data_nobelprize_org.nt";
		String targetFilePath = "data/lov_linkeddata_es_dataset_lov.nt";
		String sourceRestrictions = "http://xmlns.com/foaf/0.1/Person";
		String targetRestrictions = "http://xmlns.com/foaf/0.1/Person";
		

		debug = true;

		// if the endpoint is filetype
		if (inputEndpoint == "fileType") {

			propertiesListSource1 = getPropertiesFromFile(sourceFilePath, sourceRestrictions, maxLimit);
			propertiesListTarget1 = getPropertiesFromFile(targetFilePath, targetRestrictions, maxLimit);
			System.out.println("------------------------------------------------");
			System.out.println("propertiesListSource from source -->: " + propertiesListSource1);
			System.out.println("propertiesListTarget from target -->: " + propertiesListTarget1);
			System.out.println("------------------------------------------------");

			removePropertiesHavingLowerCoverage(coverage, propertiesListSource1);
			removePropertiesHavingLowerCoverage(coverage, propertiesListTarget1);
			System.out.println(
					"rrrrrrrrr -> Total Properties after comparing with Coverage: " + propertiesListTarget1.size());

			// If no property have the coverage than the coverage parameter(Set in
			// configuration.ttl)
			// then it thought a exception
			// propertiesListTarget1.remove(0);
			if (propertiesListSource1.size() < 1 || propertiesListTarget1.size() < 1) {

				System.out.println(
						" Can not proceed because " + "propertiesListSource`s size= " + propertiesListSource1.size()
								+ " propertiesListTarget`s size=  " + propertiesListTarget1.size());
				// if the above if is true then the execution should be stopped.
				return null;
			}

			// check if we get any data if query with following property list
			isDataAvailableFile(sourceFilePath, sourceRestrictions, maxLimit, propertiesListSource1, "source");
			isDataAvailableFile(targetFilePath, targetRestrictions, maxLimit, propertiesListTarget1, "target");

			// check

			Configuration con = createLimeConfigurationFile(sourceFilePath, sourceRestrictions, targetFilePath,
					targetRestrictions, "NT");

			System.out.println("see 0011 ");
			callLimes(con);
			System.out.println("see 0012 ");
			
			
			System.out.println("--> In Output Generating Phase");

			OutputUtility ouputUtility = new OutputUtility();
			
			List<Model>  l1 = ouputUtility.createOuput("accepted.nt", sourceRestrictions, targetRestrictions, sourceFilePath,
					targetFilePath, "File");
			
		 


			return l1;

			// System.exit(0);
		} // if the endpoint is url
		else { // if (inputEndpoint == "url") {

			System.out.println(" we are in else");

			String sourceEndpoint = "http://vocab.getty.edu/sparql";
			String targetEndpoint = "http://vocab.getty.edu/sparql";

			sourceRestrictions = "http://vocab.getty.edu/ontology#GroupConcept";
			targetRestrictions = "http://vocab.getty.edu/ontology#GroupConcept";

			// String inputEndpoint = "fileType";
			// String sourceFilePath = "data/data_nobelprize_org.nt";
			// String targetFilePath = "data/lov_linkeddata_es_dataset_lov.nt";
			// String sourceRestrictions = "http://xmlns.com/foaf/0.1/Person";
			// String targetRestrictions = "http://xmlns.com/foaf/0.1/Person";

			propertiesListSource1 = getPropertiesFromURL(sourceEndpoint, sourceRestrictions, maxLimit);

			propertiesListTarget1 = getPropertiesFromURL(targetEndpoint, targetRestrictions, maxLimit);

			System.out.println(" 91029 propertiesListTarget1 ");

			System.out.println("------------------------------------------------");
			System.out.println("propertiesListSource from source -->: " + propertiesListSource1);
			System.out.println("propertiesListTarget from target -->: " + propertiesListTarget1);
			System.out.println("------------------------------------------------");

			removePropertiesHavingLowerCoverage(coverage, propertiesListSource1);
			removePropertiesHavingLowerCoverage(coverage, propertiesListTarget1);
			System.out.println(
					"rrrrrrrrr -> Total Properties after comparing with Coverage: " + propertiesListTarget1.size());

			// If no property have the coverage than the coverage parameter(Set in
			// configuration.ttl)
			// then it thought a exception
			// propertiesListTarget1.remove(0);
			if (propertiesListSource1.size() < 1 || propertiesListTarget1.size() < 1) {

				System.out.println(
						" Can not proceed because " + "propertiesListSource`s size= " + propertiesListSource1.size()
								+ " propertiesListTarget`s size=  " + propertiesListTarget1.size());
			}

			// Check if the data is available, if we query it with following properties
			isDataAvailableURL(sourceEndpoint, sourceRestrictions, propertiesListTarget1);
			isDataAvailableURL(targetEndpoint, targetRestrictions, propertiesListTarget1);

			// check

			Configuration con = createLimeConfigurationFile(sourceEndpoint, sourceRestrictions, targetEndpoint,
					targetRestrictions, "sparql");

			System.out.println("see 0011 " + con);
			callLimes(con);
			System.out.println("see 0012 ");

			System.out.println("--> In Output Generating Phase");

			System.out.println(" now umerali ");
			// System.exit(0);

		}
//		 

		// countEntityPredicate();

		// calculateCoverage
//		calculateCoverage();
//		System.out.println(" coverageMap1 " + coverageMap);

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
		/*
		 * if (sourceType == "NT") { //
		 * calculateCoverageForNTFile("data/dbtune_org_magnatune_sparqlCut1.nt"); } else
		 * if (sourceType == "SPARQL") {
		 * 
		 * }
		 * 
		 * if (targetType == "NT") { //
		 * calculateCoverageForNTFile("data/lov_linkeddata_es_dataset_lov.nt"); } else
		 * if (targetType == "SPARQL") {
		 * 
		 * }
		 * 
		 * // int abc = totalInstanceTarget("Movie"); // System.out.println("abcd : " +
		 * abc);
		 * 
		 * // countEntityPredicateTarget();
		 * 
		 * //Configuration con = createLimeConfigurationFile(); //callLimes(con);
		 * 
		 * // File initialFile = new File("001accepted.nt"); // InputStream targetStream
		 * = null;
		 * 
		 * // 002accepted.nt file contains the output of LIMES // between movies from
		 * yago and films from Dbpedia /* String filename = "002accepted.nt"; File file
		 * = new File(filename); String content = null; try { content =
		 * FileUtils.readFileToString(file, "UTF-8"); FileUtils.write(file, content,
		 * "UTF-8"); System.out.println(" a nc d a-2 "); } catch (IOException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// Model ourModel = RDFDataMgr.loadModel("002accepted.nt");

//		Model model1 = ModelFactory.createDefaultModel();
//		RDFDataMgr.read(model1, "accepted.nt", Lang.NT); // RDFDataMgr.read(model, inputStream, ) ;
//		System.out.println(" Model1a " + model1 );

		return outputList;
	}

	/*
	 * remove properties that have lower coverage than Coverage parameter (the
	 * Parameter set from Configura.ttl file)
	 */
	private void removePropertiesHavingLowerCoverage(double coverage, List<PropertyEntity> tempPropertiesListSource) {
		System.out.println(" ahsan tempCoverage = " + coverage + " ++ \n " + tempPropertiesListSource);

		System.out.println("removePropertiesHavingLowerCoverage -> Total Properties before comparing with Coverage: "
				+ tempPropertiesListSource.size());
		Iterator itr = tempPropertiesListSource.iterator();

		// Holds true till there is sibgle element
		// remaining in the object
		while (itr.hasNext()) {

			// Remove elements smaller than 10 using
			// Iterator.remove()
			PropertyEntity propertyEntity = (PropertyEntity) itr.next();
			if (propertyEntity.coverage < coverage) {

				// System.out.println(" propertyEntity.coverage : " + propertyEntity.coverage );
				// System.out.println(" coverage : " + coverage );
				// System.out.println("----------------------------------");

				itr.remove();
			}

		}

		System.out.println(" ahsan tempCoverage after = " + tempPropertiesListSource);
		System.out.println("removePropertiesHavingLowerCoverage -> Total Properties after comparing with Coverage: "
				+ tempPropertiesListSource.size());

	}

	public Configuration createLimeConfigurationFile(String srcEndpoint, String srcRestrictions, String targetEndpoint,
			String targetRestrictions, String type) {

		Configuration conf = new Configuration();

		// dynamicPrefix();

		List<String> srcPropertylist = new ArrayList<String>();
		List<String> targetPropertylist = new ArrayList<String>();

		for (PropertyEntity list : propertiesListSource1) {
			conf.addPrefix(list.key, list.value);
			srcPropertylist.add(list.key + ":" + list.propertyName);
		}

		conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		conf.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		conf.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		KBInfo src = new KBInfo();

		src.setId("sourceId");
		src.setEndpoint(srcEndpoint);
		src.setVar("?s");
		src.setPageSize(-1);

		PrefixEntity srcRestrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(srcRestrictions);

		System.out.println(" srcRestrictionPrefixEntity1 " + srcRestrictionPrefixEntity);
		// System.out.println("?s rdf:type " + srcRestrictionPrefixEntity.key
		// +":"+srcRestrictionPrefixEntity.name);
		// System.exit(1);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] {
				"?s rdf:type " + srcRestrictionPrefixEntity.key + ":" + srcRestrictionPrefixEntity.name })));

		src.setProperties(srcPropertylist);
		src.setType(type);

		Map<String, String> prefixes = new HashMap<String, String>();

		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		// setting prefix for source
		for (PropertyEntity list : propertiesListSource1) {
			// adding Prefix
			prefixes.put(list.key, list.value);
			System.out.println("scotlandyard - prefixes -: " + list.key + list.value);

			System.out.println("debug new prefixes.put key: + " + list.key + " value: " + list.value);
		}

		System.out.println(" polp ");
//		System.out.println("prefixMap length : " + prefixMap.size());
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		// prefixes.put("", "http://purl.org/ontology/mo/");

		// Setting prefix for source restriction
		PrefixEntity sourceRestrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(srcRestrictions);
		prefixes.put(sourceRestrictionPrefixEntity.key, sourceRestrictionPrefixEntity.value);

		// THis is okay

		src.setPrefixes(prefixes);

		HashMap<String, String> tempHashMap = new HashMap<String, String>();
		tempHashMap.put("rdfs:label", "");
		LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<String, Map<String, String>>();
		functions.put("rdfs:label", tempHashMap);
		src.setFunctions(functions);

		conf.setSourceInfo(src);

		Map<String, String> targetPrefixesMap = new HashMap<String, String>();
		targetPrefixesMap.put("owl", "http://www.w3.org/2002/07/owl#");
		targetPrefixesMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		// targetPrefixesMap.put("", "http://purl.org/ontology/mo/");

		// Setting prefix for target restriction
		PrefixEntity targetRestrictionPrefix = PrefixUtility.splitPreficFromProperty(targetRestrictions);
		targetPrefixesMap.put(targetRestrictionPrefix.key, targetRestrictionPrefix.value);

		// setting prefix for target
		for (PropertyEntity list : propertiesListTarget1) {

			conf.addPrefix(list.key, list.value);

			targetPrefixesMap.put(list.key, list.value);
			System.out.println("scotlandyard - targetPrefixesMap -: " + list.key + list.value);

			targetPropertylist.add(list.key + ":" + list.propertyName);
			System.out.println(
					"debug new target  : list.key  + " + list.key + " list.propertyName:  " + list.propertyName);
		}

		KBInfo target = new KBInfo();
		target.setId("targetId");
		target.setEndpoint(targetEndpoint);
		target.setVar("?z");
		target.setPageSize(-1);

		PrefixEntity targetRestrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(targetRestrictions);

		target.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] {
				"?z rdf:type " + targetRestrictionPrefixEntity.key + ":" + targetRestrictionPrefixEntity.name })));

		/*
		 * There is a problem when we have an entity has lot of properties but all
		 * instances don't have all those properties then the Sparql query return 0
		 * instance.
		 * 
		 * By reading the LIMES documentation we came to know that we can use optional
		 * for properties but the method "target.setOptionalProperties(list)" is not
		 * working either.
		 * 
		 * For now we have hard coded the one property "xmfo:name"
		 */

		ArrayList<String> al1 = new ArrayList<String>();
		// al1.add("xmfo:name");
		// al1.add("");
		target.setProperties(targetPropertylist);
		// target.setProperties(al1);
		// target.setOptionalProperties(targetPropertylist);

		target.setPrefixes(targetPrefixesMap);

		target.setFunctions(functions);

		target.setType(type);
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
		conf.setAcceptanceThreshold(0.8);

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
		conf.setOutputFormat("TAB"); // NT or TTL

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

		System.out.println(" doe  done ");
		// System.exit(0);
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
				// System.out.println("****************-URL with Hash********************");
				// System.out.println("predicate : " + predicate);

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
				// System.out.println("****************-URL without Hash********************");
				// System.out.println("predicate : " + predicate);

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
			// System.out.println("propertyMap10 : " +
			// qsol.getResource("predicate").toString());

		});

		System.out.println("Here is the log propertyMap : " + propertyMap);

		resultOne.forEachRemaining(qsol -> System.out.println("khad2 : " + qsol.getLiteral("predicate").getInt()));
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
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
	 * Takes entity as input return list of properties from file. Listing properties
	 * with their counts
	 */
	public List<PropertyEntity> getPropertiesFromFile(String path, String restriction, int maximumProperties) {

		// restriction = "http://xmlns.com/foaf/0.1/Person";

		PrefixEntity restrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(restriction);
		System.out.println("restrictionPrefixEntity:: " + restrictionPrefixEntity);

		InstanceCount instanceCount = new InstanceCount();
		double size = instanceCount.countInstanceFromFile(path, restrictionPrefixEntity);

		System.out.println("getPropertiesFromFile -> Total instance of '" + restriction + "' is : " + size);

		List<PropertyEntity> propertiesListTemp = new ArrayList<PropertyEntity>();

		Model model = ModelFactory.createDefaultModel();

		RDFDataMgr.read(model, path, Lang.NTRIPLES);
		String queryString1 = "PREFIX " + restrictionPrefixEntity.key + ": <" + restrictionPrefixEntity.value + ">\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "PREFIX url: <http://schema.org/>\r\n"
				+ "\r\n" + "PREFIX url1: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
				+ "  ?instance rdf:type " + restrictionPrefixEntity.key + ":" + restrictionPrefixEntity.name + " .\r\n"
				+ "  ?instance ?predicate ?o .\r\n" + "  FILTER(isLiteral(?o)) \r\n" + "} \r\n"
				+ "GROUP BY ?predicate\r\n" + "order by desc ( ?count )\r\n" + "LIMIT " + maximumProperties;

		// url1:Person
//		http://xmlns.com/foaf/0.1/Person

		// JUST FOR DEBUG remove before commit
		Query query1 = QueryFactory.create(queryString1);
		QueryExecution qexec1 = QueryExecutionFactory.create(query1, model);
		ResultSet results = qexec1.execSelect();

		if (debug) {
			System.out.println("result 009 The output of sparql query : " + results);
			ResultSetFormatter.out(System.out, results);
		}
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

				// System.out.println(" ckcpppa PredicateCount :" + PredicateCount);

				// System.out.println(" ckcpppa size :" + size);
				// System.out.println(" ckcpppa coverage :" + coverage);
				// System.out.println(" ckcpppa check :" + PredicateCount / size);

			} else {
				coverage = 0;
			}

			PropertyEntity p1 = new PropertyEntity(prefixEntity.key, prefixEntity.value, prefixEntity.name,
					PredicateCount, coverage);
			propertiesListTemp.add(p1);

		});

		System.out.println("propertiesListTemp: " + propertiesListTemp);
		return propertiesListTemp;
	}

	/*
	 * Takes entity as input return list of properties from file. Listing properties
	 * with their counts
	 */
	public List<PropertyEntity> getPropertiesFromURL(String path, String restriction, int maximumProperties) {

		System.out.println(" In there 855 " + path + " - " + restriction + " - " + maximumProperties);
		PrefixEntity restrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(restriction);
		System.out.println(" In there 856 restrictionPrefixEntity :  " + restrictionPrefixEntity);

		System.out.println("restrictionPrefixEntity:: " + restrictionPrefixEntity);

		InstanceCount instanceCount = new InstanceCount();
		double size = instanceCount.countInstanceFromURL(path, restrictionPrefixEntity);

		System.out.println("getPropertiesFromFile -> Total instance of '" + restriction + "' is : " + size);

		List<PropertyEntity> propertiesListTemp = new ArrayList<PropertyEntity>();
		System.out.println(" khalid Bin Huda  : " + InstanceCount.getFinalRedirectedUrl(path));

		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(InstanceCount.getFinalRedirectedUrl(path),
				"PREFIX " + restrictionPrefixEntity.key + ": <" + restrictionPrefixEntity.value + ">\r\n"
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
						+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n"
						+ "PREFIX url1: <http://xmlns.com/foaf/0.1/>\r\n"
						+ "SELECT  (COUNT(Distinct ?instance) as ?count) ?predicate\r\n" + "WHERE\r\n" + "{\r\n"
						+ "  ?instance rdf:type " + restrictionPrefixEntity.key + ":" + restrictionPrefixEntity.name
						+ " .\r\n" + "  ?instance ?predicate ?o .\r\n" + "  FILTER(isLiteral(?o)) \r\n" + "} \r\n"
						+ "GROUP BY ?predicate\r\n" + "order by desc ( ?count )\r\n" + "LIMIT " + maximumProperties);

		// url1:Person
//		http://xmlns.com/foaf/0.1/Person

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		resultOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();

			// System.out.println(" lookit : " + predicate);
			PrefixEntity prefixEntity = PrefixUtility.splitPreficFromProperty(predicate);

			double coverage;

			if (size > 0) {
				coverage = PredicateCount / size;

				// System.out.println(" ckcpppa PredicateCount :" + PredicateCount);

				// System.out.println(" ckcpppa size :" + size);
				// System.out.println(" ckcpppa coverage :" + coverage);
				// System.out.println(" ckcpppa check :" + PredicateCount / size);

			} else {
				coverage = 0;
			}

			PropertyEntity p1 = new PropertyEntity(prefixEntity.key, prefixEntity.value, prefixEntity.name,
					PredicateCount, coverage);
			propertiesListTemp.add(p1);

		});

		System.out.println("propertiesListTemp url: " + propertiesListTemp);

		return propertiesListTemp;
	}

	public void addStatement(String s, String p, String o, Model model) {
		Resource subject = model.createResource(s);
		Property predicate = model.createProperty(p);
		RDFNode object = model.createResource(o);
		Statement stmt = model.createStatement(subject, predicate, object);
		model.add(stmt);
	}

	private boolean isDataAvailableURL(String url, String restriction, List<PropertyEntity> propertyEntities) {

		StringBuilder varibleQueryPart = new StringBuilder();
		StringBuilder prefixQueryPart = new StringBuilder();
		StringBuilder propertyQueryPart = new StringBuilder();

		int i = 1;
		for (PropertyEntity propertyEntity : propertyEntities) {
			varibleQueryPart.append(" ?v" + i);
			prefixQueryPart.append("PREFIX " + propertyEntity.key + ": <" + propertyEntity.value + ">\r\n");
			propertyQueryPart
					.append("?t " + propertyEntity.key + ":" + propertyEntity.propertyName + " ?v" + i + " . \n");
			i++;
		}
		String queryString = prefixQueryPart + "SELECT DISTINCT ?t " + varibleQueryPart + "\nWHERE {" + "\n"
				+ propertyQueryPart + " } LIMIT 1";
		System.out.println("queryString:  \n" + queryString);

		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(url, queryString);
		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		System.out.println(" Output from  isDataAvailableURL : " + resultOne);
		ResultSetFormatter.out(System.out, resultOne);

		// resultOne.forEachRemaining(qsol -> totalInstances =
		// qsol.getLiteral("totalInstances").getInt());
		qe.close();
		return true;
	}

	/*
	 * Checking if the data is available in the data set, if we do a query with
	 * specific properties
	 */
	public boolean isDataAvailableFile(String path, String restriction, int maximumProperties,
			List<PropertyEntity> propertyEntities, String tag) {

		Model model = ModelFactory.createDefaultModel();

		RDFDataMgr.read(model, path, Lang.NTRIPLES);

		StringBuilder varibleQueryPart = new StringBuilder();
		StringBuilder prefixQueryPart = new StringBuilder();
		StringBuilder propertyQueryPart = new StringBuilder();

		int i = 1;
		for (PropertyEntity propertyEntity : propertyEntities) {
			varibleQueryPart.append(" ?v" + i);
			prefixQueryPart.append("PREFIX " + propertyEntity.key + ": <" + propertyEntity.value + ">\r\n");
			propertyQueryPart
					.append("?t " + propertyEntity.key + ":" + propertyEntity.propertyName + " ?v" + i + " . \n");
			i++;
		}

		String queryString = prefixQueryPart + "SELECT DISTINCT ?t " + varibleQueryPart + "\nWHERE {" + "\n"
				+ propertyQueryPart + " } LIMIT 1";

		System.out.println("queryString:  \n" + queryString);

		/*
		 * 
		 * // JUST FOR DEBUG remove before commit Query query1 =
		 * QueryFactory.create(queryString1); QueryExecution qexec1 =
		 * QueryExecutionFactory.create(query1, model); ResultSet results =
		 * qexec1.execSelect(); // System.out.println("result 009 : " + results); //
		 * ResultSetFormatter.out(System.out, results); ///
		 */

		String a = "PREFIX w3200: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX dbpr: <http://dbpedia.org/property/>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "SELECT DISTINCT ?t  ?v1 ?v2 ?v3 ?v4 ?v5 ?v6 ?v7\r\n" + "WHERE {\r\n" + "?t w3200:label ?v1 .\r\n"
				+ "?t xmfo:gender ?v2 .\r\n" + "?t xmfo:givenName ?v3 .\r\n" + "?t xmfo:nasdame ?v4 .\r\n"
				+ "?t dbpr:dateOfBirth ?v5 .\r\n" + "?t xmfo:birthday ?v6 .\r\n" + "?t xmfo:familyName ?v7 .\r\n"
				+ " } LIMIT 1";

		Query query = QueryFactory.create(queryString); // queryString, a
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		// results.
		System.out.println(" 099kkhan bhai : " + results.toString());
		System.out.println(" 099kkhan bhai getRowNumber : " + results.getRowNumber());
		System.out.println(" 099kkhan bhai hasNext : " + results.hasNext());

		if (results.hasNext() == false) {
			System.out.println(" !! No data avaible for following query for " + tag + " !! ");
			System.out.println(" File Path: " + path);
			System.out.println("*****************************************");
			System.out.println(" Query String: " + "\n" + queryString + "\n");
			System.out.println("*****************************************");
		}

		if (debug) {
			System.out.println("result 0019 show data that is return after query : " + results); //
			ResultSetFormatter.out(System.out, results);
		}

		Query query2 = QueryFactory.create(queryString);
		QueryExecution qexec2 = QueryExecutionFactory.create(queryString, model);
		ResultSet resultsOne = ResultSetFactory.copyResults(qexec2.execSelect());
		resultsOne.forEachRemaining(qsol -> {
			String predicate = qsol.getLiteral("v1").toString();
			System.out.println(" lookit : " + predicate);
		});
		// resultsOne.getRowNumber()

		return true;
	}

	public void printOut(Object toBePrinted) {
		if (debugLogs)
			System.out.println(toBePrinted);
	}

	/*
	 * HashMap tabuPropertiesToList(String propertyString ){
	 * 
	 * return HashMap;
	 * 
	 * }
	 */

}