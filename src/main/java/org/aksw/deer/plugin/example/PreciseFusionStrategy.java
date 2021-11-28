package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class PreciseFusionStrategy {
  public static final FusionStrategy fusionStrategy = FusionStrategy.precise;
  private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();

  static {
    /*
    String
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#string",
      PreciseFusionStrategy::computeFusionForString //
    );
    dispatchMap.put(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
      PreciseFusionStrategy::computeFusionForString
    );
    /*
    Integer
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#int",
      PreciseFusionStrategy::computeFusionForInteger
    );

    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#byte",
      PreciseFusionStrategy::computeFusionForInteger
    );

    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#ling",
      PreciseFusionStrategy::computeFusionForInteger
    );

    // todo: right URI below here
    // Core Type
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#boolean",
      PreciseFusionStrategy::computeFusionForBoolean
    );
    //floating points
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#decimal",
      PreciseFusionStrategy::computeFusionForDecimal
    );
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#float",
      PreciseFusionStrategy::computeFusionForDecimal
    );
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#double",
      PreciseFusionStrategy::computeFusionForDecimal
    );
    /*
    Time & Date
     */
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#date",
      PreciseFusionStrategy::computeFusionForDate
    );
    //
  }

  //primitives
  static Literal computeFusionForInteger(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToInt(Literal::getInt).min()
    );
    // minimum more precise?
  }

  static Literal computeFusionForDecimal(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToDouble(Literal::getDouble).average() //stimmt nicht mach nochma
    );
  }

  static Literal computeFusionForBoolean(List<Literal> alternatives){
    return null;

  }

  // complex
  static Literal computeFusionForString(List<Literal> alternatives){
    return null;

  }

  static Literal computeFusionForDate(List<Literal> alternatives){
    return null;
  }

  static Literal fallBackFusion(List<Literal> alternatives){
    return alternatives.get(0); //todo: better Fallback option?
  }
  private Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      PreciseFusionStrategy::fallBackFusion
    ).apply(alternatives);
  }
}
