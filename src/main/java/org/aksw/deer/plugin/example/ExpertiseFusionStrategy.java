package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ExpertiseFusionStrategy {


  public static boolean takeSource = true;
  private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();

  static {
    dispatchMap.put(
      "http://www.w3.org/2001/XMLSchema#string", // we dont need this
      ExpertiseFusionStrategy::fallBackFusion
    );
  }



  static Literal fallBackFusion(List<Literal> alternatives){
    return alternatives.get(takeSource?0:1);
  }

  private Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      ExpertiseFusionStrategy::fallBackFusion
    ).apply(alternatives);
  }

  public static boolean isTakeSource() {
    return takeSource;
  }

  public static void setTakeSource(boolean takeSource) {
    ExpertiseFusionStrategy.takeSource = takeSource;
  }




}
//https://github.com/puppetlabs/pcore-java/blob/main/src/main/java/com/puppet/pcore/impl/Polymorphic.java