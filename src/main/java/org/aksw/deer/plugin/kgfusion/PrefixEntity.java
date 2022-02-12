package org.aksw.deer.plugin.kgfusion;

/**
 * This an entity class for prefix.
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class PrefixEntity {

	String key;
	String value;
	String name;

	public PrefixEntity(){}
	

	/**
	 * @param key the key
	 * @param value the value of prefix
	 * @param name the name of predic
	 */
	public PrefixEntity(String key, String value, String name) {
		super();
		this.key = key;
		this.value = value;
		this.name = name;
	}


	@Override
	public String toString() {
		return "PrefixEntity [key=" + key + ", Value=" + value + ", Name=" + name + "]";
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		value = value;
	}

	public String getValue2() {
		return name;
	}

	public void setValue2(String value2) {
		name = value2;
	}

}
