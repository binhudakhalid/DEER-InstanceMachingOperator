package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceF;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.*;
import java.util.function.Function;

public class ConsolidationStrategys {

  static final String fallBackUri = "http://example.org/fallBack";
  static final Map<FusionStrategy, Map<String, Function<List<Literal>, Literal>>> strategyMapping = new HashMap<>();

  /**
   * Dispatchmaps
   * add another with a {@link FusionStrategy} into the strategyMapping
   */
  static final Map<String, Function<List<Literal>, Literal>> dispatchMapExpertiseSource = new HashMap<>();
  static final Map<String, Function<List<Literal>, Literal>> dispatchMapExpertiseTarget = new HashMap<>();
  static final Map<String, Function<List<Literal>, Literal>> dispatchMapPrecise = new HashMap<>();
  static final Map<String, Function<List<Literal>, Literal>> dispatchMapVoting = new HashMap<>();
  static final Map<String, Function<List<Literal>, Literal>> dispatchMapStandard = new HashMap<>();

  // fill strategyMapping


  /*
  w/o type
   */

  static Literal computeFusionExpertiseSource(List<Literal> alternatives){
    return alternatives.get(0);
  }

  static Literal computeFusionExpertiseTarget(List<Literal> alternatives){
    return alternatives.get(1);
  }

  /*
  Integer
  */
  static Literal computeFusionForIntegerAverage(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToInt(Literal::getInt).average()
    );
  }

  static Literal computeFusionForIntegerMax(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToInt(Literal::getInt).max()
    );
  }

  static Literal computeFusionForIntegerMin(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToInt(Literal::getInt).min()
    );
  }

  static Literal computeFusionForIntegerX(List<Literal> alternatives){
    return null;
  }



  /*
  Decimal
   */

  static Literal computeFusionForDecimalAverage(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToDouble(Literal::getDouble).average()
    );
  }
  static Literal computeFusionForDecimalMin(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToDouble(Literal::getDouble).min()
    );
  }
  static Literal computeFusionForDecimalMax(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToDouble(Literal::getDouble).max()
    );
  }


  /*
  Boolean
   */

  /**
   * Compute fusion for boolean literal.
   *
   * @param alternatives the literals
   * @return the literal
   */

  static Literal computeFusionForBoolean(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.get(0).getBoolean() &&
        alternatives.stream().distinct().count() == 1
    );
  }


  // complex
  /*
  String
   */

  public static Literal computeFusionForStringMax(List<Literal> alternatives) {
    return alternatives.stream()
      .max(Comparator.comparingInt(l -> l.getString().length()))
      .orElse(ResourceFactory.createStringLiteral(""));
  }
  public static Literal computeFusionForStringMin(List<Literal> alternatives) {
    return alternatives.stream()
      .min(Comparator.comparingInt(l -> l.getString().length()))
      .orElse(ResourceFactory.createStringLiteral(""));
  }

  /*
  Date
   */

  // TODO: CreateTypedLiteral isnt right type yet
  public static Literal computeFusionForDateAverage(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.stream().mapToLong(Literal::getLong).average()
    );
  }
  public static Literal computeFusionForDateMin(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.stream().mapToLong(Literal::getLong).min()
    );
  }
  public static Literal computeFusionForDateMax(List<Literal> literals) {
    return ResourceFactory.createTypedLiteral(
      literals.stream().mapToLong(Literal::getLong).max()
    );
  }






  //https://docs.oracle.com/javase/8/docs/api/java/util/function/BiFunction.html




  static Literal fallBackFusion(List<Literal> alternatives){
    return alternatives.get(0); //todo: better Fallback option?
  }

  public static Literal executeFusion(List<Literal> alternatives, FusionStrategy fusionStrategy) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      strategyMapping.get(fusionStrategy).get(typeURL),
      //dispatchMap.get(typeURL),
      Objects.requireNonNullElse(strategyMapping.get(fusionStrategy).get(fallBackUri),
        ConsolidationStrategys::fallBackFusion)
    ).apply(alternatives);
  }

  /*
  Filling of the dispatch maps
   */
  static {
    /**
    FallBack , if not specified use fallBackFusion

     */

    dispatchMapExpertiseSource.put(
      fallBackUri,
      ConsolidationStrategys::computeFusionExpertiseSource //
    );
    dispatchMapExpertiseTarget.put(
      fallBackUri,
      ConsolidationStrategys::computeFusionExpertiseTarget
    );
    dispatchMapStandard.put(
      fallBackUri,
      ConsolidationStrategys::fallBackFusion
    );
    /* // test it without if the double requirenotnullelse works
    dispatchMapPrecise.put(
      fallBackUri,
      ConsolidationStrategys::fallBackFusion
    );*/

    // String
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#string",
      ConsolidationStrategys::computeFusionForStringMax //
    );
    dispatchMapStandard.put(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
      ConsolidationStrategys::computeFusionForStringMax
    );
    dispatchMapPrecise.put(
      "http://www.w3.org/2001/XMLSchema#string",
      ConsolidationStrategys::computeFusionForStringMin //
    );
    dispatchMapPrecise.put(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
      ConsolidationStrategys::computeFusionForStringMin
    );
    /*
    Integer
     */
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#int",
      ConsolidationStrategys::computeFusionForIntegerAverage
    );

    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#byte",
      ConsolidationStrategys::computeFusionForIntegerAverage
    );

    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#ling",
      ConsolidationStrategys::computeFusionForIntegerAverage
    );
    
    // todo: right URI below here
    // Core Type
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#boolean",
      ConsolidationStrategys::computeFusionForBoolean
    );
    //floating points
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#decimal",
      ConsolidationStrategys::computeFusionForDecimalAverage
    );
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#float",
      ConsolidationStrategys::computeFusionForDecimalAverage
    );
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#double",
      ConsolidationStrategys::computeFusionForDecimalAverage
    );
    /*
    Time & Date
     */
    dispatchMapStandard.put(
      "http://www.w3.org/2001/XMLSchema#date",
      ConsolidationStrategys::computeFusionForDateAverage
    );
    //
  }

  static {
    strategyMapping.put(
      FusionStrategy.standard, dispatchMapStandard
    );
    strategyMapping.put(
      FusionStrategy.precise, dispatchMapPrecise
    );
    strategyMapping.put(
      FusionStrategy.expertiseSource, dispatchMapExpertiseSource
    );
    strategyMapping.put(
      FusionStrategy.expertiseTarget, dispatchMapExpertiseTarget
    );

    strategyMapping.put(
      FusionStrategy.voting, dispatchMapVoting
    );
  }
}

