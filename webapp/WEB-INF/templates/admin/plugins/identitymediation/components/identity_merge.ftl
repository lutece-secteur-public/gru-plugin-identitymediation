<#--
  Macro: identityMerge

  Renders the UI for merging identities.

  The macro generates a column with actions and UI elements allowing the user to merge identities. 
  This macro makes use of filtered attributes and certain conditions to render buttons and 
  other UI components for each identity attribute.

  @param identity_to_keep The primary identity data object.
  @param identity_to_merge The secondary identity data object to be potentially merged into the primary identity.
  @param current_rule_code The code of the current rule in context.
  @param current_page The current page reference.
  @param service_contract A reference containing attribute definitions and other related data.
  
  @returns A rendered column for merging actions between identities.
  
  Usage:
    <@identityMerge />
-->
<#macro identityMerge>
    <div id="lutece-merge" class="p-0 m-0 position-absolute start-50 translate-middle-x z-2" style="width:100px;">
        <ul class="list-group list-group-flush">
            <li class="position-relative d-flex justify-content-center align-items-center" style="height:155px;">
                <div class="position-absolute top-50 start-0 end-0 bg-dark border-top border-primary-subtle mediation-line-merge" style="z-index:-1"></div>
                <div class="text-center w-100">
                    <a href="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp?action_swapIdentities&cuid=${identity_to_keep.customerId}&code=${current_rule_code}&page=${current_page}&identity-cuid-1=${identity_to_keep.customerId}&identity-cuid-2=${identity_to_merge.customerId}" class="btn btn-rounded border-primary-subtle btn-light m-auto mediation-btn-merge" data-bs-toggle="tooltip" data-bs-placement="top" title="Inverser">
                        <i class="ti ti-switch-horizontal"></i>
                    </a>
                </div>
            </li>
            <#list service_contract.attributeDefinitions?filter(a -> a.attributeRight.readable) as readableAttr>
                <li class="list-group-item text-center d-flex justify-content-center align-items-center border-0" data-name="${readableAttr.name}" style="min-height:55px">
                    <#if readableAttr.attributeRight.writable >
                        <#list identity_to_merge.attributes?filter(a -> a.key == readableAttr.keyName) as attrToMerge>
                            <#assign attrToKeep = (identity_to_keep.attributes?filter(a -> a.key == readableAttr.keyName)?first)!{} >
                            <#if ( !identity_to_keep.monParisActive && !identity_to_merge.monParisActive && ( attrToMerge.certificationLevel gte (attrToKeep.certificationLevel)!0 ) && ( !(attrToKeep?has_content) || (attrToKeep.value != attrToMerge.value) ) )
                                 || !(attrToKeep?has_content) >
                                <div class="position-absolute top-50 start-0 end-0 bg-dark border-top border-primary-subtle mediation-line-merge" style="z-index:-1"></div>
                                <div class="text-center w-100">
                                    <@button class='btn btn-rounded border-primary-subtle btn-light m-auto mediation-btn-merge' color='-' buttonIcon='arrow-left' params=' data-name="${readableAttr.name}" data-key="${attrToMerge.key}" data-value="${attrToMerge.value}" data-certif="${attrToMerge.certifier}" data-certifdate="${attrToMerge.certificationDate?date}" data-timestamp-certif="${attrToMerge.certificationDate?long}"' />
                                </div>
                            </#if>
                        </#list>
                    </#if>
                </li>
            </#list>
        </ul>
    </div>
</#macro>
