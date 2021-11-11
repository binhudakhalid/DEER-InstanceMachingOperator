package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Literal;

import java.util.List;

public class StandardFusionStrategy extends AbstractFusionStrategy{

  @Override
  Literal computeFusionForInteger(List<Literal> alternatives) {
    return null;
  }

  @Override
  Literal computeFusionForDecimal(List<Literal> alternatives) {
    return null;
  }

  @Override
  Literal computeFusionForBoolean(List<Literal> alternatives) {
    return null;
  }

  @Override
  Literal computeFusionForString(List<Literal> alternatives) {
    return null;
  }

  @Override
  Literal computeFusionForDate(List<Literal> alternatives) {
    return null;
  }

  @Override
  Literal fallBackFusion(List<Literal> alternatives) {
    return null;
  }
}
