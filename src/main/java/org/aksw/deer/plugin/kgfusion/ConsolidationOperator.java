package org.aksw.deer.plugin.kgfusion;

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
public class ConsolidationOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(ConsolidationOperator.class);

	public ConsolidationOperator() {

		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder()
				.declareValidationShape(getValidationModelFor(ConsolidationOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3

	//	System.out.println("shah" + "The output from Instance Matching Operator models.get(0) " + models.size() ); 

//		System.out.println("The output from Instance Matching Operator models.get(0) " + models.get(0).size() ); 
		System.out.println(" aop The output from Instance Matching Operator models " + models ); 
		System.out.println(" end aop khd"); 
		

		FileWriter writer;
		try {
			writer = new FileWriter("outputFromInstanceMatching.nt");
			models.get(0).write(writer, "N-TRIPLE");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   
		//System.out.println("The output from Instance Matching Operator models.get(1) " + models.get(1) ); 

		 
		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		return List.of(model);
	}

}