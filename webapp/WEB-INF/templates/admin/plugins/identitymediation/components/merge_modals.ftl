<#macro mergeModals>
	<div class="modal fade" id="merge-modal" tabindex="-1" aria-labelledby="mergeModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg rounded-5">
			<div class="modal-content rounded-5">
				<form id="mediation-merge-form" class="form-inline container" action="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp">
					<input type="hidden" name="code" value="${current_rule_code}" />
					<input type="hidden" name="page" value="${current_page}" />
					<input type="hidden" name="cuid" value="${suspicious_identity.customerId}" />
					<div class="modal-header border-0">
						<h1 class="modal-title text-center w-100 p-4 pb-0" id="mergeModalLabel">#i18n{identitymediation.resolve_duplicates.confirm}</h1>
						<button type="button" class="btn btn-rounded border position-absolute end-0 me-3 top-0 mt-3" data-bs-dismiss="modal" aria-label="Close">x</button>
					</div>
					<div class="modal-body text-center border-0 pt-0">
						<h3 class="text-center w-100">#i18n{identitymediation.resolve_duplicates.confirmMerge}</h3>
						<ul class="text-start">
							<li><b>#i18n{identitymediation.resolve_duplicates.mergeEntities}</b></li>
							<li class="d-none">
								<b>#i18n{identitymediation.resolve_duplicates.copyAttributes}</b>
								<ul id="recap-attributes-merge-ul"></ul>
							</li>
						</ul>
					</div>
					<div class="modal-footer justify-content-center pb-4 pt-0 border-0">
						<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">#i18n{identitymediation.resolve_duplicates.buttonCancel}</button>
						<button class="btn btn-primary" name="action_mergeDuplicate">#i18n{identitymediation.resolve_duplicates.buttonMergeDuplicate}</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	<div class="modal fade" id="exclude-modal" tabindex="-1" aria-labelledby="excludeModalLabel" aria-hidden="true">
		<div class="modal-dialog rounded-5">
			<div class="modal-content rounded-5">
				<form class="form-inline container" action="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp">
					<input type="hidden" name="code" value="${current_rule_code}" />
					<input type="hidden" name="page" value="${current_page}" />
					<input type="hidden" name="cuid" value="${suspicious_identity.customerId}" />
					<input type="hidden" name="cuid_to_exclude" value="${identity_to_merge.customerId}" />
					<div class="modal-header border-0">
						<h1 class="modal-title text-center w-100 p-4 pb-0" id="excludeModalLabel">#i18n{identitymediation.resolve_duplicates.confirm}</h1>
						<button type="button" class="btn btn-rounded border position-absolute end-0 me-3 top-0 mt-3" data-bs-dismiss="modal" aria-label="Close">x</button>
					</div>
					<div class="modal-body text-center border-0 pt-0">
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
	<div class="modal fade" id="notify-modal" tabindex="-1" aria-labelledby="notifyModalLabel" aria-hidden="true">
		<div class="modal-dialog rounded-5">
			<div class="modal-content rounded-5">
				<form class="form-inline container" action="jsp/admin/plugins/identitymediation/IdentityDuplicate.jsp">
					<input type="hidden" name="customer_id" value="${identity_to_keep.customerId}" />
					<input type="hidden" name="account_merge_second_cuid" value="${identity_to_merge.customerId}" />
					<input type="hidden" name="keep_connected" value="${identity_to_keep.monParisActive?c}" />
					<input type="hidden" name="merge_connected" value="${identity_to_merge.monParisActive?c}" />
					<input type="hidden" name="cuid" value="${cuid}" />
					<input type="hidden" name="code" value="${code}" />
					<input type="hidden" name="only_one" value="${only_one?c}" />
					<input type="hidden" name="identity-cuid-1" value="${identity_to_keep.customerId}" />
					<input type="hidden" name="identity-cuid-2" value="${identity_to_merge.customerId}" />
					<div class="modal-header border-0">
						<h1 class="modal-title text-center w-100 p-4 pb-0" id="notifyModalLabel">#i18n{identitymediation.resolve_duplicates.notify_users.confirm}</h1>
						<button type="button" class="btn btn-rounded border position-absolute end-0 me-3 top-0 mt-3" data-bs-dismiss="modal" aria-label="Close">x</button>
					</div>
					<div class="modal-body text-center border-0 pt-0">
						#i18n{identitymediation.resolve_duplicates.notify_users.description}
					</div>
					<div class="modal-footer justify-content-center pb-4 pt-0 border-0">
						<button class="btn btn-primary" name="action_createIdentityMergeTask">#i18n{identitymediation.resolve_duplicates.notify_users.buttonNotify}</button>
						<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">#i18n{identitymediation.resolve_duplicates.notify_users.buttonCancel}</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</#macro>