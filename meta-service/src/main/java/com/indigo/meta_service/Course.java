package com.indigo.meta_service;

public class Course {
	private String name;
	private String id;
	private String lector;
	private String type;
	private Float ects;
	private Boolean isEnrolledBy;

	public Course(String name, String id, String lector, String type, Float ects, Boolean isEnrolledBy) {
		this.name = name;
		this.id = id;
		this.lector = lector;
		this.type = type;
		this.ects = ects;
		this.isEnrolledBy = isEnrolledBy;
	}

	public Course(String name) {
		this.name = name;
		id = "-";
		lector = "-";
		type = "-";
		ects = (float) 0;
		isEnrolledBy = false;
	}

	public Course() {
		name = "-";
		id = "-";
		lector = "-";
		type = "-";
		ects = (float) 0;
		isEnrolledBy = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLector() {
		return lector;
	}

	public void setLector(String lector) {
		this.lector = lector;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsEnrolledBy() {
		return isEnrolledBy;
	}

	public void setIsEnrolledBy(Boolean isEnrolledBy) {
		this.isEnrolledBy = isEnrolledBy;
	}

	public Float getEcts() {
		return ects;
	}

	public void setEcts(Float ects) {
		this.ects = ects;
	}
}
