package org.aksw.deer.plugin.kgfusion;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/*
 * Three Jena Models
 * 1- info
 * 2- limeOuputModel
 * 3- finalOuputModel
 * */

public class OutputUtility {

	public List<Model> createOuput(String limeOutputfile,
			String sourceFilePath, String targetFilePath, String type, Restriction sourceResObj,
			Restriction targetResObj) {

		System.out.println("sourceResObj ::" + sourceResObj);

		// [restrictionPrefixEntity=[PrefixEntity

		// ------------
		List<Model> InstanceMatcherOutputList = new ArrayList<>();

		//// information about entities and class
		Model info = ModelFactory.createDefaultModel();
		//addStatement("DEER:sourceClass", "DEER:is", sourceRestrictions, info);
		//addStatement("DEER:targetClass", "DEER:is", targetRestrictions, info);

		addStatement("DEER:dataSourceType", "DEER:is", type, info);
		// addStatement("DEER:sourceDataSource", "DEER:is", "3", info);
		// addStatement("DEER:targetDataSource", "DEER:is", "4", info);

		// load accepted.nt into Jena model
		Model limesOutputModel = ModelFactory.createDefaultModel();

		Model m1 = ModelFactory.createDefaultModel();

		final String NS = "https://w3id.org/deer/";
		final Property confidence = limesOutputModel.createProperty(NS + "confidence");

		//////////////

		Model limeOutputModel = ModelFactory.createDefaultModel();
		try {
			File myObj = new File("accepted.nt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();

				// Break line after tab
				String[] splittedData = data.split("\t", -1);

				/*
				 * System.out.println("splittedData : " + splittedData[0]);
				 * System.out.println("splittedData : " + splittedData[1]);
				 * System.out.println("splittedData : " + splittedData[2]);
				 */

				Resource subject = m1.createResource(splittedData[0].replace("<", "").replace(">", ""));
				Property predicate = m1.createProperty("http://www.w3.org/2002/07/owl#sameAs");
				RDFNode object = m1.createResource(splittedData[1].replace("<", "").replace(">", ""));

				Statement stmt = m1.createStatement(subject, predicate, object);

				final ReifiedStatement rstmt = limeOutputModel.createReifiedStatement(stmt);
				limeOutputModel.add(rstmt, confidence, splittedData[2]);
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		// Ouput Model
		Model finalOuputModel = ModelFactory.createDefaultModel();

		// Adding source data set
		addStatement("https://w3id.org/deer/datasetSource", "https://w3id.org/deer/path", sourceFilePath,
				finalOuputModel);

		// Adding target data set
		addStatement("https://w3id.org/deer/datasetTarget", "https://w3id.org/deer/path", targetFilePath,
				finalOuputModel);

		// Adding subject types, or source restrictions
		int i = 1;
		for (PrefixEntity rpe : sourceResObj.restrictionPrefixEntity) {
			System.out.println("str" + rpe);

			System.out.println("aaaa:: " + rpe.value + rpe.name);
			String tmpObj = rpe.value + rpe.name;
			String tmpSub = "https://w3id.org/deer/subjectType#" + i;
			addStatement(tmpSub, "https://w3id.org/deer/is", tmpObj, finalOuputModel);
			// Do something
			i++;
		}

		// Adding object types, or target restrictions
		int j = 1;
		for (PrefixEntity rpe : targetResObj.restrictionPrefixEntity) {
			System.out.println("str" + rpe);

			System.out.println("aaaa:: " + rpe.value + rpe.name);
			String tmpObj = rpe.value + rpe.name;
			String tmpSub = "https://w3id.org/deer/objectType#" + j;
			addStatement(tmpSub, "https://w3id.org/deer/is", tmpObj, finalOuputModel);
			// Do something
			j++;
		}
		

		// Add info model
		finalOuputModel.add(info);

		// Add Reified statements to final model
		finalOuputModel.add(limeOutputModel);
		InstanceMatcherOutputList.add(finalOuputModel);
		// System.out.println(" ZGreat: " + InstanceMatcherOutputList);

		return InstanceMatcherOutputList;

	}

	public void addStatement(String s, String p, String o, Model model) {
		Resource subject = model.createResource(s);
		Property predicate = model.createProperty(p);
		RDFNode object = model.createResource(o);
		Statement stmt = model.createStatement(subject, predicate, object);
		model.add(stmt);
	}

}
