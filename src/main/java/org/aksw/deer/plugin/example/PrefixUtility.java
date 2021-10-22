package org.aksw.deer.plugin.example;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.jena.query.ResultSet;

public class PrefixUtility {
	
	
	
	public static PrefixEntity splitPreficFromProperty(String predicate){
			String propertyKey, properyValue, propertyName;
			URL aURL = null;
	
			// http://www.w3.org/2002/07/owl#sameAs=903475
			if (predicate.contains("#")) {
				//System.out.println("****************-URL0 with Hash********************");
				//System.out.println("predicate : " + predicate);

				propertyName = predicate.substring(predicate.indexOf("#") + 1, predicate.length());

				aURL = null;
				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					propertyKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
				} else {
					propertyKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
				}
				properyValue = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath() + "#";

				//System.out.println("properyValue  l20 : " + properyValue);
				//System.out.println("propertyName l20 : " + propertyName);
				//System.out.println("propertyKey l20: " + propertyKey);
				
				PrefixEntity prefix = new PrefixEntity(propertyKey, properyValue, propertyName);
				return prefix;
			
			} else {
				//System.out.println("****************-URL without Hash********************");
				//System.out.println("predicate : " + predicate);

				// propertyKey, predicatePrefixValue, propertyName;

				try {
					aURL = new URL(predicate);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				/// creating predicate Prefix Key
				if (aURL.getHost().contains("www.")) {
					propertyKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 3);
				} else {
					propertyKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 3);
				}

				String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
				properyValue = temp.substring(0, temp.lastIndexOf('/') + 1);
				propertyName = predicate.substring(predicate.lastIndexOf("/") + 1, predicate.length());

				//System.out.println("properyValue  l22 : " + properyValue);
				//System.out.println("propertyName l22 : " + propertyName);
				//System.out.println("propertyKey l22: " + propertyKey);
				PrefixEntity prefix = new PrefixEntity(propertyKey, properyValue, propertyName);
				//propertiesPrefixesSource.add(prefix);

				return prefix;
			}
	 }
	
	private HashMap<String,String> extractPrefixFromFile() {
		HashMap<String, String> TempHashMap = null;
		return TempHashMap;
	}

	public void removeUnecessayPrefix(String sparqlQuery) {
	}
}