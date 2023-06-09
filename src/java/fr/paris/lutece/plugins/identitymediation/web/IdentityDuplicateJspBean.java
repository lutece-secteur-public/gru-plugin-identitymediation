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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitymediation.cache.ServiceContractCache;
import fr.paris.lutece.plugins.identityquality.v3.web.service.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentitySearchStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummaryDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummarySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummarySearchStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage identity duplicates (search, resolve)
 */
@Controller( controllerJsp = "IdentityDuplicate.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_MANAGEMENT" )
public class IdentityDuplicateJspBean extends MVCAdminJspBean
{
    // Messages
    private static final String MESSAGE_FETCH_DUPLICATE_RULES_ERROR = "identitymediation.message.fetch_duplicate_rules.error";
    private static final String MESSAGE_FETCH_DUPLICATE_RULES_NORESULT = "identitymediation.message.fetch_duplicate_rules.noresult";
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_ERROR = "identitymediation.message.fetch_duplicate_holders.error";
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_NORESULT = "identitymediation.message.fetch_duplicate_holders.noresult";
    private static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitymediation.message.get_service_contract.error";
    private static final String MESSAGE_GET_IDENTITY_ERROR = "identitymediation.message.get_identity.error";
    private static final String MESSAGE_FETCH_DUPLICATES_ERROR = "identitymediation.message.fetch_duplicates.error";
    private static final String MESSAGE_FETCH_DUPLICATES_NORESULT = "identitymediation.message.fetch_duplicates.noresult";
    private static final String MESSAGE_MERGE_DUPLICATES_SUCCESS = "identitymediation.message.merge_duplicates.success";
    private static final String MESSAGE_EXCLUDE_DUPLICATES_SUCCESS = "identitymediation.message.exclude_duplicates.success";

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

    // Markers
    private static final String MARK_DUPLICATE_RULE_LIST = "duplicate_rule_list";
    private static final String MARK_DUPLICATE_HOLDER_LIST = "duplicate_holder_list";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY_TO_KEEP = "identity_to_keep";
    private static final String MARK_IDENTITY_TO_MERGE = "identity_to_merge";

    // Beans
    private static final ServiceContractCache _serviceContractCache = SpringContextService.getBean( "identitymediation.serviceContractCache" );
    private static final IdentityQualityService _serviceQuality = SpringContextService.getBean( "identityQualityService.rest.httpAccess" );
    private static final ServiceContractService _serviceContractService = SpringContextService.getBean( "serviceContract.rest.httpAccess" );
    private static final IdentityService _serviceIdentity = SpringContextService.getBean( "identityService.rest.httpAccess" );

    // Properties
    private final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitymediation.attribute.order" ).split( "," ) );

    // Session variable to store working values
    private ServiceContractDto _serviceContract;
    private String _currentClientCode = AppPropertiesService.getProperty( "identitymediation.default.client.code" );
    private QualifiedIdentity _identityToKeep;
    private QualifiedIdentity _identityToMerge;

    /**
     *
     * @param request
     * @return
     */
    @View( value = VIEW_CHOOSE_DUPLICATE_TYPE, defaultView = true )
    public String getDuplicateTypes( final HttpServletRequest request )
    {
        initClientCode( request );
        initServiceContract( _currentClientCode );

        final List<DuplicateRuleSummaryDto> duplicateRules = new ArrayList<>( );
        try
        {
            duplicateRules.addAll( fetchDuplicateRules( ) );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while fetching duplicate calculation rules.", e );
            addError( MESSAGE_FETCH_DUPLICATE_RULES_ERROR, getLocale( ) );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_DUPLICATE_RULE_LIST, duplicateRules );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return getPage( PROPERTY_PAGE_TITLE_CHOOSE_DUPLICATE_TYPE, TEMPLATE_CHOOSE_DUPLICATE_TYPE, model );
    }

    private List<DuplicateRuleSummaryDto> fetchDuplicateRules( ) throws IdentityStoreException
    {
        final DuplicateRuleSummarySearchResponse response = _serviceQuality.getAllDuplicateRules( _currentClientCode );
        if ( response == null )
        {
            throw new IdentityStoreException( "DuplicateRuleSummarySearchResponse is null." );
        }
        if ( response.getStatus( ) == DuplicateRuleSummarySearchStatusType.FAILURE )
        {
            throw new IdentityStoreException( "Status of DuplicateRuleSummarySearchResponse is FAILURE. Message = " + response.getStatus( ).getMessage( ) );
        }
        if ( response.getStatus( ) == DuplicateRuleSummarySearchStatusType.NOT_FOUND || CollectionUtils.isEmpty( response.getDuplicateRuleSummaries( ) ) )
        {
            AppLogService.error( "No duplicate rules found." );
            addError( MESSAGE_FETCH_DUPLICATE_RULES_NORESULT, getLocale( ) );
        }
        return response.getDuplicateRuleSummaries( );
    }

    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_DUPLICATES )
    public String getSearchDuplicates( final HttpServletRequest request )
    {
        final String ruleId = request.getParameter( "rule-id" );
        if ( StringUtils.isBlank( ruleId ) )
        {
            throw new RuntimeException( "error" ); // TODO
        }
        final List<QualifiedIdentity> identities = new ArrayList<>( );
        try
        {
            identities.addAll( fetchPotentialDuplicateHolders( ruleId ) );
            if ( CollectionUtils.isEmpty( identities ) )
            {
                addInfo( MESSAGE_FETCH_DUPLICATE_HOLDERS_NORESULT, getLocale( ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while fetching potential identity duplicates.", e );
            addError( MESSAGE_FETCH_DUPLICATE_HOLDERS_ERROR, getLocale( ) );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_DUPLICATE_HOLDER_LIST, identities );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_DUPLICATES, model );
    }

    /**
     * Fetches identities that are likely to have duplicates.
     */
    private List<QualifiedIdentity> fetchPotentialDuplicateHolders( final String ruleId ) throws IdentityStoreException
    {
        final List<QualifiedIdentity> identities = new ArrayList<>( );
        final SuspiciousIdentitySearchResponse response = _serviceQuality.getSuspiciousIdentites( Integer.parseInt( ruleId ), 30, null, null );
        if ( response != null && response.getStatus( ) != SuspiciousIdentitySearchStatusType.FAILURE )
        {
            for ( final SuspiciousIdentityDto suspiciousIdentity : response.getSuspiciousIdentities( ) )
            {
                final IdentitySearchResponse identityResponse = _serviceIdentity.getIdentityByCustomerId( suspiciousIdentity.getCustomerId( ),
                        _currentClientCode );
                if ( identityResponse != null && identityResponse.getIdentities( ) != null && identityResponse.getIdentities( ).size( ) == 1 )
                {
                    identities.addAll( identityResponse.getIdentities( ) );
                }
            }
        }
        return identities;
    }

    /**
     * Returns the form to select which identities to process
     * 
     * @param request
     * @return
     */
    @View( value = VIEW_SELECT_IDENTITIES )
    public String getSelectIdentities( final HttpServletRequest request )
    {
        final QualifiedIdentity identity;
        try
        {
            identity = getQualifiedIdentityFromCustomerId( request.getParameter( "cuid" ) );
            if ( identity == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getDuplicateTypes( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getDuplicateTypes( request );
        }

        final List<QualifiedIdentity> identityList = new ArrayList<>( );
        identityList.add( identity );
        try
        {
            final List<QualifiedIdentity> duplicateList = fetchPotentialDuplicates( identity );
            if ( CollectionUtils.isEmpty( duplicateList ) )
            {
                addError( MESSAGE_FETCH_DUPLICATES_NORESULT, getLocale( ) );
                return getDuplicateTypes( request );
            }
            identityList.addAll( duplicateList );
            sortByQuality( identityList );
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_FETCH_DUPLICATES_ERROR, getLocale( ) );
            return getDuplicateTypes( request );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY_LIST, identityList );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return getPage( PROPERTY_PAGE_TITLE_SELECT_IDENTITIES, TEMPLATE_SELECT_IDENTITIES, model );
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
        final List<String> cuidList = request.getParameterMap( ).entrySet( ).stream( ).filter( e -> e.getKey( ).startsWith( "identity-cuid-" ) )
                .map( e -> e.getValue( ) [0] ).collect( Collectors.toList( ) );
        if ( CollectionUtils.isEmpty( cuidList ) || cuidList.size( ) != 2 )
        {
            throw new RuntimeException( "error" ); // TODO
        }
        try
        {
            final QualifiedIdentity identity1 = getQualifiedIdentityFromCustomerId( cuidList.get( 0 ) );
            // FIXME MOCK DATA, TO DELETE
            identity1.setAttributes( identity1.getAttributes( ).stream( )
                    .filter( a -> !a.getKey( ).equals( "mobile_phone" ) && !a.getKey( ).equals( "preferred_username" ) ).collect( Collectors.toList( ) ) );
            //

            final QualifiedIdentity identity2 = getQualifiedIdentityFromCustomerId( cuidList.get( 1 ) );
            // FIXME MOCK DATA, TO DELETE
            // identity2.setAttributes( identity2.getAttributes( ).stream( ).filter( a -> !a.getKey( ).equals( "login" ) ).collect( Collectors.toList( ) ) );
            //

            if ( identity1 == null || identity2 == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getDuplicateTypes( request );
            }
            setWorkedIdentities( identity1, identity2 );
        }
        catch( final IdentityStoreException e )
        {
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getDuplicateTypes( request );
        }

        sendAcknoledgement( _identityToKeep, _identityToMerge );

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

        return getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES, model );
    }

    /**
     * Swaps the selected identities to work with.
     * 
     * @param request
     * @return
     */
    @Action( ACTION_SWAP_IDENTITIES )
    public String doSwapIdentities( final HttpServletRequest request )
    {
        final QualifiedIdentity previouslyToKeep = _identityToKeep;
        _identityToKeep = _identityToMerge;
        _identityToMerge = previouslyToKeep;

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY_TO_KEEP, _identityToKeep );
        model.put( MARK_IDENTITY_TO_MERGE, _identityToMerge );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );

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
            throw new RuntimeException( "error" ); // TODO
        }
        final IdentityChangeRequest identityChangeRequest = buildIdentityChangeRequest( request );
        if ( identityChangeRequest != null )
        {
            // TODO send request
        }
        // TODO send merge duplicate request

        releaseAcknoledgement( _identityToKeep, _identityToMerge );
        _identityToKeep = null;
        _identityToMerge = null;

        addInfo( MESSAGE_MERGE_DUPLICATES_SUCCESS, getLocale( ) );
        return getDuplicateTypes( request );
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
        if ( _identityToKeep == null || _identityToMerge == null || _identityToMerge.equals( _identityToKeep ) )
        {
            throw new RuntimeException( "error" ); // TODO
        }
        // TODO send exclude duplicate request

        releaseAcknoledgement( _identityToKeep, _identityToMerge );
        _identityToKeep = null;
        _identityToMerge = null;

        addInfo( MESSAGE_EXCLUDE_DUPLICATES_SUCCESS, getLocale( ) );
        return getDuplicateTypes( request );
    }

    /**
     * Cancel and release the acknolegement for the 2 selected identities.
     *
     * @param request
     * @return
     */
    @Action( ACTION_CANCEL )
    public String doCancel( final HttpServletRequest request )
    {
        if ( _identityToKeep == null || _identityToMerge == null )
        {
            throw new RuntimeException( "error" ); // TODO
        }

        releaseAcknoledgement( _identityToKeep, _identityToMerge );
        _identityToKeep = null;
        _identityToMerge = null;
        return getDuplicateTypes( request );
    }

    /**
     * Send an acknolegement to the backend to mark both identities as being currently resolved.
     *
     * @param identity1
     * @param identity2
     */
    private void sendAcknoledgement( final QualifiedIdentity identity1, final QualifiedIdentity identity2 )
    {
        // FIXME mock for now
    }

    /**
     * Send an acknolegement release to the backend for both identities.
     *
     * @param identity1
     * @param identity2
     */
    private void releaseAcknoledgement( final QualifiedIdentity identity1, final QualifiedIdentity identity2 )
    {
        // FIXME mock for now
    }

    /**
     * Sort the duplicate list by quality (highest quality first)
     *
     * @param identityList
     */
    private void sortByQuality( final List<QualifiedIdentity> identityList )
    {
        identityList.sort( Comparator.comparing( QualifiedIdentity::getQuality ).reversed( ) );
    }

    /**
     * Init worked identities for resolve duplicate screen. Identity to keep is set with the identity having a login attribute (Mon Paris account), and/or
     * better quality score.
     * 
     * @param identity1
     * @param identity2
     */
    private void setWorkedIdentities( final QualifiedIdentity identity1, final QualifiedIdentity identity2 )
    {
        if ( identity1.getAttributes( ).stream( ).anyMatch( a -> a.getKey( ).equals( "login" ) ) )
        {
            if ( identity2.getAttributes( ).stream( ).anyMatch( a -> a.getKey( ).equals( "login" ) ) )
            {
                if ( identity1.getQuality( ) >= identity2.getQuality( ) )
                {
                    _identityToKeep = identity1;
                    _identityToMerge = identity2;
                }
                else
                {
                    _identityToKeep = identity2;
                    _identityToMerge = identity1;
                }
            }
            else
            {
                _identityToKeep = identity1;
                _identityToMerge = identity2;
            }
        }
        else
        {
            if ( identity2.getAttributes( ).stream( ).anyMatch( a -> a.getKey( ).equals( "login" ) ) )
            {
                _identityToKeep = identity2;
                _identityToMerge = identity1;
            }
            else
            {
                if ( identity1.getQuality( ) >= identity2.getQuality( ) )
                {
                    _identityToKeep = identity1;
                    _identityToMerge = identity2;
                }
                else
                {
                    _identityToKeep = identity2;
                    _identityToMerge = identity1;
                }
            }
        }
    }

    /**
     * get QualifiedIdentity From CustomerId
     *
     * @param customerId
     * @return the QualifiedIdentity , null otherwise
     * @throws IdentityStoreException
     */
    private QualifiedIdentity getQualifiedIdentityFromCustomerId( final String customerId ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( customerId ) )
        {
            return null;
        }
        // FIXME mock for now
        try
        {
            return new ObjectMapper( ).readValue( "{\"scoring\":1,\"quality\":82,\"coverage\":66,\"connection_id\":\"mock-connection-id-2\",\"customer_id\":\""
                    + customerId
                    + "\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durand\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"mobile_phone\",\"value\":\"06.66.32.89.01\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"sms\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"preferred_username\",\"value\":\"Dupont\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"fc\",\"certificationDate\":\"2023-05-05\"},{\"key\":\"login\",\"value\":\"login@monparis.fr\",\"type\":\"string\",\"certificationLevel\":400,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-13\"}]}",
                    QualifiedIdentity.class );
        }
        catch( final Exception e )
        {
            throw new IdentityStoreException( "error", e );
        }
    }

    /**
     * Fetches identities that are likely to be duplicates of the identity passed in parameter.
     * 
     * @param identity
     * @return the List of potential duplicates.
     */
    private List<QualifiedIdentity> fetchPotentialDuplicates( final QualifiedIdentity identity ) throws IdentityStoreException
    {
        // FIXME mock for now
        try
        {
            final ArrayList<QualifiedIdentity> list = new ArrayList<>( );
            final ObjectMapper mapper = new ObjectMapper( );

            list.add( mapper.readValue(
                    "{\"scoring\":1,\"quality\":77,\"coverage\":66,\"connection_id\":\"mock-connection-id-3\",\"customer_id\":\"mock-cuid-3\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durand\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gille\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"mobile_phone\",\"value\":\"06.66.32.89.01\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"sms\",\"certificationDate\":\"2023-05-03\"}]}",
                    QualifiedIdentity.class ) );
            list.add( mapper.readValue(
                    "{\"scoring\":1,\"quality\":79,\"coverage\":66,\"connection_id\":\"mock-connection-id-4\",\"customer_id\":\"mock-cuid-4\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durant\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":500,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"mobile_phone\",\"value\":\"06.12.23.34.45\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"sms\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"login\",\"value\":\"login@monparis.fr\",\"type\":\"string\",\"certificationLevel\":400,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-13\"}]}",
                    QualifiedIdentity.class ) );
            list.add( mapper.readValue(
                    "{\"scoring\":1,\"quality\":81,\"coverage\":66,\"connection_id\":\"mock-connection-id-5\",\"customer_id\":\"mock-cuid-5\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durant\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":500,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"mobile_phone\",\"value\":\"06.31.55.63.28\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"sms\",\"certificationDate\":\"2023-05-03\"}]}",
                    QualifiedIdentity.class ) );

            return list;
        }
        catch( Exception e )
        {
            throw new IdentityStoreException( "error", e );
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
     * init service contract
     *
     * @param clientCode
     */
    private void initServiceContract( final String clientCode )
    {
        if ( _serviceContract == null )
        {
            try
            {
                _serviceContract = _serviceContractService.getActiveServiceContract( clientCode ).getServiceContract( );
                sortServiceContractAttributes( _serviceContract );
            }
            catch( final Exception e )
            {
                AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
                addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }

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
     * Build the update request in case of attributes copied from a duplicate.
     * 
     * @return the request ready to be sent, null if no update needed.
     */
    private IdentityChangeRequest buildIdentityChangeRequest( final HttpServletRequest request )
    {
        if ( request.getParameterMap( ).entrySet( ).stream( ).noneMatch( entry -> entry.getKey( ).startsWith( "override-" ) ) )
        {
            return null;
        }
        final IdentityChangeRequest changeRequest = new IdentityChangeRequest( );
        final Identity identity = new Identity( );
        identity.setCustomerId( _identityToKeep.getCustomerId( ) );
        identity.setConnectionId( _identityToKeep.getConnectionId( ) );

        request.getParameterMap( ).entrySet( ).stream( ).filter( entry -> entry.getKey( ).startsWith( "override-" ) && !entry.getKey( ).endsWith( "-certif" ) )
                .forEach( entry -> {
                    final String overrideKey = entry.getKey( );
                    final String value = entry.getValue( ) [0];
                    final String certif = request.getParameter( overrideKey + "-certif" );
                    final String timestamp = request.getParameter( overrideKey + "-timestamp-certif" );

                    final CertifiedAttribute attr = new CertifiedAttribute( );
                    attr.setKey( StringUtils.removeStart( overrideKey, "override-" ) );
                    attr.setValue( value );
                    attr.setCertificationProcess( certif );
                    attr.setCertificationDate( new Date( Long.parseLong( timestamp ) ) );
                    identity.getAttributes( ).add( attr );
                } );

        RequestAuthor author = new RequestAuthor( );
        author.setName( getUser( ).getEmail( ) );
        author.setType( AuthorType.application );

        changeRequest.setIdentity( identity );
        changeRequest.setOrigin( author );

        return changeRequest;
    }

}
