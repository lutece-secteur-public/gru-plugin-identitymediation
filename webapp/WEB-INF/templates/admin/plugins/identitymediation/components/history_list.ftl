<link href="css/admin/plugins/identitymediation/timeline.css" rel="stylesheet" />
<#assign description>
  <#if current_rule_code??>
    <#list duplicate_rule_list as rule>
      <#if rule.code == current_rule_code>
        #i18n{identitymediation.search_history_ruleFilter} <strong>${rule.name}</strong>
      </#if>
    </#list>
  <#else>
    #i18n{identitymediation.search_history_ruleFilter.all}   
  </#if>
</#assign>
<#if identity_history_date_list?size == 0>
  <@pageColumn flush=true center=true class=" bg-secondary ">
      <@messages infos=infos />
      <@messages errors=errors />
      <@messages warnings=warnings />
    <div class="jumbotron jumbotron-fluid text-center pb-4">
      <div class="col-7 mt-5 mb-3 card bg-secondary p-5 rounded-5 shadow-none" style="margin:0 auto">
        <i class="ti ti-activity-heartbeat fs-1"></i>
        <h1 class="mt-3">#i18n{identitymediation.search_history.noActivity}</h1>
        ${description}
      </div>
    </div>
  </@pageColumn>
<#else>
  <@pageColumn flush=true class=" bg-secondary ">
    <div class="jumbotron jumbotron-fluid d-flex align-items-center justify-content-center text-center pb-3">
      <div class="col-8 mt-5">
        <#assign title><i class="ti ti-activity"></i> #i18n{identitymediation.search_history.pageTitle}</#assign>
        <@pageHeader title=title description=description>
        </@pageHeader>
        <@messages infos=infos />
        <@messages errors=errors />
        <@messages warnings=warnings />
      </div>
    </div>
    <ul class="timeline">
      <#list identity_history_date_list as modificationDate, innerMap>
        <#list innerMap as identityDto, attributeChanges>
        <#assign familyNameAttr = identityDto.attributes?filter(a -> a.key == "family_name")?first />
        <#assign firstNameAttr = identityDto.attributes?filter(a -> a.key == "first_name")?first />
          <li>
            <div class="timeline-time">
              <span class="date">${(modificationDate?number_to_datetime)?string("d MMMM yyyy")}</span>
              <span class="time">${(modificationDate?number_to_datetime)?string("HH:mm")}</span>
            </div>
            <div class="timeline-icon">
              <a href="javascript:;">&nbsp;</a>
            </div>
            <div class="card timeline-body shadow-lg mb-0 rounded-4">
              <div class="timeline-content d-flex justify-content-between align-items-center">
                <div class="text-start flex-grow-1">
                  <h3>     
                    <#if familyNameAttr?has_content>
                      ${familyNameAttr.value} ${firstNameAttr.value}
                    <#else>N/A</#if>
                  </h3>
                  <p class="m-0">#i18n{identitymediation.search_history.modify} <strong>${attributeChanges[0].authorName!"N/A"}</strong> - #i18n{identitymediation.search_history.type} : <strong>${attributeChanges[0].authorType!"N/A"}</strong></p>
                  <div class="mt-3 mb-0">
                    <@tag color="success" class="mt-1">
                      <i class="ti ti-check"></i> Rapprochement
                    </@tag>
                    <#list attributeChanges as attributeChange>
                      <@tag color="primary" class="mt-1">
                        <i class="ti ti-forms"></i> ${getName(attributeChange.attributeKey)}
                      </@tag>
                    </#list>
                  </div>
                </div>
              </div>
            </div>
          </li>
        </#list>
      </#list>
    </ul>
  </@pageColumn>
</#if>
