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
@Table(name="TblOprXyz") 
public class TblOprXyz implements Serializable {
	@Id  
	@GeneratedValue(strategy = GenerationType.IDENTITY)
@javax.validation.constraints.NotNull(message="ID can not be null")
 @Column(name="ID")
Integer ID ; 
 
public Integer getID() { 
 return ID; 
} 
public void setID(Integer ID) { 
this.ID = ID; 
} 
@javax.validation.constraints.NotNull(message="Table Name can not be null")@Pattern(regexp="^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$",message="Enter proper Table Name.")
 @Column(name="tbname")
String tbname ; 
 
public String getTbname() { 
 return tbname; 
} 
public void setTbname(String tbname) { 
this.tbname = tbname; 
} 
@javax.validation.constraints.NotNull(message="Table Id can not be null")
 @Column(name="tbid")
Integer tbid ; 
 
public Integer getTbid() { 
 return tbid; 
} 
public void setTbid(Integer tbid) { 
this.tbid = tbid; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> tbname : "+tbname+"-> tbid : "+tbid+" "; 
	}
 }
