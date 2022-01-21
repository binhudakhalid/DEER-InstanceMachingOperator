package org.aksw.deer.plugin.kgfusion;

import java.util.HashMap;
import java.util.Map;

public class Util {
	
	public Restriction restrictionUriToString(HashMap<String, String> sourceRestriction, String variable) {
		System.out.println("prefixURIs :" + sourceRestriction);
		// prefixURIs.

		Restriction sourceResObj = new Restriction();

		for (Map.Entry<String, String> entry : sourceRestriction.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			PrefixEntity restrictionPredicate = PrefixUtility.splitPreficFromProperty(key);
			PrefixEntity restrictionObject = PrefixUtility.splitPreficFromProperty(value);

			String s1 = "?" + variable + " " + restrictionPredicate.key + ":" + restrictionPredicate.name + " "
					+ restrictionObject.key + ":" + restrictionObject.name;
			System.out.println("s1 " + s1);

			System.out.println(" s1s1 var :"+ variable + " : " + s1);
			sourceResObj.restrictionList.add(s1);
			sourceResObj.restrictionPrefixEntity.add(restrictionPredicate);
			sourceResObj.restrictionPrefixEntity.add(restrictionObject);
		}
		
		System.out.println("see : " + sourceResObj.restrictionList);
		System.out.println("see restrictionPrefixEntity: " + sourceResObj.restrictionPrefixEntity);

		/*
		 * for(String prefixURIsItem : sourceRestriction){
		 * System.out.println("prefixURIsItem :" + prefixURIsItem); PrefixEntity
		 * targetRestrictionPrefixEntity =
		 * PrefixUtility.splitPreficFromProperty(prefixURIsItem);
		 * System.out.println("targetRestrictionPrefixEntity : " +
		 * targetRestrictionPrefixEntity); // String queryString =
		 * 
		 * }
		 */
		// PrefixEntity targetRestrictionPrefixEntity =
		// PrefixUtility.splitPreficFromProperty(prefixURIs);
		
		return sourceResObj;

	}


}
