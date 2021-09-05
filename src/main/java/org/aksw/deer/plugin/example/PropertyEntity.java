package org.aksw.deer.plugin.example;

public class PropertyEntity {
		String key;
		String value;
		String property;
		int count;
		
		
		public PropertyEntity(String key, String value, String property, int count) {
			super();
			this.key = key;
			this.value = value;
			this.property = property;
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
		public String getProperty() {
			return property;
		}
		public void setProperty(String property) {
			this.property = property;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		@Override
		public String toString() {
			return "PropertyEntity [key=" + key + ", value=" + value + ", property=" + property + ", count=" + count
					+ "]";
		}

		 

	}

