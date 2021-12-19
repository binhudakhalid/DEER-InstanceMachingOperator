package org.aksw.deer.plugin.example;

import junit.framework.TestCase;
import org.aksw.deer.Deer;
import org.aksw.deer.enrichments.AuthorityConformationEnrichmentOperator;
import org.aksw.faraday_cage.engine.CompiledExecutionGraph;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;
import org.pf4j.DefaultPluginManager;

import java.util.List;
import java.util.Objects;

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
      .addProperty(ConsolidationOperator.PROPERTY_VALUE, input.createProperty("http://www.w3.org/2000/01/rdf-schema#","seeAlso"))// input.expandPrefix("ex:")))
      .addProperty(ConsolidationOperator.FUSION_VALUE, expected.createResource("expertiseTarget"))
    );

    co.initDegrees(1,1);
    expectedParams.init();

  }

  @Test
  public void testSafeApply() {
    List<Model> res = co.safeApply(List.of(input));
  //  System.out.println(res.get(0));
  }
  @Test
  public void testListOfParams(){

    co.initParameters(expectedParams);
    List<Model> res = co.safeApply(List.of(input));

  }
  @Test
  public void testNonInitializedFusionStrategy(){
    expectedParams = co.createParameterMap();
    expectedParams.add(ConsolidationOperator.PROPERTY_FUSION_MAPPING, expectedParams.createResource()
      .addProperty(ConsolidationOperator.PROPERTY_VALUE, input.createProperty("http://xmlns.com/foaf/0.1/","name"))// input.expandPrefix("ex:")))
      .addProperty(ConsolidationOperator.FUSION_VALUE, expected.createResource("voting"))
    );
    expectedParams.init();
    co.initParameters(expectedParams);
    List<Model> res = co.safeApply(List.of(input));
  }
  @Test
  public void testTestAuthority(){
  //Todo: add expected Params addTarget: Use Output to feed to Authority and check if the names are usable for fusion
    String url = Objects.requireNonNull(ConsolidationOperatorTest.class.getClassLoader().getResource("config_authority.ttl")).toExternalForm();
    Model configurationModel = ModelFactory.createDefaultModel().read(url);
    CompiledExecutionGraph executionGraph = Deer.getExecutionContext(new DefaultPluginManager()).compile(configurationModel);
    executionGraph.run();
    executionGraph.join();
  }
  @Test
  public void testConfiguration_consolidation_only(){
    String url = Objects.requireNonNull(ConsolidationOperatorTest.class.getClassLoader().getResource("config_consolidation_only.ttl")).toExternalForm();
    Model configurationModel = ModelFactory.createDefaultModel().read(url);
    CompiledExecutionGraph executionGraph = Deer.getExecutionContext(new DefaultPluginManager()).compile(configurationModel);
    executionGraph.run();
    executionGraph.join();
  }
  @Test
  public void testConfiguration_with_FusionStrategyPerProperty(){
    String url = Objects.requireNonNull(ConsolidationOperatorTest.class.getClassLoader().getResource("config_consolidation_withFusionStrategy.ttl")).toExternalForm();
    Model configurationModel = ModelFactory.createDefaultModel().read(url);
    CompiledExecutionGraph executionGraph = Deer.getExecutionContext(new DefaultPluginManager()).compile(configurationModel);
    executionGraph.run();
    executionGraph.join();
  }

}