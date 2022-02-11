package org.aksw.deer.plugin.kgfusion;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * This class create the DEER Operator for instance matching Operator. It the
 * entry point for the operator.
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
@Extension
public class InstanceMatchingOperator extends AbstractParameterizedEnrichmentOperator {

	/**
	 * The constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(InstanceMatchingOperator.class);

	/**
	 * Declaring the variables
	 */
	public HashMap<String, String> prefixMap;
	public HashMap<String, Integer> propertyMap;
	public HashMap<String, Double> coverageMap;
	public List<PrefixEntity> propertiesPrefixesSource;
	public List<PropertyEntity> propertiesListSource1;
	public List<PropertyEntity> propertiesListTarget1;

	public int totalInstances;
	private static boolean debug;
	private static boolean debugLogs;

	Set<String> entityListFile;

	private HashMap<String, Resource> tabuSourceProperty = new HashMap<String, Resource>();
	private HashMap<String, Resource> tabuTargetProperty = new HashMap<String, Resource>();;

	/**
	 * Declaring the variables from DEER configuration file. 
	 */
	public static final Property COVERAGE = DEER.property("coverage");
	public static final Property MAX_LIMIT = DEER.property("maxLimit");
	public static final Property TEST = DEER.property("test");
	public static final Property TYPE = DEER.property("type");
	public static final Property SOURCE = DEER.property("source");
	public static final Property TARGET = DEER.property("target");
	public static final Property SOURCE_RESTRICTION = DEER.property("sourceRestriction");
	public static final Property TARGET_RESTRICTION = DEER.property("targetRestriction");
	public static final Property TABU_SOURCE_PROPERTY = DEER.property("tabuSourceProperty");
	public static final Property TABU_TARGET_PROPERTY = DEER.property("tabuTargetProperty");
	public static final Property DEBUG_LOGS = DEER.property("debugLogs");
	public static final Property PROPERTY_URI = DEER.property("propertyURI");
	public static final Property RESTRICTION_PREDICATE_URI = DEER.property("restrictionPredicateURI");
	public static final Property RESTRICTION_URI = DEER.property("restrictionURI");
	public static final Property RESTRICTION_ORDER = DEER.property("restrictionOrder");
	public static final Property SOURCE_R = DEER.property("sourceR");
	public static final Property ONTOLOGYINPUT = DEER.property("ontologyInput");
	String ontologyInputSubject = null;
	String ontologyInputObject = null;
	String ontologyInputSubjectEndpoint = null;
	String ontologyInputObjectEndpoint = null;

	List<Model> outputList = new ArrayList<>();;
	Restriction sourceResObj;
	Restriction targetResObj;
	RestrictionEntity restrictionEntity;
	RestrictionEntity temp;

	public InstanceMatchingOperator() {
		super();
	}

	/** createParameterMap */
	@Override
	public ValidatableParameterMap createParameterMap() {
		return ValidatableParameterMap.builder().declareProperty(COVERAGE).declareProperty(MAX_LIMIT)
				.declareProperty(TEST).declareProperty(TYPE).declareProperty(SOURCE).declareProperty(TARGET)
				.declareProperty(SOURCE_RESTRICTION).declareProperty(TARGET_RESTRICTION)
				.declareProperty(TABU_SOURCE_PROPERTY).declareProperty(TABU_TARGET_PROPERTY).declareProperty(DEBUG_LOGS)
				.declareProperty(RESTRICTION_PREDICATE_URI).declareProperty(RESTRICTION_URI)
				.declareProperty(RESTRICTION_ORDER).declareProperty(SOURCE_R).declareProperty(ONTOLOGYINPUT)
				.declareValidationShape(getValidationModelFor(InstanceMatchingOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3

		/** output from ontology operator */
		Model ontologyModel = models.get(0);

		List<RestrictionEntity> srcRes_temp = new ArrayList<RestrictionEntity>();
		List<RestrictionEntity> tarRes_temp = new ArrayList<RestrictionEntity>();

		/**
		 * Getting restrictions for target data set from configuration file.
		 */
		getParameterMap().listPropertyObjects(TARGET_RESTRICTION).map(RDFNode::asResource).forEach(op -> {
			final Resource res_pre_uri = op.getPropertyResourceValue(RESTRICTION_PREDICATE_URI).asResource();
			final Resource res_uri = op.getPropertyResourceValue(RESTRICTION_URI).asResource();
			final Resource res_order = op.getPropertyResourceValue(RESTRICTION_ORDER).asResource();

			String res_order_string = res_order.toString();
			int order = Integer.parseInt(res_order_string.substring(res_order_string.lastIndexOf('/') + 1).trim());

			restrictionEntity = new RestrictionEntity(order, res_pre_uri.toString(), res_uri.toString());

			tarRes_temp.add(restrictionEntity);
		});

		String ontologyInput = getParameterMap().getOptional(ONTOLOGYINPUT).map(RDFNode::asLiteral)
				.map(Literal::getString).orElse("false");

		Comparator<RestrictionEntity> c = (s1, s2) -> s1.order.compareTo(s2.order);
		tarRes_temp.sort(c);

		/**
		 * Getting source restriction for target data set from configuration file.
		 */
		getParameterMap().listPropertyObjects(SOURCE_RESTRICTION).map(RDFNode::asResource).forEach(op -> {
			final Resource res_pre_uri = op.getPropertyResourceValue(RESTRICTION_PREDICATE_URI).asResource();
			final Resource res_uri = op.getPropertyResourceValue(RESTRICTION_URI).asResource();
			final Resource res_order = op.getPropertyResourceValue(RESTRICTION_ORDER).asResource();

			String res_order_string = res_order.toString();
			int order = Integer.parseInt(res_order_string.substring(res_order_string.lastIndexOf('/') + 1).trim());

			restrictionEntity = new RestrictionEntity(order, res_pre_uri.toString(), res_uri.toString());

			srcRes_temp.add(restrictionEntity);
		});

		Comparator<RestrictionEntity> d = (s1, s2) -> s1.order.compareTo(s2.order);
		srcRes_temp.sort(d);

		Util util = new Util();
		Restriction sourceResObj = new Restriction("s");
		Restriction targetResObj = new Restriction("t");

		/**
		 * only execute if you want to take input from ontology operator.
		 */
		if (ontologyInput.equals("true")) {

			StmtIterator it = ontologyModel.listStatements();
			while (it.hasNext()) {
				Statement stmt = it.next();
				System.out.println("stmt.getPredicate().toString()" + stmt.getPredicate().toString());

				/* Get Source Restriction http://www.w3.org/1999/02/22-rdf-syntax-ns#subject */
				if (stmt.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject")) {
					ontologyInputSubject = stmt.getObject().toString();
				}

				/* Get Target Restriction http://www.w3.org/1999/02/22-rdf-syntax-ns#object */
				if (stmt.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#object")) {
					ontologyInputObject = stmt.getObject().toString();
				}

				/* Get https://w3id.org/deer/SubjectEndPoint */
				if (stmt.getPredicate().toString().equals("https://w3id.org/deer/SubjectEndPoint")) {
					ontologyInputSubjectEndpoint = stmt.getObject().toString();
					// get object
				}

				/* Get https://w3id.org/deer/ObjectEndPoint */
				if (stmt.getPredicate().toString().equals("https://w3id.org/deer/ObjectEndPoint")) {
					ontologyInputObjectEndpoint = stmt.getObject().toString();
					// get object
				}
			}

			/* Creating restriction entity for source */
			temp = new RestrictionEntity(1, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ontologyInputSubject);
			srcRes_temp.clear();
			srcRes_temp.add(temp);

			sourceResObj = util.restrictionUriToString(srcRes_temp, sourceResObj);

			/* Creating restriction entity for target */
			temp = new RestrictionEntity(1, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ontologyInputObject);
			tarRes_temp.clear();
			tarRes_temp.add(temp);

			targetResObj = util.restrictionUriToString(tarRes_temp, targetResObj);

		} else { /* else it will take the input from configuration file */
			sourceResObj = util.restrictionUriToString(srcRes_temp, sourceResObj);
			targetResObj = util.restrictionUriToString(tarRes_temp, targetResObj);

		}

		/* Getting the input from configuration file */
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
		getParameterMap().listPropertyObjects(TABU_SOURCE_PROPERTY).map(RDFNode::asResource).forEach(op -> {
			final Resource propertyUri = op.getPropertyResourceValue(PROPERTY_URI).asResource();
			tabuSourceProperty.put(propertyUri.toString(), propertyUri);
		});
		getParameterMap().listPropertyObjects(TABU_TARGET_PROPERTY).map(RDFNode::asResource).forEach(op -> {
			final Resource propertyUri = op.getPropertyResourceValue(PROPERTY_URI).asResource();
			tabuTargetProperty.put(propertyUri.toString(), propertyUri);
		});

		/**
		 * only debugging
		 */
		System.out.println(" --------Logging Parameters-------------");
		System.out.println(" Coverage: " + coverage);
		System.out.println(" MaxLimit: " + maxLimit);
		System.out.println(" Test: " + test);
		System.out.println(" Type: " + type);
		System.out.println(" Source: " + source);
		System.out.println(" Target: " + target);
		System.out.println(" \n SourceResObj1:: : \n " + sourceResObj.restrictionPrefixEntity);
		System.out.println("\n TargetResObj : \n  " + targetResObj.restrictionPrefixEntity);
		System.out.println(" -------------------------");

		String inputEndpoint = type;
		String sourceFilePath = source;
		String targetFilePath = target;
		debug = true;

		/* if the end point is a file */
		if (inputEndpoint.equals("file")) {
			System.out.println(" ENDPOINT: FILE");

			if (ontologyInput.equals("true")) {
				sourceFilePath = ontologyInputSubjectEndpoint;
				targetFilePath = ontologyInputObjectEndpoint;
			}


			/**
			 * Getting properties from source file
			 * 
			 */
			propertiesListSource1 = getPropertiesFromFile(sourceFilePath, sourceResObj, maxLimit);

			/**
			 * Getting properties from target file
			 */
			propertiesListTarget1 = getPropertiesFromFile(targetFilePath, targetResObj, maxLimit);

			System.out.println("------------------------------------------------");
			System.out.println("propertiesListSource from source -->: " + propertiesListSource1);
			System.out.println("propertiesListTarget from target -->: " + propertiesListTarget1);
			System.out.println("------------------------------------------------");

			/**
			 * Remove tabu properties for source
			 */
			removeTabuProperties(tabuSourceProperty, propertiesListSource1);
			
			/**
			 * Remove tabu properties for target
			 */
			removeTabuProperties(tabuTargetProperty, propertiesListTarget1);

			/**
			 * Remove properties that have less coverage for source than coverage parameter(Set in
		     * configuration.ttl)
			 */
			removePropertiesHavingLowerCoverage(coverage, propertiesListSource1);
			
			/**
			 * Remove properties that have less coverage for target than coverage parameter(Set in
		     * configuration.ttl)
			 */
			removePropertiesHavingLowerCoverage(coverage, propertiesListTarget1);

			System.out.println(
					"rrrrrrrrr -> Total Properties after comparing with Coverage: " + propertiesListTarget1.size());

			// 
			

			/**
			 *If no property have the coverage than the coverage parameter(Set in
			 * configuration.ttl) then it will stop the execution
			 */
			if (propertiesListSource1.size() < 1 || propertiesListTarget1.size() < 1) {

				System.out.println(
						" Can not proceed because " + "propertiesListSource`s size= " + propertiesListSource1.size()
								+ " propertiesListTarget`s size=  " + propertiesListTarget1.size());
				// if the above if is true then the execution should be stopped.
				return null;
			}

			
			/**
			 * Check if we get any data if query with following property list for source.
			 */
			isDataAvailableFile(sourceFilePath, sourceResObj, maxLimit, propertiesListSource1, "source");
			
			/**
			 * Check if we get any data if query with following property list for target.
			 */
			isDataAvailableFile(targetFilePath, targetResObj, maxLimit, propertiesListTarget1, "target");

			/**
			 * Creating configuration object for LIMES.
			 */
			Configuration con = createLimeConfigurationFile(sourceFilePath, targetFilePath, "NT", sourceResObj,
					targetResObj);

			/** Calling LIMES */
			callLimes(con);
			
			System.out.println("--> In Output Generating Phase");

			OutputUtility ouputUtility = new OutputUtility();
			
			/** Creating Reificated output*/
			List<Model> l1 = ouputUtility.createOuput("accepted.nt", sourceFilePath, targetFilePath, "File",
					sourceResObj, targetResObj);
			
			/** Returing the output to consolidation operator  */
			return l1;
		} 
		else { /** if the endpoint is url */
			System.out.println(" ENDPOINT: URL");

			String sourceEndpoint = source;
			String targetEndpoint = target;
			
			if (ontologyInput.equals("true")) {
				sourceEndpoint = ontologyInputSubjectEndpoint;
				targetEndpoint = ontologyInputObjectEndpoint;
			}

			
			/**
			 * Getting properties from Url end-point for source
			 * 
			 */
			propertiesListSource1 = getPropertiesFromURL(sourceEndpoint, sourceResObj, maxLimit, "s");
			
			/**
			 * Getting properties from Url end-point for target
			 * 
			 */
			propertiesListTarget1 = getPropertiesFromURL(targetEndpoint, targetResObj, maxLimit, "t");

			System.out.println("------------------------------------------------");
			System.out.println("propertiesListSource from source -->: " + propertiesListSource1);
			System.out.println("propertiesListTarget from target -->: " + propertiesListTarget1);
			System.out.println("------------------------------------------------");

			
			/**
			 * Remove properties that have less coverage for target than coverage parameter(Set in
		     * configuration.ttl) for source
			 */
			removePropertiesHavingLowerCoverage(coverage, propertiesListSource1);
		
			
			/**
			 * Remove properties that have less coverage for target than coverage parameter(Set in
		     * configuration.ttl) for target
			 */
			removePropertiesHavingLowerCoverage(coverage, propertiesListTarget1);

			System.out.println(
					"rrrrrrrrr -> Total Properties after comparing with Coverage: " + propertiesListTarget1.size());

			 
			/**
			 *If no property have the coverage than the coverage parameter(Set in
			 * configuration.ttl) then it will stop the execution
			 */
			if (propertiesListSource1.size() < 1 || propertiesListTarget1.size() < 1) {

				System.out.println(
						" Can not proceed because " + "propertiesListSource`s size= " + propertiesListSource1.size()
								+ " propertiesListTarget`s size=  " + propertiesListTarget1.size());
			}

			// Check if the data is available, if we query it with following properties
			// isDataAvailableURL(sourceEndpoint, sourceRestrictions,
			// propertiesListSource1);
			// isDataAvailableURL(targetEndpoint, targetRestrictions,
			// propertiesListTarget1);

			/**
			 * Creating configuration object for LIMES.
			 */
			Configuration con = createLimeConfigurationFile(sourceEndpoint, targetEndpoint, "sparql", sourceResObj,
					targetResObj);

			/** Calling LIMES */
			callLimes(con);

			System.out.println("--> In Output Generating Phase");
			OutputUtility ouputUtility = new OutputUtility();

			/** Creating Reificated output*/
			List<Model> l1 = ouputUtility.createOuput("accepted.nt", sourceFilePath, targetFilePath, "File",
					sourceResObj, targetResObj);
			/** Returing the output to consolidation operator  */
			return l1;
		}

	}

	 /**
	   * Remove tabu properties.
	   *
	   * @param tabuSourceProperty  the tabu properties list
	   * @param propertiesList the properties list
	   */
	private void removeTabuProperties(HashMap<String, Resource> tabuSourceProperty,
			List<PropertyEntity> propertiesList) {

		System.out.println(
				"removeTabuProperties ->  Total Properties before removing tabu properties: " + propertiesList.size());

		if (!tabuSourceProperty.isEmpty()) {

			for (Entry<String, Resource> entry : tabuSourceProperty.entrySet()) {
				String propertyString = entry.getKey();

				String value = propertyString.substring(0, (propertyString.lastIndexOf("/")) + 1);
				String property = propertyString.substring(propertyString.lastIndexOf("/") + 1);

				for (int i = 0; i < propertiesList.size(); i++) {
					if (propertiesList.get(i).propertyName.equals(property)
							&& propertiesList.get(i).value.equals(value)) {
						System.out
								.println("propertiesList.get(i).propertyName : " + propertiesList.get(i).propertyName);
						System.out.println("property : " + property);
						System.out.println(" propertiesList.get(i).value : " + propertiesList.get(i).value);
						System.out.println("value : " + value);
						System.out.println(" *matched* ");
						propertiesList.remove(i);
					}
				}
			}

		}
		System.out.println(
				"removeTabuProperties -> Total Properties after removing tabu properties: " + propertiesList.size());
	}

	/*
	 * remove properties that have lower coverage than Coverage parameter (the
	 * Parameter set from Configura.ttl file)
	 */

	
	 /**
	   * Remove properties that have lower coverage.
	   *
	   * @param coverage  the coverage
	   * @param tempPropertiesListSource the property list
	   */
	private void removePropertiesHavingLowerCoverage(double coverage, List<PropertyEntity> tempPropertiesListSource) {
		System.out.println(" ahsan tempCoverage = " + coverage + " ++ \n " + tempPropertiesListSource);

		System.out.println("removePropertiesHavingLowerCoverage -> Total Properties before comparing with Coverage: "
				+ tempPropertiesListSource.size());
		Iterator itr = tempPropertiesListSource.iterator();

		while (itr.hasNext()) {
			PropertyEntity propertyEntity = (PropertyEntity) itr.next();
			if (propertyEntity.coverage < coverage) {
				itr.remove();
			}
		}

		System.out.println("removePropertiesHavingLowerCoverage -> Total Properties after comparing with Coverage: "
				+ tempPropertiesListSource.size());
		System.out.println("removePropertiesHavingLowerCoverage -> list after = " + tempPropertiesListSource);
	}

	
	 /**
	   * Create LIMES Configuration.
	   *
	   * @param srcEndpoint  the source end-point
	   * @param targetEndpoint the target end-point
	   * @param type  the type (URL or File). 
	   * @param targetEndpoint the target end-point
	   * @param sourceResObj2 the source restriction
	   * @param targetResObj2 the target restriction
	   */
	public Configuration createLimeConfigurationFile(String srcEndpoint, String targetEndpoint, String type,
			Restriction sourceResObj2, Restriction targetResObj2) {

		Configuration conf = new Configuration();

		List<String> srcPropertylist = new ArrayList<String>();
		List<String> targetPropertylist = new ArrayList<String>();

		for (PropertyEntity list : propertiesListSource1) {
			conf.addPrefix(list.key, list.value);
			srcPropertylist.add(list.key + ":" + list.propertyName);
		}

		conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		conf.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		conf.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		conf.addPrefix("url", "http://schema.org/");

		KBInfo src = new KBInfo();

		src.setId("sourceId");
		src.setEndpoint(srcEndpoint);
		src.setVar("?s");
		src.setPageSize(10000);

		src.setRestrictions(sourceResObj2.restrictionList);
		src.setProperties(srcPropertylist);
		src.setType(type);

		Map<String, String> prefixes = new HashMap<String, String>();

		prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("url", "http://schema.org/");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

		for (PropertyEntity list : propertiesListSource1) {
			/** adding Prefix */
			prefixes.put(list.key, list.value);
			System.out.println("debug new prefixes.put key: + " + list.key + " value: " + list.value);
		}

		/** Setting Prefix for source restriction */
		for (PrefixEntity list : sourceResObj2.restrictionPrefixEntity) {
			prefixes.put(list.key, list.value);
		}

		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("url", "http://schema.org/");

		src.setPrefixes(prefixes);

		HashMap<String, String> tempHashMap = new HashMap<String, String>();
		tempHashMap.put("rdfs:label", "");
		LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<String, Map<String, String>>();
		src.setFunctions(functions);

		conf.setSourceInfo(src);

		Map<String, String> targetPrefixesMap = new HashMap<String, String>();
		targetPrefixesMap.put("owl", "http://www.w3.org/2002/07/owl#");
		targetPrefixesMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		targetPrefixesMap.put("url", "http://schema.org/");
		targetPrefixesMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		targetPrefixesMap.put("yago", "http://yago-knowledge.org/resource/");

		// setting prefix for target
		for (PropertyEntity list : propertiesListTarget1) {

			conf.addPrefix(list.key, list.value);

			targetPrefixesMap.put(list.key, list.value);
			targetPropertylist.add(list.key + ":" + list.propertyName);
			/** For debugging */
			System.out.println(
					"debug new target  : list.key  + " + list.key + " list.propertyName:  " + list.propertyName);
		}

		KBInfo target = new KBInfo();
		target.setId("targetId");
		target.setEndpoint(targetEndpoint);
		target.setVar("?t");
		target.setPageSize(10000);

		target.setRestrictions(targetResObj2.restrictionList);

		/** Setting Prefix for target restriction */
		for (PrefixEntity list : targetResObj2.restrictionPrefixEntity) {
			targetPrefixesMap.put(list.key, list.value);
		}

		ArrayList<String> al1 = new ArrayList<String>();
		target.setProperties(targetPropertylist);
		target.setPrefixes(targetPrefixesMap);

		target.setType(type);
		conf.setTargetInfo(target);

		/** Set either Metric or MLALGORITHM */
		MLImplementationType mlImplementationType = MLImplementationType.UNSUPERVISED;
		conf.setMlAlgorithmName("wombat simple");
		conf.setMlImplementationType(mlImplementationType);

		LearningParameter learningParameter = new LearningParameter();
		learningParameter.setName("max execution time in minutes");
		learningParameter.setValue(60);

		List<LearningParameter> mlAlgorithmParameters = new ArrayList<>();
		mlAlgorithmParameters.add(learningParameter);

		conf.setMlAlgorithmParameters(mlAlgorithmParameters);

		/** Acceptance */
		conf.setAcceptanceThreshold(0.6);

		conf.setAcceptanceFile("accepted.nt");
		conf.setAcceptanceRelation("owl:sameAs");

		/** Review */
		conf.setVerificationThreshold(0.2);
		conf.setVerificationFile("reviewme.nt");
		conf.setVerificationRelation("owl:sameAs");

		/** EXECUTION */
		conf.setExecutionRewriter("default");
		conf.setExecutionPlanner("default");
		conf.setExecutionEngine("default");

		/** Output format */
		conf.setOutputFormat("TAB"); // NT or TTL

		System.out.println(" The Configuration Object: \n" + conf + " \n");
		return conf;
	}

	/*
	 * This is the function where the configuration object is given to the LIMES.
	 * @param config The LIMES configuration Object
	 */
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

	/*
	 * Takes entity/restriction as input return list of properties from file. Listing properties
	 * with their counts.
	 *
	 * @param path the path to the file
	 * @param resObj the Restriction object
	 * @param maximumProperties the maximum amount of properties
	 * @return PropertyEntity the list of PropertyEntity
	 */
	public List<PropertyEntity> getPropertiesFromFile(String path, Restriction resObj, int maximumProperties) {

		InstanceCount instanceCount = new InstanceCount();

		double size = instanceCount.countInstanceFromFile(path, resObj);
		List<PropertyEntity> propertiesListTemp = new ArrayList<PropertyEntity>();
		System.out.println("getPropertiesFromFile -> Total instance of '" + resObj + "' is : " + size);

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
		}

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}

		String getPropertiesFromURLString = restrictionQueryPrefix
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n "
				+ "SELECT ?predicate (COUNT(?predicate) as ?count)\r\n" + "WHERE\r\n" + "{\r\n" +
				// + " ?" + variable + " rdf:type url:Movie .\r\n" + " ?"+ variable
				restrictionQuery + "?" + resObj.variable + " ?predicate ?o .\r\n" + " FILTER(isLiteral(?o) ). \r\n "
				+ "} \r\n" + "GROUP BY ?predicate\r\n" + "order by desc ( ?count )" + " LIMIT " + maximumProperties;

		Model model = ModelFactory.createDefaultModel();

		RDFDataMgr.read(model, path, Lang.NTRIPLES);
		 
		// JUST FOR DEBUG remove before commit
		Query query1 = QueryFactory.create(getPropertiesFromURLString);
		QueryExecution qexec1 = QueryExecutionFactory.create(query1, model);
		ResultSet results = qexec1.execSelect();

		System.out.println(" The result sparql query : " + results);
		ResultSetFormatter.out(System.out, results);

		Query query = QueryFactory.create(getPropertiesFromURLString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		// ADDING HERE
		ResultSet resultsOne = ResultSetFactory.copyResults(qexec.execSelect());

		resultsOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();

			PrefixEntity prefixEntity = PrefixUtility.splitPreficFromProperty(predicate);

			double coverage;
			
			/** Calculating coverage */
			if (size > 0) {
				coverage = PredicateCount / size;
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
	 * Takes entity/restriction as input return list of properties from file. Listing properties
	 * with their counts.
	 *
	 * @param path the path to the file
	 * @param resObj the Restriction object
	 * @param maximumProperties the maximum amount of properties
	 * @return PropertyEntity the list of PropertyEntity
	 */
	public List<PropertyEntity> getPropertiesFromURL(String path, Restriction resObj, int maximumProperties,
			String variable) {

		InstanceCount instanceCount = new InstanceCount();

		double size = instanceCount.countInstanceFromURL(path, resObj.restrictionPrefixEntity.get(1), resObj);
		List<PropertyEntity> propertiesListTemp = new ArrayList<PropertyEntity>();

		String restrictionQuery = "";
		for (String list : resObj.restrictionList) {
			restrictionQuery = restrictionQuery + list + " .\r\n";
		}

		String restrictionQueryPrefix = "";
		for (PrefixEntity list : resObj.restrictionPrefixEntity) {
			restrictionQueryPrefix = restrictionQueryPrefix + "PREFIX " + list.key + ": <" + list.value + ">\r\n";
		}

		String getPropertiesFromURLString = restrictionQueryPrefix
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "\r\n "
				+ "SELECT ?predicate (COUNT(?predicate) as ?count)\r\n" + "WHERE\r\n" + "{\r\n" +
				// + " ?" + variable + " rdf:type url:Movie .\r\n" + " ?"+ variable
				restrictionQuery + "?" + variable + " ?predicate ?o .\r\n" + " FILTER(isLiteral(?o) ). \r\n " + "} \r\n"
				+ "GROUP BY ?predicate\r\n" + "order by desc ( ?count )" + " LIMIT " + maximumProperties;

		QueryExecution qe = QueryExecutionFactory.sparqlService(path, getPropertiesFromURLString);

		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());
		resultOne.forEachRemaining(qsol -> {
			String predicate = qsol.getResource("predicate").toString();
			int PredicateCount = qsol.getLiteral("count").getInt();
			/*
			 * System.out.println("-----------------------");
			 * System.out.println(" predicate: " + predicate);
			 * System.out.println(" PredicateCount: " + PredicateCount);
			 * System.out.println("-----------------------"); //
			 * System.out.println(" lookit : " + predicate);
			 */
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

		System.out.println("***************************************");
		System.out.println("getPropertiesFromURL -> endpoint: " + path + " - " + resObj.restrictionPrefixEntity + " - "
				+ maximumProperties);
		System.out.println("getPropertiesFromURL -> Total instance is : " + size);
		System.out.println("getPropertiesFromURL -> The Property List From URL Endpoint: " + propertiesListTemp);
		System.out.println("***************************************");

		return propertiesListTemp;
	}

	/**
	 * Check if the data is available for following restriction
	 *
	 * @param url the url of end-point
	 * @param restriction the Restriction
	 * @param propertyEntities the property entities
	 * @return boolean true if data available
	 */
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

		/** restriction */
		PrefixEntity restrictionPrefixEntity = PrefixUtility.splitPreficFromProperty(restriction);

		/** Adding restriction prefix and restriction */
		String restrictionPrefix = "PREFIX " + restrictionPrefixEntity.key + ": <" + restrictionPrefixEntity.value
				+ "> \n";

		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				+ "PREFIX url: <http://schema.org/> \n " + restrictionPrefix + prefixQueryPart + "SELECT DISTINCT ?t "
				+ varibleQueryPart + "\nWHERE { \n " + " ?t rdf:type url:Movie ." + "\n" + propertyQueryPart
				+ " } LIMIT 1";

		
		String qtest = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX url: <http://schema.org/>\r\n" + "PREFIX w3200: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX w3200: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "PREFIX scac: <http://schema.org/>\r\n"
				+ "PREFIX w3199: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX scsa: <http://schema.org/>\r\n" + "SELECT DISTINCT ?t ?v0 ?v1 ?v2 ?v3 ?v4 ?v5\r\n"
				+ "WHERE {\r\n" + "?t rdf:type url:Movie  .\r\n" + "?t w3200:comment ?v1 .\r\n"
				+ "?t w3200:label ?v2 .\r\n" + "?t scac:actor ?v3 .\r\n" + "?t w3199:type ?v4 .\r\n"
				+ "?t scsa:sameAs ?v5 .\r\n" + " } LIMIT 1";

		System.out.println("queryString 090 restriction: toString " + restrictionPrefixEntity.toString());

		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(url, queryString);
		ResultSet resultOne = ResultSetFactory.copyResults(qe.execSelect());

		if (resultOne.hasNext() == false) {
			System.out.println("*****************************************");
			System.out.println(" !! No data avaible for following query for " + " !! ");
			System.out.println(" URL Enpoint: " + url);
			System.out.println(" Query String: " + "\n" + queryString + "\n");
			System.out.println(" It might give null point exception in LIMES \n");
			System.out.println("*****************************************");
		}

		System.out.println(" Output from  isDataAvailableURL : " + resultOne);
		ResultSetFormatter.out(System.out, resultOne);
		qe.close();
		return true;
	}

	/**
	 * Checking if the data is available in the data set, if we do a query with
	 * specific properties
	 * @param path the path
	 * @param res the restriction object
	 * @param maximumProperties the number of maximum properties
	 * @param propertyEntities propertyEntities
	 * @param tag the tag
	 * @return boolean true if data is present
	 */
	public boolean isDataAvailableFile(String path, Restriction res, int maximumProperties,
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

		/**String a = "PREFIX w3200: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX dbpr: <http://dbpedia.org/property/>\r\n"
				+ "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n" + "PREFIX xmfo: <http://xmlns.com/foaf/0.1/>\r\n"
				+ "SELECT DISTINCT ?t  ?v1 ?v2 ?v3 ?v4 ?v5 ?v6 ?v7\r\n" + "WHERE {\r\n" + "?t w3200:label ?v1 .\r\n"
				+ "?t xmfo:gender ?v2 .\r\n" + "?t xmfo:givenName ?v3 .\r\n" + "?t xmfo:nasdame ?v4 .\r\n"
				+ "?t dbpr:dateOfBirth ?v5 .\r\n" + "?t xmfo:birthday ?v6 .\r\n" + "?t xmfo:familyName ?v7 .\r\n"
				+ " } LIMIT 1"; */

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		
		/** results */
		System.out.println(" Results : " + results.toString());

		if (results.hasNext() == false) {
			System.out.println(" !! No data avaible for following query for " + tag + " !! ");
			System.out.println(" File Path: " + path);
			System.out.println("*****************************************");
			System.out.println(" Query String: " + "\n" + queryString + "\n");
			System.out.println("*****************************************");

		}

		if (debug) {
			System.out.println("Result: " + results); //
			ResultSetFormatter.out(System.out, results);
		}

		Query query2 = QueryFactory.create(queryString);
		QueryExecution qexec2 = QueryExecutionFactory.create(queryString, model);
		ResultSet resultsOne = ResultSetFactory.copyResults(qexec2.execSelect());
		resultsOne.forEachRemaining(qsol -> {
			String predicate = qsol.getLiteral("v1").toString();
			System.out.println(" lookit : " + predicate);
		});

		return true;
	}

}
