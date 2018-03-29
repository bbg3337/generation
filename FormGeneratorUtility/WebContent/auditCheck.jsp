<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html >
<html>
<head>
<title>auditCheck</title>
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/jquery.datetimepicker.min.css">
<link rel="stylesheet" href="../css/forms.css">
<script src="../js/jquery-3.2.1.min.js"></script>
<script src="../js/bootstrap.min.js"></script>
<script src="../js/jquery.datetimepicker.full.js"></script>
<script src="../js/formutility.js"></script>
<script src="../js/custom_alert.js"></script>
<script src="../js/bootstrap-show-password.min.js"></script>
<script src="../js/js-form-validator.min.js"></script>
</head>
<body>
	<a href='../index' class='glyphicon glyphicon-home btn-lg'
		style='margin: 10px 20px; color: black;'></a>
	<div class="container">
		<center>
			<h2>auditCheck</h2>
		</center>
		<form action="../insertForm/auditCheck" id="idauditCheck"
			class="form-horizontal" method="post">
			<div class="form-group">
				<label class="control-label col-sm-4">code</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" name="code" id="code"
						tabindex="1" maxlength="10" data-rule="required|Aplha_numeric" />
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-4">pwd</label>
				<div class="col-sm-5">
					<input type="password" class="form-control" name="pwd" id="pwd"
						tabindex="2" maxlength="8" data-rule="required|Password" /><input
						type="checkbox" onclick="showPassword('pwd')">Show
					Password
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-4 col-sm-5">
					<input type="submit" class="btn btn-success" id="submitauditCheck"
						value="Submit" />
				</div>
			</div>
		</form>
		<font color='red'><p id="errMessage"></p></font>
		<c:if test="${not empty message}">
			<font color='green'><c:out value="${message }"></c:out></font>
		</c:if>
		<c:if test="${not empty errMessage}">
			<font color='red'> ${errMessage } </font>
		</c:if>
	</div>
	<div class="container" style="overflow-x: auto;">
		<c:if test="${not empty listData}">
			<table class="table">
				<thead style='color: #ccffe6; background-color: #87ceeb'>
					<tr>
						<th>code</th>
						<th>pwd</th>
					</tr>
				</thead>
				<c:forEach items="${listData}" var="obj">
					<tbody>
						<tr>
							<td class="col-m-2"><c:out value="${obj.code}" /></td>
							<td class="col-m-2"><c:out value="${obj.pwd}" /></td>
						</tr>
					</tbody>
				</c:forEach>
			</table>
		</c:if>
	</div>
</body>
<script src="../js/formutility_date.js"></script>
<script>
	$(function() {
		$('#submitauditCheck').click(function() {
			document.getElementById("errMessage").innerHTML = "";
			return true;
		});
	});
</script>
<script>
	new Validator(
			document.querySelector('#idauditCheck'),
			function(err, res) {
				return res;
			},
			options = {
				rules : {
					Aplha_numeric : function(a) {
						return (new RegExp('^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$'))
								.test(a)
					},
					Password : function(a) {
						return (new RegExp(
								'(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[@#$%^&+=].*[@#$%^&+=])(?=\\S+$).{8,}'))
								.test(a)
					},
				},
				messages : {
					en : {
						Aplha_numeric : {
							empty : "Only alphanumeric allow.",
							incorrect : "Only alphanumeric allow."
						},
						Password : {
							empty : "Password must be, 2 Latter Capital,2 special char ,and any later and any digit",
							incorrect : "Password must be, 2 Latter Capital,2 special char ,and any later and any digit"
						},
					}
				}
			});
</script>
<script></script>
</html>
