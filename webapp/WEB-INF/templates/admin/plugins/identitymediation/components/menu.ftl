<#--
  Element: Menu

  Displays the filtering menu for identity search and mediation.

  The element produces a column featuring a form section that allows the user to filter
  potential duplicates based on different rules and to provide search criteria to refine the results.

  @param first_name The first name to search for.
  @param family_name The family name to search for.
  @param birthdate The date of birth to search for.
  @param current_rule_code The current rule code in context.
  @param duplicate_rule_list The list of rules for finding duplicates.
  
  @returns A rendered column for identity mediation and search.
  
  Usage:
    <@pageColumn ... />
-->
<@pageColumn id="mediation-filter-menu" width="22rem" responsiveMenuSize="xl" responsiveMenuPlacement="start"
    responsiveMenuTitle="#i18n{identitymediation.search_duplicates.pageTitle}" class=" pt-xl-4 px-md-4 ">
    <div class="">
        <h1 class="mb-0 py-2 pb-1">#i18n{identitymediation.search_duplicates.pageTitle}</h1>
    </div>
    <hr class="d-none d-xxl-block">
    <div class="" id="searchForm">
        <div class="bg-transparent">
            <div class=" bg-transparent">
                <div id="searchContent" class="show" aria-labelledby="searchHeader" data-bs-parent="#searchAccordion">
                    <div class="">
                        <@tform method='post' name='search_identity' action='jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates'
                        id='identitySearch'>
                            <input type='hidden' id="ruleCode" name='code' value='${current_rule_code!""}' />
                            <input type='hidden' id="currentPage" name='page' value='${current_page!""}' />
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
                                <div class="col-xs-6">
                                    <button id="sendForm" type="submit" style="width: 45%;"
                                            class="btn btn-primary btn-sm text-decoration-none border rounded-5">
                                        <span><i class="ti ti-filter"></i> #i18n{portal.util.labelFilter}</span>
                                    </button>
                                    <button id="resetForm" type="button" style="width: 45%;"
                                            class="btn btn-primary btn-sm text-decoration-none border rounded-5" onclick="resetSuspectForm()">
                                        <span><i class="ti ti-rotate"></i> #i18n{portal.util.labelReset}</span>
                                    </button>
                                </div>
                            </@formGroup>
                            <hr>
                            <div id="rulesContent" class="show" aria-labelledby="rulesHeader" data-bs-parent="#searchAccordion">
                                <div class="p-0">
                                    <ul class="list-group list-group-flush">
                                        <#list duplicate_rule_list as rule>
                                            <#assign isCurrent = (current_rule_code?? && rule.code == current_rule_code) />
                                            <a onclick="sendForm(`${rule.code}`, ${isCurrent?c});" style="cursor: pointer;"
                                               class="list-group-item list-group-item-action border-bottom-0 <#if isCurrent>text-primary</#if>"
                                               id="${rule.code}">
                                                <div class="d-flex justify-content-between align-items-center">
                                                    <span class="my-auto <#if isCurrent>fw-bolder</#if>">${rule.name}</span>
                                                    <div class="d-flex align-items-center">
                                                        <#if count_duplicate_by_rule?size == duplicate_rule_list?size>
                                                            <#if count_duplicate_by_rule[rule.code] gt 0>
                                                                <@tag color="danger" class="border-1">${count_duplicate_by_rule[rule.code]}</@tag>
                                                            <#else>
                                                                <@tag color="success">${count_duplicate_by_rule[rule.code]}</@tag>
                                                            </#if>
                                                        <#else>
                                                            <#if rule.duplicateCount?number gt 0>
                                                                <@tag color="danger" class="border-1">${rule.duplicateCount}</@tag>
                                                            <#else>
                                                                <@tag color="success">${rule.duplicateCount}</@tag>
                                                            </#if>
                                                        </#if>
                                                    </div>
                                                </div>
                                            </a>
                                        </#list>
                                    </ul>
                                </div>
                            </div>
                        </@tform>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        function sendForm(code, isCurrent) {
            if(isCurrent) {
                code = "";
            }
            document.getElementById('ruleCode').value = code;
            document.getElementById('currentPage').value = 1;
            document.getElementById('sendForm').click();
            return false;
        }
        function resetSuspectForm() {
            document.getElementById('ruleCode').value = "";
            document.getElementById('first_name').value = "";
            document.getElementById('family_name').value = "";
            document.getElementById('birthdate').value = "";
            document.getElementById('currentPage').value = 1;
            document.getElementById('sendForm').click();
            return false;
        }
    </script>
</@pageColumn>