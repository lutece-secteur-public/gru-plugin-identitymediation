<#-- static files -->
<link href="css/admin/plugins/identitymediation/mediation.css" rel="stylesheet" />

<#-- 
  Retrieves the name associated with a given keyName in the list of attribute definitions.
  @param keyName                  The keyName for which to find the corresponding name.
  @param service_contract         service contract attributeDefinitions.
  @return Returns the attribute name from the service contract.
-->
<#function getName keyName >
  <#assign targetName = keyName>
  <#list service_contract.attributeDefinitions as attr>
    <#if attr.keyName == keyName>
      <#assign targetName = "${attr.name}">
    </#if>
  </#list>
  <#return targetName>
</#function>
