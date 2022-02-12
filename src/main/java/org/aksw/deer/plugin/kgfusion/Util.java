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

	public void restrictionUriToQueryString(Restriction targetResObj) {
		String s;
		String tmp;
		for (PrefixEntity list : targetResObj.restrictionPrefixEntity) {
			// System.out.println(" *ali* :" + list);
			// tmp = " ?s rdf:type url:Movie .\r\n" +
		}
	}

	public static String getRedirectedUrl(String url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
		con.setConnectTimeout(1000);
		con.setReadTimeout(1000);
		con.setRequestProperty("User-Agent", "Googlebot");
		con.setInstanceFollowRedirects(false);
		con.connect();
		String headerField = con.getHeaderField("Location");
		return headerField == null ? url : headerField;

	}

	public static String getFinalRedirectedUrl(String url) {
		String finalRedirectedUrl = url;
		try {
			HttpURLConnection connection;
			do {
				connection = (HttpURLConnection) new URL(finalRedirectedUrl).openConnection();
				connection.setInstanceFollowRedirects(false);
				connection.setUseCaches(false);
				connection.setRequestMethod("GET");
				connection.connect();
				int responseCode = connection.getResponseCode();
				if (responseCode >= 300 && responseCode < 400) {
					String redirectedUrl = connection.getHeaderField("Location");
					if (null == redirectedUrl) {
						break;
					}
					finalRedirectedUrl = redirectedUrl;
				} else
					break;
			} while (connection.getResponseCode() != HttpURLConnection.HTTP_OK);
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalRedirectedUrl;
	}

}
