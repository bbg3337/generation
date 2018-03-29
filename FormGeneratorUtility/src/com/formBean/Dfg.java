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
@Table(name="dfg") 
public class Dfg implements Serializable {
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
@javax.validation.constraints.NotNull(message="dfg can not be null")
 @Column(name="dfg")
Integer dfg ; 
 
public Integer getDfg() { 
 return dfg; 
} 
public void setDfg(Integer dfg) { 
this.dfg = dfg; 
} 
@javax.validation.constraints.NotNull(message="rtyrty can not be null")
@RangeValidation(endDate="2018-01-04 13:59" ,startDate="2018-01-18 13:59", message = "dtgtr must be in range (2018-01-04 13:59 to 2018-01-18 13:59 )")
 @Column(name="rtyrty")
java.sql.Date rtyrty ; 
 
public java.sql.Date getRtyrty() { 
 return rtyrty; 
} 
public void setRtyrty(java.sql.Date rtyrty) { 
this.rtyrty = rtyrty; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> dfg : "+dfg+"-> rtyrty : "+rtyrty+" "; 
	}
 }
