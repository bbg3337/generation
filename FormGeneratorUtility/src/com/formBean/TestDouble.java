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
@Table(name="testDouble") 
public class TestDouble implements Serializable {
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
@javax.validation.constraints.NotNull(message="test can not be null")
@org.hibernate.validator.constraints.Range(min=1,message="test can not be null") @Column(name="test")
Double test ; 
 
public Double getTest() { 
 return test; 
} 
public void setTest(Double test) { 
this.test = test; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> test : "+test+" "; 
	}
 }
