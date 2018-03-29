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
@Table(name="qqqqq") 
public class Qqqqq implements Serializable {
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
@javax.validation.constraints.NotNull(message="qqq can not be null")
 @Column(name="qqq")
Integer qqq ; 
 
public Integer getQqq() { 
 return qqq; 
} 
public void setQqq(Integer qqq) { 
this.qqq = qqq; 
} 
@javax.validation.constraints.NotNull(message="dd can not be null")@Pattern(regexp="^[A-Za-z]{0,}$",message="Enter proper dd.")
 @Column(name="ddd")
String ddd ; 
 
public String getDdd() { 
 return ddd; 
} 
public void setDdd(String ddd) { 
this.ddd = ddd; 
} 
@javax.validation.constraints.NotNull(message="dddd can not be null")
@RangeValidation(endDate="2018-01-09 12:56" ,startDate="2018-01-23 12:56", message = "dddd must be in range (2018-01-09 12:56 to 2018-01-23 12:56 )")
 @Column(name="dddd")
java.sql.Date dddd ; 
 
public java.sql.Date getDddd() { 
 return dddd; 
} 
public void setDddd(java.sql.Date dddd) { 
this.dddd = dddd; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> qqq : "+qqq+"-> ddd : "+ddd+"-> dddd : "+dddd+" "; 
	}
 }
