<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!DOCTYPE html ><html><head><title>demoTable</title><link rel="stylesheet" href="../css/bootstrap.min.css"><link rel="stylesheet" href="../css/jquery.datetimepicker.min.css"><link rel="stylesheet" href="../css/forms.css"><script src="../js/jquery-3.2.1.min.js"></script><script src="../js/bootstrap.min.js"></script><script src="../js/jquery.datetimepicker.full.js"></script><script src="../js/formutility.js"></script><script src="../js/custom_alert.js"></script><script src="../js/bootstrap-show-password.min.js"></script><script src="../js/js-form-validator.min.js"></script></head><body><a href='../index' class='glyphicon glyphicon-home btn-lg' style='margin: 10px 20px; color: black;'></a><div class="container"><center><h2>demoTable</h2></center><form action="../insertForm/demoTable" id="iddemoTable" class="form-horizontal" method="post" ><div class="form-group"><label class="control-label col-sm-4">code</label><div class="col-sm-5"><input type="text"  class="form-control" name="code" id="code" tabindex="1" maxlength="10" data-rule="required|Aplha_numeric" /></div></div><div class="form-group"><label class="control-label col-sm-4">pwd</label><div class="col-sm-5"><input type="password"  class="form-control" name="pwd" id="pwd" tabindex="2" maxlength="20"  data-rule="required|Password" /><input type="checkbox" onclick="showPassword('pwd')" >Show Password</div></div><div class="form-group"><label class="control-label col-sm-4">Gender</label><div class="col-sm-5"> <div class='row'> <div class='col-sm-6'><input type="radio" name="gender" id="gender" tabindex="3"  value=male data-rule="required" />male<br /></div> <div class='col-sm-6'><input type="radio" name="gender" id="gender" tabindex="3"  value=female data-rule="required" />female<br /></div></div></div></div><div class="form-group"><label class="control-label col-sm-4">address</label><div class="col-sm-5"><textarea name="adr" id="adr"  class="form-control" placeholder="Enter text..." rows="5" cols="30" tabindex="4" maxlength="40"  data-rule="required|Address_Validation" ></textarea></div></div><div class="form-group"><label class="control-label col-sm-4">dob</label><div class="col-sm-5"><input type="text"  class="form-control" name="dob" id="dob" tabindex="11" onkeypress="return false;" readonly  data-rule="required" /></div></div><div class="form-group"><label class="control-label col-sm-4">check in</label><div class="col-sm-5"><input type="text"  class="form-control" name="checkin" id="checkin" tabindex="12" onkeypress="return false;" readonly  data-rule="required" /></div></div><div class="form-group"><div class="col-sm-offset-4 col-sm-5"><input type="submit" class="btn btn-success" id="submitdemoTable" value="Submit" /></div></div></form><font color='red'><p id="errMessage"></p></font><c:if test="${not empty message}"><font color='green'><c:out value="${message }"></c:out></font></c:if><c:if test="${not empty errMessage}"><font color='red'> ${errMessage } </font></c:if></div><div class="container" style="overflow-x:auto;"><c:if test="${not empty listData}" >	<table class="table"> 	<thead style='color:#ccffe6;background-color:#87ceeb'>		<tr><th>code</th><th>pwd</th><th>Gender</th><th>address</th><th>dob</th><th>check in</th>	</tr></thead>	<c:forEach items="${listData}" var="obj">	<tbody><tr><td class="col-m-2"><c:out value="${obj.code}" /></td><td class="col-m-2"><c:out value="${obj.pwd}" /></td><td class="col-m-2"><c:out value="${obj.gender}" /></td><td class="col-m-2"><c:out value="${obj.adr}" /></td><td class="col-m-2"><c:out value="${obj.dob}" /></td><td class="col-m-2"><c:out value="${obj.checkin}" /></td>	</tr></tbody>	</c:forEach></table></c:if></div></body><script src="../js/formutility_date.js"></script><script>$(function(){ $('#submitdemoTable').click(function() {document.getElementById("errMessage").innerHTML = ""; return true; }); });</script><script>new Validator(document.querySelector('#iddemoTable'), function (err, res) {return res;},options = { rules : { Aplha_numeric: function(a){ return (new RegExp('^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$')).test(a) },Password: function(a){ return (new RegExp('(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[@#$%^&+=].*[@#$%^&+=])(?=\\S+$).{8,}')).test(a) },Address_Validation: function(a){ return (new RegExp('^[A-Za-z0-9,/-]{1,}$')).test(a) },},messages : { en : { Aplha_numeric:{ empty: "Only alphanumeric allow.", incorrect: "Only alphanumeric allow." },Password:{ empty: "Password must be, 2 Latter Capital,2 special char ,and any later and any digit", incorrect: "Password must be, 2 Latter Capital,2 special char ,and any later and any digit" },Address_Validation:{ empty: "Please enter proper address...", incorrect: "Please enter proper address..." },}}});</script><script>$('#dob').datetimepicker({timepicker:false,maxDate:'+1970/01/01',format: 'd.m.Y',formatDate : 'd.m.Y',});$('#checkin').datetimepicker({defaultDate:'17:08 01.01.2018',minDate:'17:08 01.01.2018',maxDate:'17:08 31.01.2018',format: 'H:i d.m.Y',formatDate: 'H:i d.m.Y',step : 1,});</script></html>
