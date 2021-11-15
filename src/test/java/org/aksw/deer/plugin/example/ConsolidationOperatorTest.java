package org.aksw.deer.plugin.example;

import junit.framework.TestCase;
import org.aksw.deer.enrichments.AuthorityConformationEnrichmentOperator;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ConsolidationOperatorTest extends TestCase {

  private ConsolidationOperator co;
  Model input, expected;
  private ValidatableParameterMap expectedParams;

  @Before
  public void setUp(){
    input = ModelFactory.createDefaultModel();
    expected = ModelFactory.createDefaultModel();

    co = new ConsolidationOperator();
    co.initPluginId(ResourceFactory.createResource("urn:consolidation-operator"));
    co.initDegrees(1,1); // todo: real init degrees
    ValidatableParameterMap params = co.createParameterMap().init();
    co.initParameters(params);
    input = RDFDataMgr.loadModel("instanceMatchingOutput.ttl");

    expectedParams = co.createParameterMap();
    expectedParams.add(ConsolidationOperator.PROPERTY_FUSION_MAPPING, expectedParams.createResource()
      .addProperty(ConsolidationOperator.PROPERTY_VALUE, input.createProperty("http://xmlns.com/foaf/0.1/","name"))// input.expandPrefix("ex:")))
      .addProperty(ConsolidationOperator.FUSION_VALUE,expected.createResource("expertiseSource"))
    );

    expectedParams.add(ConsolidationOperator.PROPERTY_FUSION_MAPPING, expectedParams.createResource()
      .addProperty(ConsolidationOperator.PROPERTY_VALUE, input.createProperty("http://xmlns.com/foaf/0.1/","firstName"))// input.expandPrefix("ex:")))
      .addProperty(ConsolidationOperator.FUSION_VALUE, expected.createResource("precise"))
    );

    co.initDegrees(1,1);
    expectedParams.init();

  }

  @Test
  public void testSafeApply() {
    co.initParameters(expectedParams);
    List<Model> res = co.safeApply(List.of(input));
  //  System.out.println(res.get(0));
  }
}