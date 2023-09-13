<#list duplicate_rule_list as rule>
<#if rule.code == current_rule_code>
<#assign title><strong>${rule.duplicateCount}</strong> <#if rule.duplicateCount?number gt 1>#i18n{identitymediation.choose_duplicate_type.pendingDuplicates}<#else>#i18n{identitymediation.choose_duplicate_type.pendingDuplicate}</#if></#assign>
</#if>
</#list>
<@pageColumn width="25rem" flush=true responsiveMenuSize="xxl" responsiveMenuPlacement="start"
    responsiveMenuTitle="#i18n{identitymediation.search_duplicates.pageTitle}" id="mediation-duplicate-list" class=" border-start-0 ">
  <div class="bg-body border-bottom p-4 sticky-top shadow">
    <h1 class="text-center mb-0 py-2 pb-1">${title!''}
    </h1>  
  </div>
  <ul class="list-group list-group-flush">
    <#if mediation_identity_list?size gt 0>
      <#list mediation_identity_list as mediation_identity>
        <a href='jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view=selectIdentities&cuid=${mediation_identity.suspiciousIdentity.customerId}&rule-code=${current_rule_code}' class="list-group-item list-group-item-action px-4 py-3">
            <#assign familyNameAttr = mediation_identity.bestIdentity.attributes?filter(a -> a.key == "family_name")?first!{}>
            <#assign firstNameAttr = mediation_identity.bestIdentity.attributes?filter(a -> a.key == "first_name")?first!{}>
            <#assign emailAttr = mediation_identity.bestIdentity.attributes?filter(a -> a.key == "email")?first!{}>
            <#assign birthdateAttr = mediation_identity.bestIdentity.attributes?filter(a -> a.key == "birthdate")?first!{}>
            <div class="d-flex w-100 justify-content-between">
              <h3 class="mb-1 title mt-1 text-break fw-bold">
                <#if familyNameAttr??>
                  ${familyNameAttr.value!'-'}
                </#if>
                <#if familyNameAttr??>
                  ${firstNameAttr.value!'-'}
                </#if>
              </h3>
            <div>
              <#if mediation_identity.duplicatesToMergeAttributes??>
              <#assign duplicatesToMergeAttributesSize = mediation_identity.duplicatesToMergeAttributes?size />
              <#if duplicatesToMergeAttributesSize gt 1>
                <@tag color="primary"><strong>${duplicatesToMergeAttributesSize}</strong> #i18n{identitymediation.search_duplicates.status.selection}</@tag>
              <#elseif  duplicatesToMergeAttributesSize == 1 >
                <#assign firstKey = mediation_identity.duplicatesToMergeAttributes?keys[0]>
                <#if firstKey.monParisActive >
                  <@tag color="warning">#i18n{identitymediation.search_duplicates.status.notification}</@tag>
                <#else>
                  <@tag color="warning">#i18n{identitymediation.search_duplicates.status.merge}</@tag>
                </#if>
              <#else>
                <@tag color="success">#i18n{identitymediation.search_duplicates.status.empty}</@tag>
              </#if>
            </#if>
              </div>
              </div>
              <#if emailAttr.value??>
                <div>${emailAttr.value}</div>
              </#if>
              <#if birthdateAttr.value??>
              <div>${birthdateAttr.value}</div>
            </#if>
            <#if mediation_identity.duplicatesToMergeAttributes??>
          </#if>
        </a>
      </#list>
    </#if>
  </ul>
</@pageColumn>