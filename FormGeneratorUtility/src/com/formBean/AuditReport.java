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
@Table(name="AuditReport") 
public class AuditReport implements Serializable {
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
 @Column(name="todate")
 private java.sql.Date todate ; 
 
public java.sql.Date getTodate() { 
 return todate; 
} 
public void setTodate(java.sql.Date todate) { 
this.todate = todate; 
} 
 @Column(name="action")
 private String action ; 
 
public String getAction() { 
 return action; 
} 
public void setAction(String action) { 
this.action = action; 
} 
 @Column(name="fromdate")
 private java.sql.Date fromdate ; 
 
public java.sql.Date getFromdate() { 
 return fromdate; 
} 
public void setFromdate(java.sql.Date fromdate) { 
this.fromdate = fromdate; 
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
  return  "-> ID : "+ID+"-> todate : "+todate+"-> action : "+action+"-> fromdate : "+fromdate+" "; 
	}
 }
