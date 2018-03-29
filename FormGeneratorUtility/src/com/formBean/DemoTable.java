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
@Table(name="demoTable") 
public class DemoTable implements Serializable {
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
@javax.validation.constraints.NotNull(message="checkin can not be null")
@RangeValidation(endDate="2018-01-01 17:08" ,startDate="2018-01-31 17:08", message = "check in must be in range (2018-01-01 17:08 to 2018-01-31 17:08 )")
 @Column(name="checkin")
java.sql.Timestamp checkin ; 
 
public java.sql.Timestamp getCheckin() { 
 return checkin; 
} 
public void setCheckin(java.sql.Timestamp checkin) { 
this.checkin = checkin; 
} 
@javax.validation.constraints.NotNull(message="Gender can not be null")@Pattern(regexp="(.*)",message="Enter proper Gender.")
 @Column(name="gender")
String gender ; 
 
public String getGender() { 
 return gender; 
} 
public void setGender(String gender) { 
this.gender = gender; 
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
@javax.validation.constraints.NotNull(message="pwd can not be null")@Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[@#$%^&+=].*[@#$%^&+=])(?=\\S+$).{8,}",message="Enter proper pwd.")
 @Column(name="pwd")
String pwd ; 
 
public String getPwd() { 
 return pwd; 
} 
public void setPwd(String pwd) { 
this.pwd = pwd; 
} 
@javax.validation.constraints.NotNull(message="address can not be null")@Pattern(regexp="^[A-Za-z0-9,/-]{1,}$",message="Enter proper address.")
 @Column(name="adr")
String adr ; 
 
public String getAdr() { 
 return adr; 
} 
public void setAdr(String adr) { 
this.adr = adr; 
} 
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> code : "+code+"-> checkin : "+checkin+"-> gender : "+gender+"-> dob : "+dob+"-> pwd : "+pwd+"-> adr : "+adr+" "; 
	}
 }
