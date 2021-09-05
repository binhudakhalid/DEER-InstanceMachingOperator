package org.aksw.deer.plugin.example;

public class PropertyEntity {
		String key;
		String value;
		String propertyName;
		int count;
		
		
		public PropertyEntity(String key, String value, String propertyName, int count) {
			super();
			this.key = key;
			this.value = value;
			this.propertyName = propertyName;
			this.count = count;
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
			return "PropertyEntity [key=" + key + ", value=" + value + ", property=" + propertyName + ", count=" + count
					+ "]";
		}

		 

	}

