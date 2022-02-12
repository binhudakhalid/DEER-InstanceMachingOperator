package org.aksw.deer.plugin.kgfusion;


/**
 * This is the entity class for Restriction
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class RestrictionEntity {
	String predicate;
	String name;
	Integer order;

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "RestrictionEntity [predicate=" + predicate + ", name=" + name + ", order=" + order + "]";
	}

	public RestrictionEntity(Integer order, String predicate, String name) {
		super();
		this.predicate = predicate;
		this.name = name;
		this.order = order;
	}

}
