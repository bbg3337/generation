package com.formBean;  
import java.io.Serializable; 
import javax.persistence.Column;
 import javax.persistence.Entity; 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType; 
 import javax.persistence.Id; 
 import javax.validation.constraints.Pattern; 
 import javax.persistence.Table; 
 import com.sttl.hibernate.custom.validation.RangeValidation; 
 @Entity 
@Table(name="empTemp") 
public class EmpTemp implements Serializable {
	@Id  
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name="ID")
Integer ID ; 
 
public Integer getID() { 
 return ID; 
} 
public void setID(Integer ID) { 
this.ID = ID; 
} 
@javax.validation.constraints.NotNull(message="code can not be null")@Pattern(regexp="^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$",message="Enter proper code.")
 @Column(name="code")
String code ; 
 
public String getCode() { 
 return code; 
} 
public void setCode(String code) { 
this.code = code; 
} 
@javax.validation.constraints.NotNull(message="gender can not be null")@Pattern(regexp="(.*)",message="Enter proper gender.")
 @Column(name="gender")
String gender ; 
 
public String getGender() { 
 return gender; 
} 
public void setGender(String gender) { 
this.gender = gender; 
} 
@javax.validation.constraints.NotNull(message="project can not be null")
 @Column(name="projcet")
String projcet ; 
 
public String getProjcet() { 
 return projcet; 
} 
public void setProjcet(String projcet) { 
this.projcet = projcet; 
} 
@javax.validation.constraints.NotNull(message="dob can not be null")
@javax.validation.constraints.Past(message="dob date must be in past") 

 @Column(name="dob")
java.sql.Date dob ; 
 
public java.sql.Date getDob() { 
 return dob; 
} 
public void setDob(java.sql.Date dob) { 
this.dob = dob; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> code : "+code+"-> gender : "+gender+"-> projcet : "+projcet+"-> dob : "+dob+" "; 
	}
 }
