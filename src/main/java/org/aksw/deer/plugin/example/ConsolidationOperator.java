package org.aksw.deer.plugin.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LimesResult;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.config.reader.AConfigurationReader;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.config.writer.RDFConfigurationWriter;
import org.aksw.limes.core.io.serializer.ISerializer;
import org.aksw.limes.core.io.serializer.SerializerFactory;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.VCARD;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class ConsolidationOperator extends AbstractParameterizedEnrichmentOperator {

	private static final Logger logger = LoggerFactory.getLogger(ConsolidationOperator.class);

	public static final Property SUBJECT = DEER.property("subject");
	public static final Property PREDICATE = DEER.property("predicate");
	public static final Property OBJECT = DEER.property("object");
	public static final Property SELECTOR = DEER.property("selector");
	public static final Property SPARQL_CONSTRUCT_QUERY = DEER.property("sparqlConstructQuery");

	public ConsolidationOperator() {

		super();
	}

	@Override
	public ValidatableParameterMap createParameterMap() { // 2
		return ValidatableParameterMap.builder().declareProperty(SELECTOR).declareProperty(SPARQL_CONSTRUCT_QUERY)
				.declareValidationShape(getValidationModelFor(ConsolidationOperator.class)).build();
	}

	@Override
	protected List<Model> safeApply(List<Model> models) { // 3
		
		System.out.println(" models.get(Jei)0 " + models.get(0) + " models.get()0");
		
		//System.out.println(" models.get(khalidbhia)0 " + models.get(0).getProperty("@http://www.w3.org/2001/vcard-rdf/3.0#FN").getPr.toString() + " models.get()0");

		
		Model a = filterModel(models.get(0));
	 
		//Configuration con = createLimeConfigurationFile();
		//System.out.println("Just running Limes into it KHD");
		//callLimes(con);

	 	// create an empty Model
		Model model = ModelFactory.createDefaultModel();
		return List.of(model);
	}

	private Model filterModel(Model model) { // 4

		final Model resultModel = ModelFactory.createDefaultModel();
		final Optional<RDFNode> sparqlQuery = getParameterMap().getOptional(SPARQL_CONSTRUCT_QUERY);
		if (sparqlQuery.isPresent()) {
			logger.info("Executing SPARQL CONSTRUCT query for " + getId() + " ...");
			return QueryExecutionFactory.create(sparqlQuery.get().asLiteral().getString(), model).execConstruct();
		} else {
			getParameterMap().listPropertyObjects(SELECTOR).map(RDFNode::asResource).forEach(selectorResource -> {
				RDFNode s = selectorResource.getPropertyResourceValue(SUBJECT);
				RDFNode p = selectorResource.getPropertyResourceValue(PREDICATE);
				Resource o = selectorResource.getPropertyResourceValue(OBJECT);

				logger.info("Running filter " + getId() + " for triple pattern {} {} {} ...",
						s == null ? "[]" : "<" + s.asResource().getURI() + ">",
						p == null ? "[]" : "<" + p.asResource().getURI() + ">",
						o == null ? "[]" : "(<)(\")" + o.toString() + "(\")(>)");
				SimpleSelector selector = new SimpleSelector(s == null ? null : s.asResource(),
						p == null ? null : p.as(Property.class), o);
				resultModel.add(model.listStatements(selector));
			});
		}
		return resultModel;
	}

	 
}