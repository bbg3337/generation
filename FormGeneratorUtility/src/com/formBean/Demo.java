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
 import javax.persistence.Transient; 
@Entity 
@Table(name="demo") 
public class Demo implements Serializable {
	@Id  
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name="ID")
 private Integer ID ; 
 
public Integer getID() { 
 return ID; 
} 
public void setID(Integer ID) { 
this.ID = ID; 
} 
@javax.validation.constraints.NotNull(message="Code No can not be null")
@org.hibernate.validator.constraints.Range(min=1,message="Code No can not be null") @Column(name="code")
 private Integer code ; 
 
public Integer getCode() { 
 return code; 
} 
public void setCode(Integer code) { 
this.code = code; 
} 
@javax.validation.constraints.NotNull(message="Gender can not be null")
@org.hibernate.validator.constraints.Length(max=6, message ="Gender Data must be in Proper length.")
 @Column(name="gender")
 private String gender ; 
 
public String getGender() { 
 return gender; 
} 
public void setGender(String gender) { 
this.gender = gender; 
} 
@javax.validation.constraints.NotNull(message="Hobby can not be null")
@org.hibernate.validator.constraints.Length(max=46, message ="Hobby Data must be in Proper length.")
 @Column(name="hobbies")
 private String hobbies ; 
 
public String getHobbies() { 
 return hobbies; 
} 
public void setHobbies(String hobbies) { 
this.hobbies = hobbies; 
} 
@javax.validation.constraints.NotNull(message="dob can not be null")

@javax.validation.constraints.Past(message="date of birth date must be in past") 
 @Column(name="dob")
 private java.sql.Date dob ; 
 
public java.sql.Date getDob() { 
 return dob; 
} 
public void setDob(java.sql.Date dob) { 
this.dob = dob; 
} 
@javax.validation.constraints.NotNull(message="Numbers can not be null")
@org.hibernate.validator.constraints.Length(max=5, message ="Numbers Data must be in Proper length.")
@Pattern(regexp="^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$",message="Enter proper Numbers.") @Column(name="num")
 private String num ; 
 
public String getNum() { 
 return num; 
} 
public void setNum(String num) { 
this.num = num; 
} 
@javax.validation.constraints.NotNull(message="Name can not be null")
@org.hibernate.validator.constraints.Length(max=25, message ="Name Data must be in Proper length.")
@Pattern(regexp="^[A-Za-z]{0,}$",message="Enter proper Name.") @Column(name="name")
 private String name ; 
 
public String getName() { 
 return name; 
} 
public void setName(String name) { 
this.name = name; 
} 
 @Column(name="pan")
 private String pan ; 
 
public String getPan() { 
 return pan; 
} 
public void setPan(String pan) { 
this.pan = pan; 
} 
@javax.validation.constraints.NotNull(message="pwd can not be null")@Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[@#$%^&+=].*[@#$%^&+=])(?=\\S+$).{8,}",message="Enter proper pwd.")
 @Column(name="pwd")
 private String pwd ; 
 
public String getPwd() { 
 return pwd; 
} 
public void setPwd(String pwd) { 
this.pwd = pwd; 
} 

 @Transient 
private String edit ;public String getEdit() { 
return "<a href='#' onclick=\"editRow('"+ID+"')\" >Edit</a>";
 }
public void setEdit(String edit) { 
	this.edit = edit; 
}

 @Transient 
private String delete ;public String getDelete() { 
return "<a href='#' onclick=\"deleteRow('"+ID+"')\" >Delete</a>";
 }
public void setDelete(String edit) { 
	this.edit = edit; 
}
	@Override 
	public String toString() { 
  return  "-> ID : "+ID+"-> code : "+code+"-> gender : "+gender+"-> hobbies : "+hobbies+"-> dob : "+dob+"-> num : "+num+"-> name : "+name+"-> pan : "+pan+"-> pwd : "+pwd+" "; 
	}
 }
