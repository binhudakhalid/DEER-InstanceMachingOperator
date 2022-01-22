package org.aksw.deer.plugin.kgfusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Restriction {

	// Set<String> letter = new HashSet<String>();
	ArrayList<PrefixEntity> restrictionPrefixEntity;
	Set<String> restrictionString;
	ArrayList<String> restrictionList;
	String variable;

	@Override
	public String toString() {
		return "Restriction [restrictionPrefixEntity=" + restrictionPrefixEntity + ", restrictionString="
				+ restrictionString + ", restrictionList=" + restrictionList + ", variable=" + variable + "]";
	}

	Restriction(String variable) {
		this.variable = variable;
		restrictionList = new ArrayList<>();
		restrictionPrefixEntity = new ArrayList<>();
	}

}
