package org.aksw.deer.plugin.kgfusion;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * This utility class for operator
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class Util {

	
	/**
	 * @param list the restriction entity list 
	 * @param sourceResObj the source restriction object
	 * @return return source Restriction Object
	 */
	public Restriction restrictionUriToString(List<RestrictionEntity> list, Restriction sourceResObj) {

		for (RestrictionEntity i : list) {

			String key = i.getPredicate();
			String value = i.getName();
			PrefixEntity restrictionPredicate = PrefixUtility.splitPreficFromProperty(key);
			PrefixEntity restrictionObject = PrefixUtility.splitPreficFromProperty(value);

			String s1 = "?" + sourceResObj.variable + " " + restrictionPredicate.key + ":" + restrictionPredicate.name
					+ " " + restrictionObject.key + ":" + restrictionObject.name;

			sourceResObj.restrictionList.add(s1);
			sourceResObj.restrictionPrefixEntity.add(restrictionPredicate);
			sourceResObj.restrictionPrefixEntity.add(restrictionObject);

		}
		
		return sourceResObj;

	}


	/**
	 * @param inputURL the url
	 * @return the redirected url
	 */
	public static String returnRedirectedURL(String inputURL) {
		String finalURL = inputURL;
		int multipleResponseStatusCode = 300;
		int badRequestResponseStatusCode = 400;

		try {
			HttpURLConnection urlCon;
			do {
				urlCon = (HttpURLConnection) new URL(finalURL).openConnection();
			
				urlCon.setUseCaches(false);
				urlCon.setInstanceFollowRedirects(false);
				urlCon.connect();
				urlCon.setRequestMethod("GET");

				int resCode = urlCon.getResponseCode();
				if (resCode >= multipleResponseStatusCode && resCode < badRequestResponseStatusCode) {
					String redirectUrl = urlCon.getHeaderField("Location");
					if (redirectUrl == null) {
						break;
					}
					finalURL = redirectUrl;
				} else
					break;
			} while (urlCon.getResponseCode() != HttpURLConnection.HTTP_OK);
			urlCon.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return finalURL;
	}

}
