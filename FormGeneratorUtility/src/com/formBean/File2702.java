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
@Table(name="file2702") 
public class File2702 implements Serializable {
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
@javax.validation.constraints.NotNull(message="code can not be null")
@org.hibernate.validator.constraints.Range(min=1,message="code can not be null") @Column(name="code")
 private Integer code ; 
 
public Integer getCode() { 
 return code; 
} 
public void setCode(Integer code) { 
this.code = code; 
} 

 @Column(name = "file2") 
private String file2 ;public String getFile2() { 
if(file2 == null || file2.equals("")) { return "File2"; } return "<a href='#' onclick=\"downloadFile('"+file2+"')\">File2</a>";
 }
public void setFile2(String file2) { 
	this.file2 = file2; 
}

 @Column(name = "file3") 
private String file3 ;public String getFile3() { 
if(file3 == null || file3.equals("")) { return "File3"; } return "<a href='#' onclick=\"downloadFile('"+file3+"')\">File3</a>";
 }
public void setFile3(String file3) { 
	this.file3 = file3; 
}

 @Column(name = "file1") 
private String file1 ;public String getFile1() { 
if(file1 == null || file1.equals("")) { return "File1"; } return "<a href='#' onclick=\"downloadFile('"+file1+"')\">File1</a>";
 }
public void setFile1(String file1) { 
	this.file1 = file1; 
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
  return  "-> ID : "+ID+"-> code : "+code+"-> file2 : "+file2+"-> file3 : "+file3+"-> file1 : "+file1+" "; 
	}
 }
