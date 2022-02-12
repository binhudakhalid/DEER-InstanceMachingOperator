package org.aksw.deer.plugin.kgfusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Helper class for restriction
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class Restriction {

	ArrayList<PrefixEntity> restrictionPrefixEntity;
	Set<String> restrictionString;
	ArrayList<String> restrictionList;
	String variable;

	@Override
	public String toString() {
		return "Restriction [restrictionPrefixEntity=" + restrictionPrefixEntity + ", restrictionString="
				+ restrictionString + ", restrictionList=" + restrictionList + ", variable=" + variable + "]";
	}
	
	
	/**
	 * This constructor initiate array for list of restriction.
	 * @param variable the variable
	 */
	Restriction(String variable) {
		this.variable = variable;
		restrictionList = new ArrayList<>();
		restrictionPrefixEntity = new ArrayList<>();
	}

}
