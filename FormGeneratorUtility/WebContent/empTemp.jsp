<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html >
<html>
<head>
<title>empTemp</title>
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
<script src="../js/jquery.serializejson.js"></script>
<script>
	$(document).ready(function() {
		$('[data-toggle="popover"]').popover();
	});
</script>
</head>
<body>
	<a href='../index' class='glyphicon glyphicon-home btn-lg'
		style='text-decoration: none; margin: 10px 20px; color: black;'></a>
	<a class='glyphicon glyphicon-question-sign btn-lg' href='#'
		title='Help' data-toggle='popover' data-placement='left'
		data-trigger='focus' data-content='This is emp temp form.'
		style='text-decoration: none; position: absolute; top: 0; right: 0; margin: 10px 20px; color: black;'></a>
	<div class="container">
		<center>
			<h2>empTemp</h2>
		</center>
		<form action="../insertForm/empTemp" id="idempTemp"
			class="form-horizontal" method="post" enctype="multipart/form-data">
			<div class="form-group">
				<label class="control-label col-sm-4">code</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" name="code" id="code"
						tabindex="1" maxlength="5" title=":title"
						data-rule="required|Aplha_numeric" />
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-4">gender</label>
				<div class="col-sm-5">
					<div class='row'>
						<div class='col-sm-6'>
							<input type="radio" name="gender" id="gender" tabindex="2"
								title=":title" value=male data-rule="required" />male<br />
						</div>
						<div class='col-sm-6'>
							<input type="radio" name="gender" id="gender" tabindex="2"
								title=":title" value=female data-rule="required" />female<br />
						</div>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-4">project</label>
				<div class="col-sm-5">
					<div class='row'>
						<div class='col-sm-6'>
							<input type="checkbox" name="projcet" id="projcet1" tabindex="3"
								title=":title" value=HSIDC /> HSIDC<br />
						</div>
						<div class='col-sm-6'>
							<input type="checkbox" name="projcet" id="projcet2" tabindex="3"
								title=":title" value=Alfresco /> Alfresco<br />
						</div>
						<div class='col-sm-6'>
							<input type="checkbox" name="projcet" id="projcet3" tabindex="3"
								title=":title" value=CRM /> CRM<br />
						</div>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-4">dob</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" name="dob" id="dob"
						tabindex="4" onkeypress="return false;" readonly title=":title"
						data-rule="required" />
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-4">File upload</label>
				<div class="col-sm-5">
					<input type="file" class="form-control" name="file" id="file"
						tabindex="5" onkeypress="return false;" />
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-4 col-sm-5">
					<input type="button" class="btn btn-success" id="submitempTemp"
						value="Submit" onClick="insert()"/>
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
						<th>gender</th>
						<th>project</th>
						<th>dob</th>
					</tr>
				</thead>
				<c:forEach items="${listData}" var="obj">
					<tbody>
						<tr>
							<td class="col-m-2"><c:out value="${obj.code}" /></td>
							<td class="col-m-2"><c:out value="${obj.gender}" /></td>
							<td class="col-m-2"><c:out value="${obj.projcet}" /></td>
							<td class="col-m-2"><c:out value="${obj.dob}" /></td>
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
		$('#submitempTemp')
				.click(
						function() {
							if (!$('input[name=projcet]:checked').length > 0) {
								document.getElementById("errMessage").innerHTML = "project can not be null";
								return false;
							}
							document.getElementById("errMessage").innerHTML = "";
							return true;
						});
	});
</script>
<!-- <script>
	new Validator(document.querySelector('#idempTemp'), function(err, res) {
		return res;
	}, options = {
		rules : {
			Aplha_numeric : function(a) {
				return (new RegExp('^[A-Za-z0-9]{1}[A-Za-z0-9\\s]{0,}$'))
						.test(a)
			},
		},
		messages : {
			en : {
				Aplha_numeric : {
					empty : "Only alphanumeric allow.",
					incorrect : "Only alphanumeric allow."
				},
			}
		}
	});
</script> -->
<script>
	$('#dob').datetimepicker({
		timepicker : false,
		maxDate : '+1970/01/01',
		format : 'Y-m-d',
		formatDate : 'Y-m-d',
	});
</script>
<script>
function insert(){
	console.log($('#idempTemp').serializeJSON());
	$.ajax({				
		type : "POST",
		url : "../insert/empTemp",
		data:  /* ConvertFormToJSON('#idempTemp') */ JSON.stringify( $('#idempTemp').serializeJSON()) /* $('#idempTemp').serializeJSON() */ ,
		contentType: "application/json; charset=utf-8",
        dataType: "json",
        cache: false,
		success : function(result) 
		{
			alert("Insert" + result);
		}
	});
}
function ConvertFormToJSON(form){
    var array = jQuery(form).serializeArray();
    var json = {};
    
    jQuery.each(array, function() {
        json[this.name] = this.value || '';
    });
    
    return json;
}
</script>

</html>
