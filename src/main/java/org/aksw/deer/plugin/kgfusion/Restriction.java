package org.aksw.deer.plugin.kgfusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Restriction {

	// Set<String> letter = new HashSet<String>();
	Set<PrefixEntity> restrictionPrefixEntity;
	Set<String> restrictionString;
	ArrayList<String> restrictionList;
  


	Restriction() {
		restrictionList = new ArrayList<>();
		restrictionPrefixEntity = new HashSet<PrefixEntity>();
	}

}
