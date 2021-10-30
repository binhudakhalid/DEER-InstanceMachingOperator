package org.aksw.deer.plugin.example;

import java.util.*;
import java.util.function.Function;

import de.vandermeer.asciitable.CWC_FixedWidth;
import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class ConsolidationOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(ConsolidationOperator.class);
  private static final List<String> results = new ArrayList<>();
  private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();
  private static final Map<Property,SourceTargetMatch> sourceTargetMap = new HashMap<>();

  // Controller for this
  private static final Property SAME_AS = DEER.property("sameAs");
  private static final Property ENTITY_NAME = DEER.property("entityName");
  private static final Resource SOURCE_NAME = DEER.resource("sourceName");
  private static final Resource TARGET_NAME = DEER.resource("targetName");

  private static final boolean addTarget = true;

  // data sources
  private static Statement dataSourceStatement;
  private static Statement dataTargetStatement;
  private static Model entities;
  private static Model matches;

  static {
    /*
    String
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#string",
      ConsolidationOperator::computeFusionForString //
    );
    dispatchMap.put(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
      ConsolidationOperator::computeFusionForString
    );
    /*
    Integer
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#int",
      ConsolidationOperator::computeFusionForInteger
    );

    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#byte",
      ConsolidationOperator::computeFusionForInteger
    );

    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#ling",
      ConsolidationOperator::computeFusionForInteger
    );

    // todo: right URI below here
    // Core Type
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#boolean",
      ConsolidationOperator::computeFusionForBoolean
    );
    //floating points
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#decimal",
      ConsolidationOperator::computeFusionForDecimal
    );
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#float",
      ConsolidationOperator::computeFusionForDecimal
    );
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#double",
      ConsolidationOperator::computeFusionForDecimal
    );
    /*
    Time & Date
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#date",
      ConsolidationOperator::computeFusionForDate
    );
    //
  }



  // as Data return ?
  // does this makes sense?
  private static Literal computeFusionForDate(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.stream().mapToLong(Literal::getLong).average()
    );
  }

  private static Literal computeFusionForDecimal(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.stream().mapToDouble(Literal::getDouble).average()
    );
  }

  // True if all the same and
  private static Literal computeFusionForBoolean(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.get(0).getBoolean() &&
        literals.stream().distinct().count() == 1
    );
  }


  //https://docs.oracle.com/javase/8/docs/api/java/util/function/BiFunction.html

  private static Literal computeFusionForInteger(List<Literal> alternatives) { // @todo: keep more precise e.g
    // take average int
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToInt(Literal::getInt).average()
    );
  }

  private static Literal computeFusionForString(List<Literal> alternatives) {
    // take longest string ; (Dont know if there is a avg variant of string)
    return alternatives.stream()
      .max(Comparator.comparingInt(l -> l.getString().length()))
      .orElse(ResourceFactory.createStringLiteral(""));
  }

  private static Literal fallBackFusion(List<Literal> alternatives) {
    // take first
    return alternatives.get(0);
  }



  private Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      ConsolidationOperator::fallBackFusion
    ).apply(alternatives);
  }

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder()
				.declareValidationShape(getValidationModelFor(ConsolidationOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3
    System.out.println("\n\n ---- Consolidation Operator started ---- \n\n");
    // Here parameter mapping
    // auswahl der eigentlihcen Implementierung meiner Fusion Strategie
    // get Model
    Model model = models.get(0);
    // pick the data into usable form:
    entities = ModelFactory.createDefaultModel();
    matches = ModelFactory.createDefaultModel();
    getDataOutOfModel(model);
    // do the consolidation!!

    System.out.println("\n\n --- try consolidation --");
    consolidateModel();
    System.out.println("\n\n---- Consolidation Operator stopped ---- ");
    //System.out.println("The output from Instance Matching Operator models.get(1) " + models.get(1) );


		return List.of(model);
	}

  private void getDataOutOfModel(Model model) {

    StmtIterator it = model.listStatements();
    while(it.hasNext()){
      Statement statement = it.nextStatement();
      //todo: Final Property given as optional Parameter
      if(!statement.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#sameAs")){
        fillGlobalData(statement);
      }
      else{
        matches.add(statement);
      }
    }

  }

  private void fillGlobalData( Statement statement) {

    //todo: Final Property given as optional Parameter
      if(statement.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
        this.entities.add(statement);
      }
      else{
        if(statement.getSubject().toString().equals("https://w3id.org/deer/datasetSource")){
          dataSourceStatement = statement;
        }
        else if(statement.getSubject().toString().equals("https://w3id.org/deer/datasetTarget")){ // should be target, we could try it out
          dataTargetStatement = statement;
        }
        else {
          //todo throw error
        }
      }
  }
  private void consolidateModel(){
    StmtIterator matchedIterator = matches.listStatements();
    while(matchedIterator.hasNext()){
      Statement statement = matchedIterator.nextStatement();
      Model source = constructModel(statement.getSubject().toString(), true);
      Model target = constructModel(statement.getObject().toString(), true);
      sourceTargetMap.putAll(getStatementsFromModel(source,true));
      for(StmtIterator targetIt = target.listStatements(); targetIt.hasNext();){
        Statement targetStatement = targetIt.nextStatement();
        if(sourceTargetMap.containsKey(targetStatement.getPredicate())){
          SourceTargetMatch tmp = sourceTargetMap.get(targetStatement.getPredicate());
          tmp.endpointTarget = dataTargetStatement.getObject().toString(); // todo think about it
          tmp.target = targetStatement;
          try{
            tmp.result = executeFusion(tmp.getAlternatives());
            sourceTargetMap.put(targetStatement.getPredicate(), tmp);
          }
          catch (LiteralRequiredException e){
            // no literal found
            e.printStackTrace();
          }
        }
        else{ // not found
          SourceTargetMatch tmp = new SourceTargetMatch(statement, dataTargetStatement.getObject().toString(), "" );
          try{
            tmp.setResult(tmp.source.getLiteral());
            sourceTargetMap.put(targetStatement.getPredicate(), tmp);
          }
          catch (LiteralRequiredException e){
            // no literal found
            e.printStackTrace();
          }
        }
      }
      // after done. go through map and do the real
      // Todo: loop through source target map: add statement / change statement of source


    }
  }

  private Map<Property,SourceTargetMatch> getStatementsFromModel(Model model, boolean source) {
    StmtIterator stmtIterator = model.listStatements();
    Map<Property,SourceTargetMatch> sourceTargetMatches = new HashMap<>();
    while(stmtIterator.hasNext()){
      Statement statement = stmtIterator.nextStatement();
      try{
        SourceTargetMatch sourceTargetMatch;
        if(source){
          sourceTargetMatch = new SourceTargetMatch(statement, dataSourceStatement.getObject().toString());

        }
        else{
          //where do i get the namespace from?
          sourceTargetMatch = new SourceTargetMatch(statement,dataTargetStatement.getObject().toString(),"");
        }
        sourceTargetMatches.put(statement.getPredicate(),sourceTargetMatch);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return sourceTargetMatches;
  }

  private Model constructModel(String subject, boolean source){
    //todo : take statement
    //  before i did a query on the sparql service, now i dont know what i have , so how do you query the given dataset?
     return null;
  }

  private Model constructModelBefore( String subject, String endpoint){
    String queryString =
      "CONSTRUCT { <" +
        subject+
        "> ?p ?o}\n" +
        "WHERE {\n<" +
        subject+
        ">  ?p ?o.\n" +
        "\n" +
        "}";

    QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,queryString);
    Model result = qexec.execConstruct();
    qexec.close();
    return result;
  }
}
