package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractFusionStrategy {

  private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();
  /* // QUestion here
  static {
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#string",
      AbstractFusionStrategy::computeFusionForString
    );
  }*/

  //primitives
  abstract Literal computeFusionForInteger(List<Literal> alternatives);

  abstract Literal computeFusionForDecimal(List<Literal> alternatives);

  abstract Literal computeFusionForBoolean(List<Literal> alternatives);

  // complex
  abstract Literal computeFusionForString(List<Literal> alternatives);

  abstract Literal computeFusionForDate(List<Literal> alternatives);

  abstract Literal fallBackFusion(List<Literal> alternatives);
/*
  private Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      AbstractFusionStrategy::fallBackFusion
    ).apply(alternatives);
  }

 */
}
//https://github.com/puppetlabs/pcore-java/blob/main/src/main/java/com/puppet/pcore/impl/Polymorphic.java