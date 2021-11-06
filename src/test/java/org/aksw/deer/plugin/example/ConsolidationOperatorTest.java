package org.aksw.deer.plugin.example;

import junit.framework.TestCase;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;

import java.util.List;

public class ConsolidationOperatorTest extends TestCase {

  public void testSafeApply() {
    Model modelExpected = ModelFactory.createDefaultModel();
    ConsolidationOperator co = new ConsolidationOperator();
    co.initPluginId(ResourceFactory.createResource("urn:consolidation-operator"));
    co.initDegrees(1,1); // todo: real init degrees
    ValidatableParameterMap params = co.createParameterMap().init();
    co.initParameters(params);
    Model input = RDFDataMgr.loadModel("instanceMatchingOutput.ttl");

    List<Model> res = co.safeApply(List.of(input));
  }
}