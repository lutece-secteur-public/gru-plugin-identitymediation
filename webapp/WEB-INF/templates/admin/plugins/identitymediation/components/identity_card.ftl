<#--
  Macro: identityCard
  Renders a card UI for identity details.
  @param identity The main data object containing the identity's details.
  @param index The index of the current identity in the list.
  @param merge Optional boolean parameter to conditionally render merge-related UI elements.
  @param class Optional CSS classes for customization.
  @param width Optional width for the card.
  @returns A rendered identity card based on provided parameters.
-->
<#macro identityCard identity index family_name first_name birthdate merge=false class="" width="">
    <#if index == 0>
        <#assign firstIdentity = identity>
    </#if>
    <#assign familyNameAttr = identity.attributes?filter(a -> a.key == "family_name")?first!{}>
    <#assign firstNameAttr = identity.attributes?filter(a -> a.key == "first_name")?first!{}>
    <#assign emailAttr = identity.attributes?filter(a -> a.key == "email")?first!{}>
    <#assign birthdateAttr = identity.attributes?filter(a -> a.key == "birthdate")?first!{}>
    <div class="lutece-compare-item-container border-end p-3 position-relative <#if index=0>bg-primary-subtle border border-primary-subtle rounded-start-5<#else> border-top border-bottom</#if><#if !merge & index=0> border-2 border-end-dashed </#if><#if !merge && index!=0> border-dark-subtle<#elseif merge & index!=0> border-warning-subtle</#if> ${class}" style="<#if width!=''>width:${width}</#if>">
        <div class="position-absolute start-50 translate-middle-x d-flex justify-content-center" style="top: -10px;">
            <#if merge && index=0>
                <div class="badge text-primary-emphasis bg-primary-subtle border border-primary rounded-5 px-2 py-1 mb-1 d-inline-block text-nowrap">
                    <i class="ti ti-check"></i> #i18n{identitymediation.resolve_duplicates.identityToKeep}
                </div>
            <#elseif merge && index!=0>
                <div class="badge text-warning-emphasis bg-warning-subtle border border-warning rounded-5 px-2 py-1 mb-1 d-inline-block text-nowrap">
                    <i class="ti ti-arrow-badge-left"></i> #i18n{identitymediation.resolve_duplicates.identityToMerge}
                </div>
            </#if>
        </div>
        <div class="lutece-compare-item card p-0 rounded-5 shadow-xl mb-0">
            <#if !merge>
                <#if index == 0>
                    <button type="button" class="btn btn-warning" data-name="identity-cuid-${index}" data-cuid="${identity.customerId}">
                        #i18n{identitymediation.select_identities.selectedIdentity}
                    </button>
                <#else>
                    <button type="button" class="btn btn-dark" data-name="identity-cuid-${index}" data-cuid="${identity.customerId}" disabled>
                        #i18n{identitymediation.select_identities.identityToProcess}
                    </button>
                </#if>
            </#if>
            <div class="py-4 text-center">
                <h3 class="px-2 text-truncate mb-0 copy-tooltip" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" data-customer-id="${identity.customerId}" title="<small  class='text-truncate text-break'>${identity.customerId?truncate(33)}</small>">
                <#if familyNameAttr?? && familyNameAttr.value?? && familyNameAttr.value?has_content>
                    ${familyNameAttr.value}
                </#if>
                <#if firstNameAttr?? && firstNameAttr.value?? && firstNameAttr.value?has_content>
                    ${firstNameAttr.value}
                </#if>
                </h3>
                <div class="d-flex flex-row justify-content-center align-items-center mt-2">
                    <div class="mr-2">
                        <#if identity.quality.quality?is_number>
                            <#assign qualityPercent=(identity.quality.quality * 100)?round>
                            <#if qualityPercent gt 79>
                                <@tag color="success" >#i18n{identitymediation.quality} : ${qualityPercent}%</@tag>
                            <#elseif qualityPercent gt 50 && qualityPercent lt 80>
                                <@tag color="warning">#i18n{identitymediation.quality} : ${qualityPercent}%</@tag>
                            <#else>
                                <@tag color="danger">#i18n{identitymediation.quality} : ${qualityPercent}%</@tag>
                            </#if>
                        <#else>
                            <@tag color="danger">-</@tag>
                        </#if>
                    </div>
                    <div>
                        <#if identity.monParisActive>
                            <@tag color="success" class="ms-2">MON PARIS</@tag>
                        <#else>
                            <@tag color="danger" class="ms-2 text-decoration-line-through">MON PARIS</@tag>
                        </#if>
                    </div>
                </div>
                <#if !merge>
                    <div class="mt-3">
                    <#if index!=0>
                        <a class="btn btn-outline-primary" href="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view_resolveDuplicates=&identity-cuid-1=${suspicious_identity.customerId}&identity-cuid-2=${identity.customerId}&cuid=${suspicious_identity.customerId}&code=${current_rule_code}&page=${current_page}&family_name=${family_name}&first_name=${first_name}&birthdate=${birthdate}">
                            <i class="ti ti-arrow-big-left-filled"></i> #i18n{identitymediation.select_identities.buttonMergeDuplicate}
                        </a>
                        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal"
                            data-bs-target="#exclude-modal-${identity.customerId}" >
                            #i18n{identitymediation.select_identities.buttonExcludeDuplicate}  <i class="ti ti-arrow-big-right-filled"></i>
                        </button>
                    <#else>
                        <button type="button" class="btn btn-light invisible" disabled>&nbsp;</button>
                    </#if>
                    </div>
                </#if>
            </div>
            <ul class="list-group list-group-flush rounded-bottom-5">
                <#list service_contract.attributeDefinitions?filter(a -> a.attributeRight.readable) as readableAttr>
                    <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" data-name="${readableAttr.name}" data-key="${readableAttr.keyName}" style="min-height:55px">
                        <div class="w-100 d-flex">
                            <#assign attributesList=identity.attributes?filter(a -> a.key == readableAttr.keyName)>
                            <#if index != 0>
                                <#assign firstIdentityAttr=firstIdentity.attributes?filter(a -> a.key == readableAttr.keyName)?first!{}>
                            </#if>
                            <#if attributesList?size gt 0>
                                <#list attributesList as attr>
                                    <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                        <div class="opacity-50">
                                            ${readableAttr.name}
                                        </div>
                                        <div class="fw-bold">
                                             <h3 class="mb-0 fw-bold <#if index!=0 && ( !(firstIdentityAttr.value?has_content) || normalize(firstIdentityAttr.value) != normalize(attr.value!"") )>text-danger</#if>">
                                                <#if attr.value?? && attr.value?has_content>
                                                    <#if attr.key == 'gender'>
                                                        <#if attr.value == '0'>
                                                            #i18n{identitymediation.select_identities.undefined}
                                                        <#elseif attr.value == '1'>
                                                            #i18n{identitymediation.select_identities.female}
                                                        <#elseif attr.value == '2'>
                                                            #i18n{identitymediation.select_identities.male}
                                                        <#else>
                                                            ${attr.value}
                                                        </#if>
                                                    <#else>
                                                        ${attr.value}
                                                    </#if>
                                                <#else>
                                                    <span class="text-warning">Vide</span>
                                                </#if>
                                            </h3>
                                        </div>
                                    </div>
                                    <div class="flex-1 border-start py-2 px-2 text-break" style="width:110px;min-width:110px;max-width:110px;">
                                        <#if attr.certificationDate??>
                                            <div class="text-center opacity-50">
                                                ${attr.certificationDate?date}
                                            </div>
                                        </#if>
                                        <#if attr.certifier??>
                                            <div class="certifier text-truncate text-center w-100">
                                                <span class="fw-medium">
                                                    ${attr.certifier}
                                                </span>
                                            </div>
                                        </#if>
                                    </div>
                                </#list>
                            <#else>
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        ${readableAttr.name}
                                    </div>
                                    <h3 class="mb-0 fw-bold"><span class="text-warning">Inexistant</span></h3>
                                </div>
                            </#if>
                        </div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
    <#if !merge & index!=0>
        <div class="modal fade" id="exclude-modal-${identity.customerId}" tabindex="-1" aria-labelledby="excludeModalLabel-${identity.customerId}" aria-hidden="true">
            <div class="modal-dialog rounded-5">
                <div class="modal-content rounded-5">
                    <form class="form-inline container" action="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp">
                        <input type="hidden" name="code" value="${current_rule_code}" />
                        <input type="hidden" name="page" value="${current_page}" />
                        <input type="hidden" name="cuid" value="${suspicious_identity.customerId}" />
                        <input type="hidden" name="cuid_to_exclude" value="${identity.customerId}" />
                        <div class="modal-header border-0">
                            <h1 class="modal-title text-center w-100 p-4 pb-0" id="excludeModalLabel--${identity.customerId}">#i18n{identitymediation.resolve_duplicates.confirm}</h1>
                            <button type="button" class="btn btn-rounded border position-absolute end-0 me-3 top-0 mt-3" data-bs-dismiss="modal" aria-label="Close">x</button>
                        </div>
                        <div class="modal-body text-center border-0 pt-0">
                            ${identity.customerId}
                            #i18n{identitymediation.resolve_duplicates.confirmExclude}
                        </div>
                        <div class="modal-footer justify-content-center pb-4 pt-0 border-0">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">#i18n{identitymediation.resolve_duplicates.buttonCancel}</button>
                            <button class="btn btn-primary" name="action_excludeDuplicate">#i18n{identitymediation.resolve_duplicates.buttonExcludeDuplicate}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </#if>
</#macro>

<#function normalize str>
    <#return str?trim?lower_case?replace('é','e')?replace('è','e')?replace('ê','e')?replace('ë','e')
                                ?replace('à','a')?replace('â','a')?replace('ä','a')
                                ?replace('ù','u')?replace('û','u')?replace('ü','u')
                                ?replace('ô','o')?replace('ö','o')
                                ?replace('î','i')?replace('ï','i')
                                ?replace('ç','c') >
</#function>