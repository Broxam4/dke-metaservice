package com.indigo.meta_service;

public class Student {
	private String FIRSTNAME;
	private String LASTNAME;
	private String MATRNR;
	
	public Student(String first, String last, String matrnr) {
		FIRSTNAME = first;
		LASTNAME = last;
		MATRNR = matrnr;
	}
	
	public Student() {
		
	}

	public String getFIRST_NAME() {
		return FIRSTNAME;
	}

	public String getLAST_NAME() {
		return LASTNAME;
	}

	public String getMATR_NR() {
		return MATRNR;
	}

	public void setFIRSTNAME(String fIRSTNAME) {
		FIRSTNAME = fIRSTNAME;
	}

	public void setLASTNAME(String lASTNAME) {
		LASTNAME = lASTNAME;
	}

	public void setMATRNR(String mATRNR) {
		MATRNR = mATRNR;
	}
	
	
}