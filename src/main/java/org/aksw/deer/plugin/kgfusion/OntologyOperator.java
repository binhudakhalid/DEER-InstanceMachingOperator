package org.aksw.deer.plugin.kgfusion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class OntologyOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(OntologyOperator.class);

	public OntologyOperator() {

		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder()
				.declareValidationShape(getValidationModelFor(OntologyOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3

		//sample output from Ontology 

		 Model inputModel=ModelFactory.createDefaultModel();
		 try {
			inputModel.read(new FileInputStream("OntologyOutput.ttl"),null,"TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return List.of(inputModel);
	}

}