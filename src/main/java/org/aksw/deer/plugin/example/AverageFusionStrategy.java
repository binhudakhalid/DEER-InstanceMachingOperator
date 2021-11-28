package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AverageFusionStrategy {

    public static final FusionStrategy fusionStrategy = FusionStrategy.precise;
    private static final Map<String, Function<List<Literal>, Literal>> dispatchMap = new HashMap<>();

    static {
    /*
    String
     */
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#string",
        AverageFusionStrategy::computeFusionForString //
      );
      dispatchMap.put(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
        AverageFusionStrategy::computeFusionForString
      );
    /*
    Integer
     */
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#int",
        AverageFusionStrategy::computeFusionForInteger
      );

      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#byte",
        AverageFusionStrategy::computeFusionForInteger
      );

      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#ling",
        AverageFusionStrategy::computeFusionForInteger
      );

      // todo: right URI below here
      // Core Type
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#boolean",
        AverageFusionStrategy::computeFusionForBoolean
      );
      //floating points
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#decimal",
        AverageFusionStrategy::computeFusionForDecimal
      );
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#float",
        AverageFusionStrategy::computeFusionForDecimal
      );
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#double",
        AverageFusionStrategy::computeFusionForDecimal
      );
    /*
    Time & Date
     */
      dispatchMap.put(
        "http://www.w3.org/2001/XMLSchema#date",
        AverageFusionStrategy::computeFusionForDate
      );
      //
    }

    //primitives
    static Literal computeFusionForInteger(List<Literal> alternatives){
      return ResourceFactory.createTypedLiteral(
        alternatives.stream().mapToInt(Literal::getInt).average()
      );
      // minimum more precise?
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

  private Literal executeFusion(List<Literal> alternatives) {
    // compute common datatype here or pass it into the function
    var typeURL = alternatives.get(0).getDatatypeURI();
    return Objects.requireNonNullElse(
      dispatchMap.get(typeURL),
      AverageFusionStrategy::fallBackFusion
    ).apply(alternatives);
  }
 }

