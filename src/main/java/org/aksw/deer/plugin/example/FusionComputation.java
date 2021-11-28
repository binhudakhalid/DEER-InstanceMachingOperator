package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface FusionComputation {
  
  FusionStrategy fusionStrategy = null;
  static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();

  static Literal computeFusionForInteger(List<Literal> alternatives){
    return alternatives.get(0);
  }
  static Literal computeFusionForDecimal(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.stream().mapToDouble(Literal::getDouble).average()
    );
  }

  static Literal computeFusionForBoolean(List<Literal> alternatives){
    return ResourceFactory.createTypedLiteral(
      alternatives.get(0).getBoolean() &&
        alternatives.stream().distinct().count() == 1
    );

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

  private static Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      FusionComputation::fallBackFusion
    ).apply(alternatives);
  }
}
