package org.aksw.deer.plugin.kgfusion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.apache.jena.query.ResultSet;

/**
 * This is the utility class for prefix entity
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class PrefixUtility {

	
	/**
	 * @param predicate the predicate
	 * @return prefix entity
	 */
	public static PrefixEntity splitPreficFromProperty(String predicate) {
		String propertyKey, properyValue, propertyName;
		URL aURL = null;

		/** http://www.w3.org/2002/07/owl#sameAs=903475 */
		if (predicate.contains("#")) {
			 
			propertyName = predicate.substring(predicate.indexOf("#") + 1, predicate.length());

			aURL = null;
			try {
				aURL = new URL(predicate);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			/** creating predicate Prefix Key */
			if (aURL.getHost().contains("www.")) {
				propertyKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 4);
			} else {
				propertyKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 4);
			}
			properyValue = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath() + "#";

			/** Random number to prfixKey */
			Random random = new Random();
			int ran = random.nextInt(599) + 1;
			propertyKey = propertyKey + ran;

			PrefixEntity prefix = new PrefixEntity(propertyKey, properyValue, propertyName);
			return prefix;

		} else {
			
			try {
				aURL = new URL(predicate);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			/** creating predicate Prefix Key */
			if (aURL.getHost().contains("www.")) {
				propertyKey = aURL.getHost().substring(4, 6) + aURL.getPath().substring(1, 3);
			} else {
				propertyKey = aURL.getHost().substring(0, 2) + aURL.getPath().substring(1, 3);
			}

			String temp = aURL.getProtocol() + "://" + aURL.getHost() + aURL.getPath();
			properyValue = temp.substring(0, temp.lastIndexOf('/') + 1);
			propertyName = predicate.substring(predicate.lastIndexOf("/") + 1, predicate.length());

			/** Random number to prfixKey */
			Random random = new Random();
			int ran = random.nextInt(599) + 1;
			propertyKey = propertyKey + ran;

			PrefixEntity prefix = new PrefixEntity(propertyKey, properyValue, propertyName);
			return prefix;
		}
	}
}