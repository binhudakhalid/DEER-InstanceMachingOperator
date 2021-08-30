package org.aksw.deer.plugin.example;

public class PrefixEntity {

	String key;
	String Value;
	String Value2;
	int Count;

	public PrefixEntity()
	{
		
	}
	public PrefixEntity(String key, String value, String value2) {
		super();
		this.key = key;
		Value = value;
		Value2 = value2;
	}

	@Override
	public String toString() {
		return "PrefixEntity [key=" + key + ", Value=" + Value + ", Value2=" + Value2 + ", Count=" + Count + "]";
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}

	public String getValue2() {
		return Value2;
	}

	public void setValue2(String value2) {
		Value2 = value2;
	}

	public int getCount() {
		return Count;
	}

	public void setCount(int count) {
		Count = count;
	}

}
