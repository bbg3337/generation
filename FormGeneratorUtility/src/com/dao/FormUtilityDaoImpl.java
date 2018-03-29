package com.dao;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.bean.AuditLogView;
import com.bean.FileUpload;
import com.bean.HtmlElement;
import com.bean.PageMapping;
import com.bean.RegexPattern;
import com.bean.TblFormSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sttl.crypto.CryptoException;
import com.sttl.crypto.CryptoUtils;
import com.sttl.rest.model.JQueryDataTableParamModel;
import com.sttl.rest.util.CommonImpl;
import com.util.common.CommonUtility;
import com.util.hibernate.operation.Operation;
import com.util.jdbc.operation.JDBCOperation;
import com.util.jdbc.util.JDBCUtil;

@Repository("formUtilityDao")
public class FormUtilityDaoImpl implements FormUtilityDao {

	@Autowired
	JDBCUtil jdbcUtil;

	@Autowired
	CommonUtility commonUtility;

	@Autowired
	Operation hibernateOperation;
	
	private Logger logger = Logger.getLogger(FormUtilityDaoImpl.class);
	
	@Override
	public void genrateClass() {
		// TODO Auto-generated method stub
		System.out.println("hello");
	}

	@Override
	public boolean generateClassFromTable(String tableName, String buildPath,Map<String, TblFormSchema> tableFormSchemaMap) throws Exception {
		logger.info("FormUtilityDaoImpl.generateClassFromTable() start");
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		ResultSet resultSetOfPK = null;
		ResultSet resultSetForAutoIncrement = null;
		StringBuffer classFileData = new StringBuffer();
		StringBuffer toStringMethod = new StringBuffer();
		toStringMethod.append(" ");
		classFileData.append("package "+commonUtility.getValueFromPropertieFile("beanPackage")+";  \n" + "import java.io.Serializable; \n");
		classFileData.append("import javax.persistence.Column;\n" + " import javax.persistence.Entity; \n "
				+ "import javax.persistence.GeneratedValue;\n import javax.persistence.GenerationType; \n"
				+ " import javax.persistence.Id; \n"
				+ " import javax.validation.constraints.Pattern; \n"
				// + " import javax.persistence.NamedQuery; \n"
				+ " import javax.persistence.Table; \n " + "import com.sttl.hibernate.custom.validation.RangeValidation; \n import javax.persistence.Transient; \n" );

		classFileData.append("@Entity \n" + "@Table(name=\"" + tableName + "\") \n");
		classFileData.append("public class " + tableName.substring(0, 1).toUpperCase() + tableName.substring(1)
				+ " implements Serializable {\n");

		try {
			connection = jdbcUtil.getConnection();
			DatabaseMetaData dbmd;

			dbmd = connection.getMetaData();
			String[] types = { "TABLE" };
			resultSet = dbmd.getColumns(null, null, tableName, "%");

			// This code for get Primarykey Column
			resultSetOfPK = dbmd.getPrimaryKeys(null, null, tableName);
			Set<String> primaryKeys = new HashSet<>();
			while (resultSetOfPK.next()) {
				// System.out.println("Primary key id " + );
				primaryKeys.add(resultSetOfPK.getString(4));
			}
			statement = connection.createStatement();

			// This code for get AutoincrementColumn
			Set<String> autoIncrementColumn = new HashSet<>();
			resultSetForAutoIncrement = statement.executeQuery("SELECT * FROM " + tableName);
			ResultSetMetaData rsmd = resultSetForAutoIncrement.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String name = rsmd.getColumnName(i);
				if (rsmd.isAutoIncrement(i)) {
					autoIncrementColumn.add(name);
				}
			}

			// Create getter and setter for java file.
			while (resultSet.next()) {
				String fieldName = resultSet.getString(4);
				String fieldType = resultSet.getString(6);
				int typeCode = resultSet.getInt(5);
				String fieldSize = resultSet.getString(7);
				

				if (primaryKeys.contains(fieldName)) {
					classFileData.append("	@Id  \n");
				}
				if (autoIncrementColumn.contains(fieldName)) {
					/*classFileData
							.append("@GenericGenerator(name = \"increment\", strategy = \"org.hibernate.id.IncrementGenerator\") \n"
									+ "@GeneratedValue(generator = \"increment\") \n");*/
					
					classFileData.append("	@GeneratedValue(strategy = GenerationType.IDENTITY)\n");
				}
				System.out.println("field name :"+fieldName.toLowerCase());
				if( tableFormSchemaMap.get(fieldName.toLowerCase()) != null? tableFormSchemaMap.get(fieldName.toLowerCase()).getFieldController().equals("password") && tableFormSchemaMap.get(fieldName.toLowerCase()).getRequired().equals("yes"):false){
					
					String dataType = commonUtility.getSqlTypeName(typeCode);
					// fieldName = fieldName.substring(0, 1).toUpperCase() +
					// fieldName.substring(1);
					String fieldNameForMethod = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					String getterSetter = "@javax.validation.constraints.NotNull(message=\""+fieldName+" can not be null\")"
							/*+ "@org.hibernate.validator.Length(max="+fieldSize+", message =\""+fieldName+"Data must be in Proper length.\")\n"*/
						+ "@Pattern(regexp=\""+tableFormSchemaMap.get(fieldName).getRegex().replace("//","\\\\")+"\",message=\"Enter proper "+fieldName+".\")\n"
							+ " @Column(name=\"" + fieldName + "\")\n"
					// + " @Getter \n" + " @Setter \n" + " "
							+" private "+ dataType + " " + fieldName + " ; \n \n" + "public " + dataType + " get" + fieldNameForMethod
							+ "() { \n" + " return " + fieldName + "; \n" + "} \n" + "public void set" + fieldNameForMethod + "("
							+ dataType + " " + fieldName + ") { \n" + "this." + fieldName + " = " + fieldName + "; \n" + "} \n";
					classFileData.append(getterSetter);
					
				} else if (tableFormSchemaMap.get(fieldName.toLowerCase()) != null? tableFormSchemaMap.get(fieldName.toLowerCase()).getFieldController().equals("file") : false) {
					TblFormSchema tblFormSchema = tableFormSchemaMap.get(fieldName.toLowerCase());
					String fieldNameForMethod = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					if(tableFormSchemaMap.get(fieldName.toLowerCase()).getRequired().equalsIgnoreCase("yes")) {
						classFileData.append("@javax.validation.constraints.NotNull(message = \""+tableFormSchemaMap.get(fieldName.toLowerCase()).getFieldLabel()+" is required\")");
					}
					classFileData.append("\n @Column(name = \""+fieldName+"\") \n"
							+ "private String "+fieldName+" ;"
							+ "public String get"+fieldNameForMethod+"() { \n"
							+ "if("+fieldName+" == null || "+fieldName+".equals(\"\")) { return \""+fieldNameForMethod+"\"; } "
							+ "return \"<a href='#' onclick=\\\"downloadFile('\"+"+fieldName+"+\"')\\\">"+fieldNameForMethod+"</a>\";\n "
							+ "}\n"
							+ "public void set"+fieldNameForMethod+"(String "+fieldName+") { \n"
							+ "	this."+fieldName+" = "+fieldName+"; \n"
							+ "}\n");
				} else {
					classFileData.append(commonUtility.createGetterSatter(fieldType, fieldName, typeCode,fieldSize,tableFormSchemaMap.get(fieldName.toLowerCase())));
					//System.out.println("Regex : " + regexMapWithName.get(fieldName).replace("\\","\\\\"));
				}
				toStringMethod.append("\"-> "+fieldName+" : \"+"+fieldName +"+");
								/*
				 * System.out.println("Field Name: "+fieldName);
				 * System.out.println("Table catalog :" + fieldType);
				 * System.out.println("Table Schema :" + fieldSize);
				 * System.out.println("===================" +
				 * resultSet.getString(11));
				 */
			}
			//toStringMethod.append("); \n");
			toStringMethod.append("\" \"");
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				resultSet.close();
				resultSetForAutoIncrement.close();
				resultSetOfPK.close();
				statement.close();
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		classFileData.append("\n @Transient \n"
				+ "private String edit ;"
				+ "public String getEdit() { \n"
				+ "return \"<a href='#' onclick=\\\"editRow('\"+ID+\"')\\\" >Edit</a>\";\n "
				+ "}\n"
				+ "public void setEdit(String edit) { \n"
				+ "	this.edit = edit; \n"
				+ "}\n");
		classFileData.append("\n @Transient \n"
				+ "private String delete ;"
				+ "public String getDelete() { \n"
				+ "return \"<a href='#' onclick=\\\"deleteRow('\"+ID+\"')\\\" >Delete</a>\";\n "
				+ "}\n"
				+ "public void setDelete(String edit) { \n"
				+ "	this.edit = edit; \n"
				+ "}\n");
		
		classFileData.append("	@Override \n"
							+ "	public String toString() { \n"
							+ "" + " "
							//+ " return \"\" ; \n"
							+" return "+toStringMethod.toString()+"; \n"
							+ "	}\n" ) ;
		classFileData.append(" }");
		logger.info("class file data : " + classFileData.toString());
		System.out.println(classFileData.toString());
		String javaFileUri = commonUtility.getValueFromPropertieFile("javaFileLocation")
				+ tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
		if (!commonUtility.createFile(javaFileUri, classFileData.toString(), "java")) {
			
			return false;
		}
		//buildPath + "WEB-INF\\classes\\com\\bean\\"
		commonUtility.createFile(buildPath + "WEB-INF\\classes\\"+  commonUtility.getValueFromPropertieFile("beanFilePath") +tableName.substring(0, 1).toUpperCase() + tableName.substring(1), classFileData.toString(), "java");
		
		if (commonUtility.compileJavaFile(buildPath + "WEB-INF\\classes\\"+ commonUtility.getValueFromPropertieFile("beanFilePath")+tableName.substring(0, 1).toUpperCase() + tableName.substring(1) + ".java")) {
			logger.info("Class Compile on server is done");
			return true;
		}
	
		// compiler.run(null, null,
		// null,commonUtility.getValueFromPropertieFile("javaFileLocation")+tableName.substring(0,
		// 1).toUpperCase() + tableName.substring(1)+".java" );
		// System.out.println( System.getProperty("java.class.path"));
		// System.out.println("Final code is \n"+ classFileData.toString());
		logger.info("FormUtilityDaoImpl.generateClassFromTable() end");
		return false;
	}

	
	@Override
	public boolean insertData(Object dataObject, Class<?> cls) throws Exception {
		logger.info("FormUtilityDaoImpl.insertData() start");
		logger.info("FormUtilityDaoImpl.insertData() end");
		return hibernateOperation.insertData(dataObject, cls);
	}
	
	//Parth code.
	
	@Override
	public List<TblFormSchema> getTableFormSchemaData(int fk) throws Exception {
		logger.info("FormUtilityDaoImpl.getTableFormSchemaData() start");
		List<TblFormSchema> list = hibernateOperation.listDataWithWhreClause("TblFormSchema", "fk="+fk + " order by field_index");
		logger.info("FormUtilityDaoImpl.getTableFormSchemaData() end");
		return list;
	}
	
	@Override
	public void generateForm(List<TblFormSchema> list, PageMapping pageMapping, String compileLocation) throws Exception
	{
		logger.info("FormUtilityDaoImpl.generateForm() start");
		List<HtmlElement> htmlElements = hibernateOperation.listData("HtmlElement");
		List<RegexPattern> regexPatterns = hibernateOperation.listData("RegexPattern");
		
		StringBuilder htmlStringBuilder=new StringBuilder();
		StringBuilder listHeader = new StringBuilder();
		StringBuilder listField = new StringBuilder();
		StringBuilder alertString = new StringBuilder();
		StringBuilder datatableTable = new StringBuilder();
		StringBuilder datatableColumns = new StringBuilder();
		StringBuilder dialogForm = new StringBuilder();
		StringBuilder downlaodFunction = new StringBuilder();
		StringBuilder fileValidation = new StringBuilder();
		boolean formData = false;
		dialogForm.append("<div id=\"dialog"+pageMapping.getPageName()+"\" style=\"display: none\">");
		dialogForm.append("<form id=\"dialogform"+pageMapping.getPageName()+"\" class=\"form-horizontal\">");
		dialogForm.append("<input type=\"hidden\" id=\"formOption\" value=\"\"><input type=\"hidden\" id=\"ID\" value=\"\">");
		String scriptHTML = "";
		String scriptCheckFormData = " var returnType = true; var errMessage = \"\"; $(function(){ $('#submit"+pageMapping.getPageName()+"').click(function() {";
		String customJsRules = "options = { rules : { ";
		String customJsMessages = "},messages : { en : { ";
		String validateRules = "";
		String validateMessages = "";
		//datatableTable.append();
		datatableColumns.append("\"columns\":[");
		
		htmlStringBuilder.append("<%@taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>"
				+ "<%@include file=\"datatableLib.jsp\" %>"
				+ "<!DOCTYPE html ><html><head><title>"+pageMapping.getPageName()+"</title>");
		htmlStringBuilder.append("<link rel=\"stylesheet\" href=\"../css/bootstrap.min.css\">" +
			"<link rel=\"stylesheet\" href=\"../css/jquery.datetimepicker.min.css\">" +
			"<link rel=\"stylesheet\" href=\"../css/forms.css\">" +
			"<!-- <script src=\"../js/jquery-3.2.1.min.js\"></script> -->" +
			"<script src=\"../js/bootstrap.min.js\"></script>" +
			"<script src=\"../js/jquery.datetimepicker.full.js\"></script>" +
			"<script src=\"../js/formutility.js\"></script>"+
			"<script src=\"../js/custom_alert.js\"></script>" +
			"<script src=\"../js/bootstrap-show-password.min.js\"></script>" +
			"<script src=\"../js/js-form-validator.min.js\"></script>"
			+ "<script src=\"../js/jquery.serializejson.js\"></script>"
			+ "<script> "
			+ "$(document).ready(function(){"
			+ "    $('[data-toggle=\"popover\"]').popover(); "
			+ "    $('body').tooltip({ selector: '[rel=tooltip]' });"
			+ "}); "
			+ "</script>"
			+ "<style type=\"text/css\"> "
			+ "label[id*=\"-error\"] { "
			+ "color: red; } "
			+ "</style>");
		htmlStringBuilder.append("</head>");
		htmlStringBuilder.append("<body>");
		htmlStringBuilder.append("<a href='../index' class='glyphicon glyphicon-home btn-lg' style=' text-decoration:none; margin: 10px 20px; color: black;'></a>");
		htmlStringBuilder.append("<a  class='glyphicon glyphicon-question-sign btn-lg' href='#' title='Help' data-toggle='popover'  data-placement='left' data-trigger='focus' data-content='"+pageMapping.getFromContextHelp()+"' style=' text-decoration:none;  position:absolute;  top:0; right:0; margin: 10px 20px; color: black;'></a>");
		htmlStringBuilder.append("<div class=\"container\">");
		htmlStringBuilder.append("<center><h2>" +pageMapping.getPageName()+ "</h2></center>");
		htmlStringBuilder.append("<form action=\"../insertForm/"+pageMapping.getPageName()+"\" id=\"id"+pageMapping.getPageName()+"\" class=\"form-horizontal\" method=\"post\" enctype=\"multipart/form-data\" >");
		
		for(int i=0;i<list.size();i++)
		{
			String titleMessage ;
			TblFormSchema tblFormSchema = list.get(i);
			
			if(tblFormSchema == null){
				continue;
			}
			
			List<RegexPattern> listRegexPatterns = hibernateOperation.listData("RegexPattern");
			List<String> jsValidationList = commonUtility.getJsValidationName(listRegexPatterns, tblFormSchema.getRegex());
			List<String> validateList = commonUtility.getValidateList(tblFormSchema, listRegexPatterns);
			String requiredOrNot = tblFormSchema.getRequired().equalsIgnoreCase("yes") ? "required" : "";
			
			if(!(tblFormSchema.getFieldController().equals("alert") || tblFormSchema.getFieldController().equals("hyperlink"))){
				listHeader.append("<th>"+tblFormSchema.getFieldLabel()  + "</th>");
				listField.append("<td class=\"col-m-2\"><c:out value=\"${obj."+tblFormSchema.getFieldName().toLowerCase()+"}\" /></td>");	
			}
			
			if(!tblFormSchema.getFieldController().equals("alert"))
			{
				htmlStringBuilder.append("<div class=\"form-group\">");
				htmlStringBuilder.append("<label class=\"control-label col-sm-4\">"+tblFormSchema.getFieldLabel() + "</label>");
				htmlStringBuilder.append("<div class=\"col-sm-5\">");
				
				dialogForm.append("<div class=\"form-group\">");
				dialogForm.append("<label class=\"control-label col-sm-4\">"+tblFormSchema.getFieldLabel() + "</label>");
				dialogForm.append("<div class=\"col-sm-5\">");
				
				if(!tblFormSchema.getFieldController().equals("hyperlink"))
				{
					datatableTable.append("<th>"+tblFormSchema.getFieldLabel()+"</th>");
					datatableColumns.append("{\"data\" : \""+tblFormSchema.getFieldName()+"\"},");
				}
				
			}
			//patternId = commonUtility.getRegexPatternId(tblFormSchema.getFieldType());
			/*if(tblFormSchema.getFieldController().equals("password")){
				patternId = commonUtility.getRegexPatternId("password");
			}*/
			titleMessage = commonUtility.getRegexPatternTitle(regexPatterns,tblFormSchema.getRegex());
			
			if(tblFormSchema.getFieldController().equals("textbox"))
			{
				System.out.println("Size of js validation"  + jsValidationList.size());
				htmlStringBuilder.append(htmlElements.get(0).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ " data-rule=\""+requiredOrNot+"|"+(jsValidationList.size()!=0? jsValidationList.get(0):"")+"\" />");
				
				dialogForm.append(htmlElements.get(0).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", "")
						+ " />");
				
				customJsRules += (jsValidationList.size()!=0? jsValidationList.get(1):"");
				customJsMessages += (jsValidationList.size()!=0? jsValidationList.get(2):"");
				
				validateRules += validateList.get(0);
				validateMessages += validateList.get(1);

			}
			else if(tblFormSchema.getFieldController().equals("password"))
			{
				htmlStringBuilder.append(htmlElements.get(6).getElementName()
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ " data-rule=\""+requiredOrNot+"|"+jsValidationList.get(0)+"\" />");
				
				dialogForm.append(htmlElements.get(6).getElementName()
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", "")
						+ " />");
				
				htmlStringBuilder.append("<input type=\"checkbox\" onclick=\"showPassword('"+tblFormSchema.getFieldName()+"')\" >Show Password");
				dialogForm.append("<input type=\"checkbox\" onclick=\"showPassword('df_"+tblFormSchema.getFieldName()+"')\" >Show Password");
				dialogForm.append("<label id=\"df_"+tblFormSchema.getFieldName()+"-error\" class=\"error\" for=\"df_"+tblFormSchema.getFieldName()+"\"></label>");
				
				customJsRules += jsValidationList.get(1);
				customJsMessages += jsValidationList.get(2);
				
				validateRules += validateList.get(0);
				validateMessages += validateList.get(1);
				System.out.println("psw " + titleMessage );
			}
			else if(tblFormSchema.getFieldController().equals("number"))
			{
				if(tblFormSchema.getFieldType().equalsIgnoreCase("int")){
					htmlStringBuilder.append(htmlElements.get(8).getElementName()
							.replace(":id", tblFormSchema.getFieldName())
							.replace(":name", tblFormSchema.getFieldName())
							.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
							.replace(":step","1")
							.replace(":title", tblFormSchema.getTooltip())
							+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
							+ " data-rule=\""+requiredOrNot+"|"+jsValidationList.get(0)+"\" />");
					
					dialogForm.append(htmlElements.get(8).getElementName()
							.replace(":id", "df_"+tblFormSchema.getFieldName())
							.replace(":name", tblFormSchema.getFieldName())
							.replace(":step","1")
							.replace(":title", "")
							+ " />");
				}else{
					htmlStringBuilder.append(htmlElements.get(8).getElementName()
							.replace(":id", tblFormSchema.getFieldName())
							.replace(":name", tblFormSchema.getFieldName())
							.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
							.replace(":step","0.01")
							.replace(":title", tblFormSchema.getTooltip())
							+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
							+ " data-rule=\""+requiredOrNot+"|"+jsValidationList.get(0)+"\" />");
					
					dialogForm.append(htmlElements.get(8).getElementName()
							.replace(":id", "df_"+tblFormSchema.getFieldName())
							.replace(":name", tblFormSchema.getFieldName())
							.replace(":step","0.01")
							.replace(":title", "")
							+ " />");
				}
				customJsRules += jsValidationList.get(1);
				customJsMessages += jsValidationList.get(2);
				
				validateRules += validateList.get(0);
				validateMessages += validateList.get(1);
			}
			else if(tblFormSchema.getFieldController().equals("textarea"))
			{
				htmlStringBuilder.append(htmlElements.get(1).getElementName()
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ " data-rule=\""+requiredOrNot+"|"+jsValidationList.get(0)+"\" ></textarea>");
				
				dialogForm.append(htmlElements.get(1).getElementName()
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", "")
						+ " ></textarea>");
				
				customJsRules += jsValidationList.get(1);
				customJsMessages += jsValidationList.get(2);
				
				validateRules += validateList.get(0);
				validateMessages += validateList.get(1);
			}
			else if(tblFormSchema.getFieldController().equals("radio"))
			{
				int idCnt = 1;
				String fldValSplit[] = tblFormSchema.getFieldValue().split(",");
				System.out.println("--- " + Arrays.toString(fldValSplit));
				htmlStringBuilder.append(" <div class='row'>");
				dialogForm.append(" <div class='row'>");
				for(int j=0;j<fldValSplit.length;j++)
				{
					System.out.println("'"+fldValSplit[j].trim()+"'");
					if( (!fldValSplit[j].trim().equals("")) && fldValSplit[j].trim() != null )
					{
						htmlStringBuilder.append(" <div class='col-sm-6'>");
						htmlStringBuilder.append(htmlElements.get(2).getElementName()
								.replace(":id", tblFormSchema.getFieldName())
								.replace(":name", tblFormSchema.getFieldName())
								.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
								.replace(":title", tblFormSchema.getTooltip())
								+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
								+ " value=\""+fldValSplit[j].trim()+"\" "
								+ "" + (requiredOrNot.trim().equals("")? "" : " data-rule=\""+requiredOrNot+"\" " )
								+ " />" + fldValSplit[j].trim() +"<br />");
						htmlStringBuilder.append("</div>");
						
						dialogForm.append(" <div class='col-sm-6'>");
						dialogForm.append(htmlElements.get(2).getElementName()
								.replace(":id", "df_"+tblFormSchema.getFieldName())
								.replace(":name", tblFormSchema.getFieldName())
								.replace(":title", "")
								+ " value=\""+fldValSplit[j].trim()+"\" "
								+ " />" + fldValSplit[j].trim() +"<br />");
						dialogForm.append("</div>");
					}
				}
				htmlStringBuilder.append("</div>");
				dialogForm.append("</div>");
				dialogForm.append("<label id=\""+tblFormSchema.getFieldName()+"-error\" class=\"error\" for=\""+tblFormSchema.getFieldName()+"\"></label>");
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					validateRules += tblFormSchema.getFieldName()+":{ required: true },";
					validateMessages += tblFormSchema.getFieldName()+":{ required: \"Please select "+tblFormSchema.getFieldLabel() + "\" },";
				}
			}
			else if(tblFormSchema.getFieldController().equals("checkBox"))
			{
				int idCnt = 1;
				String fldValSplit[] = tblFormSchema.getFieldValue().split(",");
				htmlStringBuilder.append(" <div class='row'>");
				dialogForm.append(" <div class='row'>");
				for(int j=0;j<fldValSplit.length;j++)
				{
					if( (!fldValSplit[j].trim().equals("")) && fldValSplit[j].trim() != null )
					{
						htmlStringBuilder.append(" <div class='col-sm-6'>");
						htmlStringBuilder.append(htmlElements.get(3).getElementName()
								.replace(":id", tblFormSchema.getFieldName()+ (idCnt++))
								.replace(":name", tblFormSchema.getFieldName())
								.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
								.replace(":title", tblFormSchema.getTooltip())
								+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
								+ " value=\""+fldValSplit[j].trim()+"\" /> " + fldValSplit[j].trim() +"<br />");
						htmlStringBuilder.append("</div>");
						
						dialogForm.append(" <div class='col-sm-6'>");
						dialogForm.append(htmlElements.get(3).getElementName()
								.replace(":id", "df_"+tblFormSchema.getFieldName())
								.replace(":name", tblFormSchema.getFieldName()+"[]")
								.replace(":title", "")
								+ " value=\""+fldValSplit[j].trim()+"\" required /> " + fldValSplit[j].trim() +"<br />");
						dialogForm.append("</div>");
					}
				}
				htmlStringBuilder.append("</div>");
				dialogForm.append("</div>");
				dialogForm.append("<label id=\""+tblFormSchema.getFieldName()+"[]-error\" class=\"error\" for=\""+tblFormSchema.getFieldName()+"[]\" ></label>");
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					scriptCheckFormData += "if(!$('input[name="+tblFormSchema.getFieldName()+"]:checked').length > 0) {" +
							"document.getElementById(\"errMessage\").innerHTML = \""+tblFormSchema.getFieldLabel()+" can not be null\";" +
							"return false;" +
							"}";
					
					validateRules += tblFormSchema.getFieldName()+":{ required: true },";
					validateMessages += tblFormSchema.getFieldName()+":{ required: \"Please select atleast one "+tblFormSchema.getFieldLabel() + "\" },";
				}
			}
			
			else if(tblFormSchema.getFieldController().equals("comboBox"))
			{
				htmlStringBuilder.append(htmlElements.get(4).getElementName()
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ "" + (requiredOrNot.trim().equals("")? "" : " data-rule=\""+requiredOrNot+"\" " )
						+" >");
				htmlStringBuilder.append("<option value='' selected>Please select</option>");
				
				String fldValSplit[] = tblFormSchema.getFieldValue().split(",");
				for(int j=0;j<fldValSplit.length;j++)
				{
					if( (!fldValSplit[j].trim().equals("")) && fldValSplit[j].trim() != null )
					{
						htmlStringBuilder.append("<option value='"+fldValSplit[j].trim()+"'>"+fldValSplit[j].trim()+"</option>");
					}
				}
				htmlStringBuilder.append("</select>");
				
				dialogForm.append(htmlElements.get(4).getElementName()
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":title", "")
						+" >");
				dialogForm.append("<option value='' selected>Please select</option>");
				
				fldValSplit = tblFormSchema.getFieldValue().split(",");
				for(int j=0;j<fldValSplit.length;j++)
				{
					if( (!fldValSplit[j].trim().equals("")) && fldValSplit[j].trim() != null )
					{
						dialogForm.append("<option value='"+fldValSplit[j].trim()+"'>"+fldValSplit[j].trim()+"</option>");
					}
				}
				dialogForm.append("</select>");
				
				/*if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					customJsRules += tblFormSchema.getFieldName() + ":" + " function(a){ return (a.trim().toLowerCase() !== 'please select') },";
					customJsMessages += tblFormSchema.getFieldName() + ":" + "{ empty: \"Please select "+tblFormSchema.getFieldLabel()+"\", incorrect: \"Please select "+tblFormSchema.getFieldLabel()+"\" },";
				}*/
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					validateRules += tblFormSchema.getFieldName()+":{ required: true },";
					validateMessages += tblFormSchema.getFieldName() + ":{ required: \"" + tblFormSchema.getFieldLabel()+" is required\" },";
				}
				
			}
			
			else if(tblFormSchema.getFieldController().equalsIgnoreCase("date") && tblFormSchema.getFieldType().equalsIgnoreCase("DATE")){
				htmlStringBuilder.append(htmlElements.get(7).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ " data-rule=\""+requiredOrNot+"\" />");
				
				dialogForm.append(htmlElements.get(7).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":title", "")
						+ " />");
				
				//	"$('#"+tblFormSchema.getFieldName()+"') to "$('#df_"+tblFormSchema.getFieldName()+"')
				scriptHTML += "$('#df_"+tblFormSchema.getFieldName()+"').datetimepicker({" +
						"timepicker:false,";
				if(tblFormSchema.getDatePast().equals("true"))
				{
					scriptHTML += "maxDate:'+1970/01/01'," ;
				}
				if(tblFormSchema.getDateFuture().equals("true"))
				{
					scriptHTML += "minDate:'-1970/01/01'," ;
				}
				if(!tblFormSchema.getStartDate().equals(""))
				{
					try {
						scriptHTML += "defaultDate:'" + commonUtility.convertDateInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getStartDate()) + "'," +
								"minDate:'" + commonUtility.convertDateInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getStartDate()) + "'," +
								"maxDate:'" + commonUtility.convertDateInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getEndDate()) + "'," ;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(tblFormSchema.getDateFormat().equals(""))
				{
					scriptHTML += "format: 'Y-m-d',";
					scriptHTML += "formatDate : 'Y-m-d',";
				}
				else
				{
					scriptHTML += "format: '" + tblFormSchema.getDateFormat().replaceAll("H:i", "").trim() +"',";
					scriptHTML += "formatDate : '" + tblFormSchema.getDateFormat().replaceAll("H:i", "").trim() +"',";
				}
				scriptHTML += "});";
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					validateRules += tblFormSchema.getFieldName()+":{ required: true },";
					validateMessages += tblFormSchema.getFieldName() + ":{ required: \"" + tblFormSchema.getFieldLabel()+" is required\" },";
				}
			}
			
			else if(tblFormSchema.getFieldController().equals("date") && tblFormSchema.getFieldType().equals("DATETIME")){
				htmlStringBuilder.append(htmlElements.get(7).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ " data-rule=\""+requiredOrNot+"\" />");
				
				dialogForm.append(htmlElements.get(7).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":title", "")
						+ " />");
				
				//	"$('#"+tblFormSchema.getFieldName()+"') to "$('#df_"+tblFormSchema.getFieldName()+"')
				scriptHTML += "$('#df_"+tblFormSchema.getFieldName()+"').datetimepicker({";
				if(tblFormSchema.getDatePast().equals("true"))
				{
					scriptHTML += "maxDate:'+1970/01/01'," ;
				}
				if(tblFormSchema.getDateFuture().equals("true"))
				{
					scriptHTML += "minDate:'-1970/01/01'," ;
				}
				if(!tblFormSchema.getStartDate().equals(""))
				{
					try {
						scriptHTML += "defaultDate:'" + commonUtility.convertDateTimeInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getStartDate()) + "'," +
								"minDate:'" + commonUtility.convertDateTimeInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getStartDate()) + "'," +
								"maxDate:'" + commonUtility.convertDateTimeInDiffFormat(tblFormSchema.getDateFormat(), tblFormSchema.getEndDate()) + "'," ;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(tblFormSchema.getDateFormat().equals("")) {
					scriptHTML += "format: 'Y-m-d H:i',";
					scriptHTML += "formatDate: 'Y-m-d H:i',";
				} else {
					scriptHTML += "format: '" + tblFormSchema.getDateFormat() +"',";
					scriptHTML += "formatDate: '" + tblFormSchema.getDateFormat() +"',";
				}
				scriptHTML += "step : 1,";
				scriptHTML += "});";
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					validateRules += tblFormSchema.getFieldName()+":{ required: true },";
					validateMessages += tblFormSchema.getFieldName() + ":{ required: \"" + tblFormSchema.getFieldLabel()+" is required\" },";
				}
			}
			
			else if(tblFormSchema.getFieldController().equals("alert"))
			{
				String functionArgu = new String() ; 
				for(String data : tblFormSchema.getFieldValue().split(",")){
					functionArgu += ",'TYPE."+ data.trim().toLowerCase() +"','"+data.trim()+"'" ;
				}
				alertString.append("&nbsp <a class='btn btn-default' data-toggle='modal' title='"+tblFormSchema.getTooltip()+"' rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" data-target='#myModal' tabindex="+tblFormSchema.getFieldIndex()+" onclick=\"alertFuntion('"+tblFormSchema.getFieldLabel()+"','"+tblFormSchema.getFieldName().replace("_", " ")+"'"+functionArgu+") \">"+tblFormSchema.getFieldLabel()+"</a>");
				
				dialogForm.append("<div class=\"form-group\">");
				dialogForm.append("<label class=\"control-label col-sm-4\">"+tblFormSchema.getFieldLabel() + "</label>");
				dialogForm.append("<div class=\"col-sm-5\">");
				dialogForm.append("<a class='btn btn-default' data-toggle='modal' title='"+tblFormSchema.getTooltip()+"' rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" data-target='#myModal' tabindex="+tblFormSchema.getFieldIndex()+" onclick=\"alertFuntion('"+tblFormSchema.getFieldLabel()+"','"+tblFormSchema.getFieldName().replace("_", " ")+"'"+functionArgu+") \">"+tblFormSchema.getFieldLabel()+"</a>");
				dialogForm.append("</div>");
				dialogForm.append("</div>");
			}
			
			else if(tblFormSchema.getFieldController().equals("hyperlink"))
			{
				htmlStringBuilder.append("<a href='"+tblFormSchema.getFieldValue()+"' tabindex="+tblFormSchema.getFieldIndex()+" title='"+tblFormSchema.getTooltip()+"' rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" >"+ tblFormSchema.getFieldLabel() +"</a>");
				dialogForm.append("<a href='"+tblFormSchema.getFieldValue()+"' tabindex="+tblFormSchema.getFieldIndex()+" title='"+tblFormSchema.getTooltip()+"' rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" >"+ tblFormSchema.getFieldLabel() +"</a>");
			}
			
			else if(tblFormSchema.getFieldController().equals("file"))
			{
				formData = true;
				System.err.println("-" + tblFormSchema.getFieldValue());
				String accept = commonUtility.getAcceptedMimeType(tblFormSchema.getFieldValue());
				
				htmlStringBuilder.append(htmlElements.get(9).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", tblFormSchema.getFieldName())
						.replace(":tabindex", Integer.toString( tblFormSchema.getFieldIndex()))
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", tblFormSchema.getTooltip())
						+ " rel=\"tooltip\" data-toggle=\"tooltip\" data-placement=\"right\" "
						+ "onchange=\"checkFile('"+tblFormSchema.getFieldName()+"')\" "
						+ (tblFormSchema.getRegex().equals("single")? "":"multiple") + "  />");
				
				dialogForm.append(htmlElements.get(9).getElementName()
						.replace(":name", tblFormSchema.getFieldName())
						.replace(":id", "df_"+tblFormSchema.getFieldName())
						.replace(":maxlength", tblFormSchema.getSize()+"")
						.replace(":title", "")
						.replace(":accept", accept)
						+ (tblFormSchema.getRegex().equals("single")? "":"multiple")
						+ " />");
				
				if(downlaodFunction.length() == 0) {
					downlaodFunction.append("function downloadFile(names) { "
							+ "var val = confirm(\"Do you want to download file/files?\"); "
							+ "if(val == true) { var name = names.split(\"|\"); for(var i=0;i<name.length;i++){ download(name[i]); } } } ");
				}
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					scriptCheckFormData += "var control = document.getElementById(\""+tblFormSchema.getFieldName()+"\"); var files = control.files; "
							+ "if(files.length == 0) { document.getElementById(\"errMessage\").innerHTML = \"Please select file.\";	return false; } "
							+ "else if (errMessage != \"\") { document.getElementById(\"errMessage\").innerHTML = errMessage; return false;	}";
				}
				/*customJsRules += (jsValidationList.size()!=0? jsValidationList.get(1):"");
				customJsMessages += (jsValidationList.size()!=0? jsValidationList.get(2):"");*/
				
				if(tblFormSchema.getRequired().equalsIgnoreCase("yes")) {
					validateRules += tblFormSchema.getFieldName()+":{ required: true, accept: \""+accept+"\", filesize : "+(Long.parseLong(tblFormSchema.getSize()) * 1024)+", },";
					//validateMessages += tblFormSchema.getFieldName()+":{ required: \"Please select "+tblFormSchema.getFieldLabel() + "\" },";
				} else {
					validateRules += tblFormSchema.getFieldName()+":{ accept: \""+accept+"\", filesize : "+(Long.parseLong(tblFormSchema.getSize()) * 1024)+", },";
				}
			}
			
			if(!tblFormSchema.getFieldController().equals("alert")) {
				htmlStringBuilder.append("</div>");
				htmlStringBuilder.append("</div>");
				
				dialogForm.append("</div>");
				dialogForm.append("</div>");
			}
		}
		scriptCheckFormData += "document.getElementById(\"errMessage\").innerHTML = \"\"; return returnType; }); });";
		htmlStringBuilder.append("<div class=\"form-group\">");
		htmlStringBuilder.append("<div class=\"col-sm-offset-4 col-sm-5\">");
		htmlStringBuilder.append("<input type=\"submit\" class=\"btn btn-success\" id=\"submit"+pageMapping.getPageName()+"\" value=\"Submit\" />");
		//add search button
		htmlStringBuilder.append("<input type='button' class='btn Info' id='search' onclick='loadDataTable()'	value='Search' />");
		//end search button
		htmlStringBuilder.append(alertString);
		htmlStringBuilder.append("</div>");
		htmlStringBuilder.append("</div>");
		htmlStringBuilder.append("</form>"
				/*+ "<textarea class='form-control'>"+pageMapping.getFromContextHelp()+"</textarea>"*/
				+ "<font color='red'><p id=\"errMessage\"></p></font>"
				+ "<c:if test=\"${not empty message}\">"
				+ "<font color='green'><c:out value=\"${message }\"></c:out></font>"
				+"</c:if>");
		htmlStringBuilder.append("<c:if test=\"${not empty errMessage}\">"
				+ "<font color='red'> ${errMessage } </font>"
				+ "</c:if>"
				+ "</div>");
		/*htmlStringBuilder.append("<div class=\"container\" style=\"overflow-x:auto;\">"
				+ "<c:if test=\"${not empty listData}\" >"
				+ "	<table class=\"table\"> 	<thead style='color:#ccffe6;background-color:#87ceeb'>		<tr>"
				+ ""+listHeader.toString()
				+ "	</tr></thead>"
				+ "	<c:forEach items=\"${listData}\" var=\"obj\">"
				+ "	<tbody><tr>"
				+ ""+listField.toString()
				+ "	</tr></tbody>"
				+ "	</c:forEach>"
				+"</table>"
				+"</c:if>");*/
		htmlStringBuilder.append("<div class=\"container\" style=\"overflow-x:auto;\">"
				+ "<table id=\"table"+pageMapping.getPageName()+"\">"
				+ "<thead><tr>"
				+ datatableTable
				+ "<th>Edit</th><th>Delete</th>"
				+ "</tr></thead>"
				+ "<tfoot><tr>"
				+ datatableTable
				+ "<th>Edit</th><th>Delete</th>"
				+ "</tr></tfoot>"
				+ "</table>");
		if(htmlStringBuilder.toString().contains("#myModal")){
			htmlStringBuilder.append("<jsp:include page='alertForm.jsp'></jsp:include>");
		}
		htmlStringBuilder.append("</div>");
		dialogForm.append("</form>");
		dialogForm.append("</div>");
		htmlStringBuilder.append(dialogForm.toString());
		htmlStringBuilder.append("</body>");
		htmlStringBuilder.append("<script src=\"../js/formutility_date.js\"></script>");
		htmlStringBuilder.append("<script>");
		htmlStringBuilder.append(fileValidation);
		htmlStringBuilder.append(scriptCheckFormData);
		htmlStringBuilder.append("function checkFile(id) { checkFileType(id); }");
		htmlStringBuilder.append("</script>");
		htmlStringBuilder.append("<script>");
		htmlStringBuilder.append("new Validator(document.querySelector('#id"+pageMapping.getPageName()+"'), function (err, res) {" +
			"return res;" +
			"},"+customJsRules+customJsMessages+"}}});");
		htmlStringBuilder.append("</script>");
		htmlStringBuilder.append("<script>");
		htmlStringBuilder.append(scriptHTML);
		htmlStringBuilder.append("</script>");
		htmlStringBuilder.append("<script>"
				//Start Loaddatatable function
				+ "function loadDataTable(){"
					+ "if ($.fn.DataTable.isDataTable('#table"+pageMapping.getPageName()+"')) {"
					+ "		$('#table"+pageMapping.getPageName()+"').DataTable().clear().destroy();"
					+ "	}"
					+ "var oTable = $('#table"+pageMapping.getPageName()+"').dataTable({"
					+ " \"dom\" : 'Blfrtip', "
			        + " \"buttons\" : [ { text: 'Add', action: function ( e, dt, node, config ) { "
			        + "var validator = $('#dialogform"+pageMapping.getPageName()+"').validate(); "
					+ "document.getElementById('dialogform"+pageMapping.getPageName()+"').reset(); "
					+ "validator.resetForm(); "
					+ "$(\"#formOption\").val(\"add\"); "
			        + "$('#dialog"+pageMapping.getPageName()+"').dialog('open'); } } ], "
					+ datatableColumns 
					+ "{ \"data\" : \"edit\" }, { \"data\" : \"delete\"	},"
					+ "],"
					+ "\"columnDefs\": [ { orderable: false, targets: [-1,-2] } ],"
					+ " \"processing\" : true, "
					+ " \"serverSide\" : true, "
					+ " \"paging\": true, "
					+ " \"ajax\" : { "
					+ " \"contentType\": \"application/json\", "
					+ " \"data\": function ( d ) { " // AJAX data function
					+ "		d.formData = $('#id"+pageMapping.getPageName()+"').serializeArray();" //for form Serach 
					+ "		return JSON.stringify( d ); "
	           		+ "	  }, "
	           		+ " \"url\" : \"../listForDatatable/"+pageMapping.getPageName()+"\", "
	           		+ " \"dataSrc\" : \"data\", "
	           		+ " \"type\" : \"POST\" "
	           		+ "} } ); "
	           		//On Enter Data-table search work.
	           		+ "$('#table"+pageMapping.getPageName()+"_filter input').unbind();"
	           		+ "$('#table"+pageMapping.getPageName()+"_filter input').bind('keyup', function(e) { "
	           		+ " if(e.keyCode == 13) { "
	           		+ "   oTable.fnFilter(this.value); "
	           		+ "	} "
	           		+ "	else if(!this.value.trim()) { "
	           		+ "oTable.fnFilter(this.value); "
	           		+ "	}  });"
	           		+ ""
	           		+ ""
	           		//End onEnter
	           		//Add column search.
	           		+ "$('#table"+pageMapping.getPageName()+" tfoot th').each( function () { "
	           		+ "     var title = $(this).text(); "
	           		+ "		if(!(title.toUpperCase() == ('edit').toUpperCase() ) && !(title.toUpperCase() == ('delete').toUpperCase() )){"
	           		+ "			$(this).html( '<input type=\"text\" placeholder=\"Search '+title+'\" />' ); "
	           		+ " 	}"
	           		+ " } );  "
	           		
	           		+ "oTable.api().columns().every( function () { "
	           		+ " var that = this; "
	           		+ "   $( 'input', this.footer() ).on( 'keyup change', function (e) { "
	           		+ "	   if(e.keyCode == 13) { "
	           		+ "	     that.search( this.value).draw(); "
	           		+ "	   }"
	           		+ "		else if(!this.value.trim()) { "
	           		+ "		  /*fire when search box gets empty */"
	           		+ "		  that.search( this.value).draw();"
	           		+ "		}"
	           		+ "   }); "
	           		+ " });"
	           	+ "}"
           		//End column search.
	           	//end Loaddatatable function
           		+ "function editRow(id)	{ "
           		+ "var validator = $('#dialogform"+pageMapping.getPageName()+"').validate(); "
           		+ "document.getElementById('dialogform"+pageMapping.getPageName()+"').reset(); "
           		+ "validator.resetForm(); "
           		+ "$(\"#ID\").val(\"\"); "
           		+ "$(\"#formOption\").val(\"edit\"); "
           		+ "document.getElementById(\"errMessage\").innerHTML = \"\"; "
           		+ "$.ajax({ type : \"POST\", url : \"../restFatchDataFromID/"+pageMapping.getPageName()+"\", data : { \"id\" : id }, "
           		+ "cache : false, "
           		+ "success : function(result) { var jsonData = JSON.parse(result); "
				+ "if (jsonData['error']) { $.each(jsonData['error'],function(key,value) { "
				+ "document.getElementById(\"errMessage\").innerHTML += \"<font color='red'>\"+ value+ \"</font><br>\";}); } "
				+ "else { $.each(jsonData, function(key, value) { { if (key == 'ID') { $(\"#ID\").val(value); } "
				+ "if ($(\"#df_\" + key).is(':radio')) { $(\"input[id=df_\" + key + \"][value=\\\"\" + value + \"\\\"]\").prop('checked', true); } "
				+ "else if ($(\"#df_\" + key).is(':checkbox')) { "
				+ "if (value.indexOf(',') > -1) { var array = value.split(','); "
				+ "for(var i=0;i<array.length;i++) { $(\"input[id=df_\" + key + \"][value=\\\"\" + array[i] + \"\\\"]\").prop('checked', true); } } " 
				+ "else { $(\"input[id=df_\" + key + \"][value=\\\"\" + value + \"\\\"]\").prop('checked', true); } "
				+ " } "
				+ "else if ($(\"#df_\" + key).is(':file')) { } "
				+ "else { $(\"#df_\" + key).val(value); } } }); "
				+ "$('#dialog"+pageMapping.getPageName()+"').dialog('open'); } }, "
				+ "error : function(XMLHttpRequest,textStatus,errorThrown) { "
				+ "document.getElementById(\"errMessage\").innerHTML = \"<font color='red'>Problem in fatching data.</font>\"; } }); }"
		    	//Function of deleteRow.
				+ "function deleteRow(id) {	var value = confirm(\"Are you sure want to delete this record?\"); "
		    	+ "if(value == true) { "
		    	+ "document.getElementById(\"errMessage\").innerHTML = \"\";"
		    	+ "$.ajax({ type : \"POST\", url : \"../delete/"+pageMapping.getPageName()+"\", "
				+ "data: \"{\\\"ID\\\":\"+id+\"}\", "
				+ "contentType: \"application/json; charset=utf-8\", dataType: \"json\", cache: false, "
				+ "success : function(result) { "
				+ "if(result['error']) { $.each(result['error'], function(key, value) { document.getElementById(\"errMessage\").innerHTML += \"<font color='red'>\"+value+\"</font><br>\"; }); } "
				+ "else if(result['success']) { $.each(result['success'], function(key, value) { document.getElementById(\"errMessage\").innerHTML += \"<font color='green'>\"+value+\"</font><br>\"; }); oTable.api().rows().remove().draw(); oTable.api().draw(); } }, "
				+ "error: function(XMLHttpRequest, textStatus, errorThrown) { document.getElementById(\"errMessage\").innerHTML = \"<font color='red'>Record deletion failed.</font>\"; } }); "
				+ " /*$('#table"+pageMapping.getPageName()+"').DataTable().draw(); */  } }"
				//End deleteRow Function
				+ downlaodFunction
           		+ "</script>");
		//show only data-table without search
		htmlStringBuilder.append("<script>"
				+ "var oTable = $('#table"+pageMapping.getPageName()+"').dataTable({"
				+ " \"dom\" : 'Blfrtip', "
		        + " \"buttons\" : [ { text: 'Add', action: function ( e, dt, node, config ) { "
		        + "var validator = $('#dialogform"+pageMapping.getPageName()+"').validate(); "
				+ "document.getElementById('dialogform"+pageMapping.getPageName()+"').reset(); "
				+ "validator.resetForm(); "
				+ "$(\"#formOption\").val(\"add\"); "
		        + "$('#dialog"+pageMapping.getPageName()+"').dialog('open'); } } ], "
				+ datatableColumns 
				+ "{ \"data\" : \"edit\" }, { \"data\" : \"delete\"	},"
				+ "],"
				+ "\"columnDefs\": [ { orderable: false, targets: [-1,-2] } ],"
				+ " \"processing\" : true, "
				+ " \"serverSide\" : false, "
				+ " \"paging\": true, "
           		+ " } ); "
				+ "</script>");
		//end show only datatable without search
		htmlStringBuilder.append("<script>");
		htmlStringBuilder.append("$(function() { "
				+ "$('#dialog"+pageMapping.getPageName()+"').dialog({"
				+ "autoOpen : false,"
				+ "title:'"+pageMapping.getPageName()+"',"
				+ "height: 500,"
				+ "width: 550,"
				+ "buttons : {"
				+ " 'Send' : function() { "
				+ "if ($('#dialogform"+pageMapping.getPageName()+"').valid()) { "
				+ (formData==true? "var formData = new FormData($('#dialogform"+pageMapping.getPageName()+"')[0]);" : "")
				+ "var ajaxUrl = \"\"; "
				+ "var jsonData = JSON.stringify($('#dialogform"+pageMapping.getPageName()+"').serializeJSON()); "
				+ "if($(\"#formOption\").val() == \"add\") { ajaxUrl = \"../"+(formData==true? "insertWithFile/" : "insert/") +pageMapping.getPageName()+"\"; } "
				+ "else if ($(\"#formOption\").val() == \"edit\") { ajaxUrl = \"../"+(formData==true? "updateWithFile/" : "update/") +pageMapping.getPageName()+"\"; formData.append(\"ID\",$(\"#ID\").val()); jsonData = jsonData.substring(0, jsonData.length-1) + \",\\\"ID\\\":\\\"\"+$(\"#ID\").val()+\"\\\"}\"; } "
				+ "else { ajaxUrl = \"\"; } "
				+ "$('#dialog"+pageMapping.getPageName()+"').dialog('close');"
				+ "$.ajax({	"			
				+ "type : \"POST\", "
				+ "url : ajaxUrl, "
				+ "data: "+(formData==true? "formData":"jsonData")+","
				+ "contentType: \"application/json; charset=utf-8\", "
				+ "dataType: \"json\", "
				+ "cache: false, "
				+ (formData==true? "enctype: 'multipart/form-data', processData: false, contentType: false," : "")
				+ "success : function(result) { "
				+ "if(result['error']) { "
				+ "console.log(result['error']); "
				+ "$.each(result['error'], function(key, value) { "
				+ "console.log(value); "
				+ "document.getElementById(\"errMessage\").innerHTML = \"\"; "
				+ "document.getElementById(\"errMessage\").innerHTML += \"<font color='red'>\"+value+\"</font><br>\"; "
				+ "}); } else if(result['success']) { "
				+ "console.log(result['success']); "
				+ "$.each(result['success'], function(key, value) { "
				+ "console.log(value); "
				+ "document.getElementById(\"errMessage\").innerHTML = \"\"; "
				+ "document.getElementById(\"errMessage\").innerHTML += \"<font color='green'>\"+value+\"</font><br>\"; "
				+ "}); "
				+ " /*$('#table"+pageMapping.getPageName()+"').DataTable().draw();*/ "
				+ " /*oTable.ajax.reload();*/ "
				//reaload datatable
				+ " oTable.api().rows().remove().draw(); oTable.api().rows.add(result); oTable.api().draw(); "
				//end
				+ "} }, error: function(XMLHttpRequest, textStatus, errorThrown) { " 
				+ "if ($(\"#formOption\").val() == \"add\") { document.getElementById(\"errMessage\").innerHTML = \"<font color='red'>Record insertion failed.</font>\"; "
				+ "} else if ($(\"#formOption\").val() == \"edit\") { document.getElementById(\"errMessage\").innerHTML = \"<font color='red'>Record updation failed.</font>\"; } } }); "
				+ " } } } });");
		htmlStringBuilder.append("$('#dialogform"+pageMapping.getPageName()+"').validate({"
				+ "rules : {"
				+ validateRules + "}, "
				+ "messages: {"
				+ validateMessages
				+ "} }); }); ");
		htmlStringBuilder.append("</script>");
		htmlStringBuilder.append("</html>");
		
		try
		{
			commonUtility.createFile(compileLocation + pageMapping.getPageName(), htmlStringBuilder.toString(), "jsp");
			commonUtility.createFile(commonUtility.getValueFromPropertieFile("jspFileLocation") + pageMapping.getPageName(), htmlStringBuilder.toString(), "jsp");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		logger.info("form code : " + htmlStringBuilder.toString());
		System.out.println("form code : " + htmlStringBuilder.toString());
		logger.info("FormUtilityDaoImpl.generateForm() end");
	}

	@Override
	public int insertPageMappingData(PageMapping pageMapping) throws Exception {
		logger.info("FormUtilityDaoImpl.insertPageMappingData() start");
		try
		{
			logger.info("FormUtilityDaoImpl.insertPageMappingData() end");
			return hibernateOperation.insertDataAndReturnId(pageMapping);
		}
		catch(Exception e)
		{
			logger.error("insertPageMappingData failed(Exception) : " + e);
			e.printStackTrace();
		}
		logger.info("FormUtilityDaoImpl.insertPageMappingData() end");
		return 0;
	}

	/*Change by Bhadresh Bajariya on 29-12-2017
	 * Change method configuration because i have not need to call every time get listed data from DataBase so. 
	 * and also add this in classgeneration method for add regex validations.*/
	@Override
	public Map< String, TblFormSchema>  insertFieldData(String[] fld_ctrl,String[] fld_type, String[] fld_lbl, String[] fld_name, String[] fld_size, String[] fld_val,String[] fld_index, 
			String[] regex, String[] date_all, String[] date_past, String[] date_future, String[] start_date, String[] end_date, String[] date_format, int pageMappingId) throws Exception {
		logger.info("FormUtilityDaoImpl.insertFieldData() start");
		Map< String, TblFormSchema> tableFormSchemaMap = new HashMap<>();
		System.out.println("fld ctrl.length " + fld_ctrl.length);
		
		for(int i=0;i<fld_ctrl.length;i++)
		{
			TblFormSchema tblFormSchema = new TblFormSchema();
			tblFormSchema.setFk(pageMappingId);
			tblFormSchema.setFieldController(fld_ctrl[i]);
			tblFormSchema.setFieldLabel(fld_lbl[i]);
			tblFormSchema.setFieldName(fld_name[i] );
			tblFormSchema.setFieldType(fld_type[i]);
			tblFormSchema.setRegex(regex[i]);
			tblFormSchema.setFieldIndex(Integer.parseInt( fld_index[i]));
			tblFormSchema.setStartDate(start_date[i]);
			tblFormSchema.setEndDate(end_date[i]);
			tblFormSchema.setDateFormat(date_format[i]);
			tblFormSchema.setDateAll(date_all[i]);
			tblFormSchema.setDatePast(date_past[i]);
			tblFormSchema.setDateFuture(date_future[i]);
			
			if(fld_ctrl[i].equals("radio") ||  fld_ctrl[i].equals("comboBox"))
			{
				String fldValSplit[] = fld_val[i].split(",");
				int maxVal = fldValSplit[0].length();
				for(int j=1;j<fldValSplit.length;j++)
				{
					if(fldValSplit[j].trim().length() > maxVal)
					{
						maxVal = fldValSplit[j].trim().length();
					}
				}
				tblFormSchema.setSize(maxVal+"");
			}
			else if (fld_ctrl[i].equals("checkBox")) {
				tblFormSchema.setSize(fld_val[i].length()+"");
			}
			else if(fld_ctrl[i].equals("date") ){
				tblFormSchema.setSize("0");
			}
			else if(fld_ctrl[i].equals("alert") ){
				tblFormSchema.setSize("0");
				tblFormSchema.setFieldValue(fld_val[i]);
			}
			
			else
			{
				tblFormSchema.setSize(fld_size[i]);
			}
			
			if(fld_ctrl[i].equals("radio") || fld_ctrl[i].equals("checkBox") || fld_ctrl[i].equals("comboBox"))
			{
				tblFormSchema.setFieldValue(fld_val[i]);
			}
			tableFormSchemaMap.put(tblFormSchema.getFieldName().toLowerCase(), tblFormSchema);
			hibernateOperation.insertData(tblFormSchema);
			logger.info("tblFormSchema : " + tblFormSchema);
		}
		tableFormSchemaMap.put("id",null);
		logger.info("FormUtilityDaoImpl.insertFieldData() end");
		return tableFormSchemaMap;
	}

	@Override
	public boolean createTable(String pageName, String[] fld_ctrl, String[] fld_name, String[] fld_type,
			String[] fld_size, String[] fld_val) throws Exception {
		logger.info("FormUtilityDaoImpl.createTable() start");
		JDBCOperation operation = new JDBCOperation();
		
		String query = "CREATE TABLE "+pageName+" ( "
				+ " id INT PRIMARY KEY AUTO_INCREMENT, ";
		
		for(int i=0;i<fld_name.length;i++)
		{
			System.out.println("Controller " + fld_ctrl[i]);
			if(fld_ctrl[i].equals("alert") || fld_ctrl[i].equals("hyperlink")){
				if( i == fld_name.length-1)
				{
					query = query.substring(0,query.length()-3);
					//query += ", ";
				}
				continue;
			}
			if(fld_ctrl[i].equals("radio") || fld_ctrl[i].equals("comboBox"))
			{
				String fldValSplit[] = fld_val[i].split(",");
				int maxVal = fldValSplit[0].trim().length();
				for(int j=1;j<fldValSplit.length;j++)
				{
					if(fldValSplit[j].trim().length() > maxVal)
					{
						maxVal = fldValSplit[j].trim().length();
					}
				}
				query += " "+fld_name[i].toLowerCase() +" "+fld_type[i]+"("+maxVal+") ";
			}
			else if(fld_ctrl[i].equals("checkBox"))
			{
				query += " "+fld_name[i].toLowerCase() +" "+fld_type[i]+"("+fld_val[i].length()+") ";
			}else if( fld_ctrl[i].equals("date") || (fld_ctrl[i].equals("textbox") && fld_type[i].equals("DATE"))){
				query += " "+fld_name[i].toLowerCase()+" "+fld_type[i]+" ";
			}
			else
			{ 
				if( Arrays.stream(new String[]{ "FLOAT","DOUBLE","NUMERIC"}).parallel().anyMatch(fld_type[i]::contains)){
					if(fld_size[i].contains(",")){
						query += " "+fld_name[i].toLowerCase() +" "+fld_type[i]+"("+fld_size[i]+") ";
					}else{
						query += " "+fld_name[i].toLowerCase() +" "+fld_type[i]+" ";
					}
				}else{
					query += " "+fld_name[i].toLowerCase() +" "+fld_type[i]+"("+fld_size[i]+") ";
				}
				
			}
			if(i < fld_name.length-1)
			{
				query += ", ";
			}
		}
		query += ")";
		logger.info("Query : " + query);
		logger.info("FormUtilityDaoImpl.createTable() end");
		return operation.createTable(query);
	}
	
	@Override
	public PageMapping getPageMappingData(String pageMappingID) throws Exception {
		logger.info("FormUtilityDaoImpl.getPageMappingData() start");
		logger.info("FormUtilityDaoImpl.getPageMappingData() end");
		return (PageMapping) hibernateOperation.getDataById(PageMapping.class,Integer.parseInt( pageMappingID));
	}

	@Override
	public boolean updateTableSchema(String pageName, String[] fld_name, String[] fld_type, String[] fld_size,
			String[] fld_ctrl, String[] fld_val) throws Exception {
		logger.info("FormUtilityDaoImpl.updateTableSchema() start");
		JDBCOperation operation = new JDBCOperation();
		System.out.println("In create table");

		String query = "Alter TABLE " + pageName + "  " + "  ";

		query += updateTableQuery(pageName, fld_name, fld_type, fld_size, fld_ctrl, fld_val);
		query += "";
		logger.info("query : " + query);
		logger.info("FormUtilityDaoImpl.updateTableSchema() end");
		return operation.createTable(query);
	}
	
	
	public String updateTableQuery(String pageName, String[] fld_name, String[] fld_type, String[] fld_size,
			String[] fld_ctrl, String[] fld_val) throws Exception {
		logger.info("FormUtilityDaoImpl.updateTableQuery() start");
		Connection connection = null;
		ResultSet resultSet = null;
		StringBuilder alterQuery = new StringBuilder();
		System.out.println("fld name" + fld_name.length);
		System.out.println("fld_type" + fld_type.length);
		System.out.println("fld_size" + fld_size.length);
		System.out.println("fld_ctrl" + Arrays.toString(fld_ctrl));
		try {
			connection = jdbcUtil.getConnection();
			DatabaseMetaData dbmd;
			dbmd = connection.getMetaData();
			String[] types = { "TABLE" };
			resultSet = dbmd.getColumns(null, null, pageName, "%");
			List<String> existingField = new LinkedList<>();
			// Map<String, String> formField = new HashMap<>();
			//int cntRad = 0;
			while (resultSet.next()) {
				existingField.add(resultSet.getString(4));
			}

			for (int i = 0; i < fld_name.length; i++) {
				if (fld_ctrl[i].equals("alert") || fld_ctrl[i].equals("hyperlink")){
					continue;
				}
				
				if (fld_ctrl[i].equals("radio") || fld_ctrl[i].equals("comboBox")) {
					String fldValSplit[] = fld_val[i].split(",");
					int maxVal = fldValSplit[0].trim().length();

					for (int j = 1; j < fldValSplit.length; j++) {
						if (fldValSplit[j].trim().length() > maxVal) {
							maxVal = fldValSplit[j].trim().length();
						}
					}
					
					System.out.println("Field Value" + fld_val[i] + "Max val is " + maxVal);
					if (existingField.contains(fld_name[i].toLowerCase())) {
						alterQuery.append(
								" modify " + fld_name[i].toLowerCase() + " " + fld_type[i] + "(" + maxVal + ") ");
						existingField.remove(fld_name[i].toLowerCase());
					} else {
						alterQuery
								.append(" add " + fld_name[i].toLowerCase()  + " " + fld_type[i] + "(" + maxVal + ") ");
					}
				} else if (fld_ctrl[i].equals("checkBox")) {
					System.out.println("Check Box " + fld_val[i].length());
					if (existingField.contains(fld_name[i].toLowerCase())) {
						alterQuery.append(" modify " + fld_name[i].toLowerCase()  + " " + fld_type[i] + "("
								+ fld_val[i].length() + ") ");
						existingField.remove(fld_name[i].toLowerCase());
					} else {
						alterQuery.append(" add " + fld_name[i].toLowerCase()  + " " + fld_type[i] + "("
								+ fld_val[i].length() + ") ");
					}
				}else if(fld_ctrl[i].equalsIgnoreCase("date") || (fld_ctrl[i].equals("textbox") && fld_type[i].equals("DATE"))) {

					if (existingField.contains(fld_name[i].toLowerCase())) {
						alterQuery.append(
								" modify " + fld_name[i].toLowerCase() + " " + fld_type[i] + " ");
						existingField.remove(fld_name[i].toLowerCase());
					} else {
						alterQuery.append(
								" add " + fld_name[i].toLowerCase()  + " " + fld_type[i] + " ");
					}

				} else {

					if (existingField.contains(fld_name[i].toLowerCase())) {
						alterQuery.append(
								" modify " + fld_name[i].toLowerCase() + " " + fld_type[i] + "(" + fld_size[i] + ") ");
						existingField.remove(fld_name[i].toLowerCase());
					} else {
						alterQuery.append(
								" add " + fld_name[i].toLowerCase() + " " + fld_type[i] + "(" + fld_size[i] + ") ");
					}

				}
				alterQuery.append(", \n");

			}
			for (String remainField : existingField) {
				if (!remainField.equalsIgnoreCase("id")) {
					alterQuery.append(" drop " + remainField + " ");
					alterQuery.append(", \n");
				}

			}

		} catch (SQLException e) {
			logger.error("updateTableQuery failed(SQLException) : " + e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error("updateTableQuery failed(ClassNotFoundException) : " + e);
			e.printStackTrace();
		} finally {
			try {
				resultSet.close();
				connection.close();
				logger.info("resultSet and connection closed");
			} catch (SQLException e) {
				logger.error("updateTableQuery failed(SQLException) in finally() : " + e);
			}
		}
		logger.info("FormUtilityDaoImpl.updateTableQuery() end");
		return alterQuery.toString().substring(0, alterQuery.toString().length() - 3);
	}

	@Override
	public long deletePageTableFields(int pageId) throws Exception {
		logger.info("FormUtilityDaoImpl.deletePageTableFields() start");
		logger.info("FormUtilityDaoImpl.deletePageTableFields() end");
		return hibernateOperation.deleteData("TblFormSchema", " fk = "+pageId);
	}
	
	@Override
	public boolean dropForm(int pageMappingId, String pageMappingName, String compileLocation) throws Exception {
		logger.info("FormUtilityDaoImpl.dropForm() start");
			List<String> queries = new ArrayList<String>();
		queries.add("DELETE FROM page_mapping WHERE id="+pageMappingId);
		queries.add("DELETE FROM tbl_form_schema WHERE fk="+pageMappingId);
		queries.add("DROP TABLE "+pageMappingName);
		boolean status = false;
		try
		{
			//hibernateOperation.batchProcessing("DELETE FROM tbl_form_schema WHERE fk="+pageMappingId, tblFormSchema);
			status = hibernateOperation.batchProcessing(queries);
			
			if(commonUtility.deleteFile(commonUtility.getValueFromPropertieFile("jspFileLocation") + pageMappingName + ".jsp"))
			{
				logger.info("jsp file deleted");
				if(commonUtility.deleteFile(compileLocation + pageMappingName + ".jsp"))
				{
					logger.info("jsp file deleted from the build location");
					if(commonUtility.deleteFile(commonUtility.getValueFromPropertieFile("javaFileLocation") + pageMappingName + ".java"))
					{
						logger.info("java file delted");
						if(commonUtility.deleteFile(compileLocation + "WEB-INF\\classes\\"+commonUtility.getValueFromPropertieFile("javaFileLocation")+pageMappingName.substring(0, 1).toUpperCase() + pageMappingName.substring(1) + ".java"))
						{
							logger.info("java file delted from the build location");
							return true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("dropForm failed(Exception) " + e);
			e.printStackTrace();
		}
		logger.info("FormUtilityDaoImpl.dropForm() end");
		return status;
	}
	
	@Override
	public List<PageMapping> getPageMappingData() throws Exception {
		logger.info("FormUtilityDaoImpl.getPageMappingData() start");
		List<PageMapping> list  = hibernateOperation.listDataWithOrderBy("PageMapping", "id");
		logger.info(list.toString());
		logger.info("FormUtilityDaoImpl.getPageMappingData() end");
		return list;
	}

	@Override
	public List<TblFormSchema> getListTableFormSchema(String pageMappingId) throws Exception {
		logger.info("FormUtilityDaoImpl.getListTableFormSchema() start");
		logger.info("FormUtilityDaoImpl.getListTableFormSchema() end");
		return hibernateOperation.listDataWithWhreClause("TblFormSchema", "fk = " + Integer.parseInt(pageMappingId));
	}

	@Override
	public String generateHTMLFormTableString(List<TblFormSchema> listFormSchemas,List<RegexPattern> listRegexPatterns) throws Exception {
		logger.info("FormUtilityDaoImpl.generateHTMLFormTableString() start");
		StringBuilder htmlText= new StringBuilder();
		int counter = 1;
		for(TblFormSchema tblFormSchemaObject : listFormSchemas){
			System.out.println("Controller " + tblFormSchemaObject.getFieldController());
			htmlText.append("<tr id='tbl_tr"+counter+"'>"+
					
					commonUtility.getTDTegFromControlType(tblFormSchemaObject.getFieldController(),counter) +
					commonUtility.getTDTegFromDataType(tblFormSchemaObject.getFieldType(),tblFormSchemaObject.getFieldController()) 
					
					+ "<td><input type='text' class='form-control' data-rule='required|fieldLabel' name='fld_lbl' id='fld_lbl"+counter+"' value='"+tblFormSchemaObject.getFieldLabel()+"'  /></td>" );
					if(tblFormSchemaObject.getFieldValue() == null && (!tblFormSchemaObject.getFieldController().equals("hyperlink"))   ){
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldName' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"'  /></td>" );
						if(tblFormSchemaObject.getFieldController().equals("date")) {
							htmlText.append("<td><input type='text' class='form-control' name='fld_size' id='fld_size"+counter+"' readonly value='"+tblFormSchemaObject.getSize()+"' /></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'  value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
							htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
									"<option value='Please select'>Please select</option>" +
									"<option value='Date Validation' selected>Date Validation</option></select></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
							//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"DateModule")+"</select></td>");
							htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
							htmlText.append("<td><textarea rows='2' style='display:none;' cols='22' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
							htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
						} else if(tblFormSchemaObject.getFieldController().equals("textarea")) {
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldSize|between-1-400' name='fld_size' id='fld_size"+counter+"' value='"+tblFormSchemaObject.getSize()+"' /></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'  value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
							htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
									"<option value='Please select'>Please select</option>" +
									"<option value='TextModule' selected>Text Module</option>" +
									"<option value='Custom'>Custom</option></select></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
							//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"TextModule")+"</select></td>");
							htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
							htmlText.append("<td><textarea rows='2' style='display:none;' cols='22' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
							htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
						} else if(tblFormSchemaObject.getFieldController().equals("textbox")){
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldSize|between-1-250' name='fld_size' id='fld_size"+counter+"'  value='"+tblFormSchemaObject.getSize()+"'  /></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'  value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
							htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
									"<option value='Please select'>Please select</option>" +
									"<option value='TextModule' selected>Text Module</option>" +
									"<option value='Custom'>Custom</option></select></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
							//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"TextModule")+"</select></td>");
							htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
							htmlText.append("<td><textarea rows='2' style='display:none;' cols='22' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
							htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
						}
						else if(tblFormSchemaObject.getFieldController().equals("password")){
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldSize|between-1-250' name='fld_size' id='fld_size"+counter+"'  value='"+tblFormSchemaObject.getSize()+"'  /></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'  value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
							htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
									"<option value='Please select'>Please select</option>" +
									"<option value='PasswordModule' selected>Password Module</option>" +
									"<option value='Custom'>Custom</option></select></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
							//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"PasswordModule")+"</select></td>");
							htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
							htmlText.append("<td><textarea rows='2' style='display:none;' cols='22' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
							htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
						}
						else if(tblFormSchemaObject.getFieldController().equals("number")){
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldSize|between-1-250' name='fld_size' id='fld_size"+counter+"'  value='"+tblFormSchemaObject.getSize()+"'  /></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'  value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
							htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
									"<option value='Please select'>Please select</option>" +
									"<option value='NumberModule' selected>Number Module</option>" +
									"<option value='Custom'>Custom</option></select></td>");
							htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
							//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"NumberModule")+"</select></td>");
							htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
							htmlText.append("<td><textarea rows='2' style='display:none;' cols='22' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
							htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
							htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
									"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
						}
					}else if(tblFormSchemaObject.getFieldController().equals("file")){
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldName' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"'  /></td>" );
						htmlText.append("<td><input type='text' class='form-control' name='fld_size' id='fld_size"+counter+"' value='"+tblFormSchemaObject.getSize()+"' data-rule='required|fieldSize' placeholder='Please enter size in KiloByte' rel='tooltip' data-toggle='tooltip' title='Please enter size in KiloByte' /></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
								"<option value='Please select'>Please select</option>" +
								"<option value='FileModule' selected>File Module</option></select></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
						htmlText.append("<td> <input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control' name='fld_val' id='fld_val"+counter+"' style='display: none;' >"+tblFormSchemaObject.getFieldValue()+"</textarea>");
						htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
					}else if(tblFormSchemaObject.getFieldController().equals("alert")){
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_name' id='fld_name"+counter+"' rel='tooltip' data-toggle='tooltip' title='Alert Discription'   />"+tblFormSchemaObject.getFieldName()+"</textarea>");
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...' rel='tooltip' data-toggle='tooltip' title='Enter comma separated values here!'   >"+tblFormSchemaObject.getFieldValue()+"</textarea></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='regex' id='regex'><option value='No validation' selected>No validation</option></select></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
						htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><input type='hidden' class='form-control' name='fld_size' id='fld_size"+counter+"'>");
						htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
					}else if(tblFormSchemaObject.getFieldController().equals("hyperlink")){
						System.out.println("in hyperlink");
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_val' id='fld_val"+counter+"' rel='tooltip' data-toggle='tooltip' title='Hyperlink URL'   >" +tblFormSchemaObject.getFieldValue()+"</textarea></td>" );
						htmlText.append("<td><input type='text' class='form-control' name='fld_size' id='fld_size"+counter+"' readonly='true' value='"+tblFormSchemaObject.getSize()+"' ></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='regex' id='regex'><option value='No validation' selected>No validation</option></select></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
						htmlText.append("<td><input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><input type='text' class='form-control' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
					}
					else if(tblFormSchemaObject.getFieldController().equals("checkBox") || tblFormSchemaObject.getFieldController().equals("radio")){
						System.out.println("in CheckBox");
						String module = tblFormSchemaObject.getFieldController().equals("checkBox")? "CheckboxModule" : "RadioModule";
						String moduleName = tblFormSchemaObject.getFieldController().equals("checkBox")? "Checkbox Module" : "Radio Module";
						
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldName' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"'  /></td>" );
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...'   >"+tblFormSchemaObject.getFieldValue()+"</textarea></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
								"<option value='Please select'>Please select</option>" +
								"<option value='"+module+"' selected>"+moduleName+"</option></select></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
						//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"RadioModule")+"</select></td>");
						htmlText.append("<td> <input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><input type='hidden' name='fld_size' id='fld_size"+counter+"'>");
						htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
					}
					else if(tblFormSchemaObject.getFieldController().equals("comboBox") ){
						System.out.println("in Select");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldName' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"'  /></td>" );
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_val' id='fld_val"+counter+"' placeholder='Enter values...'   >"+tblFormSchemaObject.getFieldValue()+"</textarea></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='validation' id='validation"+counter+"' data-rule='regex' onChange='onRegexChange(\"validation"+counter+"\")' >" +
								"<option value='Please select'>Please select</option>" +
								"<option value='SelectModule' selected>Select Module</option></select></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required' name='tooltip' id='tooltip"+counter+"' value='"+tblFormSchemaObject.getTooltip()+"' /></td>");
						//htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' data-rule='regex' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternDataForModule(listRegexPatterns, tblFormSchemaObject.getRegex(),"SelectModule")+"</select></td>");
						htmlText.append("<td> <input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><input type='hidden' name='fld_size' id='fld_size"+counter+"'>");
						htmlText.append("<input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' />");
						htmlText.append("<input type='text' class='form-control' name='regex' id='regex"+counter+"' value='"+tblFormSchemaObject.getRegex()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='required' id='required"+counter+"' value='"+tblFormSchemaObject.getRequired()+"' style='display: none;' /></td>");
					} /*else{
						System.out.println("in Else");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|fieldName' name='fld_name' id='fld_name"+counter+"' value='"+tblFormSchemaObject.getFieldName()+"'  /></td>" );
						htmlText.append("<td><textarea rows='2' cols='22' class='form-control txt_area_val' data-rule='required|minlength-5' name='fld_val' id='fld_val"+counter+"' rel='tooltip' data-toggle='tooltip' title='Enter comma separated values here!' placeholder='Enter values...'   >"+tblFormSchemaObject.getFieldValue()+"</textarea></td>");
						htmlText.append("<td><input type='text' class='form-control' data-rule='required|digitOnly|between-1-100' name='fld_index' id='fld_index"+counter+"' size='1' min='1' max='100'   value='"+tblFormSchemaObject.getFieldIndex()+"' ></td>");
						htmlText.append("<td><select class='form-control' name='regex' id='regex"+counter+"' onChange='onRegexChange(\"regex"+counter+"\")'>"+commonUtility.getSelectedHTMLRegexPatternData(listRegexPatterns, tblFormSchemaObject.getRegex())+"</select></td>");
						htmlText.append("<td> <input type='checkBox' name='cb' id='cb' value='tbl_tr"+counter+"' > </td>");
						htmlText.append("<td><input type='hidden' name='fld_size' id='fld_size"+counter+"'></td>");
						htmlText.append("<td><input type='text' class='form-control' name='date_all' id='date_all"+counter+"' value='"+tblFormSchemaObject.getDateAll()+"' style='display: none;' /></td>");
						htmlText.append("<td><input type='text' class='form-control' name='date_past' id='date_past"+counter+"' value='"+tblFormSchemaObject.getDatePast()+"' style='display: none;' /></td>");
						htmlText.append("<td><input type='text' class='form-control' name='date_future' id='date_future"+counter+"' value='"+tblFormSchemaObject.getDateFuture()+"' style='display: none;' /></td>");
						htmlText.append("<td><input type='text' class='form-control' name='start_date' id='start_date_id"+counter+"' value='"+tblFormSchemaObject.getStartDate()+"' style='display: none;' />" +
								"<input type='text' class='form-control' name='end_date' id='end_date_id"+counter+"' value='"+tblFormSchemaObject.getEndDate()+"' style='display: none;' /></td>");
						htmlText.append("<td><input type='text' class='form-control' name='date_format' id='date_format"+counter+"' value='"+tblFormSchemaObject.getDateFormat()+"' style='display: none;' /></td>");
					}*/
					//htmlText.append("<td><input type='button' id='dropBtn' onClick='removeTableRow(\"tbl_tr"+counter+"\")' value='Drop'></td>");
					
				
					htmlText.append( "</tr>");
					++counter; 
		}
		logger.info("generated string for Update page : " + htmlText.toString());
		logger.info("FormUtilityDaoImpl.generateHTMLFormTableString() end");
		return htmlText.toString();
	}

	@Override
	public List getListDataFromModelName(String modelName) throws Exception {
		logger.info("FormUtilityDaoImpl.getListDataFromModelName() start");
		System.out.println("modelName : " + modelName);
		Class<?> cls = commonUtility.getClassTypeFromName(modelName);
		logger.info("FormUtilityDaoImpl.getListDataFromModelName() end");
		return hibernateOperation.listData(modelName,cls);
	}

	@Override
	 public boolean isPageExist(String pageName) throws Exception {
		logger.info("FormUtilityDaoImpl.isPageExist() start");
		if (hibernateOperation.countTotalData("PageMapping", "page_name='" + pageName + "'") != 0) {
			logger.info("FormUtilityDaoImpl.isPageExist() end");
			return true;
		}
		logger.info("FormUtilityDaoImpl.isPageExist() end");
		return false;
	 }

	@Override
	public String getHTMLRegexPatternData(List<RegexPattern> listRegexPatterns) throws Exception {
		logger.info("FormUtilityDaoImpl.getHTMLRegexPatternData() start");
		logger.info("FormUtilityDaoImpl.getHTMLRegexPatternData() end");
		return commonUtility.createHTMLRegexPatternData(listRegexPatterns);
	}

	@Override
	public List<RegexPattern> getListRegEx(String[] regex) throws Exception {
		logger.info("FormUtilityDaoImpl.getListRegEx() start");
		if(regex ==null ){
			logger.info("FormUtilityDaoImpl.getListRegEx() end");
			return hibernateOperation.listData("RegexPattern");
		}else{
			logger.info("FormUtilityDaoImpl.getListRegEx() end");
			return hibernateOperation.listDataWithWhreClause("RegexPattern", " id in("+Arrays.toString(regex).replace("[", "").replace("]",	 "")+")");	
		}
		
	}

	@Override
	public boolean insertObjectIntoDB(Object regexObj) throws Exception {
		logger.info("FormUtilityDaoImpl.insertObjectIntoDB() start");
		logger.info("FormUtilityDaoImpl.insertObjectIntoDB() end");
		return hibernateOperation.insertData(regexObj);
	}

	@Override
	public Map<String, TblFormSchema> setTableFormSchemaInMap(String[] fld_ctrl, String[] fld_type, String[] fld_lbl,
			String[] fld_name, String[] fld_size, String[] fld_val, String[] fld_index, String[] regex,  String[] tooltip,
			String[] date_all, String[] date_past, String[] date_future, String[] start_date, String[] end_date,
			String[] date_format, String[] required, int pageMappingId) throws Exception {
		logger.info("FormUtilityDaoImpl.setTableFormSchemaInMap() start");
		Map< String, TblFormSchema> tableFormSchemaMap = new HashMap<>();
		System.out.println("fld ctrl.length " + fld_ctrl.length);
		
		for(int i=0;i<fld_ctrl.length;i++)
		{
			TblFormSchema tblFormSchema = new TblFormSchema();
			tblFormSchema.setFk(pageMappingId);
			tblFormSchema.setFieldController(fld_ctrl[i]);
			tblFormSchema.setFieldLabel(fld_lbl[i].trim());
			tblFormSchema.setFieldName(fld_name[i].trim().toLowerCase() );
			tblFormSchema.setFieldType(fld_type[i]);
			tblFormSchema.setFieldIndex(Integer.parseInt( fld_index[i]));
			tblFormSchema.setRegex(regex[i]);
			tblFormSchema.setTooltip(tooltip[i]);
			tblFormSchema.setStartDate(start_date[i]);
			tblFormSchema.setEndDate(end_date[i]);
			tblFormSchema.setDateFormat(date_format[i]);
			tblFormSchema.setDateAll(date_all[i]);
			tblFormSchema.setDatePast(date_past[i]);
			tblFormSchema.setDateFuture(date_future[i]);
			tblFormSchema.setRequired(required[i]);
			System.out.println("fldctrl123" + fld_ctrl[i]);
			System.out.println("required123" + required[i]);
			if(fld_ctrl[i].equals("radio") ||  fld_ctrl[i].equals("comboBox"))
			{
				String fldValSplit[] = fld_val[i].split(",");
				int maxVal = fldValSplit[0].length();
				for(int j=1;j<fldValSplit.length;j++)
				{
					if(fldValSplit[j].trim().length() > maxVal)
					{
						maxVal = fldValSplit[j].trim().length();
					}
				}
				tblFormSchema.setSize(maxVal+"");
			}
			else if (fld_ctrl[i].equals("checkBox")) {
				tblFormSchema.setSize(fld_val[i].length()+"");
			}
			else if(fld_ctrl[i].equals("date") ){
				tblFormSchema.setSize("0");
			}
			else if(fld_ctrl[i].equals("alert") ){
				tblFormSchema.setSize("0");
				tblFormSchema.setFieldValue(fld_val[i].trim());
			}
			
			else
			{
				tblFormSchema.setSize(fld_size[i].trim());
			}
			
			if(fld_ctrl[i].equals("radio") || fld_ctrl[i].equals("checkBox") || fld_ctrl[i].equals("comboBox") 
					|| fld_ctrl[i].equals("hyperlink") || fld_ctrl[i].equals("file"))
			{
				tblFormSchema.setFieldValue(fld_val[i].trim());
			}
			tableFormSchemaMap.put(tblFormSchema.getFieldName().toLowerCase(), tblFormSchema);
			//hibernateOperation.insertData(tblFormSchema);
		}
		tableFormSchemaMap.put("id",null);
		logger.info("tableFormSchemaMap : " + tableFormSchemaMap.toString());
		logger.info("FormUtilityDaoImpl.setTableFormSchemaInMap() end");
		return tableFormSchemaMap;
	}

	@Override
	public HashMap<String, String> validateFormGenerationData(PageMapping pageMapping,
			Map<String, TblFormSchema> tableFormSchemaMapList) throws Exception {
		logger.info("FormUtilityDaoImpl.validateFormGenerationData() start");
		HashMap<String,String> errorList =  new HashMap<>();
		
		for(Entry<String, TblFormSchema> tblFormSchemaEntry : tableFormSchemaMapList.entrySet() ){
			System.out.println("table schema value : "+tblFormSchemaEntry.getValue());
			if(tblFormSchemaEntry.getValue() != null){
				TblFormSchema tblFormSchema = tblFormSchemaEntry.getValue();
				if(tblFormSchema.getFieldController().equals("alert") || tblFormSchema.getFieldController().equals("hyperlink")
						|| tblFormSchema.getFieldController().equals("file")){
					System.out.println("continue");
					continue;
				}
				HashMap<String, String> errorListOfOneObject = commonUtility.hibernateValidation(TblFormSchema.class, tblFormSchema);
				if(errorListOfOneObject != null){
					errorList.putAll( errorListOfOneObject);	
				}
			}
		}
		
		HashMap<String, String> errorListOfOneObject = commonUtility.hibernateValidation(PageMapping.class,pageMapping);
		if(errorListOfOneObject != null){
			errorList.putAll( errorListOfOneObject);	
		}
		
		for(Entry<String, String> error : errorList.entrySet() ){
			//errorList.putAll( commonUtility.hibernateValidation(TblFormSchema.class, tblFormSchema.getValue()));
			logger.error("validation error : " + error.getValue());
			System.out.println("Error : "+ error.getValue());
		}
		logger.info("FormUtilityDaoImpl.validateFormGenerationData() end");
		return errorList;
	}

	@Override
	public boolean insertFieldData(Map<String, TblFormSchema> tableFormSchemaMapList, int pageMappingId) throws Exception {
		logger.info("FormUtilityDaoImpl.insertFieldData() start");
		boolean result = true;
		for(Entry<String, TblFormSchema> tableFormSchema : tableFormSchemaMapList.entrySet() ){
			//errorList.putAll( commonUtility.hibernateValidation(TblFormSchema.class, tblFormSchema.getValue()));
			logger.info("Key : " +tableFormSchema.getKey() + " Values : " + tableFormSchema.getValue());
			System.out.println("Key : " +tableFormSchema.getKey() + " Values : " + tableFormSchema.getValue());
			if(tableFormSchema.getValue() != null){
				TblFormSchema tblFormSchema = tableFormSchema.getValue();
				tblFormSchema.setFk(pageMappingId);
				result = hibernateOperation.insertData(tblFormSchema)? true : false ;
			}
		}
		logger.info("FormUtilityDaoImpl.insertFieldData() end");
		return result;
	}

	@Override
	public boolean createTable(String pageName, Map<String, TblFormSchema> tableFormSchemaMapList) throws Exception {
		logger.info("FormUtilityDaoImpl.createTable() start");
		JDBCOperation operation = new JDBCOperation();
		//int cntRad = 0;
		tableFormSchemaMapList.remove("id");
		String query = "CREATE TABLE "+pageName+" ( "
				+ " ID INT PRIMARY KEY AUTO_INCREMENT, ";
		int index = 1;
		for (Entry<String, TblFormSchema> tableFormSchema : tableFormSchemaMapList.entrySet()) {
			if (tableFormSchema.getValue() != null) {
				TblFormSchema tblFormSchema = tableFormSchema.getValue();
				System.out.println("Controller " + tblFormSchema.getFieldController());
				if (tblFormSchema.getFieldController().equals("alert")
						|| tblFormSchema.getFieldController().equals("hyperlink")) {
					System.out.println("In fields index->" +index + " -> size " +tableFormSchemaMapList.size()  );
					/*if ( index == tableFormSchemaMapList.size()) {
						query = query.substring(0, query.length() - 3);
						// query += ", ";
					}*/

					continue;
				}
				if (tblFormSchema.getFieldController().equals("radio")
						|| tblFormSchema.getFieldController().equals("comboBox")) {
					String fldValSplit[] = tblFormSchema.getFieldValue().split(",");
					int maxVal = fldValSplit[0].trim().length();
					for (int j = 1; j < fldValSplit.length; j++) {
						if (fldValSplit[j].trim().length() > maxVal) {
							maxVal = fldValSplit[j].trim().length();
						}
					}
					query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "(" + maxVal
							+ ") ";
				} else if (tblFormSchema.getFieldController().equals("checkBox")) {
					query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "("
							+ tblFormSchema.getFieldValue().length() + ") ";
				} else if (tblFormSchema.getFieldController().equals("date")
						|| (tblFormSchema.getFieldController().equals("textbox")
								&& tblFormSchema.getFieldType().equals("DATE"))) {
					query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + " ";
				} else {
					if (Arrays.stream(new String[] { "FLOAT", "DOUBLE", "NUMERIC" }).parallel()
							.anyMatch(tblFormSchema.getFieldType()::contains)) {
						if (tblFormSchema.getSize().contains(",")) {
							query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "("
									+ tblFormSchema.getSize() + ") ";
						} else {
							query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + " ";
						}
					} else {
						query += " " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "("
								+ tblFormSchema.getSize() + ") ";
					}

				}
					query += ", ";
			}
			++index ;
		}
		query =	query.substring(0, query.toString().length() - 3);
		query += ")";
		logger.info("query : " + query);
		System.out.println("query : " + query);
		logger.info("FormUtilityDaoImpl.createTable() end");
		return operation.createTable(query);
	}

	@Override
	public boolean updateTableSchema(String pageName, Map<String, TblFormSchema> tableFormSchemaMapList) throws Exception {
		logger.info("FormUtilityDaoImpl.updateTableSchema() start");
		JDBCOperation operation = new JDBCOperation();
		System.out.println("In create table");

		String query = "Alter TABLE " + pageName + "  " + "  ";

		query += updateTableQuery(pageName, tableFormSchemaMapList);
		query += "";
		System.out.println("query : " + query);
		logger.info("FormUtilityDaoImpl.updateTableSchema() end");
		return operation.createTable(query);
	}

	private String updateTableQuery(String pageName, Map<String, TblFormSchema> tableFormSchemaMapList) throws Exception {
		logger.info("FormUtilityDaoImpl.updateTableQuery() start");
		Connection connection = null;
		ResultSet resultSet = null;
		StringBuilder alterQuery = new StringBuilder();
		try {
			connection = jdbcUtil.getConnection();
			DatabaseMetaData dbmd;
			dbmd = connection.getMetaData();
			String[] types = { "TABLE" };
			resultSet = dbmd.getColumns(null, null, pageName, "%");
			List<String> existingField = new LinkedList<>();
			// Map<String, String> formField = new HashMap<>();
			//int cntRad = 0;
			while (resultSet.next()) {
				existingField.add(resultSet.getString(4));
			}
			tableFormSchemaMapList.remove("id");
			
			for (Entry<String, TblFormSchema> tableFormSchema : tableFormSchemaMapList.entrySet()) {
				
				if (tableFormSchema.getValue() != null) {
					TblFormSchema tblFormSchema = tableFormSchema.getValue();
					if (tblFormSchema.getFieldController().equals("alert") || tblFormSchema.getFieldController().equals("hyperlink")){
						continue;
					}
					
					if (tblFormSchema.getFieldController().equals("radio") || tblFormSchema.getFieldController().equals("comboBox")) {
						String fldValSplit[] = tblFormSchema.getFieldValue().split(",");
						int maxVal = fldValSplit[0].trim().length();

						for (int j = 1; j < fldValSplit.length; j++) {
							if (fldValSplit[j].trim().length() > maxVal) {
								maxVal = fldValSplit[j].trim().length();
							}
						}
						
						System.out.println("Field Value" + tblFormSchema.getFieldValue()+ "Max val is " + maxVal);
						if (existingField.contains(tblFormSchema.getFieldName())) {
							alterQuery.append(
									" modify " + tblFormSchema.getFieldName()+ " " + tblFormSchema.getFieldType() + "(" + maxVal + ") ");
							existingField.remove(tblFormSchema.getFieldName());
						} else {
							alterQuery
									.append(" add " + tblFormSchema.getFieldName()  + " " + tblFormSchema.getFieldType() + "(" + maxVal + ") ");
						}
					} else if (tblFormSchema.getFieldController().equals("checkBox")) {
						System.out.println("Check Box " + tblFormSchema.getFieldType().length());
						if (existingField.contains(tblFormSchema.getFieldName())) {
							alterQuery.append(" modify " + tblFormSchema.getFieldName()  + " " +tblFormSchema.getFieldType() + "("
									+ tblFormSchema.getFieldValue().length() + ") ");
							existingField.remove(tblFormSchema.getFieldName());
						} else {
							alterQuery.append(" add " + tblFormSchema.getFieldName()  + " " + tblFormSchema.getFieldType() + "("
									+ tblFormSchema.getFieldValue().length() + ") ");
						}
					}else if(tblFormSchema.getFieldController().equalsIgnoreCase("date") || (tblFormSchema.getFieldController().equals("textbox") && tblFormSchema.getFieldController().equals("DATE"))) {

						if (existingField.contains(tblFormSchema.getFieldName())) {
							alterQuery.append(
									" modify " + tblFormSchema.getFieldName() + " " +tblFormSchema.getFieldType() + " ");
							existingField.remove(tblFormSchema.getFieldName());
						} else {
							alterQuery.append(
									" add " + tblFormSchema.getFieldName()  + " " + tblFormSchema.getFieldType() + " ");
						}

					} else {

						if (existingField.contains(tblFormSchema.getFieldName())) {
							if (Arrays.stream(new String[] { "FLOAT", "DOUBLE", "NUMERIC" }).parallel()
									.anyMatch(tblFormSchema.getFieldType()::contains)) {
								if (tblFormSchema.getSize().contains(",")) {
									alterQuery.append(" modify " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "("
											+ tblFormSchema.getSize() + ") ");
								} else {
									alterQuery.append( " modify " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + " ");
								}
							} else {
								alterQuery.append(" modify " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "("
										+ tblFormSchema.getSize() + ") ");
							}
							
							    //alterQuery.append(" modify " +tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "(" + tblFormSchema.getSize() + ") ");
							existingField.remove(tblFormSchema.getFieldName());
						} else {
							if (Arrays.stream(new String[] { "FLOAT", "DOUBLE", "NUMERIC" }).parallel()
									.anyMatch(tblFormSchema.getFieldType()::contains)) {
								if (tblFormSchema.getSize().contains(",")) {
									alterQuery.append(
											" add " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "(" + tblFormSchema.getSize() + ") ");
								}else{
									alterQuery.append(
											" add " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + " ");
								}
							}else{
								alterQuery.append(
										" add " + tblFormSchema.getFieldName() + " " + tblFormSchema.getFieldType() + "(" + tblFormSchema.getSize() + ") ");
							}
						}
					}
					alterQuery.append(", \n");
				}

			}
			for (String remainField : existingField) {
				if (!remainField.equalsIgnoreCase("id")) {
					alterQuery.append(" drop " + remainField + " ");
					alterQuery.append(", \n");
				}

			}

		} catch (SQLException e) {

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				resultSet.close();
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
			}
		}
		logger.info("alter query : " + alterQuery);
		logger.info("FormUtilityDaoImpl.updateTableQuery() end");
		return alterQuery.toString().substring(0, alterQuery.toString().length() - 3);
	}

	@Override
	public String getHTMLRegexPatternDataForModule(List<RegexPattern> listRegexPatterns, String moduleName) throws Exception {
		logger.info("FormUtilityDaoImpl.getHTMLRegexPatternDataForModule() start");
		logger.info("FormUtilityDaoImpl.getHTMLRegexPatternDataForModule() end");
		return commonUtility.createHTMLRegexPatternDataForModule(listRegexPatterns,moduleName);
	}

	@Override
	public boolean deletePageMappingFromPageName(String pageName) throws Exception {
		logger.info("FormUtilityDaoImpl.deletePageMappingFromPageName() start");
		
		/*List<PageMapping> listData = hibernateOperation.listDataWithWhreClause("PageMapping", "pageName="+pageName);
		if(listData != null && listData.size() != 0 ){
		  hibernateOperation.de	listData.get(0);
		}else{
			
		}
		*/
		
		if (hibernateOperation.deleteData("PageMapping", "pageName='"+pageName+"'") != 0){
			logger.info(pageName + " delted from PageMapping");
			logger.info("FormUtilityDaoImpl.deletePageMappingFromPageName() end");
			return true;
		}else{
			logger.error(pageName + " not deleted from PageMapping");
			logger.info("FormUtilityDaoImpl.deletePageMappingFromPageName() end");
			return false;
		}
	}

	@Override
	public boolean regenerateTableAndForm(int pageId, String pageName,
			List<TblFormSchema> listCurrentTblFormSchemaData,String buildPath) throws Exception {
		logger.info("FormUtilityDaoImpl.regenerateTableAndForm() start");
		PageMapping pageMapping = new PageMapping();
		pageMapping  = this.getPageMappingData(pageId+"");
		
		dropForm(pageId, pageName, buildPath);
		
		int pageMappingId = this.insertPageMappingData(pageMapping);
		System.out.println("pageMappingId : " + pageMappingId);
		Map<String, TblFormSchema> tableFormSchemaMapList =  new HashMap<>();
		for(TblFormSchema tblFormSchema : listCurrentTblFormSchemaData){
			if(tblFormSchema.getFieldName().equalsIgnoreCase("id")){
				continue;
			}
			tableFormSchemaMapList.put(tblFormSchema.getFieldName(), tblFormSchema);
		}
		
		if (this.insertFieldData(tableFormSchemaMapList, pageMappingId)) {
			logger.info("inserting field data done");
			if (this.createTable(pageName, tableFormSchemaMapList)) {
				logger.info("create table done");
				if (this.generateClassFromTable(pageName, buildPath, tableFormSchemaMapList)) {
					logger.info("generating class file from table done");
					//List<TblFormSchema> list = formUtilityService.getTableFormSchemaData(pageMappingId);
					this.generateForm(listCurrentTblFormSchemaData, pageMapping, buildPath);
					logger.info("geration of form done");
					logger.info("FormUtilityDaoImpl.regenerateTableAndForm() end");
					return true;
				} else {
					logger.error("All form data deleted Due to get Exception of class genration");
					System.out.println("All form data deleted Due to get Exception of class genration");
					return false;
				}
			} else {
				logger.error("Page Mapping deleted");
				logger.error("Page Mapping Field deleted");
				return false;
			}
		}else{
			logger.error("Page Mapping deleted");
			return false;
		}
		
	}

	@Override
	public boolean updateObject(Object obj) throws Exception {
		logger.info("FormUtilityDaoImpl.updateObject() start");
		logger.info("FormUtilityDaoImpl.updateObject() end");
		return hibernateOperation.updateData(obj);
	}

	@Override
	public Integer uploadFile(FileUpload fileUpload,MultipartFile file) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("File name :"+  file.getName());
		System.out.println("File Type :"+  file.getContentType());
		DateFormat dateFormat = new SimpleDateFormat("MMddyyyyHHmmssS");
		String fileStoreLocation = commonUtility.getValueFromPropertieFile("imagePath");
		
		//fileUpload.setFileName(fileStoreLocation+"//"+dateFormat.format(new Date())+"_"+fileUpload.getTableName()+".txt");
		fileUpload.setFileType(file.getContentType());
	
		try {
			FileOutputStream io =  new FileOutputStream(fileStoreLocation+"//"+fileUpload.getFileName());
			io.write(CryptoUtils.encrypt("Silvertouch java", file.getBytes()));
			io.close();
		} catch (CryptoException|FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hibernateOperation.insertDataRestunAutoGenratedId(fileUpload, FileUpload.class);
	}

	@Override
	public Integer insertDataRestunAutoGenratedId(Object dataObject, Class<? extends Object> cls) {
		// TODO Auto-generated method stub
		 return hibernateOperation.insertDataRestunAutoGenratedId(dataObject, cls);	
	}

	
	@Override
	public List<Object> getListDataFromModelNameById(String modelName, String fileName) throws Exception {
		logger.info("FormUtilityDaoImpl.getListDataFromModelName() start");
		System.out.println("modelName : " + modelName);
		Class<?> cls = commonUtility.getClassTypeFromFullName("com.bean."+modelName);
		logger.info("FormUtilityDaoImpl.getListDataFromModelName() end");
		return hibernateOperation.listDataWithWhreClause(modelName, "filename='"+fileName+"'");
	}

	@Override
	public List<Object> getPageMappingDataByName(String name) throws Exception {
		// TODO Auto-generated method stub
		logger.info("FormUtilityDaoImpl.getPageMappingDataByName() start");
		logger.info("FormUtilityDaoImpl.getPageMappingDataByName() end");
		return (List<Object>) hibernateOperation.listDataWithWhreClause("PageMapping","page_name='"+name+"'");
	}

	@Override
	public Map<String, String> checkFileValidation(String path, MultipartHttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		boolean noFile = true;
		List<TblFormSchema> tblFormSchema = new ArrayList<TblFormSchema>();
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> validateMap = null;
		PageMapping pageMapping = new PageMapping();
		List<Object> list = null;
		
		list = this.getPageMappingDataByName(path);
		
		if(list.size() != 0) {
			pageMapping = (PageMapping) list.get(0);
			tblFormSchema = this.getTableFormSchemaData(pageMapping.getId());
			/*Map<String,TblFormSchema> tblMap = tblFormSchema.stream().collect(Collectors.toMap(
																	X -> X.getFieldController().toLowerCase() ,
																	X -> X, (x1,x2) ->{ return x1; }));
			if(tblMap.containsKey("file")){
				return commonUtility.checkFileValidation(tblMap.get("file"), request);
			}else{
				map.put("nofile", "");
			}*/
			for(TblFormSchema tblSchema : tblFormSchema) {
				if(tblSchema.getFieldController().equals("file")) {
					noFile = false;
					Iterator<String> itrator = request.getFileNames();
					while(itrator.hasNext()) {
						List<MultipartFile> multipartFiles = request.getFiles(itrator.next());
						for(MultipartFile multipartFile : multipartFiles) {
							if(multipartFile.getName().equalsIgnoreCase(tblSchema.getFieldName())) {
								validateMap = new HashMap<String, String>();
								validateMap = commonUtility.checkFileValidation(tblSchema, multipartFile); 
								if(validateMap.containsKey("error")) {
									return validateMap;
								} else {
									map.putAll(validateMap);
								}
							}
						}
					}
				}
			}
			
			if(noFile == true) {
				map.put("nofile", "");
			}
			return map;
		}
		return null;
	}

	@Override
	public StringBuilder getAuditLog(String action, String fromDate, String toDate,String json)  {
		// TODO Auto-generated method stub
		List<Object> criterias = new ArrayList<Object>();
		Operation operation = new Operation();
		StringBuilder tableData=new StringBuilder();
		java.util.Properties formatter = commonUtility.loadPropertieFile("config/database.properties");
		
		Gson g = new GsonBuilder().create();
		JQueryDataTableParamModel jqDataTable= g.fromJson(json, JQueryDataTableParamModel.class);
		List<AuditLogView> list = operation.searchForAuditDataTable(AuditLogView.class,jqDataTable,formatter);
		
		/*for(AuditLogView auditLogView : list){
			
			System.out.println(""+auditLogView.getDetail() );
		}*/
		for(int i = 0;i<list.size();i++ ){
			AuditLogView auditLogView = list.get(i);
			auditLogView.setDetail( commonUtility.stringJsonToHtml(auditLogView.getDetail()));
			list.set(i,auditLogView);
		}
		jqDataTable.setData( list );

		jqDataTable.setRecordsTotal(operation.coutTotalRecord(AuditLogView.class));
		jqDataTable.setRecordsFiltered((int) operation.searchForAuditDataTableCount(AuditLogView.class,jqDataTable,formatter));
		String jsonStr = null;
		ObjectMapper mapperObj = new CommonImpl().createObjectMapper();
		try{
				jsonStr = mapperObj.writeValueAsString(jqDataTable);
				System.out.println("data " + jsonStr);
			//	return jsonStr;
		}catch (javax.persistence.OptimisticLockException | org.hibernate.StaleStateException e) {
			//return "{ \"error\":" + "{\"message\" : \"Delete operation unsuccessful due to data not exist.\"" + "}}";
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new StringBuilder(jsonStr);
	}
	@Override
	public boolean deleteUploadedFile(String path, int id) throws Exception {
		String className = path.substring(0, 1).toUpperCase() + path.substring(1);
		PageMapping pageMapping = new PageMapping();
		boolean flag = true;
		String fileStoreLocation = commonUtility.getValueFromPropertieFile("imagePath");
		List<TblFormSchema> listTblFormSchemas = new ArrayList<TblFormSchema>();
		List<Object> list = null;
		List<String> fieldNames = new ArrayList<String>();
		List<String> fieldNamesValues = new ArrayList<String>();
		Class<?> clazz;
		clazz = Class.forName(commonUtility.getValueFromPropertieFile("beanPackage") + "." + className);
		list = this.getPageMappingDataByName(path);
		if(list.size() != 0) {
			pageMapping = (PageMapping) list.get(0);
			listTblFormSchemas = this.getTableFormSchemaData(pageMapping.getId());
			for(TblFormSchema tblFormSchema : listTblFormSchemas) {
				if(tblFormSchema.getFieldController().equals("file")) {
					fieldNames.add(tblFormSchema.getFieldName());
				}
			}
			if(fieldNames.size() != 0) {
				System.out.println("clazz" + clazz);
				//Object dataObject = commonUtility.bindDataInObject(clsObject, clsObject.getClass(), request, null, null);
				List<Object> listObject = hibernateOperation.listDataWithWhreClause(className, "id="+id, clazz);
				if(listObject.size() != 0) {
					for(int i=0; i<fieldNames.size(); i++) {
						fieldNamesValues.add(BeanUtils.getProperty(listObject.get(0), fieldNames.get(i)));
					}
				}
			}
			if(fieldNamesValues.size() != 0) {
				for(String value : fieldNamesValues) {
					if(value.length() > 45) {
						System.err.println("--- " + value.substring(35,value.lastIndexOf('\'')));
						String values[] = value.substring(35,value.lastIndexOf('\'')).split("\\|");
						for(String name : values) {
							commonUtility.deleteFile(fileStoreLocation+"//"+name);
							hibernateOperation.deleteData(FileUpload.class, Integer.valueOf(name));
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public Object getDataById(Class<?> type, Integer id) throws Exception {
		// TODO Auto-generated method stub
		return hibernateOperation.getDataById(type, id);
	}
}