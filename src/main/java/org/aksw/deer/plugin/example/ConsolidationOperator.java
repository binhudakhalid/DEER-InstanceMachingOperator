package org.aksw.deer.plugin.example;

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

		System.out.println(" Data from InstanceMatchingOperator " + models.get(0) + " models.get()0090");

		// list the statements in the Model from Instance Matching
		StmtIterator iter = models.get(0).listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " s0d-o ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}

		/// ConsolidationOperator team, add your code here

		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		return List.of(model);
	}

}