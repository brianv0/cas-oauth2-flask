<%@page import="org.pac4j.core.context.*"%>
<%@ page import="org.pac4j.core.profile.ProfileManager" %>
<%
	WebContext context = new J2EContext(request, response);
	ProfileManager manager = new ProfileManager(context);
%>
<h1>index</h1>
<a href="cas/index.jsp">Protected url by CAS: cas/index.jsp</a> (use login = pwd)<br />
<a href="saml2/index.jsp">Protected url by SAML2: saml2/index.jsp</a> (use testpac4j at gmail.com / Pac4jtest)<br />
<a href="oidc/index.jsp">Protected url by Google OpenID Connect: oidc/index.jsp</a> (use a real account)<br />
<a href="protected/index.jsp">Protected url: protected/index.jsp</a> (won't start any login process)<br />
<br />
<a href="jwt.jsp">Generate a JWT token</a> (after being authenticated)<br />
<a href="/rest-jwt/index.jsp">Protected url by ParameterClient: /rest-jwt/index.jsp</a> (with request parameter: token=<em>jwt_generated_token</em>)<br />
<br />
<a href="/logout?url=/?forcepostlogouturl">logout</a>
<br /><br />
sessionId: <%=session.getId()%><br />
profiles: <%=manager.getAll(true)%>