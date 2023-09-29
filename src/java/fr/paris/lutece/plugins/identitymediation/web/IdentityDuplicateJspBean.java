/*
 * Copyright (c) 2002-2023, City of Paris
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

import fr.paris.lutece.plugins.identitymediation.buisness.MediationIdentity;
import fr.paris.lutece.plugins.identityquality.v3.web.service.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummaryDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummarySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.lock.SuspiciousIdentityLockRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.lock.SuspiciousIdentityLockResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ocsp.Request;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage identity duplicates (search, resolve)
 */
@Controller( controllerJsp = "IdentityDuplicate.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_MANAGEMENT" )
public class IdentityDuplicateJspBean extends MVCAdminJspBean
{
    // Messages
    private static final String MESSAGE_CHOOSE_DUPLICATE_TYPE_ERROR = "identitymediation.message.choose_duplicate_type.error";
    private static final String MESSAGE_FETCH_DUPLICATE_RULES_ERROR = "identitymediation.message.fetch_duplicate_rules.error";
    private static final String MESSAGE_FETCH_DUPLICATE_RULES_NORESULT = "identitymediation.message.fetch_duplicate_rules.noresult";
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_ERROR = "identitymediation.message.fetch_duplicate_holders.error";
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_NORESULT = "identitymediation.message.fetch_duplicate_holders.noresult";
    private static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitymediation.message.get_service_contract.error";
    private static final String MESSAGE_GET_IDENTITY_ERROR = "identitymediation.message.get_identity.error";
    private static final String MESSAGE_LOCK_IDENTITY_ERROR = "identitymediation.message.lock_identity.error";
    private static final String MESSAGE_UNLOCK_IDENTITY_ERROR = "identitymediation.message.unlock_identity.error";
    private static final String MESSAGE_SELECT_IDENTITIES_ERROR = "identitymediation.message.select_identities.error";
    private static final String MESSAGE_FETCH_DUPLICATES_ERROR = "identitymediation.message.fetch_duplicates.error";
    private static final String MESSAGE_FETCH_DUPLICATES_NORESULT = "identitymediation.message.fetch_duplicates.noresult";
    private static final String MESSAGE_MERGE_DUPLICATES_SUCCESS = "identitymediation.message.merge_duplicates.success";
    private static final String MESSAGE_MERGE_DUPLICATES_ERROR = "identitymediation.message.merge_duplicates.error";
    private static final String MESSAGE_EXCLUDE_DUPLICATES_SUCCESS = "identitymediation.message.exclude_duplicates.success";
    private static final String MESSAGE_EXCLUDE_DUPLICATES_ERROR = "identitymediation.message.exclude_duplicates.error";
    private static final String MESSAGE_FETCH_ERROR = "identitymediation.message.fetch.error";

    // Views
    private static final String VIEW_CHOOSE_DUPLICATE_TYPE = "chooseDuplicateType";
    private static final String VIEW_SEARCH_DUPLICATES = "searchDuplicates";
    private static final String VIEW_SELECT_IDENTITIES = "selectIdentities";
    private static final String VIEW_RESOLVE_DUPLICATES = "resolveDuplicates";

    // Actions
    private static final String ACTION_SWAP_IDENTITIES = "swapIdentities";
    private static final String ACTION_MERGE_DUPLICATE = "mergeDuplicate";
    private static final String ACTION_EXCLUDE_DUPLICATE = "excludeDuplicate";
    private static final String ACTION_CANCEL = "cancel";

    // Templates
    private static final String TEMPLATE_CHOOSE_DUPLICATE_TYPE = "/admin/plugins/identitymediation/choose_duplicate_type.html";
    private static final String TEMPLATE_SEARCH_DUPLICATES = "/admin/plugins/identitymediation/search_duplicates.html";
    private static final String TEMPLATE_SELECT_IDENTITIES = "/admin/plugins/identitymediation/select_identities.html";
    private static final String TEMPLATE_RESOLVE_DUPLICATES = "/admin/plugins/identitymediation/resolve_duplicates.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_CHOOSE_DUPLICATE_TYPE = "identitymediation.choose_duplicate_type.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_SELECT_IDENTITIES = "identitymediation.select_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES = "identitymediation.resolve_duplicates.pageTitle";
    private static final String PROPERTY_RULE_PRIORITY_MINIMUM = "identitymediation.rules.priority.minimum";

    // Parameters
    final String [ ] PARAMETERS_DUPLICATE_SEARCH = {
            Constants.PARAM_FIRST_NAME, Constants.PARAM_FAMILY_NAME, Constants.PARAM_BIRTH_DATE
    };
    final String PARAMETER_PAGE = "page";
    final String PARAMETER_CUID_PINNED = "cuid_pinned";
    final String PARAMETER_CUID_TO_EXCLUDE = "cuid_to_exclude";

    // Markers
    private static final String MARK_DUPLICATE_RULE_LIST = "duplicate_rule_list";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY_TO_KEEP = "identity_to_keep";
    private static final String MARK_IDENTITY_TO_MERGE = "identity_to_merge";
    private static final String MARK_CURRENT_RULE_CODE = "current_rule_code";
    private static final String MARK_MEDIATION_IDENTITY_LIST = "mediation_identity_list";
    private static final String MARK_IDENTITY_HISTORY_DATE_LIST = "identity_history_date_list";
    private static final String MARK_SUSPICIOUS_IDENTITY = "suspicious_identity";
    private static final String MARK_TOTAL_PAGES = "total_pages";
    private static final String MARK_CURRENT_PAGE = "current_page";

    // Beans
    private static final IdentityQualityService _serviceQuality = SpringContextService.getBean( "identityQualityService.rest.httpAccess" );
    private static final ServiceContractService _serviceContractService = SpringContextService.getBean( "serviceContract.rest.httpAccess" );
    private static final IdentityService _serviceIdentity = SpringContextService.getBean( "identityService.rest.httpAccess" );

    // Properties
    private final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitymediation.attribute.order" ).split( "," ) );
    private final List<String> _attributeKeyToShowList = Arrays.asList( AppPropertiesService.getProperty( "identitymediation.attribute.show" ).split( "," ) );

    // Session variable to store working values
    private ServiceContractDto _serviceContract;
    private String _currentClientCode = AppPropertiesService.getProperty( "identitymediation.default.client.code" );
    private final Integer _rulePriorityMin = AppPropertiesService.getPropertyInt( PROPERTY_RULE_PRIORITY_MINIMUM, 100 );
    private String _previousRuleCode;
    private String _currentRuleCode;
    private IdentityDto _identityToKeep;
    private IdentityDto _identityToMerge;
    private IdentityDto _suspiciousIdentity;
    private List<DuplicateRuleSummaryDto> _duplicateRules = new ArrayList<>( );
    private List<MediationIdentity> _mediationIdentities = new ArrayList<>( );
    private Integer _totalPages;
    private Integer _currentPage;
    private RequestAuthor _agentAuthor;
    private RequestAuthor _applicationAuthor;

    /**
     *
     * @param request
     * @return
     */
    @View( value = VIEW_CHOOSE_DUPLICATE_TYPE )
    public String getDuplicateTypes( final HttpServletRequest request )
    {
        _suspiciousIdentity = null;
        init( request, true );

        final Map<String, Object> model = getModel( );
        model.put( MARK_DUPLICATE_RULE_LIST, _duplicateRules );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return getPage( PROPERTY_PAGE_TITLE_CHOOSE_DUPLICATE_TYPE, TEMPLATE_CHOOSE_DUPLICATE_TYPE, model );
    }

    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_DUPLICATES, defaultView = true )
    public String getSearchDuplicates( final HttpServletRequest request )
    {
        _suspiciousIdentity = null;
        init( request, true );

        final Map<Long, Map<IdentityDto, List<AttributeChange>>> identityHistoryDateList = new TreeMap<>( Collections.reverseOrder( ) );

        try
        {
            identityHistoryDateList.putAll( fetchItentityHistoryByDate( 30 ) );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while fetching potential identity duplicates.", e );
            addError( MESSAGE_FETCH_ERROR, getLocale( ) );
        }

        Map<String, Object> model = populateModel( );

        Arrays.asList( PARAMETERS_DUPLICATE_SEARCH ).forEach( searchKey -> model.put( searchKey, request.getParameter( searchKey ) ) );

        model.put( MARK_IDENTITY_HISTORY_DATE_LIST, identityHistoryDateList );

        return getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_DUPLICATES, model );
    }

    /**
     * Returns the form to select which identities to process
     * 
     * @param request
     * @return the view
     */
    @View( value = VIEW_SELECT_IDENTITIES )
    public String getSelectIdentities( final HttpServletRequest request )
    {
        final String cuidPinned = request.getParameter( PARAMETER_CUID_PINNED );
        if ( StringUtils.isBlank( request.getParameter( Constants.PARAM_RULE_CODE ) ) )
        {
            return getSearchDuplicates( request );
        }
        init( request, false );
        try
        {
            _suspiciousIdentity = getQualifiedIdentityFromCustomerId( request.getParameter( "cuid" ) );
            if ( _suspiciousIdentity == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getSearchDuplicates( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        final List<IdentityDto> identityList = new ArrayList<>( );
        try
        {
            final List<IdentityDto> duplicateList = fetchPotentialDuplicates( _suspiciousIdentity );
            if ( CollectionUtils.isEmpty( duplicateList ) )
            {
                addError( MESSAGE_FETCH_DUPLICATES_NORESULT, getLocale( ) );
                return getSearchDuplicates( request );
            }
            identityList.addAll( duplicateList );
            sortByQuality( identityList );
            identityList.add( 0, _suspiciousIdentity );
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_FETCH_DUPLICATES_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        if ( StringUtils.isNotBlank( cuidPinned ) )
        {
            Optional<IdentityDto> pinnedIdentityOpt = identityList.stream( ).filter( identity -> cuidPinned.equals( identity.getCustomerId( ) ) ).findFirst( );

            if ( pinnedIdentityOpt.isPresent( ) )
            {
                IdentityDto pinnedIdentity = pinnedIdentityOpt.get( );
                identityList.remove( pinnedIdentity );
                identityList.add( 0, pinnedIdentity );
            }
        }

        Map<String, Object> model = populateModel( );
        model.put( MARK_IDENTITY_LIST, identityList );

        if ( identityList.size( ) == 2 )
        {
            // skip selection, resolve directly
            final Map<String, String> cuidMap = new HashMap<>( );
            cuidMap.put( "identity-cuid-1", identityList.get( 0 ).getCustomerId( ) );
            cuidMap.put( "identity-cuid-2", identityList.get( 1 ).getCustomerId( ) );
            cuidMap.put( Constants.PARAM_RULE_CODE, _currentRuleCode );
            cuidMap.put( PARAMETER_PAGE, String.valueOf( _currentPage ) );

            return redirect( request, VIEW_RESOLVE_DUPLICATES, cuidMap );
        }
        else
        {
            // Go to pair selection page
            return getPage( PROPERTY_PAGE_TITLE_SELECT_IDENTITIES, TEMPLATE_SELECT_IDENTITIES, model );

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
    public String getResolveDuplicates( final HttpServletRequest request )
    {
        final String _suspiciousCuid = request.getParameter( "cuid" );

        if ( StringUtils.isBlank( request.getParameter( Constants.PARAM_RULE_CODE ) ) && _currentRuleCode == null )
        {
            AppLogService.error( "No duplicate rule code provided." );
            addError( MESSAGE_CHOOSE_DUPLICATE_TYPE_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        if ( _suspiciousIdentity == null && StringUtils.isBlank( _suspiciousCuid ) )
        {
            AppLogService.error( "No suspicious identity provided." );
            addError( MESSAGE_SELECT_IDENTITIES_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        init( request, false );
        final List<String> cuidList = request.getParameterMap( ).entrySet( ).stream( ).filter( e -> e.getKey( ).startsWith( "identity-cuid-" ) )
                .map( e -> e.getValue( ) [0] ).collect( Collectors.toList( ) );
        if ( CollectionUtils.isEmpty( cuidList ) || cuidList.size( ) != 2 )
        {
            addError( MESSAGE_SELECT_IDENTITIES_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }
        try
        {
            if ( _suspiciousIdentity == null )
            {
                _suspiciousIdentity = getQualifiedIdentityFromCustomerId( _suspiciousCuid );
            }
            final IdentityDto identity1 = getQualifiedIdentityFromCustomerId( cuidList.get( 0 ) );
            final IdentityDto identity2 = getQualifiedIdentityFromCustomerId( cuidList.get( 1 ) );

            if ( identity1 == null || identity2 == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getSearchDuplicates( request );
            }
            sortWorkedIdentities( identity1, identity2 );
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        try
        {
            sendAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            addError( MESSAGE_LOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            addError( e.getMessage( ) );
            return getSearchDuplicates( request );
        }

        Map<String, Object> model = populateModel( );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );

        return getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES, model );
    }

    /**
     * Swaps the selected identities to work with.
     * 
     * @param request
     * @return the view
     */
    @Action( ACTION_SWAP_IDENTITIES )
    public String doSwapIdentities( final HttpServletRequest request )
    {
        final IdentityDto previouslyToKeep = _identityToKeep;
        _identityToKeep = _identityToMerge;
        _identityToMerge = previouslyToKeep;

        Map<String, Object> model = populateModel( );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );

        return getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES, model );
    }

    /**
     * Merges the selected identity (the duplicate) with the previously selected identity (the main identity).
     * 
     * @param request
     * @return
     */
    @Action( ACTION_MERGE_DUPLICATE )
    public String doMergeDuplicate( final HttpServletRequest request )
    {
        if ( _identityToKeep == null || _identityToMerge == null || _identityToMerge.equals( _identityToKeep ) )
        {
            addError( MESSAGE_MERGE_DUPLICATES_ERROR, getLocale( ) );
            _identityToKeep = null;
            _identityToMerge = null;
            return getSelectIdentities( request );
        }
        try
        {
            final IdentityMergeRequest identityMergeRequest = buildMergeRequest( request );
            final IdentityMergeResponse response = _serviceIdentity.mergeIdentities( identityMergeRequest, _currentClientCode, buildAgentAuthor( ) );
            if ( response.getStatus( ).getType( ) == ResponseStatusType.FAILURE )
            {
                addError( MESSAGE_MERGE_DUPLICATES_ERROR, getLocale( ) );

                // TODO : get i18n msg
                if ( response.getStatus( ).getMessage( ) != null )
                {
                    addError( response.getStatus( ).getMessage( ) );
                }
                _identityToKeep = null;
                _identityToMerge = null;
                return getSelectIdentities( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while merging identities", e );
            addError( MESSAGE_MERGE_DUPLICATES_ERROR, getLocale( ) );
            _identityToKeep = null;
            _identityToMerge = null;
            return getSelectIdentities( request );
        }

        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            addError( e.getMessage( ) );
            return getSelectIdentities( request );
        }
        _identityToKeep = null;
        _identityToMerge = null;

        addInfo( MESSAGE_MERGE_DUPLICATES_SUCCESS, getLocale( ) );
        return getSelectIdentities( request );
    }

    /**
     * Marks the selected identity (the potential duplicate) as NOT being a duplicate of the previously selected identity.
     * 
     * @param request
     * @return
     */
    @Action( ACTION_EXCLUDE_DUPLICATE )
    public String doExcludeDuplicate( final HttpServletRequest request )
    {
        final String cuidToExclude = request.getParameter( PARAMETER_CUID_TO_EXCLUDE );

        if ( cuidToExclude == null || _suspiciousIdentity == null )
        {
            addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
            return getSelectIdentities( request );
        }

        final SuspiciousIdentityExcludeRequest excludeRequest = new SuspiciousIdentityExcludeRequest( );
        excludeRequest.setIdentityCuid1( _suspiciousIdentity.getCustomerId( ) );
        excludeRequest.setIdentityCuid2( cuidToExclude );
        try
        {
            final SuspiciousIdentityExcludeResponse response = _serviceQuality.excludeIdentities( excludeRequest, _currentClientCode, buildAgentAuthor( ) );
            if ( response.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
            {
                addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
                AppLogService.error( response.getStatus( ).getMessage( ) );
                return getSelectIdentities( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while excluding the identities.", e );
            addError( MESSAGE_EXCLUDE_DUPLICATES_ERROR, getLocale( ) );
            return getSelectIdentities( request );
        }

        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            addError( e.getMessage( ) );
            return getSelectIdentities( request );
        }
        init( request, true );
        addInfo( MESSAGE_EXCLUDE_DUPLICATES_SUCCESS, getLocale( ) );
        return getSelectIdentities( request );
    }

    /**
     * Cancel and release the acknowledgement for the 2 selected identities.
     *
     * @param request
     * @return
     */
    @Action( ACTION_CANCEL )
    public String doCancel( final HttpServletRequest request )
    {
        try
        {
            releaseAcknowledgement( _suspiciousIdentity );
        }
        catch( IdentityStoreException e )
        {
            addError( MESSAGE_UNLOCK_IDENTITY_ERROR + e.getMessage( ), getLocale( ) );
            addError( e.getMessage( ) );
            return getSelectIdentities( request );
        }
        _identityToKeep = null;
        _identityToMerge = null;
        return getSelectIdentities( request );
    }

    /**
     * Initializes various components based on the provided request and forceRefresh flag.
     *
     * @param request
     *            The incoming HttpServletRequest to extract information from.
     * @param forceRefresh
     *            A boolean flag indicating whether to force a refresh or not.
     */
    public void init( final HttpServletRequest request, boolean forceRefresh )
    {
        initClientCode( request );
        initServiceContract( _currentClientCode );
        initDuplicateRules( forceRefresh );
        _previousRuleCode = _currentRuleCode;

        Optional<String> ruleCodeOpt = Optional.ofNullable( request.getParameter( Constants.PARAM_RULE_CODE ) );

        ruleCodeOpt.ifPresent( ruleCode -> {
            _currentRuleCode = ruleCode;
            _currentPage = Optional.ofNullable( request.getParameter( PARAMETER_PAGE ) ).map( Integer::parseInt ).orElse( 1 );
            try
            {
                _mediationIdentities = getMediationIdentities( request, forceRefresh );
            }
            catch( IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving mediation identities.", e );
                addError( MESSAGE_FETCH_ERROR, getLocale( ) );
            }
        } );

        if ( !ruleCodeOpt.isPresent( ) )
        {
            _currentRuleCode = null;
            _mediationIdentities.clear( );
        }
    }

    /**
     * Initializes duplicate rules based on the provided forceRefresh flag.
     *
     * @param forceRefresh
     *            A boolean flag indicating whether to forcibly fetch duplicate rules or not.
     */
    private void initDuplicateRules( boolean forceRefresh )
    {
        if ( _duplicateRules.isEmpty( ) || forceRefresh )
        {
            try
            {
                _duplicateRules = fetchDuplicateRules( );
            }
            catch( final Exception e )
            {
                AppLogService.error( "Error while retrieving duplicate rules.", e );
                addError( MESSAGE_FETCH_DUPLICATE_RULES_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * init client code * get client code from request, * or keep default client code set in properties
     *
     * @param request
     */
    private void initClientCode( final HttpServletRequest request )
    {
        String clientCode = request.getParameter( "client_code" );
        if ( !StringUtils.isBlank( clientCode ) )
        {
            _currentClientCode = clientCode;
        }
    }

    /**
     * Initializes the service contract for a given client code.
     *
     * @param clientCode
     *            The client code for which to fetch the active service contract.
     */
    private void initServiceContract( final String clientCode )
    {
        if ( _serviceContract == null )
        {
            try
            {
                _serviceContract = _serviceContractService.getActiveServiceContract( _currentClientCode, _currentClientCode, buildApplicationAuthor( ) )
                        .getServiceContract( );
                sortServiceContractAttributes( _serviceContract );
                filterServiceContractAttributes( _serviceContract );
            }
            catch( final Exception e )
            {
                AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
                addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * Sorts the attributes of the given ServiceContractDto based on their key names and their order of appearance in the _sortedAttributeKeyList.
     *
     * @param contract
     *            The ServiceContractDto whose attributes need to be sorted.
     */
    private void sortServiceContractAttributes( final ServiceContractDto contract )
    {
        if ( contract != null )
        {
            contract.getAttributeDefinitions( ).sort( ( a1, a2 ) -> {
                final int index1 = _sortedAttributeKeyList.indexOf( a1.getKeyName( ) );
                final int index2 = _sortedAttributeKeyList.indexOf( a2.getKeyName( ) );
                final Integer i1 = index1 == -1 ? 999 : index1;
                final Integer i2 = index2 == -1 ? 999 : index2;
                return i1.compareTo( i2 );
            } );
        }
    }

    /**
     * Filters the attributes of the given ServiceContractDto based on the _attributeKeyToShowList.
     *
     * @param contract
     *            The ServiceContractDto whose attributes need to be filtered.
     */
    private void filterServiceContractAttributes( final ServiceContractDto contract )
    {
        if ( contract != null && !_attributeKeyToShowList.isEmpty( ) )
        {
            contract.setAttributeDefinitions( contract.getAttributeDefinitions( ).stream( ).filter( a -> _attributeKeyToShowList.contains( a.getKeyName( ) ) )
                    .collect( Collectors.toList( ) ) );
        }
    }

    /**
     * fetch duplicate rules
     * 
     * @return list of rules
     * @throws IdentityStoreException
     */
    private List<DuplicateRuleSummaryDto> fetchDuplicateRules( ) throws IdentityStoreException
    {
        final DuplicateRuleSummarySearchResponse response = _serviceQuality.getAllDuplicateRules( _currentClientCode, buildAgentAuthor( ), _rulePriorityMin );
        if ( response == null )
        {
            throw new IdentityStoreException( "DuplicateRuleSummarySearchResponse is null." );
        }
        if ( response.getStatus( ).getType( ) == ResponseStatusType.FAILURE )
        {
            throw new IdentityStoreException( "Status of DuplicateRuleSummarySearchResponse is FAILURE. Message = " + response.getStatus( ).getMessage( ) );
        }
        if ( response.getStatus( ).getType( ) == ResponseStatusType.NOT_FOUND || CollectionUtils.isEmpty( response.getDuplicateRuleSummaries( ) ) )
        {
            AppLogService.error( "No duplicate rules found." );
            addError( MESSAGE_FETCH_DUPLICATE_RULES_NORESULT, getLocale( ) );
        }
        return response.getDuplicateRuleSummaries( );
    }

    /**
     * Fetches identities that are likely to have duplicates.
     * 
     * @return the list of identities
     * @throws IdentityStoreException
     */
    private List<IdentityDto> fetchPotentialDuplicateHolders( final HttpServletRequest request ) throws IdentityStoreException
    {
        final List<IdentityDto> identities = new ArrayList<>( );
        final ArrayList<SearchAttribute> searchAttributes = new ArrayList<>( );
        SuspiciousIdentitySearchRequest searchRequest = new SuspiciousIdentitySearchRequest( );
        searchRequest.setRuleCode( _currentRuleCode );

        for ( String searchKey : PARAMETERS_DUPLICATE_SEARCH )
        {
            String value = request.getParameter( searchKey );
            if ( value != null && !StringUtils.isBlank( value ) )
            {
                SearchAttribute searchAttribute = new SearchAttribute( );
                searchAttribute.setKey( searchKey );
                searchAttribute.setValue( value );
                searchAttribute.setTreatmentType( AttributeTreatmentType.APPROXIMATED );
                searchAttributes.add( searchAttribute );
            }
        }

        if ( !searchAttributes.isEmpty( ) )
        {
            searchRequest.setAttributes( searchAttributes );
        }

        searchRequest.setPage( _currentPage );
        searchRequest.setSize( 10 );

        final SuspiciousIdentitySearchResponse response = _serviceQuality.getSuspiciousIdentities( searchRequest, _currentClientCode, buildAgentAuthor( ) );
        if ( response != null && response.getStatus( ).getType( ) != ResponseStatusType.FAILURE && response.getSuspiciousIdentities( ) != null )
        {
            for ( final SuspiciousIdentityDto suspiciousIdentity : response.getSuspiciousIdentities( ) )
            {
                identities.add( getQualifiedIdentityFromCustomerId( suspiciousIdentity.getCustomerId( ) ) );
            }
            _totalPages = response.getPagination( ).getTotalPages( );
        }
        return identities;
    }

    /**
     * Retrieves mediation identities based on the provided HttpServletRequest and the forceRefresh flag.
     *
     * @param request
     *            The HttpServletRequest to extract required data.
     * @param forceRefresh
     *            A flag to determine whether to forcibly refresh the mediation identities.
     * @return List of mediation identities.
     * @throws IdentityStoreException
     *             If an error occurs during retrieval.
     */
    public List<MediationIdentity> getMediationIdentities( final HttpServletRequest request, boolean forceRefresh ) throws IdentityStoreException
    {
        if ( _currentRuleCode.equals( _previousRuleCode ) && _mediationIdentities != null && !forceRefresh )
        {
            return _mediationIdentities;
        }

        List<IdentityDto> potentialDuplicateHolders = fetchPotentialDuplicateHolders( request );
        return fetchMediationIdentities( potentialDuplicateHolders );
    }

    /**
     * Send an acknowledgement to the backend to mark current suspiciousIdentity as being currently resolved.
     *
     * @param suspiciousIdentity
     */
    private void sendAcknowledgement( final IdentityDto suspiciousIdentity ) throws IdentityStoreException
    {
        final SuspiciousIdentityLockRequest lockRequest = new SuspiciousIdentityLockRequest( );
        lockRequest.setCustomerId( suspiciousIdentity.getCustomerId( ) );
        lockRequest.setLocked( true );
        final SuspiciousIdentityLockResponse response = _serviceQuality.lockIdentity( lockRequest, _currentClientCode, buildAgentAuthor( ) );
        if ( !Objects.equals( ResponseStatusType.SUCCESS, response.getStatus( ).getType( ) ) )
        {
            throw new IdentityStoreException( response.getStatus( ).getMessage( ) );
        }
    }

    /**
     * Send an acknowledgement release to the backend for current suspiciousIdentity.
     *
     * @param suspiciousIdentity
     */
    private void releaseAcknowledgement( final IdentityDto suspiciousIdentity ) throws IdentityStoreException
    {
        final SuspiciousIdentityLockRequest lockRequest = new SuspiciousIdentityLockRequest( );
        lockRequest.setCustomerId( suspiciousIdentity.getCustomerId( ) );
        lockRequest.setLocked( false );
        _serviceQuality.lockIdentity( lockRequest, _currentClientCode, buildAgentAuthor( ) );
    }

    /**
     * Sort the duplicate list by quality (highest quality first)
     *
     * @param identityList
     */
    private void sortByQuality( final List<IdentityDto> identityList )
    {
        final Comparator<QualityDefinition> qualityComparator = Comparator.comparingDouble( QualityDefinition::getQuality ).reversed( );
        identityList.sort( Comparator.comparing( IdentityDto::getQuality, qualityComparator ) );
    }

    /**
     * Init worked identities for resolve duplicate screen. Identity to keep is : - the identity having an active "Mon Paris" account if there's only one
     * identity having a "Mon Paris" account - the identity having the better quality score otherwise
     *
     * @param identity1
     * @param identity2
     */
    private void sortWorkedIdentities( final IdentityDto identity1, final IdentityDto identity2 )
    {
        _identityToKeep = Optional.ofNullable( identity1 ).filter( id -> id.isMonParisActive( ) && !identity2.isMonParisActive( ) )
                .orElse( Optional.ofNullable( identity2 ).filter( id -> id.isMonParisActive( ) && !identity1.isMonParisActive( ) ).orElseGet( ( ) -> {
                    if ( identity1.getQuality( ) != null && identity2.getQuality( ) != null
                            && identity1.getQuality( ).getQuality( ) < identity2.getQuality( ).getQuality( ) )
                    {
                        return identity2;
                    }
                    else
                    {
                        return identity1;
                    }
                } ) );

        _identityToMerge = ( _identityToKeep == identity1 ) ? identity2 : identity1;
    }

    /**
     * get QualifiedIdentity From CustomerId
     *
     * @param customerId
     * @return the QualifiedIdentity , null otherwise
     * @throws IdentityStoreException
     */
    private IdentityDto getQualifiedIdentityFromCustomerId( final String customerId ) throws IdentityStoreException
    {
        if ( StringUtils.isNotBlank( customerId ) )
        {
            final IdentitySearchResponse identityResponse = _serviceIdentity.getIdentityByCustomerId( customerId, _currentClientCode, buildAgentAuthor( ) );
            if ( identityResponse != null && identityResponse.getIdentities( ) != null && identityResponse.getIdentities( ).size( ) == 1 )
            {
                return identityResponse.getIdentities( ).get( 0 );
            }
        }
        return null;
    }

    /**
     * Fetches identities that are likely to be duplicates of the identity passed in parameter.
     * 
     * @param identity
     * @return the List of potential duplicates.
     */
    private List<IdentityDto> fetchPotentialDuplicates( final IdentityDto identity ) throws IdentityStoreException
    {
        final DuplicateSearchResponse response = _serviceQuality.getDuplicates( identity.getCustomerId( ), _currentRuleCode, _currentClientCode,
                buildAgentAuthor( ) );
        if ( response != null && response.getIdentities( ) != null && !response.getIdentities( ).isEmpty( ) )
        {
            return response.getIdentities( );
        }
        return Collections.emptyList( );
    }

    /**
     * buil merge request
     * 
     * @param request
     * @return the IdentityMergeRequests
     */
    private IdentityMergeRequest buildMergeRequest( final HttpServletRequest request )
    {
        final IdentityMergeRequest req = new IdentityMergeRequest( );

        req.setPrimaryCuid( _identityToKeep.getCustomerId( ) );
        req.setPrimaryLastUpdateDate( _identityToKeep.getLastUpdateDate( ) );

        req.setSecondaryCuid( _identityToMerge.getCustomerId( ) );
        req.setSecondaryLastUpdateDate( _identityToMerge.getLastUpdateDate( ) );

        req.setDuplicateRuleCode( _currentRuleCode );

        if ( request.getParameterMap( ).entrySet( ).stream( ).anyMatch( entry -> entry.getKey( ).startsWith( "override-" ) ) )
        {
            final IdentityDto identity = new IdentityDto( );
            req.setIdentity( identity );
            final List<String> keys = request.getParameterMap( ).keySet( ).stream( )
                    .filter( key -> key.startsWith( "override-" ) && !key.endsWith( "-certif" ) ).map( key -> StringUtils.removeStart( key, "override-" ) )
                    .collect( Collectors.toList( ) );
            identity.getAttributes( ).addAll( keys.stream( ).map( key -> {
                final String value = request.getParameter( "override-" + key );
                final String certif = request.getParameter( "override-" + key + "-certif" );
                final String timestamp = request.getParameter( "override-" + key + "-timestamp-certif" );

                final AttributeDto attributeDto = new AttributeDto( );
                attributeDto.setKey( key );
                attributeDto.setValue( value );
                attributeDto.setCertifier( certif );
                attributeDto.setCertificationDate( new Timestamp( Long.parseLong( timestamp ) ) );
                return attributeDto;
            } ).collect( Collectors.toList( ) ) );
        }

        return req;
    }

    private RequestAuthor buildAgentAuthor( )
    {
        if ( _agentAuthor == null )
        {
            _agentAuthor = new RequestAuthor( );
            _agentAuthor.setName( getUser( ).getEmail( ) );
            _agentAuthor.setType( AuthorType.agent );
        }
        return _agentAuthor;
    }

    private RequestAuthor buildApplicationAuthor( )
    {
        if ( _applicationAuthor == null )
        {
            _applicationAuthor = new RequestAuthor( );
            _applicationAuthor.setName( "IdentityMediation" );
            _applicationAuthor.setType( AuthorType.application );
        }
        return _applicationAuthor;
    }

    /**
     * Fetches and processes a list of Mediation Identities based on a list of IdentityDto objects.
     *
     * @param identities
     *            The list of IdentityDto objects to process.
     * @return A List of MediationIdentity objects containing information about potential duplicates.
     * @throws IdentityStoreException
     *             if there is an issue with the identity store.
     */
    private List<MediationIdentity> fetchMediationIdentities( List<IdentityDto> identities ) throws IdentityStoreException
    {
        List<MediationIdentity> listIdentityToMerge = new ArrayList<>( );

        List<IdentityDto> identitiesCopy = new ArrayList<>( identities );
        for ( IdentityDto suspiciousIdentity : identitiesCopy )
        {
            List<IdentityDto> duplicateList = new ArrayList<>( fetchPotentialDuplicates( suspiciousIdentity ) );

            duplicateList.add( suspiciousIdentity );
            duplicateList.sort( Comparator.comparing( o -> o.getQuality( ).getQuality( ), Comparator.reverseOrder( ) ) );
            IdentityDto bestIdentity = duplicateList.get( 0 );

            MediationIdentity mediationIdentity = new MediationIdentity( );
            mediationIdentity.setSuspiciousIdentity( suspiciousIdentity );
            mediationIdentity.setBestIdentity( bestIdentity );
            mediationIdentity.setDuplicatesToMergeAttributes( new HashMap<>( ) );

            for ( IdentityDto duplicate : duplicateList )
            {
                if ( duplicate.equals( bestIdentity ) )
                    continue;

                List<String> attrToMergeList = mediationIdentity.getDuplicatesToMergeAttributes( ).computeIfAbsent( duplicate, k -> new ArrayList<>( ) );
                for ( AttributeDto attrToKeep : bestIdentity.getAttributes( ) )
                {
                    for ( AttributeDto attrToMerge : duplicate.getAttributes( ) )
                    {
                        if ( attrToKeep.getKey( ).equals( attrToMerge.getKey( ) )
                                && attrToKeep.getCertificationLevel( ) < attrToMerge.getCertificationLevel( ) )
                        {
                            attrToMergeList.add( attrToKeep.getKey( ) );
                        }
                    }
                }
            }

            listIdentityToMerge.add( mediationIdentity );
        }

        return listIdentityToMerge;
    }

    /**
     * Fetches the history of identity changes within the specified time frame, organized by date and identity.
     * 
     * @param nDaysFrom
     *            The number of days to fetch history records for.
     * @return A map containing identity history grouped by modification time.
     * @throws IdentityStoreException
     *             If there is an issue with the identity store.
     */
    private Map<Long, Map<IdentityDto, List<AttributeChange>>> fetchItentityHistoryByDate( Integer nDaysFrom ) throws IdentityStoreException
    {
        long currentTime = new Date( ).getTime( );
        long nDaysInMillis = nDaysFrom * 24 * 60 * 60 * 1000L;
        Map<Long, Map<IdentityDto, List<AttributeChange>>> groupedAttributes = new HashMap<>( );

        IdentityHistorySearchRequest request = new IdentityHistorySearchRequest( );
        request.setClientCode( _currentClientCode );
        request.setNbDaysFrom( 30 );
        request.setIdentityChangeType( IdentityChangeType.CONSOLIDATED );
        if ( _currentRuleCode != null )
        {
            Map<String, String> metadata = new HashMap<>( );
            metadata.put( Constants.METADATA_DUPLICATE_RULE_CODE, _currentRuleCode );
            request.setMetadata( metadata );
        }
        IdentityHistorySearchResponse response = _serviceIdentity.searchIdentityHistory( request, _currentClientCode, buildAgentAuthor( ) );
        if ( response != null && response.getStatus( ).getType( ) != ResponseStatusType.FAILURE && response.getHistories( ) != null )
        {
            Map<String, IdentityDto> identityMap = new HashMap<>( );
            for ( IdentityHistory h : response.getHistories( ) )
            {
                identityMap.putIfAbsent( h.getCustomerId( ), getQualifiedIdentityFromCustomerId( h.getCustomerId( ) ) );
                for ( AttributeHistory ah : h.getAttributeHistories( ) )
                {
                    for ( AttributeChange ac : ah.getAttributeChanges( ) )
                    {
                        long modTime = Optional.ofNullable( ac.getModificationDate( ) ).map( Date::getTime ).orElse( 0L );
                        if ( Math.abs( currentTime - modTime ) <= nDaysInMillis )
                        {
                            long key = ( modTime / ( 1000 * 60 ) ) * ( 1000 * 60 );
                            IdentityDto idDto = identityMap.get( h.getCustomerId( ) );
                            groupedAttributes.computeIfAbsent( key, k -> new HashMap<>( ) ).computeIfAbsent( idDto, k -> new ArrayList<>( ) ).add( ac );
                        }
                    }
                }
            }
        }
        return groupedAttributes;
    }

    /**
     * Populates the model with the necessary attributes.
     * 
     * @return the populated model.
     */
    private Map<String, Object> populateModel( )
    {
        Map<String, Object> model = getModel( );

        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_DUPLICATE_RULE_LIST, _duplicateRules );
        model.put( MARK_CURRENT_RULE_CODE, _currentRuleCode );
        model.put( MARK_MEDIATION_IDENTITY_LIST, _mediationIdentities );
        model.put( MARK_CURRENT_PAGE, _currentPage );
        model.put( MARK_TOTAL_PAGES, _totalPages );
        model.put( MARK_SUSPICIOUS_IDENTITY, _suspiciousIdentity );

        return model;
    }

}
