<#--
  Macro: identityCard

  Renders a card UI for identity details.

  @param identity The main data object containing the identity's details.
  @param index The index of the current identity.
  @param merge Optional parameter to conditionally render buttons.

  @returns A rendered identity card.

  Usage:
  <@identityCard identity=someIdentity index=someIndex />
-->
<#macro identityCard identity index merge=false class="" width="">
    <div class="lutece-compare-item-container border-end p-3 <#if index=0>bg-primary-subtle border border-primary-subtle rounded-start-5<#else>border-dark-subtle border-top border-bottom</#if> ${class}" style="<#if width!=''>width:${width}</#if>">
        <div class="lutece-compare-item card p-0 rounded-5 shadow-xl mb-0">

            <#if index=0 && !merge>
                <button class="border btn btn-primary btn-rounded float-end pin position-absolute end-0 top-0 mt-2 me-2"><i class="ti fs-6 ti-pin-filled"></i></button>
            <#elseif !merge>
                <a class="border btn btn-light btn-rounded float-end pin position-absolute end-0 top-0 mt-2 me-2" href="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?view=selectIdentities&cuid=${suspicious_identity.customerId}&code=${current_rule_code}&page=${current_page}&cuid_pinned=${identity.customerId}"><i class="ti fs-6 ti-pin"></i></a>
            </#if>
            <div class="position-absolute start-50 translate-middle-x d-flex justify-content-center" style="top: -10px;">
                <#if identity = suspicious_identity>
                    <div class="badge bg-light-subtle border text-body rounded-5  px-2 py-1 mb-1 d-inline-block me-2 text-nowrap d-none">
                        #i18n{identitymediation.select_identities.suspicious}
                    </div>
                </#if>
                <#if index = 0 >
                    <div class="badge text-success-emphasis bg-success-subtle border rounded-5 px-2 py-1 mb-1 d-inline-block text-nowrap d-none">
                        #i18n{identitymediation.select_identities.bestQuality}
                    </div>
                </#if>
            </div>
            <div class="py-4 text-center">
                <h2>#i18n{identitymediation.select_identities.identity} ${index + 1}</h2>
                <div class="d-flex flex-row justify-content-center align-items-center mt-2 mb-3">
                    <div class="mr-2">
                        <#if identity.quality.quality?is_number>
                            <#assign qualityPercent=(identity.quality.quality * 100)?round>
                            <#if qualityPercent gt 80>
                                <@tag color="success">#i18n{identitymediation.quality} : ${qualityPercent}%</@tag>
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
                    <button type="button" class="btn btn-outline-primary select-identity-btn">
                        #i18n{identitymediation.select_identities.buttonSelectIdentity}
                    </button>
                    <button type="button" class="btn btn-warning selected-identity-btn" data-name="identity-cuid-${index}" data-cuid="${identity.customerId}">
                        #i18n{identitymediation.select_identities.buttonSelectedIdentity}
                    </button>
                </#if>
            </div>
            <ul class="list-group list-group-flush rounded-bottom-5">
                <#list service_contract.attributeDefinitions?filter(a -> a.attributeRight.readable) as readableAttr>
                    <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" data-name="${readableAttr.name}" style="min-height:55px">
                        <div class="w-100 d-flex">
                            <#assign attributesList=identity.attributes?filter(a -> a.key == readableAttr.keyName)>
                            <#if attributesList?size gt 0>
                                <#list attributesList as attr>
                                    <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                        <div class="opacity-50">
                                            ${readableAttr.name}
                                        </div>
                                        <div class="fw-bold">
                                            <h3 class="mb-0 fw-bold">
                                                <#if attr.value?? && attr.value?has_content>
                                                    ${attr.value}
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
                                    <div class="small-title">
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
</#macro>
