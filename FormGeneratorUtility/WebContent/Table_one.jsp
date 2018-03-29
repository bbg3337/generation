<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!DOCTYPE html ><html><head><title>Table_one</title><link rel="stylesheet" href="../css/bootstrap.min.css"><link rel="stylesheet" href="../css/jquery.datetimepicker.min.css"><script src="../js/jquery-3.2.1.min.js"></script><script src="../js/bootstrap.min.js"></script><script src="../js/jquery.datetimepicker.full.js"></script><script src="../js/formutility.js"></script><script src="../js/custom_alert.js"></script><script src="../js/bootstrap-show-password.min.js"></script></head><body><div class="container"><center><h2>Table_one</h2></center><form action="../insertForm/Table_one" class="form-horizontal" method="post" onsubmit="return checkFormData()"><div class="form-group"><label class="control-label col-sm-4">Code</label><div class="col-sm-5"><input type="text"  class="form-control" name="code" id="code" tabindex="1" pattern="^[1-9]{1,}[0-9]{0,}$" title="Only digit allow, first character must be not Zero." required maxlength="10" /></div></div><div class="form-group"><label class="control-label col-sm-4">Password</label><div class="col-sm-5"><input type="password"  class="form-control" name="password" id="password" tabindex="2" pattern="(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[@#$%^&+=].*[@#$%^&+=])(?=\S+$).{8,}" title="Password must be, 2 Latter Capital,2 special char ,and any later and any digit" required maxlength="10"  /><input type="checkbox" onclick="showPassword('password')" >Show Password</div></div><div class="form-group"><label class="control-label col-sm-4">Gender</label><div class="col-sm-5"> <div class='row'> <div class='col-sm-6'><input type="radio" name="gender" id="gender1" tabindex="3" required pattern=":pattern"  value=Male /> Male<br /></div> <div class='col-sm-6'><input type="radio" name="gender" id="gender2" tabindex="3" required pattern=":pattern"  value=Female /> Female<br /></div></div></div></div><div class="form-group"><label class="control-label col-sm-4">Address</label><div class="col-sm-5"><textarea name="address" id="address"  class="form-control" placeholder="Enter text..." rows="5" cols="30" tabindex="4" required pattern="(.*)" maxlength="150"  ></textarea></div></div><div class="form-group"><label class="control-label col-sm-4">Hobby</label><div class="col-sm-5"> <div class='row'> <div class='col-sm-6'><input type="checkbox" name="hobby" id="hobby1" tabindex="5" pattern=":pattern"  value=Cricket /> Cricket<br /></div> <div class='col-sm-6'><input type="checkbox" name="hobby" id="hobby2" tabindex="5" pattern=":pattern"  value=Hockey /> Hockey<br /></div></div></div></div><div class="form-group"><label class="control-label col-sm-4">Project name</label><div class="col-sm-5"><select class="form-control" name="projectname" id="projectname" tabindex="7" required pattern=":pattern"  /><option value='HSIIDC'>HSIIDC</option><option value='CRM'>CRM</option></select></div></div><div class="form-group"><label class="control-label col-sm-4">Date</label><div class="col-sm-5"><input type="text"  class="form-control" name="date1" id="date1" tabindex="8" required onkeypress="return false;"  /></div></div><div class="form-group"><label class="control-label col-sm-4">Date Time</label><div class="col-sm-5"><input type="text"  class="form-control" name="datetime1" id="datetime1" tabindex="9" required onkeypress="return false;"  /></div></div><div class="form-group"><label class="control-label col-sm-4">Name</label><div class="col-sm-5"><button class='btn' data-toggle='modal' data-target='#myModal' tabindex=10 onclick="alertFuntion('Name','hello world','TYPE.ok','Ok','TYPE.cancel','Cancel')"/>Alert</button></div></div><div class="form-group"><label class="control-label col-sm-4">Number</label><div class="col-sm-5"><input type="number"  class="form-control" name="number" id="number" tabindex="11" step="0.01"  required  /></div></div><div class="form-group"><label class="control-label col-sm-4">Hyperlink</label><div class="col-sm-5"><a href='https://www.google.com' tabindex=12 >Hyperlink</a></div></div><div class="form-group"><div class="col-sm-offset-4 col-sm-5"><input type="submit" class="btn btn-success" /></div></div></form><font color='red'><p id="errMessage"></p></font><c:if test="${not empty message}"><font color='green'><c:out value="${message }"></c:out></font></c:if><c:if test="${not empty errMessage}"><font color='red'> ${errMessage } </font></c:if></div><div class="container" style="overflow-x:auto;"><c:if test="${not empty listData}" >	<table class="table"> 	<thead style='color:#ccffe6;background-color:#87ceeb'>		<tr><th>Code</th><th>Password</th><th>Gender</th><th>Address</th><th>Hobby</th><th>Project name</th><th>Date</th><th>Date Time</th><th>Number</th>	</tr></thead>	<c:forEach items="${listData}" var="obj">	<tbody><tr><td class="col-m-2"><c:out value="${obj.code}" /></td><td class="col-m-2"><c:out value="${obj.password}" /></td><td class="col-m-2"><c:out value="${obj.gender}" /></td><td class="col-m-2"><c:out value="${obj.address}" /></td><td class="col-m-2"><c:out value="${obj.hobby}" /></td><td class="col-m-2"><c:out value="${obj.projectname}" /></td><td class="col-m-2"><c:out value="${obj.date1}" /></td><td class="col-m-2"><c:out value="${obj.datetime1}" /></td><td class="col-m-2"><c:out value="${obj.number}" /></td>	</tr></tbody>	</c:forEach></table></c:if><jsp:include page='alertForm.jsp'></jsp:include></div></body><script src="../js/formutility_date.js"></script><script>function checkFormData() {if(!$('input[name=hobby]:checked').length > 0) {document.getElementById("errMessage").innerHTML = "Hobby can not be null";return false;}return true; }</script><script>$('#date1').datetimepicker({timepicker:false,minDate:'-1970/01/01',defaultDate:'09.01.2018',minDate:'09.01.2018',maxDate:'20.01.2018',format: 'd.m.Y',formatDate : 'd.m.Y',});$('#datetime1').datetimepicker({maxDate:'+1970/01/01',defaultDate:'12:25 2017/12/13',minDate:'12:25 2017/12/13',maxDate:'12:25 2017/12/28',format: 'H:i Y/m/d',formatDate: 'H:i Y/m/d',step : 1,});</script></html>
