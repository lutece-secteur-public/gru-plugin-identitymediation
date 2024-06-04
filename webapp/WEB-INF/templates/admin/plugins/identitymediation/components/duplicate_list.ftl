<#if duplicate_rule_list?has_content && mediation_identity_list?has_content && current_page??>
    <#if count_total_duplicated gt 0 >
        <#if current_rule_code?has_content>
            <#assign title><strong>${count_duplicate_by_rule[current_rule_code]}</strong> <#if count_duplicate_by_rule[current_rule_code] gt 1>#i18n{identitymediation.choose_duplicate_type.pendingDuplicates}<#else>#i18n{identitymediation.choose_duplicate_type.pendingDuplicate}</#if></#assign>
        <#else>
            <#assign title><strong>${count_total_duplicated}</strong> <#if count_total_duplicated gt 1>#i18n{identitymediation.choose_duplicate_type.pendingDuplicates}<#else>#i18n{identitymediation.choose_duplicate_type.pendingDuplicate}</#if></#assign>
        </#if>
    <#else>
      <#list duplicate_rule_list as rule>
          <#if rule.code == current_rule_code>
              <#assign title><strong>${rule.duplicateCount}</strong> <#if rule.duplicateCount?number gt 1>#i18n{identitymediation.choose_duplicate_type.pendingDuplicates}<#else>#i18n{identitymediation.choose_duplicate_type.pendingDuplicate}</#if></#assign>
          </#if>
      </#list>
    </#if>
    <#assign isClosed = false />
    <#if identity_list?? && identity_list?size gt 3>
    <#assign isClosed = true />
    </#if>
    <@pageColumn width="25rem" flush=true responsiveMenuSize="xxl" responsiveMenuPlacement="end"
      responsiveMenuTitle="#i18n{identitymediation.search_duplicates.pageTitle}" id="mediation-duplicate-list" class=" border-start-0" responsiveMenuClose=isClosed>
        <div class="border-bottom p-4 sticky-top">
        <h1 class="text-center mb-0 py-2 pb-1">${title!''}
        </h1>
        </div>
        <ul id="duplicate-list" class="list-group list-group-flush overflow-auto" style="height:calc(100vh - 280px)">
          <#if mediation_identity_list?size gt 0>
            <#list mediation_identity_list as mediation_identity>
            <#assign selectedClasses></#assign>
              <#if suspicious_identity?? && suspicious_identity.customerId == mediation_identity.suspiciousIdentity.customerId >
                <#assign selectedClasses>bg-secondary-subtle shadow selected</#assign>
              </#if>
              <a href='jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view=selectIdentities&cuid=${mediation_identity.suspiciousIdentity.customerId}&code=${rule_by_identity[mediation_identity.suspiciousIdentity.customerId]}&page=${current_page}&family_name=${family_name!""}&first_name=${first_name!""}&birthdate=${birthdate!""}'
                 class="list-group-item list-group-item-action px-4 py-3 ${selectedClasses}" style="cursor: pointer;">
                  <#assign familyNameAttr = mediation_identity.suspiciousIdentity.attributes?filter(a -> a.key == "family_name")?first!{}>
                  <#assign firstNameAttr = mediation_identity.suspiciousIdentity.attributes?filter(a -> a.key == "first_name")?first!{}>
                  <#assign emailAttr = mediation_identity.suspiciousIdentity.attributes?filter(a -> a.key == "email")?first!{}>
                  <#assign birthdateAttr = mediation_identity.suspiciousIdentity.attributes?filter(a -> a.key == "birthdate")?first!{}>
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
        <nav aria-label="Pagination" class="border-top">
            <ul class="pagination justify-content-center mt-3 mb-0">
                <li class="page-item <#if current_page == 1>disabled</#if>">
                    <a class="page-link" onclick="changePage(${current_page - 1})" tabindex="-1" aria-disabled="true"><i class="ti ti-chevron-left"></i></a>
                </li>
                <#if total_pages gt 6>
                <li class="page-item">
                    <select id="paginationSelect" class="form-select rounded-0" style="width: auto;">
                        <#list 1..total_pages as page>
                            <option value="${page}" <#if current_page == page>selected</#if>>${page}</option>
                        </#list>
                    </select>
                </li>
                <#elseif total_pages gt 0>
                    <#list 1..total_pages as page>
                    <li class="page-item <#if current_page == page>border-primary-subtle border-end border-top-0 border-start-0 border-bottom-0</#if>">
                        <a class="page-link <#if current_page == page>text-primary-emphasis bg-primary-subtle border border-primary-subtle border-end</#if> <#if current_page == page - 1>border-start-0</#if>" onclick="changePage(${page})">${page}</a>
                    </li>
                    </#list>
                </#if>
                <li class="page-item <#if current_page == total_pages>disabled</#if>">
                    <a class="page-link" onclick="changePage(${current_page + 1})"><i class="ti ti-chevron-right"></i></a>
                </li>
            </ul>
        </nav>
    </@pageColumn>
    <script type="module">
        const selectedItem = document.querySelector('#duplicate-list .selected');
        const paginationSelect = document.getElementById("paginationSelect");
        paginationSelect && paginationSelect.addEventListener("change", function() {
          let selectedPage = this.value;
          window.location.href = "jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_searchDuplicates&code=${current_rule_code}&page=" + selectedPage;
        });
        if (selectedItem) {
            setTimeout(function() {
                const ulElement = document.getElementById("duplicate-list");
                ulElement.scrollTop = selectedItem.offsetTop - (ulElement.clientHeight / 2) + (selectedItem.clientHeight / 2);
            }, 100);
        }
    </script>
    <script>
        function changePage(page)
        {
            document.getElementById('currentPage').value = page;
            document.getElementById('sendForm').click();
        }
    </script>
</#if>