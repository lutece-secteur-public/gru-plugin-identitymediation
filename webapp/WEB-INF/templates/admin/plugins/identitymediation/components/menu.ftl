<#assign shouldOpenSearchAccordion = (first_name?has_content || family_name?has_content || birthdate?has_content)>
<@pageColumn id="mediation-filter-menu" width="22rem" responsiveMenuSize="xxl" responsiveMenuPlacement="start"
    responsiveMenuTitle="#i18n{identitymediation.search_duplicates.pageTitle}" class=" pt-xl-4 px-md-4 ">
    <div class="">
        <h1 class="mb-0 py-2 pb-1">#i18n{identitymediation.search_duplicates.pageTitle}</h1>
    </div>
    <hr class="d-none d-xxl-block">
    <div class="accordion accordion-flush" id="searchAccordion">
        <div class="accordion-item bg-transparent">
            <h2 class="accordion-header" id="rulesHeader">
                <button class="accordion-button collapsed px-2 bg-transparent" type="button" data-bs-toggle="collapse" data-bs-target="#rulesContent" aria-expanded="false" aria-controls="rulesContent">
                    <i class="ti ti-filters fs-2 me-2"></i> <strong>#i18n{identitymediation.search_duplicates.rulesTitle}</strong>
                </button>
            </h2>
            <div id="rulesContent" class="accordion-collapse collapse <#if !shouldOpenSearchAccordion>show</#if>" aria-labelledby="rulesHeader" data-bs-parent="#searchAccordion">
                <div class="accordion-body p-0">
                    <ul class="list-group list-group-flush">
                        <#list duplicate_rule_list as rule>
                            <#assign isCurrent = (current_rule_code?? && rule.code == current_rule_code) />
                            <a href="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates&code=${rule.code}"
                                class="list-group-item list-group-item-action border-bottom-0 <#if isCurrent>text-primary</#if>"
                                id="${rule.code}">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span class="my-auto <#if isCurrent>fw-bolder</#if>">${rule.name}</span>
                                    <div class="d-flex align-items-center">
                                        <#if rule.duplicateCount?number gt 0>
                                            <@tag color="danger" class="border-1">${rule.duplicateCount}</@tag>
                                        <#else>
                                            <@tag color="success">${rule.duplicateCount}</@tag>
                                        </#if>
                                    </div>
                                </div>
                            </a>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
        <div class="accordion-item bg-transparent">
            <h2 class="accordion-header" id="searchHeader">
                <button class="accordion-button collapsed px-2 bg-transparent" type="button" data-bs-toggle="collapse" data-bs-target="#searchContent" aria-expanded="false" aria-controls="searchContent">
                    <i class="ti ti-user-search me-2 fs-2"></i> #i18n{identitymediation.search_duplicates.searchTitle}
                </button>
            </h2>
            <div id="searchContent" class="accordion-collapse collapse <#if shouldOpenSearchAccordion>show</#if>" aria-labelledby="searchHeader" data-bs-parent="#searchAccordion">
                <div class="accordion-body">
                    <@tform method='post' name='search_identity' action='jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates'
                    id='identitySearch'>
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
                </div>
            </div>
        </div>
    </div>
</@pageColumn>