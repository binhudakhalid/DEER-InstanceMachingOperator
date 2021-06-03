package org.aksw.deer.plugin.example;

import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LimesResult;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.config.reader.AConfigurationReader;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.config.writer.RDFConfigurationWriter;
import org.aksw.limes.core.io.config.writer.XMLConfigurationWriter;
import org.aksw.limes.core.io.serializer.ISerializer;
import org.aksw.limes.core.io.serializer.SerializerFactory;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.VCARD;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 */
@Extension
public class FilterEnrichmentOperator2 extends AbstractParameterizedEnrichmentOperator
		implements ReverseLearnable, SelfConfigurable {

	private static final Logger logger = LoggerFactory.getLogger(FilterEnrichmentOperator2.class);

	public static final Property SUBJECT = DEER.property("subject");
	public static final Property PREDICATE = DEER.property("predicate");
	public static final Property OBJECT = DEER.property("object");
	public static final Property SELECTOR = DEER.property("selector");
	public static final Property SPARQL_CONSTRUCT_QUERY = DEER.property("sparqlConstructQuery");

	public FilterEnrichmentOperator2() {

		super();
		System.out.println("--KHD-- + FilterEnrichmentOperator2 ");// 1
	}

	@Override
	public DegreeBounds getLearnableDegreeBounds() {
		System.out.println("--KHD-- + getLearnableDegreeBounds ");
		return getDegreeBounds();
	}

	@Override
	public ValidatableParameterMap createParameterMap() {
		System.out.println("--KHD-- + createParameterMap "); // 2
		return ValidatableParameterMap.builder().declareProperty(SELECTOR).declareProperty(SPARQL_CONSTRUCT_QUERY)
				.declareValidationShape(getValidationModelFor(FilterEnrichmentOperator2.class)).build();
	}

	@Override
	public ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target,
			ValidatableParameterMap prototype) {
		System.out.println("--KHD-- + learnParameterMap ");// not running
		ValidatableParameterMap result = createParameterMap();
		Model in = inputs.get(0);
		System.out.println(" ==ALI " + in);

		target.listStatements().mapWith(Statement::getPredicate).filterKeep(p -> in.contains(null, p)).toSet()
				.forEach(p -> {
					result.add(SELECTOR, result.createResource().addProperty(PREDICATE, p));
				});
		return result.init();
	}

	@Override
	public double predictApplicability(List<Model> inputs, Model target) {
		System.out.println("--KHD-- + predictApplicability ");

		// size of target < input && combined recall of input/target is high.
		Model in = inputs.get(0);
		double propertyIntersectionSize = target.listStatements().mapWith(Statement::getPredicate)
				.filterKeep(p -> in.contains(null, p)).toList().size();
		double stmtIntersectionSize = target.listStatements().filterKeep(in::contains).toList().size();
		double propertyRecall = propertyIntersectionSize / target.size();
		double stmtRecall = stmtIntersectionSize / target.size();
		return stmtRecall * 0.6 + propertyRecall * 0.3 + (in.size() - target.size()) / (double) in.size() * 0.1;
	}

	@Override
	public List<Model> reverseApply(List<Model> inputs, Model target) {
		System.out.println("--KHD-- + reverseApply ");
		return List.of(ModelFactory.createDefaultModel().add(target).add(inputs.get(0)));
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3
		System.out.println("--KHD-- + safeApply ");
		Model a = filterModel(models.get(0));
		System.out.println(" meme from operaot alph models: " + a);
		System.out.println("from operaot alph models: end");

		System.out.println("Just running Limes");
		createConfigurationFile();
		 //CallingLimes();
		System.out.println("Just running Limes");

		// create an empty Model

		// create the resource
		// some definitions
		String personURI = "http://somewhere/JohnSmith";
		String givenName = "John";
		String familyName = "Smith";
		String fullName = givenName + " " + familyName;

		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		// create the resource
		// and add the properties cascading style
		Resource johnSmith = model.createResource(personURI).addProperty(VCARD.FN, fullName).addProperty(VCARD.N,
				model.createResource().addProperty(VCARD.Given, givenName).addProperty(VCARD.Family, familyName));

		return List.of(model);
	}

	private Model filterModel(Model model) { // 4
		System.out.println("--KHD-- + filterModel ");

		final Model resultModel = ModelFactory.createDefaultModel();
		final Optional<RDFNode> sparqlQuery = getParameterMap().getOptional(SPARQL_CONSTRUCT_QUERY);
		if (sparqlQuery.isPresent()) {
			logger.info("Executing SPARQL CONSTRUCT query for " + getId() + " ...");
			return QueryExecutionFactory.create(sparqlQuery.get().asLiteral().getString(), model).execConstruct();
		} else {
			getParameterMap().listPropertyObjects(SELECTOR).map(RDFNode::asResource).forEach(selectorResource -> {
				RDFNode s = selectorResource.getPropertyResourceValue(SUBJECT);
				RDFNode p = selectorResource.getPropertyResourceValue(PREDICATE);
				Resource o = selectorResource.getPropertyResourceValue(OBJECT);

				System.out.println(" --KHD-- s: " + s);
				System.out.println(" --KHD-- p: " + p);
				System.out.println(" --KHD-- o: " + o);

				logger.info("Running filter " + getId() + " for triple pattern {} {} {} ...",
						s == null ? "[]" : "<" + s.asResource().getURI() + ">",
						p == null ? "[]" : "<" + p.asResource().getURI() + ">",
						o == null ? "[]" : "(<)(\")" + o.toString() + "(\")(>)");
				SimpleSelector selector = new SimpleSelector(s == null ? null : s.asResource(),
						p == null ? null : p.as(Property.class), o);
				resultModel.add(model.listStatements(selector));
			});
		}
		return resultModel;
	}

	public void createConfigurationFile() {

		Configuration conf = new Configuration();

		// adding prefix
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
		src.setPageSize(2000);
		src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?s rdf:type url:Movie" })));
		src.setProperties(Arrays.asList(new String[] { "rdfs:label" }));

		conf.setSourceInfo(src);
		System.out.println("Just running Limes 1");

		
		System.out.println("Just running Limes 2");

		KBInfo target = new KBInfo();
		target.setId("targetId");
		target.setEndpoint("http://dbpedia.org/sparql");
		target.setVar("?t");
		target.setPageSize(2000);
		target.setRestrictions(new ArrayList<String>(Arrays.asList(new String[] { "?t rdf:type dbpo:Film" })));
		target.setProperties(Arrays.asList(new String[] { "rdfs:label" }));
		conf.setTargetInfo(target);
		

		
		//Set either Metric or MLALGORITHM
		//conf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");

	    MLImplementationType mlImplementationType = MLImplementationType.UNSUPERVISED;
		conf.setMlAlgorithmName("wombat simple");
		conf.setMlImplementationType(mlImplementationType);
		
		
		LearningParameter learningParameter = new LearningParameter();
		learningParameter.setName("max execution time in minutes");
		learningParameter.setValue(60);
		
		
		
		
		
	    List<LearningParameter> mlAlgorithmParameters = new ArrayList<>();
	    mlAlgorithmParameters.add(learningParameter);
	    
	    conf.setMlAlgorithmParameters(mlAlgorithmParameters);
	    
	    

		//list.add(new MyObject("First MyObject"));, 0

	 //L/ist<LearningParameter> mlAlgorithmParameters = new ArrayList<>();

		
	 

	 
	
	 
		
		
		//Acceptance
		conf.setAcceptanceThreshold(0.98);
		conf.setAcceptanceFile("accepted.nt");
		conf.setAcceptanceRelation("owl:sameAs");

		//Review
		conf.setVerificationThreshold(0.9);
		conf.setVerificationFile("reviewme.nt");
		conf.setVerificationRelation("owl:sameAs");

		//EXECUTION
		conf.setExecutionRewriter("default");
		conf.setExecutionPlanner("default");
		conf.setExecutionEngine("default");

		System.out.println("Just running Limes 3");

		conf.setOutputFormat("TAB");
		System.out.println("Just running Limes 4");

		RDFConfigurationWriter writer = new RDFConfigurationWriter();
		System.out.println("Just running Limes 5" + writer.toString());

		System.out.println(" yeh Allah2 : config.toString()" + conf.toString());

		System.out.println(" yeh Allah2 : config.getVerificationThreshold()" + conf.getVerificationThreshold());
		System.out.println(" yeh Allah2 : config.getMlAlgorithmName()" + conf.getMlAlgorithmName());
		System.out.println(" yeh Allah2 please : config.getPrefixes()" + conf.getPrefixes());

		try {
			System.out.println("Just running Limes 6");
			writer.write(conf, "F:/Data/test10.ttl", "TTL");

			System.out.println("Just running Limes 7");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void CallingLimes() {

		/*
		 * String LIMES_CONFIGURATION_FILE = "F://Newfolder//LIMES//t//fileOldML.xml";
		 * String LIMES_OUTPUT_LOCATION = "F://Newfolder//LIMES//t";
		 * ValidatableParameterMap parameters = getParameterMap();
		 * 
		 * String limesConfigurationFile =
		 * parameters.get(LIMES_CONFIGURATION_FILE).asResource().getURI();
		 */

		String hardCoded = "F://Newfolder//LIMES//t//fileOldML.xml";
		String limesOutputLocation = "F://Newfolder//LIMES//t";
		AConfigurationReader reader = new XMLConfigurationReader(hardCoded.toString());
		Configuration config = reader.read();
		System.out.println(" yeh Allah : config.toString()" + config.toString());
		System.out.println(" yeh Allah : config.getMlAlgorithmName()" + config.getMlAlgorithmName());
		System.out.println(" yeh Allah : config.getVerificationThreshold()" + config.getVerificationThreshold());

		System.out.println(" yeh Allah : config.getMlAlgorithmParameters() khd: " + config.getMlAlgorithmParameters());
		System.out.println(" yeh Allah : config.getMlAlgorithmParameters()toString khd: " + config.getMlAlgorithmParameters().toString());
		System.out.println(" yeh Allah : config.getMlAlgorithmParameters()toString khd: " + config.getMlAlgorithmParameters().indexOf(0));
		
		
		String sourceEndpoint = config.getSourceInfo().getEndpoint();
		String targetEndpoint = config.getTargetInfo().getEndpoint();
		int limit = -1;
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
		System.out.println("100 ==HD== ");

	}

}
