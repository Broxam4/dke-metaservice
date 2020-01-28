package com.indigo.meta_service;

public class StudentCourseRelation {
	private Student student;
	private Course course;
	
	public StudentCourseRelation(Student student, Course course) {
		this.setStudent(student);
		this.setCourse(course);
	}
	
	public StudentCourseRelation() {
		
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
	

}
