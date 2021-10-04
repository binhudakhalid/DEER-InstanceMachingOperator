package org.aksw.deer.plugin.example;

public class PrefixEntity {

	String key;
	String value;
	String name;

	public PrefixEntity()
	{
		
	}
	

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
