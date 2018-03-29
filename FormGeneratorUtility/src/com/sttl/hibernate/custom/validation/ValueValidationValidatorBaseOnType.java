package com.sttl.hibernate.custom.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.util.hibernate.operation.Operation;

public class ValueValidationValidatorBaseOnType implements ConstraintValidator<ValueValidationBaseOnType,Object> {
	
	//RangeValidation rangeValidation;
	String controller;
	String dataType;
	String value;
	
	@Override
	public void initialize(ValueValidationBaseOnType arg0) {
		// TODO Auto-generated method stub
		
		System.out.println("Intialization ");
		controller =arg0.controller();
		dataType =arg0.dataType();
		value =arg0.value();
	}

	@Override
	public boolean isValid(Object fieldData, ConstraintValidatorContext arg1) {
		// TODO Auto-generated method stub
		String controller = null;
		String dataType = null;
		String value = null;
		try {
			controller = BeanUtils.getProperty(fieldData, this.controller);
			dataType = BeanUtils.getProperty(fieldData, this.dataType);
			value = BeanUtils.getProperty(fieldData, this.value);
			
			System.out.println("controller :: "+ controller +"  dataType::" + dataType + "  Value::"+value);
			
			// added by parth for creating form if controller is other than comboBox, checkBox and radio
			if(Arrays.stream(new String[]{ "textbox","password", "textarea", "date", "number", "alert", "hyperlink", "file"}).parallel().anyMatch(controller::contains))
			{
				System.out.println(controller + " - return true");
				return true;
			}
			
			if( Arrays.stream(new String[]{ "comboBox","radio",}).parallel().anyMatch(controller::contains) && dataType.equals("INT")){
				System.out.println("in validation side");
				if(Pattern.matches("^[0-9]{1,}[,0-9]{0,}[0-9]$",value)){
					return true;
				}
			}
			if( Arrays.stream(new String[]{ "comboBox","radio",}).parallel().anyMatch(controller::contains) && dataType.equals("FLOAT")){
				System.out.println("in validation side");
				if(Pattern.matches("^[0-9.]{1,}[,0-9.]{0,}[0-9]$",value)){
					return true;
				}
			}
			if( Arrays.stream(new String[]{ "comboBox","radio","checkBox",}).parallel().anyMatch(controller::contains) && Arrays.stream(new String[]{ "CHAR","VARCHAR",}).parallel().anyMatch(dataType::contains)){
				System.out.println("in validation side");
				if(Pattern.matches("^[A-z]{1,}[\\s]{0,}[,A-z\\s]{0,}[A-z]$",value)){
					return true;
				}
			}
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}
}
