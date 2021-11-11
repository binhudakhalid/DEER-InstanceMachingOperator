package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Property;

import java.util.ArrayList;
import java.util.List;

public class MatchablePropertys {
  String label  ;
  Property targetProperty;
  Property sourceProperty;
  FusionStrategy fusionStrategy;
  public MatchablePropertys(String label){
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Property getTargetProperty() {
    return targetProperty;
  }

  public void setTargetProperty(Property targetProperty) {
    this.targetProperty = targetProperty;
  }

  public FusionStrategy getFusionStrategy() {
    return fusionStrategy;
  }

  public void setFusionStrategy(FusionStrategy fusionStrategy) {
    this.fusionStrategy = fusionStrategy;
  }

  public Property getSourceProperty() {
    return sourceProperty;
  }

  public void setSourceProperty(Property sourceProperty) {
    this.sourceProperty = sourceProperty;
  }
}
// ask ontology matching

/*

 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=name, count= 557 coverage=0.7900709219858156],
 PropertyEntity [key=pudc, value=http://purl.org/dc/terms/, property=lastName, count= 63 coverage=0.08936170212765958],
 PropertyEntity [key=pudc, value=http://purl.org/dc/terms/, property=firstName, count= 59 coverage=0.08368794326241134],
 PropertyEntity [key=w3200, value=http://www.w3.org/2000/01/rdf-schema#, property=label, count= 17 coverage=0.024113475177304965],
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=mbox_sha1sum, count= 5 coverage=0.0070921985815602835],
 PropertyEntity [key=w3200, value=http://www.w3.org/2000/01/rdf-schema#, property=seeAlso, count= 4 coverage=0.005673758865248227],
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=familyName, count= 4 coverage=0.005673758865248227],
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=givenName, count= 4 coverage=0.005673758865248227],
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=mbox, count= 3 coverage=0.00425531914893617],
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=nick, count= 3 coverage=0.00425531914893617]]
removePropertiesHavingLowerCoverage -> Total Properties before comparing with Coverage: 10
 ahsan tempCoverage after = [
 PropertyEntity [key=xmfo, value=http://xmlns.com/foaf/0.1/, property=name, count= 557 coverage=0.7900709219858156]]

 */