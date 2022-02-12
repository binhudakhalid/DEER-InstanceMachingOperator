package org.aksw.deer.plugin.kgfusion;

/**
 * This is the entity class for properties/ predicate
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class PropertyEntity {
	String key;
	String value;
	String propertyName;
	int count;
	double coverage;

	/**
	 * @param key the key
	 * @param value the value of property
	 * @param propertyName the name of property
	 * @param count the count
	 * @param coverage the coverage of property
	 */
	public PropertyEntity(String key, String value, String propertyName, int count, double coverage) {
		super();
		this.key = key;
		this.value = value;
		this.propertyName = propertyName;
		this.count = count;
		this.coverage = coverage;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
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
		this.value = value;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return " \n PropertyEntity [key= " + key + ", value= " + value + ", property= " + propertyName + ", count= "
				+ count + " coverage= " + coverage + "]";
	}

}
