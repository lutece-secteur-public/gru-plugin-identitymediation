<jsp:useBean id="identityLocks" scope="session" class="fr.paris.lutece.plugins.identitymediation.web.IdentityLocksJspBean" />
<% String strContent = identityLocks.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>