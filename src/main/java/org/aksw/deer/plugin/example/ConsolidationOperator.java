package org.aksw.deer.plugin.example;

import java.util.*;
import java.util.function.Function;

import de.vandermeer.asciitable.CWC_FixedWidth;
import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
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
  private static final boolean addTarget = true;

  // Controller for this
  private static final Property SAME_AS = DEER.property("sameAs");
  private static final Property ENTITY_NAME = DEER.property("entityName");
  private static final Resource SOURCE_NAME = DEER.resource("sourceName");
  private static final Resource TARGET_NAME = DEER.resource("targetName");

  // data sources
  private static Statement sourceStatement;
  private static Statement targetStatement;
  private static Model entities;
  private static Model matches;

  static {
    /*
    String
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#string",
      ConsolidationOperator::computeFusionForString
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
	public ConsolidationOperator() {

		super();
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
          this.sourceStatement = statement;
        }
        else{ // should be target, we could try it out
          this.targetStatement = statement;
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


    }
  }

  private Map<? extends Property,? extends SourceTargetMatch> getStatementsFromModel(Model source, boolean b) {
    return null;
  }

  private Model constructModel(String subject, boolean source){
    return null;
  }


}
