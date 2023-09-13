<@pageColumn id="mediation-filter-menu" width="22rem" responsiveMenuSize="xxl" responsiveMenuPlacement="start"
    responsiveMenuTitle="#i18n{identitymediation.search_duplicates.pageTitle}" class=" pt-xl-4 px-md-4 ">
      <div class="">
    <h1 class="mb-0 py-2 pb-1">#i18n{identitymediation.search_duplicates.pageTitle}
    </h1>  
  </div>
    <hr class="d-none d-xxl-block">
    <h2 class="fw-bolder mb-2 mt-xxl-4"><i class="ti ti-user-search"></i> #i18n{identitymediation.search_duplicates.searchTitle}</h2>
    <@tform method='post' name='search_identity' action='jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates'
      id='identitySearch' class='border-bottom pb-3 mt-4'>
      <input type='hidden' name='code' value='${current_rule_code!""}' />
      <@formGroup labelKey='#i18n{identitymediation.search_duplicates.first_name}' labelFor='first_name'
        hideLabel=['all'] rows=2>
        <@input type='text' id='first_name' name='first_name' value='${first_name!""}'
          placeHolder='#i18n{identitymediation.search_duplicates.first_name}' size='' />
      </@formGroup>
      <@formGroup labelKey='#i18n{identitymediation.search_duplicates.family_name}' labelFor='family_name'
        hideLabel=['all'] rows=2>
        <@input type='text' id='family_name' name='family_name' value='${family_name!""}'
          placeHolder='#i18n{identitymediation.search_duplicates.family_name}' size='' />
      </@formGroup>
      <@formGroup labelKey='#i18n{identitymediation.search_duplicates.birthdate}' labelFor='birthdate'
        hideLabel=['all'] rows=2>
        <@input type='text' id='birthdate' name='birthdate' value='${birthdate!""}'
          placeHolder='#i18n{identitymediation.search_duplicates.birthdate}' size='' />
      </@formGroup>
      <@formGroup rows=2>
        <@button type='submit' style='w-100' buttonIcon='search' title='#i18n{portal.util.labelSearch}' color='primary'
          size='' />
      </@formGroup>
    </@tform>
    <h2 class="fw-bolder mb-2 mt-4"><i class="ti ti-filters"></i> #i18n{identitymediation.search_duplicates.rulesTitle}</h2>
    <ul class="list-group mt-4">
      <#list duplicate_rule_list as rule>
        <#assign isCurrent = (current_rule_code?? && rule.code == current_rule_code) />
        <a href="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates&code=${rule.code}"
          class="list-group-item list-group-item-action <#if isCurrent>bg-primary-subtle</#if>"
          id="${rule.code}">
          <div class="d-flex justify-content-between align-items-center">
            <span class="my-auto <#if isCurrent>fw-bolder</#if>">${rule.name}</span>
            <div class="d-flex align-items-center">
              <#if rule.duplicateCount?number gt 0>
                <@tag color="danger">${rule.duplicateCount}</@tag>
              <#else>
                <@tag color="success">${rule.duplicateCount}</@tag>
              </#if>
            </div>
          </div>
        </a>
      </#list>
    </ul>    
</@pageColumn>