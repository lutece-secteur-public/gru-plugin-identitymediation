<jsp:useBean id="identityDuplicate" scope="session" class="fr.paris.lutece.plugins.identitymediation.web.IdentityDuplicateJspBean" />
<% String strContent = identityDuplicate.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
