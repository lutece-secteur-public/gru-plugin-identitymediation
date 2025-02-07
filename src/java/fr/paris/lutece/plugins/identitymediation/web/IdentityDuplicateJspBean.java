/*
 * Copyright (c) 2002-2024, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitymediation.web;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.identitymediation.buisness.LocalIdentityDto;
import fr.paris.lutece.plugins.identitymediation.rbac.AccessDuplicateResource;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentityExcludeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentityExcludeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityResourceType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskCreateResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage identity duplicates (search, resolve)
 */
@Controller( controllerJsp = "IdentityDuplicate.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_MANAGEMENT" )
public class IdentityDuplicateJspBean extends AbstractIdentityDuplicateJspBean
{
    // Messages
    protected static final String MESSAGE_CHOOSE_DUPLICATE_TYPE_ERROR = "identitymediation.message.choose_duplicate_type.error";
    protected static final String MESSAGE_GET_IDENTITY_ERROR = "identitymediation.message.get_identity.error";
    protected static final String MESSAGE_LOCK_IDENTITY_ERROR = "identitymediation.message.lock_identity.error";
    protected static final String MESSAGE_UNLOCK_IDENTITY_ERROR = "identitymediation.message.unlock_identity.error";
    protected static final String MESSAGE_SELECT_IDENTITIES_ERROR = "identitymediation.message.select_identities.error";
    protected static final String MESSAGE_FETCH_DUPLICATES_ERROR = "identitymediation.message.fetch_duplicates.error";
    protected static final String MESSAGE_MERGE_DUPLICATES_SUCCESS = "identitymediation.message.merge_duplicates.success";
    protected static final String MESSAGE_MERGE_DUPLICATES_ERROR = "identitymediation.message.merge_duplicates.error";
    protected static final String MESSAGE_EXCLUDE_DUPLICATES_SUCCESS = "identitymediation.message.exclude_duplicates.success";
    protected static final String MESSAGE_EXCLUDE_DUPLICATES_ERROR = "identitymediation.message.exclude_duplicates.error";
    protected static final String MESSAGE_ACCOUNT_IDENTITY_MERGE = "identitymediation.message.account_identity_merge";
    protected static final String MESSAGE_ACCOUNT_IDENTITY_MERGE_ERROR = "identitymediation.message.account_identity_merge.error";

    // Views
    protected static final String VIEW_CHOOSE_DUPLICATE_TYPE = "chooseDuplicateType";
    protected static final String VIEW_SEARCH_DUPLICATES = "searchDuplicates";
    protected static final String VIEW_SELECT_IDENTITIES = "selectIdentities";
    protected static final String VIEW_RESOLVE_DUPLICATES = "resolveDuplicates";
    protected static final String VIEW_SEARCH_ALL_DUPLICATES = "searchAllDuplicates";

    // Actions
    protected static final String ACTION_SWAP_IDENTITIES = "swapIdentities";
    protected static final String ACTION_MERGE_DUPLICATE = "mergeDuplicate";
    protected static final String ACTION_EXCLUDE_DUPLICATE = "excludeDuplicate";
    protected static final String ACTION_CANCEL = "cancel";
    protected static final String ACTION_CREATE_IDENTITY_MERGE_TASK = "createIdentityMergeTask";
    protected static final String ACTION_NOTIFY_USER = "notifyUser";

    // Templates
    protected static final String TEMPLATE_CHOOSE_DUPLICATE_TYPE = "/admin/plugins/identitymediation/choose_duplicate_type.html";
    protected static final String TEMPLATE_SEARCH_DUPLICATES = "/admin/plugins/identitymediation/search_duplicates.html";
    protected static final String TEMPLATE_SELECT_IDENTITIES = "/admin/plugins/identitymediation/select_identities.html";
    protected static final String TEMPLATE_RESOLVE_DUPLICATES = "/admin/plugins/identitymediation/resolve_duplicates.html";
    protected static final String TEMPLATE_SEARCH_ALL_DUPLICATES = "admin/plugins/identitymediation/search_all_duplicates.html";

    // Properties for page titles
    protected static final String PROPERTY_PAGE_TITLE_CHOOSE_DUPLICATE_TYPE = "identitymediation.choose_duplicate_type.pageTitle";
    protected static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    protected static final String PROPERTY_PAGE_TITLE_SELECT_IDENTITIES = "identitymediation.select_identities.pageTitle";
    protected static final String PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES = "identitymediation.resolve_duplicates.pageTitle";


    // Parameters
    protected final String PARAMETER_CUID_PINNED = "cuid_pinned";
    protected final String PARAMETER_CUID_TO_EXCLUDE = "cuid_to_exclude";
    protected final String PARAMETER_CUID = "cuid";

    // Markers
    protected static final String MARK_DUPLICATE_RULE_LIST = "duplicate_rule_list";
    protected static final String MARK_SERVICE_CONTRACT = "service_contract";
    protected static final String MARK_IDENTITY_LIST = "identity_list";
    protected static final String MARK_IDENTITY = "identity";
    protected static final String MARK_IDENTITY_TO_KEEP = "identity_to_keep";
    protected static final String MARK_IDENTITY_TO_MERGE = "identity_to_merge";
    protected static final String MARK_CURRENT_RULE_CODE = "current_rule_code";
    protected static final String MARK_MEDIATION_IDENTITY_LIST = "mediation_identity_list";
    protected static final String MARK_IDENTITY_HISTORY_DATE_LIST = "identity_history_date_list";
    protected static final String MARK_SUSPICIOUS_IDENTITY = "suspicious_identity";
    protected static final String MARK_TOTAL_PAGES = "total_pages";
    protected static final String MARK_CURRENT_PAGE = "current_page";
    protected static final String MARK_COUNT_DUPLICATE_BY_RULE = "count_duplicate_by_rule";
    protected static final String MARK_TOTAL_DUPLICATED = "count_total_duplicated";
    protected static final String MARK_RULE_BY_IDENTITY = "rule_by_identity";
    protected static final String MARK_DUPLICATE_LIST_BY_RULE = "duplicate_list_by_rule";
    protected static final String MARK_CUID = "cuid";
    protected static final String MARK_CODE = "code";
    protected static final String MARK_EXCLUDE = "can_exclude";
    protected static final String MARK_NOTIFY = "can_notify";

    private boolean _canExclude = false;
    private boolean _canNotify = false;

    /**
     *
     * @param request
     * @return
     */
    @View( value = VIEW_CHOOSE_DUPLICATE_TYPE )
    public String getDuplicateTypes( final HttpServletRequest request ) throws AccessDeniedException {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_READ, ( User ) this.getUser( ) ) )
        {
            throw new AccessDeniedException( "You don't have the right to read duplicates" );
        }
        _suspiciousIdentity = null;
        this.init( request, true );

        final Map<String, Object> model = getModel( );
        model.put( MARK_DUPLICATE_RULE_LIST, _duplicateRules );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return this.getPage( PROPERTY_PAGE_TITLE_CHOOSE_DUPLICATE_TYPE, TEMPLATE_CHOOSE_DUPLICATE_TYPE, model );
    }

    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_DUPLICATES, defaultView = true )
    public String getSearchDuplicates( final HttpServletRequest request ) throws AccessDeniedException {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_READ, ( User ) this.getUser( ) ) )
        {
            throw new AccessDeniedException( "You don't have the right to read duplicates" );
        }
        _suspiciousIdentity = null;
        this.init( request, true );

        final Map<Long, Map<LocalIdentityDto, List<AttributeChange>>> identityHistoryDateList = new TreeMap<>( Collections.reverseOrder( ) );

        try
        {
            identityHistoryDateList.putAll( this.fetchItentityHistoryByDate( 30 ) );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while fetching potential identity duplicates.", e );
            this.addError( MESSAGE_FETCH_ERROR, getLocale( ) );
        }

        final Map<String, Object> model = this.populateModel( );

        Arrays.asList( PARAMETERS_DUPLICATE_SEARCH ).forEach( searchKey -> model.put( searchKey, request.getParameter( searchKey ) ) );

        model.put( MARK_IDENTITY_HISTORY_DATE_LIST, identityHistoryDateList );

        return this.getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_DUPLICATES, model );
    }

    /**
     * Search duplicates for each rule
     * @param request the request
     * @return the view
     */
    @View ( value = VIEW_SEARCH_ALL_DUPLICATES )
    public String getAllDuplicates( final HttpServletRequest request ) throws AccessDeniedException {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_READ, ( User ) this.getUser( ) ) )
        {
            throw new AccessDeniedException("You don't have the right to read duplicates");
        }
        String cuid = request.getParameter( PARAMETER_CUID );
        if ( StringUtils.isBlank( cuid ) )
        {
            return this.getSearchDuplicates( request );
        }

        LocalIdentityDto identity;
        try {
            identity = this.getQualifiedIdentityFromCustomerId( cuid );
        } catch ( final IdentityStoreException e ) {
            this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        if ( identity == null )
        {
            this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        init( request, true );

        final Map<String, Object> model = getModel( );
        try {
            model.put( MARK_DUPLICATE_LIST_BY_RULE, this.fetchPotentialDuplicates( identity ) );
            model.put( MARK_IDENTITY, identity );
        } catch (IdentityStoreException e) {
            this.addError( MESSAGE_FETCH_DUPLICATES_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        return this.getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_ALL_DUPLICATES, model );

    }

    /**
     * Returns the form to select which identities to process
     *
     * @param request
     * @return the view
     */
    @View( value = VIEW_SELECT_IDENTITIES )
    public String getSelectIdentities( final HttpServletRequest request ) throws AccessDeniedException {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_READ, ( User ) this.getUser( ) ) )
        {
            throw new AccessDeniedException("You don't have the right to read duplicates");
        }
        final String cuidPinned = request.getParameter( PARAMETER_CUID_PINNED );
        if ( StringUtils.isBlank( request.getParameter( Constants.PARAM_RULE_CODE ) ) )
        {
            return this.getSearchDuplicates( request );
        }

        this.init( request, false );

        try
        {
            _suspiciousIdentity = this.getQualifiedIdentityFromCustomerId( request.getParameter( "cuid" ) );
            if ( _suspiciousIdentity == null )
            {
                this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return this.getSearchDuplicates( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        final List<LocalIdentityDto> identityList = new ArrayList<>( );
        try
        {
            final List<LocalIdentityDto> duplicateList = fetchPotentialDuplicates( _suspiciousIdentity, _currentRuleCode, true );
            if ( CollectionUtils.isEmpty( duplicateList ) )
            {
                return this.getSearchDuplicates( request );
            }
            identityList.addAll( duplicateList );
            this.sortByQuality( identityList );
            identityList.add( 0, _suspiciousIdentity );
        }
        catch( final IdentityStoreException e )
        {
            this.addError( MESSAGE_FETCH_DUPLICATES_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        if ( StringUtils.isNotBlank( cuidPinned ) )
        {
            final Optional<LocalIdentityDto> pinnedIdentityOpt = identityList.stream( ).filter( identity -> cuidPinned.equals( identity.getCustomerId( ) ) ).findFirst( );

            if ( pinnedIdentityOpt.isPresent( ) )
            {
                final LocalIdentityDto pinnedIdentity = pinnedIdentityOpt.get( );
                identityList.remove( pinnedIdentity );
                identityList.add( 0, pinnedIdentity );
            }
        }

        Map<String, Object> model = populateModel( );
        model.put( MARK_IDENTITY_LIST, identityList );
        Arrays.asList( PARAMETERS_DUPLICATE_SEARCH ).forEach( searchKey -> model.put( searchKey, request.getParameter( searchKey ) ) );

        if ( identityList.size( ) == 2 )
        {
            // skip selection, resolve directly
            final Map<String, String> cuidMap = new HashMap<>( );
            cuidMap.put( "identity-cuid-1", identityList.get( 0 ).getCustomerId( ) );
            cuidMap.put( "identity-cuid-2", identityList.get( 1 ).getCustomerId( ) );
            cuidMap.put( Constants.PARAM_RULE_CODE, _currentRuleCode );
            cuidMap.put( PARAMETER_PAGE, String.valueOf( _currentPage ) );
            cuidMap.put( PARAMETER_ONLY_ONE_DUPLICATE, "true" );
            Arrays.asList( PARAMETERS_DUPLICATE_SEARCH ).forEach( searchKey -> cuidMap.put( searchKey, request.getParameter( searchKey ) ) );

            return redirect( request, VIEW_RESOLVE_DUPLICATES, cuidMap );
        }
        else
        {
            // Go to pair selection page
            return this.getPage( PROPERTY_PAGE_TITLE_SELECT_IDENTITIES, TEMPLATE_SELECT_IDENTITIES, model );

        }
    }

    /**
     * Returns the form to manually resolve an identity duplicates
     *
     * @param request
     *            The Http request
     * @return the html code of the form
     */
    @View( value = VIEW_RESOLVE_DUPLICATES )
    public String getResolveDuplicates( final HttpServletRequest request ) throws AccessDeniedException {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_WRITE, ( User ) getUser( ) ) ) {
            throw new AccessDeniedException("You don't have the right to write duplicates");
        }
        final String _suspiciousCuid = request.getParameter( MARK_CUID );
        final String code = request.getParameter(Constants.PARAM_RULE_CODE);
        final boolean onlyOneDuplicate = request.getParameter(PARAMETER_ONLY_ONE_DUPLICATE) != null && Boolean.parseBoolean(request.getParameter(PARAMETER_ONLY_ONE_DUPLICATE) );

        if ( StringUtils.isBlank(code) && _currentRuleCode == null )
        {
            AppLogService.error( "No duplicate rule code provided." );
            this.addError( MESSAGE_CHOOSE_DUPLICATE_TYPE_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        if ( _suspiciousIdentity == null && StringUtils.isBlank( _suspiciousCuid ) )
        {
            AppLogService.error( "No suspicious identity provided." );
            this.addError( MESSAGE_SELECT_IDENTITIES_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        init( request, false );
        final List<String> cuidList = request.getParameterMap( ).entrySet( ).stream( ).filter( e -> e.getKey( ).startsWith( "identity-cuid-" ) )
                .map( e -> e.getValue( ) [0] ).collect( Collectors.toList( ) );
        if ( CollectionUtils.isEmpty( cuidList ) || cuidList.size( ) != 2 )
        {
            this.addError( MESSAGE_SELECT_IDENTITIES_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }
        try
        {
            if ( _suspiciousIdentity == null )
            {
                _suspiciousIdentity = this.getQualifiedIdentityFromCustomerId( _suspiciousCuid );
                if ( _suspiciousIdentity == null )
                {
                    this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                    return this.getSearchDuplicates( request );
                }
            }
            final LocalIdentityDto identity1 = this.getQualifiedIdentityFromCustomerId( cuidList.get( 0 ) );
            final LocalIdentityDto identity2 = this.getQualifiedIdentityFromCustomerId( cuidList.get( 1 ) );

            if ( identity1 == null || identity2 == null )
            {
                this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return this.getSearchDuplicates( request );
            }
            this.sortWorkedIdentities( identity1, identity2 );
        }
        catch( final IdentityStoreException e )
        {
            this.addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return this.getSearchDuplicates( request );
        }

        try
        {
            this.sendAcknowledgement( _suspiciousIdentity );
        }
        catch( final IdentityStoreException e )
        {
            this.addError( MESSAGE_LOCK_IDENTITY_ERROR, getLocale( ) );
            this.addError( e.getMessage( ) );
            return this.getSearchDuplicates( request );
        }

        _canNotify = RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_NOTIFICATION, ( User ) this.getUser( ) );
        _canExclude = RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_EXCLUDE, ( User ) this.getUser( ) );

        final Map<String, Object> model = this.populateModel( );
        model.put( MARK_CUID, _suspiciousIdentity.getCustomerId() );
        model.put( MARK_CODE, code );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );
        model.put( MARK_NOTIFY, _canNotify );
        model.put( MARK_EXCLUDE, _canExclude );
        model.put( PARAMETER_ONLY_ONE_DUPLICATE, onlyOneDuplicate );
        Arrays.asList( PARAMETERS_DUPLICATE_SEARCH ).forEach( searchKey -> model.put( searchKey, request.getParameter( searchKey ) ) );

        return this.getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES, model );
    }

    /**
     * Swaps the selected identities to work with.
     *
     * @param request
     * @return the view
     */
    @Action( ACTION_SWAP_IDENTITIES )
    public String doSwapIdentities( final HttpServletRequest request ) throws AccessDeniedException {
        if(!RBACService.isAuthorized(new AccessDuplicateResource(), AccessDuplicateResource.PERMISSION_WRITE, (User) getUser())) {
            throw new AccessDeniedException("You don't have the right to write duplicates");
        }
        final LocalIdentityDto previouslyToKeep = _identityToKeep;
        _identityToKeep = _identityToMerge;
        _identityToMerge = previouslyToKeep;

        Map<String, Object> model = populateModel( );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );

        return this.getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES, model );
    }

    /**
     * Merges the selected identity (the duplicate) with the previously selected identity (the main identity).
     *
     * @param request
     * @return
     */
    @Action( ACTION_MERGE_DUPLICATE )
    public String doMergeDuplicate( final HttpServletRequest request ) throws AccessDeniedException {
        if(!RBACService.isAuthorized(new AccessDuplicateResource(), AccessDuplicateResource.PERMISSION_WRITE, (User) getUser())) {
            throw new AccessDeniedException("You don't have the right to write duplicates");
        }
        if ( _identityToKeep == null || _identityToMerge == null || _identityToMerge.equals( _identityToKeep ) )
        {
            this.addError( MESSAGE_MERGE_DUPLICATES_ERROR, getLocale( ) );
            _identityToKeep = null;
            _identityToMerge = null;
            return this.getSelectIdentities( request );
        }
        try
        {
            final IdentityMergeRequest identityMergeRequest = buildMergeRequest( request );
            final IdentityMergeResponse response = _serviceIdentity.mergeIdentities( identityMergeRequest, _currentClientCode, buildAgentAuthor( ) );
            if ( !_mediationService.isSuccess( response ) )
            {
                logAndDisplayStatusErrorMessage( response );
                response.getStatus( ).getAttributeStatuses( ).forEach( as -> {
                    if ( as.getStatus( ).getType( ) == AttributeChangeStatusType.WARNING )
                    {
                        addWarning( as.getKey( ) + " : " + I18nService.getLocalizedString( as.getMessageKey( ), getLocale( ) ) );
                    }
                    else
                        if ( as.getStatus( ).getType( ) == AttributeChangeStatusType.ERROR )
                        {
                            this.addError( as.getKey( ) + " : " + I18nService.getLocalizedString( as.getMessageKey( ), getLocale( ) ) );
                        }
                } );
                _identityToKeep = null;
                _identityToMerge = null;
                return this.getSelectIdentities( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while merging identities", e );
            this.addError( MESSAGE_MERGE_DUPLICATES_ERROR, getLocale( ) );
            _identityToKeep = null;
            _identityToMerge = null;
            return this.getSelectIdentities( request );
        }

        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            this.addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            this.addError( e.getMessage( ) );
            return this.getSelectIdentities( request );
        }
        _identityToKeep = null;
        _identityToMerge = null;

        addInfo( MESSAGE_MERGE_DUPLICATES_SUCCESS, getLocale( ) );
        return this.getSelectIdentities( request );
    }

    /**
     * Marks the selected identity (the potential duplicate) as NOT being a duplicate of the previously selected identity.
     *
     * @param request
     * @return
     */
    @Action( ACTION_EXCLUDE_DUPLICATE )
    public String doExcludeDuplicate( final HttpServletRequest request ) throws AccessDeniedException {
        if(!_canExclude) {
            throw new AccessDeniedException("You don't have the right to exclude duplicates");
        }
        final String cuidToExclude = request.getParameter( PARAMETER_CUID_TO_EXCLUDE );

        if ( cuidToExclude == null || _suspiciousIdentity == null )
        {
            this.addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
            return this.getSelectIdentities( request );
        }

        final SuspiciousIdentityExcludeRequest excludeRequest = new SuspiciousIdentityExcludeRequest( );
        excludeRequest.setIdentityCuid1( _suspiciousIdentity.getCustomerId( ) );
        excludeRequest.setIdentityCuid2( cuidToExclude );
        try
        {
            final SuspiciousIdentityExcludeResponse response = _serviceQuality.excludeIdentities( excludeRequest, _currentClientCode, buildAgentAuthor( ) );
            if ( !_mediationService.isSuccess( response ) )
            {
                this.addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
                logAndDisplayStatusErrorMessage( response );
                return this.getSelectIdentities( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while excluding the identities.", e );
            this.addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
            return this.getSelectIdentities( request );
        }

        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            this.addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            this.addError( e.getMessage( ) );
            return this.getSelectIdentities( request );
        }
        init( request, true );
        addInfo( MESSAGE_EXCLUDE_DUPLICATES_SUCCESS, getLocale( ) );
        return this.getSelectIdentities( request );
    }

    /**
     * Cancel and release the acknowledgement for the 2 selected identities.
     *
     * @param request
     * @return
     */
    @Action( ACTION_CANCEL )
    public String doCancel( final HttpServletRequest request ) throws AccessDeniedException {
        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            this.addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            this.addError( e.getMessage( ) );
            return this.getSelectIdentities( request );
        }
        _identityToKeep = null;
        _identityToMerge = null;
        return this.getSelectIdentities( request );
    }

    /**
     * Create a merge demand by providing both CUIDs
     * @param request
     * @return
     * @throws AccessDeniedException
     */
    @Action(ACTION_CREATE_IDENTITY_MERGE_TASK)
    public String doCreateIdentityMergeTask(final HttpServletRequest request) throws AccessDeniedException
    {
        if(!_canNotify) {
            throw new AccessDeniedException("You don't have the right to create a notification task");
        }
        final String customerId = request.getParameter( Constants.PARAM_ID_CUSTOMER );
        final String secondCuId = request.getParameter( Constants.METADATA_ACCOUNT_MERGE_SECOND_CUID );
        final boolean firstConnected = Boolean.parseBoolean( request.getParameter( Constants.PARAM_IS_IDENTITTY_TO_KEEP_CONNECTED ) );
        final boolean secondConnected = Boolean.parseBoolean( request.getParameter( Constants.PARAM_IS_IDENTITTY_TO_MERGE_CONNECTED ) );

        final String taskType = firstConnected && secondConnected ?
                IdentityTaskType.ACCOUNT_MERGE_REQUEST.name() : IdentityTaskType.ACCOUNT_IDENTITY_MERGE_REQUEST.name();
        try
        {
            final IdentityTaskCreateRequest identityTaskCreateRequest = new IdentityTaskCreateRequest( );
            final IdentityTaskDto task = new IdentityTaskDto( );
            task.setTaskType( taskType );
            task.setResourceType( IdentityResourceType.CUID.name( ) );
            task.setResourceId( customerId );
            final Map<String, String> metadata = new HashMap<>();
            metadata.put(Constants.METADATA_ACCOUNT_MERGE_SECOND_CUID, secondCuId);
            metadata.put(Constants.METADATA_ORIGIN, AuthorType.owner.name());
            task.setMetadata(metadata);
            identityTaskCreateRequest.setTask( task );
            final IdentityTaskCreateResponse identityTask = _serviceIdentity.createIdentityTask( identityTaskCreateRequest, _currentClientCode, this.buildAgentAuthor( ) );
            if ( identityTask.getStatus( ).getHttpCode( ) == 201 )
            {
                addInfo( MESSAGE_ACCOUNT_IDENTITY_MERGE, getLocale( ) );
                addInfo( identityTask.getStatus( ).getMessage( ) );
            }
            else
            {
                this.addError( MESSAGE_ACCOUNT_IDENTITY_MERGE_ERROR, getLocale( ) );
                this.addError( identityTask.getStatus( ).getMessage( ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while trying to create " + taskType + " for identity [customerId = " + customerId + "].", e );
            this.addError( MESSAGE_ACCOUNT_IDENTITY_MERGE_ERROR, getLocale( ) );
            this.addError( e.getMessage() );
        }

        return getResolveDuplicates( request );
    }

    /**
     * Populates the model with the necessary attributes.
     * 
     * @return the populated model.
     */
    protected Map<String, Object> populateModel( )
    {
        Map<String, Object> model = getModel( );

        if(!_totalRecordByRule.isEmpty())
        {
            model.put("errors", new ArrayList<>());
        }

        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_DUPLICATE_RULE_LIST, _duplicateRules );
        model.put( MARK_CURRENT_RULE_CODE, _currentRuleCode );
        model.put( MARK_MEDIATION_IDENTITY_LIST, _mediationIdentities );
        model.put( MARK_CURRENT_PAGE, _currentPage );
        model.put( MARK_TOTAL_PAGES, _totalPages );
        model.put( MARK_SUSPICIOUS_IDENTITY, _suspiciousIdentity );
        model.put( MARK_COUNT_DUPLICATE_BY_RULE, _totalRecordByRule);
        model.put( MARK_TOTAL_DUPLICATED, _totalRecords);
        model.put( MARK_RULE_BY_IDENTITY, _ruleBySuspiciousIdentity);

        return model;
    }
}
