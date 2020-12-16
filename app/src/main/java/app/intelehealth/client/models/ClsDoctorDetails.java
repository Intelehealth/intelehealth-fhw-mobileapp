package app.intelehealth.client.models;

import com.google.gson.annotations.SerializedName;

public class ClsDoctorDetails{

	@SerializedName("whatsapp")
	private String whatsapp;

	@SerializedName("qualification")
	private String qualification;

	@SerializedName("phoneNumber")
	private String phoneNumber;

	@SerializedName("address")
	private String address;

	@SerializedName("fontOfSign")
	private String fontOfSign;

	@SerializedName("registrationNumber")
	private String registrationNumber;

	@SerializedName("name")
	private String name;

	@SerializedName("specialization")
	private String specialization;

	@SerializedName("emailId")
	private String emailId;

	@SerializedName("textOfSign")
	private String textOfSign;

	public void setWhatsapp(String whatsapp){
		this.whatsapp = whatsapp;
	}

	public String getWhatsapp(){
		return whatsapp;
	}

	public void setQualification(String qualification){
		this.qualification = qualification;
	}

	public String getQualification(){
		return qualification;
	}

	public void setPhoneNumber(String phoneNumber){
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber(){
		return phoneNumber;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public String getAddress(){
		return address;
	}

	public void setFontOfSign(String fontOfSign){
		this.fontOfSign = fontOfSign;
	}

	public String getFontOfSign(){
		return fontOfSign;
	}

	public void setRegistrationNumber(String registrationNumber){
		this.registrationNumber = registrationNumber;
	}

	public String getRegistrationNumber(){
		return registrationNumber;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setSpecialization(String specialization){
		this.specialization = specialization;
	}

	public String getSpecialization(){
		return specialization;
	}

	public void setEmailId(String emailId){
		this.emailId = emailId;
	}

	public String getEmailId(){
		return emailId;
	}

	public void setTextOfSign(String textOfSign){
		this.textOfSign = textOfSign;
	}

	public String getTextOfSign(){
		return textOfSign;
	}

	@Override
 	public String toString(){
		return 
			"ClsDoctorDetails{" + 
			"whatsapp = '" + whatsapp + '\'' + 
			",qualification = '" + qualification + '\'' + 
			",phoneNumber = '" + phoneNumber + '\'' + 
			",address = '" + address + '\'' + 
			",fontOfSign = '" + fontOfSign + '\'' + 
			",registrationNumber = '" + registrationNumber + '\'' + 
			",name = '" + name + '\'' + 
			",specialization = '" + specialization + '\'' + 
			",emailId = '" + emailId + '\'' + 
			",textOfSign = '" + textOfSign + '\'' + 
			"}";
		}
}