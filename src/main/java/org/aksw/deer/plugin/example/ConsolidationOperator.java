package org.aksw.deer.plugin.example;

import java.util.*;
import java.util.function.Function;


import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Consolidation operator.
 */
@Extension
public class ConsolidationOperator extends AbstractParameterizedEnrichmentOperator {

  /**
   * The constant logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(ConsolidationOperator.class);
  /**
   * The constant results.
   */
  private static final List<String> results = new ArrayList<>();
  /**
   * The constant dispatchMap.
   */
  private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();
  /**
   * The constant sourceTargetMap.
   */
  private static final Map<Property, SourceTargetMatch> sourceTargetMap = new HashMap<>();
  /**
   * The constant statementSourceTargetMap.
   */
  private static final Map<Statement, SourceTargetMatches> statementSourceTargetMap = new HashMap<>();
  /**
   * The constant matchablePropertys.
   */
  private static final List<MatchablePropertys> matchablePropertys = new ArrayList<>();

  private static final Map<String,FusionStrategy> propertyFusionMatching = new HashMap<>();
  /**
   * The constant SAME_AS.
   */
// Controller for this
  public static final Property SAME_AS = DEER.property("sameAs");
  /**
   * The constant ENTITY_NAME.
   */
  public static final Property ENTITY_NAME = DEER.property("entityName");
  /**
   * The constant SOURCE_NAME.
   */
  public static final Property SOURCE_NAME = DEER.property("sourceName");
  /**
   * The constant TARGET_NAME.
   */
  public static final Property TARGET_NAME = DEER.property("targetName");
  /**
   * The constant NAMESPACE_INTEGRATION.
   */
  public static final Property NAMESPACE_INTEGRATION = DEER.property("namespaceIntegration");
  /**
   * The constant provenance.
   */
  public static final Property PROVENANCE = DEER.property("provenanceProperty");
  /**
   * The constant FUSION_STRATEGY.
   */
  public static final Property FUSION_STRATEGY =  DEER.property("globalFusionStrategy");
  /**
   * The constant OUTPUT_VARIANT.
   */
  public static final Property OUTPUT_VARIANT = DEER.property("outputVariant");
  /**
   * The constant ADD_TARGET.
   */
  public static final Property ADD_TARGET = DEER.property("addTarget");

  public static final Property PROPERTY_FUSION_MAPPING = DEER.property("propertyFusion");

  public static final Property PROPERTY_VALUE = DEER.property("propertyValue");

  public static final Property FUSION_VALUE = DEER.property("fusionStrategy");

  /**
   * The constant addTarget.
   */
  private static final boolean addTarget = true;
  /**
   * The Same as.
   */
  private String sameAs;
  /**
   * The Entity name.
   */
  private String entityName;
  /**
   * The Source name.
   */
  private String sourceName;
  /**
   * The Target name.
   */
  private String targetName;
  /**
   * The Namespace integration.
   */
  private String namespaceIntegration;
  /**
   * The provenance property.
   */
  private String provenanceProperty;


  /**
   * The constant dataSourceStatement.
   */
  private static Statement dataSourceStatement;
  /**
   * The constant dataTargetStatement.
   */
  private static Statement dataTargetStatement;
  /**
   * The constant entities.
   */
  private static Model entities;
  /**
   * The constant matches.
   */
  private static Model matches;
  /**
   * The constant source.
   */
  private static Model source;
  /**
   * The constant target.
   */
  private static Model target;

  private static String softwareAgentLabel = "DEERProvenanceBot";//https://www.w3.org/TR/prov-o/#SoftwareAgent
  // Agent is source or Target not a specific bot

  private String[] fileSuffixKnowledgeBase = new String[]{"ttl", "nt", "", ""};

  private boolean DEBUG = true;



  @Override
  public ValidatableParameterMap createParameterMap() { // 2
    return ValidatableParameterMap.builder()
      .declareProperty(SAME_AS)
      .declareProperty(ENTITY_NAME)
      .declareProperty(PROVENANCE)
      .declareProperty(TARGET_NAME)
      .declareProperty(SOURCE_NAME)
      .declareProperty(FUSION_STRATEGY)
      .declareProperty(OUTPUT_VARIANT)
      .declareProperty(ADD_TARGET)
      .declareProperty(PROPERTY_FUSION_MAPPING)
      .declareValidationShape(getValidationModelFor(ConsolidationOperator.class)).build();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) { // 3
    if(DEBUG)
      System.out.println("\n\n ---- Consolidation Operator started ---- \n\n");
    // Here parameter mapping
    // todo: ask if String or better Ressources

    fillParameterFromConfig();


    // auswahl der eigentlihcen Implementierung meiner Fusion Strategie
    // get Model
    Model model = models.get(0);
    // pick the data into usable form:
    entities = ModelFactory.createDefaultModel();
    matches = ModelFactory.createDefaultModel();
    getDataOutOfModel(model);
    buildSourceAndTarget();
    // do the consolidation!!
    if(DEBUG)
      buildMatchablePropertysByName();

    buildMatchablePropertys(); // hard coded
    //   consolidateModelOld();
    consolidateModel(); //functionin
    if(DEBUG)
      System.out.println("\n\n---- Consolidation Operator stopped ---- ");

    // Source (answer set), targetset, read from instance, matches, entities

    List<Model> result = List.of(source);
    if(addTarget){
      // target - w/o matched propertys, than add this to my source and return it
      // input authority = target.minus(source)
      result = List.of(source,target);  // todo: Question
    }
    return result;//, target, model, matches, entities);
  }

  private void fillParameterFromConfig() {
    sameAs = getParameterMap().
      getOptional(SAME_AS).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("http://www.w3.org/2002/07/owl#sameAs");

    entityName = getParameterMap().
      getOptional(ENTITY_NAME).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    sourceName = getParameterMap().
      getOptional(SOURCE_NAME).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("https://w3id.org/deer/datasetSource");
    //orElse("https://w3id.org/deer/datasetSource");
    targetName = getParameterMap().
      getOptional(TARGET_NAME).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("https://w3id.org/deer/datasetTarget");
     namespaceIntegration = getParameterMap().
      getOptional(NAMESPACE_INTEGRATION).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("https://w3id.org/deer/testName");
    provenanceProperty = getParameterMap().
      getOptional(PROVENANCE).
      map(RDFNode::asLiteral).map(Literal::getString).
      orElse("https://w3id.org/deer/provenance");

    getParameterMap().listPropertyObjects(PROPERTY_FUSION_MAPPING)
      .map(RDFNode::asResource)
      .forEach(op ->{
        final String property = op.getPropertyResourceValue(PROPERTY_VALUE).asResource().getURI();
        final String fusionStrategy = op.getPropertyResourceValue(FUSION_VALUE).toString();
        FusionStrategy fs;
        try {
          fs = FusionStrategy.valueOf(fusionStrategy);
        }
        catch(Exception ex){
           fs = FusionStrategy.standard;//fallback
        }

        propertyFusionMatching.put(property,fs);
      });

  /*
    final FusionStrategy fusionStrategy = FusionStrategy.valueOf(
      getParameterMap().get(FUSION_STRATEGY).asLiteral().getString()
    );
     */
    final String outputVariant = getParameterMap().getOptional(OUTPUT_VARIANT)
      .map(RDFNode::asLiteral).map(Literal::getString).orElse("ttl");
    final boolean addTarget = getParameterMap().getOptional(ADD_TARGET)
      .map(RDFNode::asLiteral).map(Literal::getBoolean).orElse(false);

    if(DEBUG && propertyFusionMatching.size() > 0){
      System.out.println("show property-fusionstrategy map");
      for (Map.Entry<String,FusionStrategy> entry : propertyFusionMatching.entrySet()) {
        System.out.println(entry.getKey() + " - " + entry.getValue().toString());
      }
      System.out.println("");
    }
  }

  /**
   * Build source and target.
   */
  private void buildSourceAndTarget() {
    // Todo: Check if we need to do a different loadModel variante later on
    target = RDFDataMgr.loadModel(dataTargetStatement.getObject().toString());
    source = RDFDataMgr.loadModel(dataSourceStatement.getObject().toString());


  }

  /**
   * Build matchable propertys.
   */
  private void buildMatchablePropertys() {
    // TODO: get it from Onto matching (no propertys yet)

    // go through source & target , and try to find same and add it
    Model model = ModelFactory.createDefaultModel();
    String prefix = "http://xmlns.com/foaf/0.1/";
    Property sourceProp = model.createProperty(prefix, "name");
    Property targetProp = model.createProperty(prefix, "name");
    // fullname :
    MatchablePropertys name = new MatchablePropertys("name");
    name.setSourceProperty(sourceProp);
    name.setTargetProperty(targetProp);
    FusionStrategy fs = FusionStrategy.standard;
    if(propertyFusionMatching.containsKey(sourceProp.getURI())){
      fs = propertyFusionMatching.get(sourceProp.getURI());
    }
    name.setFusionStrategy(fs);
    matchablePropertys.add(name);

    if(DEBUG){
      System.out.println("\nMatchable propertys: \n");
      for (MatchablePropertys entry: matchablePropertys) {
        System.out.println(entry);
      }
      System.out.println("");
    }
  }


  private void buildMatchablePropertysByName() {
    // property source & target durchgehen
    // alle source merken
    // schauen ob es gleiche gibt
    // Now we get an iterator over the resources that have type o_Parking.
    List<Property> propList = new ArrayList<>();

    final Statement statement = source.listStatements().nextStatement(); // just the first
    /*
    Model sourceModel = constructModel(statement.getSubject().toString(),true);
    Model targetModel = constructModel(statement.getObject().toString(),false);

    StmtIterator stmtIterator = sourceModel.listStatements();
     */
    StmtIterator sourceIterator = source.listStatements(statement.getSubject(),null,(RDFNode) null);

    while(sourceIterator.hasNext()){
      Statement s = sourceIterator.nextStatement();
      if(DEBUG)
        System.out.println(s.getPredicate());
      StmtIterator targetIterator = target.listStatements(null,s.getPredicate(),(RDFNode) null);
      if(targetIterator.hasNext()){
        //
        MatchablePropertys tmp = new MatchablePropertys(s.getPredicate().getLocalName());
        tmp.setSourceProperty(s.getPredicate());
        tmp.setTargetProperty(s.getPredicate());
        FusionStrategy fs = FusionStrategy.standard;
        if(propertyFusionMatching.containsKey(s.getPredicate().getURI())){
          fs = propertyFusionMatching.get(s.getPredicate().getURI());
        }
        tmp.setFusionStrategy(fs);
        matchablePropertys.add(tmp);

      }
    }
  }
   /**
   * Gets data out of model.
   *
   * @param model the model
   */
  private void getDataOutOfModel(Model model) {
    // entities
    StmtIterator entityIterator = model.listStatements(null, model.createProperty(entityName), (RDFNode) null);
    while (entityIterator.hasNext()) {
      Statement statement = entityIterator.nextStatement();
      entities.add(statement);
    }

    // matches
    StmtIterator matchesIterator = model.listStatements(null, model.createProperty(sameAs), (RDFNode) null);
    while (matchesIterator.hasNext()) {
      Statement statement = matchesIterator.nextStatement();
      matches.add(statement);
    }


    StmtIterator sourceIt = model.listStatements(model.createResource(sourceName), null, (RDFNode) null);
    dataSourceStatement = sourceIt.nextStatement(); // should only be one
    StmtIterator targetIt = model.listStatements(model.createResource(targetName), null, (RDFNode) null);
    dataTargetStatement = targetIt.nextStatement();
  }

  /**
   * Consolidate model.
   */
  private void consolidateModel(){
    StmtIterator matchedIterator = matches.listStatements();
    while (matchedIterator.hasNext()) {

      Statement statement = matchedIterator.nextStatement();
      statementSourceTargetMap.put(statement, new SourceTargetMatches());
      Model source = constructModel(statement.getSubject().toString(), true);
      Model target = constructModel(statement.getObject().toString(), false);
      // here
      for(MatchablePropertys mp:matchablePropertys) {
        StmtIterator sourceIterator = source.listStatements(null, mp.getSourceProperty(), (RDFNode) null);
        StmtIterator targetIterator = target.listStatements(null, mp.getTargetProperty(), (RDFNode) null);
        while(sourceIterator.hasNext()) {
          Statement statement1 = sourceIterator.nextStatement();
          addToSourceTargetMap(statement,statement1,mp.label,true, mp.fusionStrategy);
        }
        while(targetIterator.hasNext()) {
          Statement statement1 = targetIterator.nextStatement();
          addToSourceTargetMap(statement,statement1,mp.label,false, mp.fusionStrategy);
        }

      }
    }
    // execute fusion
    for (var sstm : statementSourceTargetMap.entrySet()){
      for (var stm:  sstm.getValue().matchingMap.entrySet()){
        if(stm.getValue().target != null && stm.getValue().source != null) {
          try {
            // change standard to propertyMathcing
            FusionStrategy fs = stm.getValue().getFusionStrategy();
            if(fs == null){
              fs = FusionStrategy.standard;
            }
            stm.getValue().result = ConsolidationStrategys.executeFusion(stm.getValue().getAlternatives(),fs);
             // = executeFusion(stm.getValue().getAlternatives());
            System.out.println(stm.getValue().toString());
            SourceTargetMatch tmp = stm.getValue();
            //change
            StmtIterator stmtIterator = source.listStatements(tmp.source.getSubject(), tmp.source.getPredicate(), (RDFNode) null);
            stmtIterator.nextStatement().changeObject(tmp.result); // change it
            // add provenance
          }
          catch(LiteralRequiredException literalRequiredException){
            logger.error("Given source target mapping is not on literals", stm.getValue().toString());
          }

        }
      }
    }

  }

  /**
   * Add provenance.
   *
   * @param model the model
   * @param stm   the stm
   */
  public void AddProvenance(Model model, SourceTargetMatch stm){
    // model.add()
  }




  /**
   * Add to source target map.
   *
   * @param key       the key
   * @param statement the statement
   * @param label     the label
   * @param source    the source
   */
  public void addToSourceTargetMap(Statement key, Statement statement, String label, boolean source, FusionStrategy fs){

    if(statementSourceTargetMap.get(key).matchingMap.containsKey(label)){
      SourceTargetMatch stm = statementSourceTargetMap.get(key).matchingMap.get(label);
      if(source){
        stm.source = statement;
        stm.endpointSource = dataSourceStatement.getObject().toString();
      }
      else{
        stm.target = statement;
        stm.endpointTarget = dataTargetStatement.getObject().toString();
      }
      if (fs != null)
        stm.fusionStrategy = fs;
    }
    else{

      if(source)
        statementSourceTargetMap.get(key).matchingMap.put(label,new SourceTargetMatch(statement,dataSourceStatement.getObject().toString()));
      else
        //todo Namespace setzen
        statementSourceTargetMap.get(key).matchingMap.put(label,new SourceTargetMatch(statement,dataTargetStatement.getObject().toString(), "" ));

      if(fs != null)
        statementSourceTargetMap.get(key).matchingMap.get(label).setFusionStrategy(fs);
    }
  }

  /**
   * Gets statements from model.
   *
   * @param model  the model
   * @param source the source
   * @return the statements from model
   */
  private Map<Property, SourceTargetMatch> getStatementsFromModel(Model model, boolean source) {
    StmtIterator stmtIterator = model.listStatements();
    Map<Property, SourceTargetMatch> sourceTargetMatches = new HashMap<>();
    while (stmtIterator.hasNext()) {
      Statement statement = stmtIterator.nextStatement();
      try {
        SourceTargetMatch sourceTargetMatch;
        if (source) {
          sourceTargetMatch = new SourceTargetMatch(statement, dataSourceStatement.getObject().toString());

        } else {
          //where do i get the namespace from?
          sourceTargetMatch = new SourceTargetMatch(statement, dataTargetStatement.getObject().toString(), "");
        }
        sourceTargetMatches.put(statement.getPredicate(), sourceTargetMatch);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return sourceTargetMatches;
  }

  /**
   * Construct model model.
   *
   * @param subject the subject
   * @param source  the source
   * @return the model
   */
  private Model constructModel(String subject, boolean source) {
    //todo : take statement
    //  before i did a query on the sparql service, now i dont know what i have , so how do you query the given dataset?
    String queryString =
      "CONSTRUCT { <" +
        subject +
        "> ?p ?o}\n" +
        "WHERE {\n<" +
        subject +
        ">  ?p ?o.\n" +
        "\n" +
        "}";

    // getObject.split(".")[last] in [fileendings] : readFile
    // else sparqlendpoint
    String usedEnpoint =  source ? dataSourceStatement.getObject().toString() : dataTargetStatement.getObject().toString();
    String ending = usedEnpoint.substring(usedEnpoint.lastIndexOf(".") +1);
    Model model = ModelFactory.createDefaultModel();

    if( Arrays.stream(fileSuffixKnowledgeBase).anyMatch(ending::equals)){
     // fileendpoint
      RDFDataMgr.read(model,usedEnpoint, Lang.NTRIPLES);
      Query query1 = QueryFactory.create(queryString);
      QueryExecution qexec1 = QueryExecutionFactory.create(query1, model);
      model = qexec1.execConstruct();;
      qexec1.close();
    }
    else { //Sparql Endpoint
      QueryExecution qexec = QueryExecutionFactory.sparqlService(usedEnpoint, queryString);
      model = qexec.execConstruct();
      qexec.close();
    }
    return model;
  }


  public Model getPredicates(String subject,boolean source){
    String queryString =
      "SELECT ?p \n" +
        "WHERE {\n<" +
        subject +
        ">  ?p ?o.\n" +
        "\n" +
        "}";

    String usedEnpoint =  source ? dataSourceStatement.getObject().toString() : dataTargetStatement.getObject().toString();
    String ending = usedEnpoint.substring(usedEnpoint.lastIndexOf(".") +1);
    Model model = ModelFactory.createDefaultModel();

    if( Arrays.stream(fileSuffixKnowledgeBase).anyMatch(ending::equals)){
      // fileendpoint
      RDFDataMgr.read(model,usedEnpoint, Lang.NTRIPLES);
      Query query1 = QueryFactory.create(queryString);
      QueryExecution qexec1 = QueryExecutionFactory.create(query1, model);
      model = qexec1.execConstruct();;
      qexec1.close();
    }
    else { //Sparql Endpoint
      QueryExecution qexec = QueryExecutionFactory.sparqlService(usedEnpoint, queryString);
      model = qexec.execConstruct();
      qexec.close();
    }
    return model;
  }

}
