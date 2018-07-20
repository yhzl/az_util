package com.az.bean.dto;

public class ValidDTO {

	private String fieldName;//字段名称
	private String fieldNote;//字段注释
	private String anno;//注解内容
	private String parentFieldName;//父字段名称
	public ValidDTO(){}
	public ValidDTO(String fieldName, String fieldNote, String anno){
		this.fieldName = fieldName;
		this.fieldNote = fieldNote;
		this.anno = anno;
	}
	public ValidDTO(String fieldName, String fieldNote, String anno, String parentFieldName){
		this.fieldName = fieldName;
		this.fieldNote = fieldNote;
		this.anno = anno;
		this.parentFieldName = parentFieldName;
	}
	public String getParentFieldName() {
		return parentFieldName;
	}
	public void setParentFieldName(String parentFieldName) {
		this.parentFieldName = parentFieldName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldNote() {
		return fieldNote;  
	}
	public void setFieldNote(String fieldNote) {
		this.fieldNote = fieldNote;
	}
	public String getAnno() {
		return anno;
	}
	public void setAnno(String anno) {
		this.anno = anno;
	}
}
