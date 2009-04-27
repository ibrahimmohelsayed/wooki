<!DOCTYPE html>
<% String version = (String)session.getAttribute("version");
// "version" is lazy loaded and will be empty until a request is made to the server
if (version==null){
	version="";
}
%>
<html>
<head>
<title>Wookie Widget Server <%=version%></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link type="text/css" href="/wookie/shared/js/jquery/themes/redmond/jquery-ui-1.7.1.custom.css" rel="stylesheet" />    
	<link type="text/css" href="../layout.css" rel="stylesheet" />
	<script type="text/javascript" src="/wookie/shared/js/jquery/jquery-1.3.2.min.js"></script>
	<script type="text/javascript" src="/wookie/shared/js/jquery/jquery-ui-1.7.custom.min.js"></script>
	<script>
	<!--
	var isValidEmailAddressFlag = false;
	
	function isValidEmailAddress(emailAddress) {
		var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
		return pattern.test(emailAddress);
	}

	function checkValue(){
		if (document.requestkeyform.email.value.length < 1 || isValidEmailAddressFlag==false){			
	        doDialog();
	        return;      
	 	}	 	
		document.requestkeyform.submit();	
	}
	
	$(document).ready(function() {		 
		$("#email").keyup(function(){
			var email = $("#email").val();		 
			if(email != 0){
				if(isValidEmailAddress(email)){		 
					$("#validEmail").css({ "background-image": "url('../shared/images/validyes.png')" });
					isValidEmailAddressFlag = true;		 
				} 
				else {		 
					$("#validEmail").css({ "background-image": "url('../shared/images/validno.png')" });
					isValidEmailAddressFlag = false;		 
				}			 
			} 
			else {		 
				$("#validEmail").css({ "background-image": "none" });		 
			}
		});
	});

	function doDialog(){
		$("#confirm").dialog({
			bgiframe: true,
	    	autoOpen: false,
	    	buttons: {
	            "Ok": function() { $(this).dialog("close"); }
	    	},
	    	resizable: false,                        
	    	modal: true,
	    	overlay: {
				backgroundColor: '#00000',
				opacity: 0.25
			},
		});			    
		$('#confirm').dialog('open');  
	}
  	//-->
	</script>  
</head>
<body>
    <div id="header">
 		<div id="banner">
    		<div style="float:left;">
    			<img style="margin: 4 8px;" border="0" src="../shared/images/furry_white.png">
    		</div>
    		<div id="menu"><a class="menulink" href="index.jsp">menu&nbsp;<img border="0" src="../shared/images/book.gif"></a>&nbsp;</div>
    	</div> 
    	<div id="pagetitle">
    		<h3>Request an API key</h3>
    	</div>
    	<!--  END HEADER -->
	</div>
     
    <div id="content">    
	
	<% String errors = (String)session.getAttribute("error_value");%>
	<% String messages = (String)session.getAttribute("message_value");%>
	<%if(errors!=null){%>
      <p><img src="../shared/images/cancel.gif" width="16" height="16"><font color=red> <%=errors%> </font> </p>
	<%}%>
	<%if(messages!=null){%>
	<p><img src="../shared/images/greentick.gif" width="16" height="16">
		<font color=green>
		<%=messages%>
		</font>
	</p>
	<%}%>
	
<p>
To request an API key, enter your email address and click Request
<form name="requestkeyform" method="post" action="WidgetWebMenuServlet">
<br>
<table class="ui-widget ui-widget-content" align="center" border="0">
<tr class="ui-widget-header"><td colspan="2">&nbsp;Request key</td></tr>
<tr>  
  <td>Email</td> 
  <td><input style="float:left;" name="email" size="50" type="text" id="email" value=""><div id="validEmail"></div></td>
</tr>
<tr>  
  <td colspan="2" align="center">
    <input class="ui-button ui-state-default ui-corner-all" type="button" name="Submit" value="Request" onClick="checkValue()">
    <input type="hidden" name="operation" value="REQUESTAPIKEY">
  </td>  
</tr>
</table>

</form>


</div>
	
<div id="footer">
	<div style="text-align:right"><a class="menulink" href="index.jsp">menu&nbsp;<img border="0" src="../shared/images/book.gif"></a>&nbsp;</div>
</div>
<div id="confirm" style="display:none;" title="Warning"><span class="ui-icon ui-icon-alert" style="float:left;"></span>Invalid email address</div>
</body>
</html>