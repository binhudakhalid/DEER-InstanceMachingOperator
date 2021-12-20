package org.aksw.deer.plugin.example;
import org.aksw.deer.vocabulary.DEER;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.Map;

public class ProvenanceOperator {
  //@prefix prov:    <http://www.w3.org/ns/prov#> .
  // https://www.w3.org/TR/prov-o/#Entity

  private static String PREFIX_NAME = "prov";
  private static String PREFIX_URI = "http://www.w3.org/ns/prov#";
  private static String WAS_ATTRIBUTED_TO = "wasAttributedTo";
  private static Statement targetStatement;
  private static Statement sourceStatement;

  public static void addProvenance(Model source, Resource sourceSubject, Resource targetSubject,
                                   Resource sourceEndpoint, Resource targetEndpoint) {


    source.setNsPrefix(PREFIX_NAME, PREFIX_URI ); // shouldnt add twice if alrdy there


    // Agent
    sourceStatement = findOrAddAgents(source,sourceEndpoint);
    targetStatement = findOrAddAgents(source,targetEndpoint);

    // build Statements
    addEntities(source, sourceSubject, sourceEndpoint);
    addEntities(source, targetSubject, targetEndpoint);

  }

  public static void addProvenance(Model source,Model target, Resource sourceSubject, Resource targetSubject,
                                   Resource sourceEndpoint, Resource targetEndpoint) {

    addProvenance(source,sourceSubject,targetSubject,sourceEndpoint,targetEndpoint);

    // take everything from target that looks like provenance and add it to source


  }

  private static void addEntities(Model model, Resource subject, Resource endpoint) {
    Resource entity = model.createResource(model.expandPrefix("prov:Entity"));
    Property wasAttributedTo = model.createProperty(PREFIX_NAME, WAS_ATTRIBUTED_TO); // todo: check if with expaned prefix or smth

    // StmtIterator stmtIterator = model.listStatements(subject,wasAttributedTo,entity);
    //not sure if necessary

    model.add(subject,wasAttributedTo,endpoint);
    model.add(subject,RDF.type, entity);

  }

  private static Statement findOrAddAgents(Model model, Resource endpoint) {

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


  public static int getCount(Model model, Resource subject){
    /*
    ask here how often this subject is present in the model with different agents
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
