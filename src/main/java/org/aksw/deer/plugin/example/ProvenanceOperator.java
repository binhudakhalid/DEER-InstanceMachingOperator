package org.aksw.deer.plugin.example;
import org.aksw.deer.vocabulary.DEER;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.Map;

public class ProvenanceOperator {
  //@prefix prov:    <http://www.w3.org/ns/prov#> .
  // https://www.w3.org/TR/prov-o/#Entity

  private static String PREFIX_URI = "http://www.w3.org/ns/prov#";
  private static String PREFIX_NAME = "prov";
  private static String WAS_ATTRIBUTED_TO = "wasAttributedTo";
  private static Statement targetStatement;
  private static Statement sourceStatement;

  /**
   * add Provenance to a given model
   * @param model model at hand (most likely model of the source
   * @param sourceSubject Subject from source
   * @param targetSubject Subject from target
   * @param sourceEndpoint source Endpoint (for ProvAgent)
   * @param targetEndpoint target Endpoint (for ProvAgent)
   */
  public static void addProvenance(Model model, Resource sourceSubject, Resource targetSubject,
                                   Resource sourceEndpoint, Resource targetEndpoint) {


    model.setNsPrefix(PREFIX_NAME, PREFIX_URI ); // shouldnt add twice if alrdy there


    // Agent
    sourceStatement = findOrAddAgents(model,sourceEndpoint);
    targetStatement = findOrAddAgents(model,targetEndpoint);

    // build Statements
    addEntities(model, sourceSubject, sourceEndpoint);
    addEntities(model, targetSubject, targetEndpoint);

  }

  /**
   * add Provenance to a given model
   * @param source model at hand (most likely model of the source
   * @param target model from which provenance should be taken over
   * @param sourceSubject Subject from source
   * @param targetSubject Subject from target
   * @param sourceEndpoint source Endpoint (for ProvAgent)
   * @param targetEndpoint target Endpoint (for ProvAgent)
   */
  public static void addProvenance(Model source,Model target, Resource sourceSubject, Resource targetSubject,
                                   Resource sourceEndpoint, Resource targetEndpoint) {

    addProvenance(source,sourceSubject,targetSubject,sourceEndpoint,targetEndpoint);

    // take everything from target that looks like provenance and add it to source
    takeProvenance(source,target);

  }

  private static void takeProvenance(Model source, Model target) {
    Resource agent = target.createResource(target.expandPrefix("prov:Agent"));
    Property wasAttributedTo = target.createProperty(PREFIX_NAME, WAS_ATTRIBUTED_TO); // todo: check if with expaned prefix or smth
    // take Agents
    StmtIterator targetAgentsIterator = target.listStatements(null, RDF.type, agent);

    while(targetAgentsIterator.hasNext()){
      source.add(targetAgentsIterator.nextStatement());
    }
    // take entities
    StmtIterator targetEntityIterator = target.listStatements(null, wasAttributedTo, (RDFNode) null);
    while(targetEntityIterator.hasNext()){
      source.add(targetEntityIterator.nextStatement());
    }

  }
  private static String lookForMetaData(Model model ){
    /*
    Try for https://www.w3.org/TR/void/ here ,

    if you find nothing , use the endpoint string provided
     */

    return null;
  }
  private static void addEntities(Model model, Resource subject, Resource endpoint) {
    Resource entity = model.createResource(model.expandPrefix("prov:Entity"));
    Property wasAttributedTo = model.createProperty(PREFIX_NAME, WAS_ATTRIBUTED_TO); // todo: check if with expaned prefix or smth

    // StmtIterator stmtIterator = model.listStatements(subject,wasAttributedTo,entity);
    //not sure if necessary

    model.add(subject,wasAttributedTo,endpoint); // same triples more than once?
    model.add(subject,RDF.type, entity);

  }

  private static Statement findOrAddAgents(Model model, Resource endpoint) {
    //endpoint name from file name or if metadata possible
    //regex the filename or smth
    Resource agent = model.createResource(model.expandPrefix("prov:Agent"));
    StmtIterator stmtIterator = model.listStatements(endpoint,null, agent);
    Statement s;
    if (!stmtIterator.hasNext()){// no Agent there yet for subject
      s = model.createStatement(endpoint, RDF.type, agent);
      model.add(s);
    }
    else
      s = stmtIterator.nextStatement();
    return s;
  }

  /**
   * Method to find amount of subject represent in the given model
   * @param model Model to search through
   * @param subject Resource to find as subject in the Model
   * @return amount of amount of subject in the given model
   */
  public static int getCount(Model model, Resource subject){
    /*
    ask here how often this subject is present in the model with different agents


    maybe: sort by date and have a set of agents, so everyone is counted only once
    */

    /***
     * data_nobelprizeorg1 - subject1
     * data_nobelprizeorg2 - subject1
     * data_nobelprizeorg3 - subject1
     *
     * maybe in sparql
     *
     * select count(distinct(?sub))
     * where {
     * ?sub prov:attributedBy ?agent.
     * ?agent a prov:agent.
     *
     * }
     */

    Property wasAttributedTo = model.createProperty(PREFIX_NAME, WAS_ATTRIBUTED_TO); // todo: check if with expaned prefix or smth
    StmtIterator stmtIterator = model.listStatements(subject, wasAttributedTo, (RDFNode) null);
    int count = 0;
    while(stmtIterator.hasNext()){
      stmtIterator.next();
      count++;
    }
    return count;
  }

}
