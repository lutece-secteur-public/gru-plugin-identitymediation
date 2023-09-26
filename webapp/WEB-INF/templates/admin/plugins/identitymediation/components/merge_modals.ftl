<#macro mergeModals>
	<div class="modal fade" id="merge-modal" tabindex="-1" aria-labelledby="mergeModalLabel" aria-hidden="true">
		<div class="modal-dialog rounded-5">
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
						#i18n{identitymediation.resolve_duplicates.confirmMerge}
						<ul id="recap-ul"></ul>
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
					<div class="modal-header border-0">
						<h1 class="modal-title text-center w-100 p-4 pb-0" id="notifyModalLabel">#i18n{identitymediation.resolve_duplicates.confirm}</h1>
						<button type="button" class="btn btn-rounded border position-absolute end-0 me-3 top-0 mt-3" data-bs-dismiss="modal" aria-label="Close">x</button>
					</div>
					<div class="modal-body text-center border-0 pt-0">
						Fonctionnalit&eacute; non d&eacute;velopp&eacute;e.
					</div>
					<div class="modal-footer justify-content-center pb-4 pt-0 border-0">
						<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">#i18n{identitymediation.resolve_duplicates.buttonCancel}</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</#macro>