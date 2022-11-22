<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper"%>
<%@page import="eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapperConfiguration"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<jsp:directive.page import="eu.earthobservatory.org.StrabonEndpoint.Common"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" href="style.css" type="text/css"/> 
		<script type="text/javascript">
			function toggleMe(a) {
				var e = document.getElementById(a);
				if (!e) {
					return true;
				}
				if (e.style.display == "none") {
					e.style.display = "block";
				} else {
					e.style.display = "none";
				}
				return true;
			}
		</script>
		
		<%String handle = "";
		if (request.getParameter("handle") != null) 
		{
			handle = request.getParameter("handle");
			
		} 
		else if (request.getAttribute("handle") != null) 
		{
			handle = (String) request.getAttribute("handle");
			
		}
		
		String selFormat = "RDF/XML";
		if (request.getParameter("format") != null) {
			selFormat = request.getParameter("format");
			
		} else if (request.getAttribute("format") != null) {
			selFormat = (String) request.getAttribute("format");
			
		}%>
		<!-- jQuery start  -->
		<link type="text/css" href="style-menu.css" rel="stylesheet" />
		<script type="text/javascript" src="js/jquery-1.8.0.min.js"></script>
		<script type="text/javascript" src="js/jquery-ui-1.8.23.custom.min.js"></script>
		<script type="text/javascript">
		$(function(){
				// Accordion
				$("#accordion").accordion({ 
					header: "h3",
					fillSpace: true,
					navigation: true,
					collapsible: true
				});
				//hover states on the static widgets
				$('#dialog_link, ul#icons li').hover(
					function() { $(this).addClass('ui-state-hover'); },
					function() { $(this).removeClass('ui-state-hover'); }
				);
		});
		</script>
		<style type="text/css">
		/*demo page css*/
		body{ font: 90% "Trebuchet MS", sans-serif; margin: 50px;}
		.container { height:410px; width:165px;}
		.demoHeaders { margin-top: 1em;}
		#dialog_link {padding: .4em 1em .4em 20px;text-decoration: none;position: relative;}
		#dialog_link span.ui-icon {margin: 0 5px 0 0;position: absolute;left: .2em;top: 50%;margin-top: -8px;}
		ul#icons {margin: 0; padding: 0;}
		ul#icons li {margin: 1px; position: relative; padding: 1px 0; cursor: pointer; float: left;  list-style: none;}
		ul#icons span.ui-icon {float: left; margin: 0 1px;}
	</style>
	
		<title>Strabon Endpoint</title>
	
	</head>
<body topmargin="0" leftmargin="0" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF">

<!-- include TELEIOS header and description -->
<jsp:include page="header.html"/>
<!-- include TELEIOS header and description -->

<FORM enctype="UTF-8" accept-charset="UTF-8" method="post" action="Describe">
<INPUT type=hidden name="view" value="HTML"/>

<table border="0" width="100%">
<tr> 
	<td width="90" valign="top"> 
		<table border="0" cellspacing="0" cellpadding="0" width="165">  
		<tr><td id="twidth">
		<div class="container">
		<div id="accordion">
		<%
							StrabonBeanWrapper strabonWrapper;
							ServletContext context;
							context = getServletContext();
							WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
							strabonWrapper=(StrabonBeanWrapper) applicationContext.getBean("strabonBean");
							Iterator <StrabonBeanWrapperConfiguration> entryListIterator = strabonWrapper.getEntries().iterator();
							boolean first = true;
							String hash = "";
							while(entryListIterator.hasNext())
							{
								StrabonBeanWrapperConfiguration entry = entryListIterator.next();
								
								if (entry.isHeader()) {
									if (!first) {
										%>
										</div></div>
										<%
									} else {
										first = false;
									}
									
									String label=entry.getLabel();
									String style = "", href = "";
									hash = new Integer(Math.abs(label.hashCode())).toString();
									href="href=\"#"+hash+"\"";									
									%>
									<div><h3><a <%=style%> <%=href%>><%=label%></a></h3><div>
									<%									
								} else if (entry.isBean()) {
									String label=entry.getLabel();
									String bean=entry.getBean();
									if(bean.equals("browse.jsp") || bean.equals("ChangeConnection"))
										continue;
									String style = "", href = "";
									hash = new Integer(Math.abs(label.hashCode()*bean.hashCode())).toString();
									href = "\"" +bean + "#"+ hash+"\"";
									style = "class=\"navText\"";
									%>
									<b>&middot;</b>&nbsp;<a class="linkText" href=<%=href%>><%=label%></a><br/>
									<%
								} else {
									String href="\""+URLEncoder.encode(entry.getBean(),"utf-8")+"?view=HTML&handle="+entry.getHandle()+"&query="+URLEncoder.encode(entry.getStatement(),"utf-8")+"&format="+URLEncoder.encode(entry.getFormat(),"utf-8")+(hash == "" ? "" : "#" + hash)+"\"";
									String title="\""+entry.getTitle()+"\"";
									String label=entry.getLabel();
									%>
									<b>&middot;</b>&nbsp;<a class="linkText" href=<%=href%> title=<%=title%>><%=label%></a><br/>
									<%
			
								}
							}
		%>
		</div>
		</div>
	</td>
</tr>
<!-- 
<tr><td width="90" class="style4"><a href="describe.jsp" class="navText">Describe</a></td></tr>
<tr><td width="90" class="style4"><a href="store.jsp" class="navText" title="Store triples">Store</a></td></tr>
 --> 
</table>
	</td>
<td width="*" valign="top">
<table cellspacing="5">
<tr>
<td id="output" style="width: 150px">stSPARQL Query:</td>
<td id="output"><textarea name="query" title="pose your DESCRIBE query here" rows="15" cols="100">
<%=request.getParameter("query") != null ? request.getParameter("query"):""%></textarea></td>
</tr>
<tr>
<td id="output">Output Format:</td>
<td id="output">
<SELECT name="format" title="select one of the following RDF graph format types">
	<% for (String format : Common.registeredFormats) {%>
		<OPTION value="<%=format%>"<%=format.equals(selFormat) ? "selected":""%>><%=format%></OPTION>
	<%}%>
</SELECT>
</td>
</tr>
<tr>
<td id="output">View Result:</td>
<td id="output">
	<SELECT name="handle" title="select how you would like to view the result">
		<OPTION value="plain"<%= ("plain".equals(handle)) ? "selected":""%>>Plain</OPTION>
		<OPTION value="download"<%= ("download".equals(handle)) ? "selected":""%>>Download</OPTION>		
	</SELECT>
</td>
</tr>
<tr>
<td colspan=2 id="output"><br/>
<center>
	<input type="submit" title="execute DESCRIBE query" value="Describe" name="submit" style="width: 350px"/>
</center><br/>
</td>
</tr>



<% if (request.getAttribute("error") != null) {%>
	<!-- Error Message -->
	<TR>
		<TD id="output">Result: </TD><TD id="output"><%=request.getAttribute("error") %></TD>
	</TR>
	<!-- Error Message -->	
<%}%>


</table></td></tr>


</table>
</form>
<% if (request.getAttribute("response") != null) {%>
	<!-- Response -->
	<div><%=request.getAttribute("response") %></div>
	<!-- Response -->
<%}%>
</body>
</html>