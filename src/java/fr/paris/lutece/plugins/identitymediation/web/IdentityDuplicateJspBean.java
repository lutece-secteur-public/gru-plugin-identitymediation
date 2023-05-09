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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
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
import java.time.Instant;
import java.util.ArrayList;
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
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_ERROR = "identitymediation.message.fetch_duplicate_holders.error";
    private static final String MESSAGE_FETCH_DUPLICATE_HOLDERS_NORESULT = "identitymediation.message.fetch_duplicate_holders.noresult";
    private static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitymediation.message.get_service_contract.error";
    private static final String MESSAGE_GET_IDENTITY_ERROR = "identitymediation.message.get_identity.error";
    private static final String MESSAGE_FETCH_DUPLICATES_ERROR = "identitymediation.message.fetch_duplicates.error";
    private static final String MESSAGE_FETCH_DUPLICATES_NORESULT = "identitymediation.message.fetch_duplicates.noresult";
    private static final String MESSAGE_MERGE_DUPLICATES_SUCCESS = "identitymediation.message.merge_duplicates.success";
    private static final String MESSAGE_EXCLUDE_DUPLICATES_SUCCESS = "identitymediation.message.exclude_duplicates.success";

    // Views
    private static final String VIEW_SEARCH_DUPLICATES = "searchDuplicates";
    private static final String VIEW_RESOLVE_DUPLICATES = "resolveDuplicates";

    // Actions
    private static final String ACTION_MERGE_DUPLICATE = "mergeDuplicate";
    private static final String ACTION_EXCLUDE_DUPLICATE = "excludeDuplicate";

    // Templates
    private static final String TEMPLATE_SEARCH_DUPLICATES = "/admin/plugins/identitymediation/search_duplicates.html";
    private static final String TEMPLATE_RESOLVE_DUPLICATES = "/admin/plugins/identitymediation/resolve_duplicates.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES = "identitymediation.resolve_duplicates.pageTitle";

    // Markers
    private static final String MARK_DUPLICATE_HOLDER_LIST = "duplicate_holder_list";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_POTENTIAL_DUPLICATE_LIST = "potential_duplicate_list";

    // Cache
    private static final ServiceContractCache _serviceContractCache = SpringContextService.getBean( "identitymediation.serviceContractCache" );

    // Session variable to store working values
    private ServiceContractDto _serviceContract;
    private String _currentClientCode = AppPropertiesService.getProperty( "identitymediation.default.client.code" );

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
        initClientCode( request );
        initServiceContract( _currentClientCode );

        final List<QualifiedIdentity> identities = new ArrayList<>( );
        try
        {
            identities.addAll( fetchPotentialDuplicateHolders( ) );
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
    private List<QualifiedIdentity> fetchPotentialDuplicateHolders( ) throws IdentityStoreException
    {
        // FIXME mock for the time being.
        try
        {
            final ArrayList<QualifiedIdentity> list = new ArrayList<>( );
            final ObjectMapper mapper = new ObjectMapper( );

            list.add( mapper.readValue(
                    "{\"scoring\":1,\"quality\":82,\"coverage\":66,\"connection_id\":\"mock-connection-id-2\",\"customer_id\":\"mock-cuid-2\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durand\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"}]}",
                    QualifiedIdentity.class ) );
            list.add( mapper.readValue(
                    "{\"scoring\":1,\"quality\":80,\"coverage\":80,\"connection_id\":\"mock-connection-id\",\"customer_id\":\"mock-cuid\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"01/01/1990\",\"type\":\"string\",\"certificationLevel\":400,\"certifier\":\"pj\",\"certificationDate\":\"2023-05-02\"},{\"key\":\"family_name\",\"value\":\"Dupont\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"fc\",\"certificationDate\":\"2023-05-02\"},{\"key\":\"first_name\",\"value\":\"Jean\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"fc\",\"certificationDate\":\"2023-05-02\"}]}",
                    QualifiedIdentity.class ) );

            return list;
        }
        catch( Exception e )
        {
            throw new IdentityStoreException( "error", e );
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
        final QualifiedIdentity identity;
        try
        {
            identity = getQualifiedIdentityFromCustomerId( request.getParameter( "cuid" ) );
            if ( identity == null )
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

        final List<QualifiedIdentity> duplicates = new ArrayList<>( );
        try
        {
            duplicates.addAll( fetchPotentialDuplicates( identity ) );
            if ( CollectionUtils.isEmpty( duplicates ) )
            {
                addError( MESSAGE_FETCH_DUPLICATES_NORESULT, getLocale( ) );
                return getSearchDuplicates( request );
            }
            sortDuplicatesByQuality( duplicates );
        }
        catch( IdentityStoreException e )
        {
            addError( MESSAGE_FETCH_DUPLICATES_ERROR, getLocale( ) );
            return getSearchDuplicates( request );
        }

        sendAcknoledgement( identity );

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, identity );
        model.put( MARK_POTENTIAL_DUPLICATE_LIST, duplicates );
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
    public String doMergeDuplicate( HttpServletRequest request )
    {
        final String identityId = request.getParameter( "customer-id" );
        final String duplicateId = request.getParameter( "duplicate-id" );
        if ( StringUtils.isAnyBlank( identityId, duplicateId ) )
        {
            throw new RuntimeException( "error" ); // TODO
        }
        final IdentityChangeRequest identityChangeRequest = buildIdentityChangeRequest( request );
        if ( identityChangeRequest != null )
        {
            // TODO send request
        }
        // TODO send merge duplicate request

        addInfo(MESSAGE_MERGE_DUPLICATES_SUCCESS, getLocale());
        return getSearchDuplicates(request);
    }

    /**
     * Marks the selected identity (the potential duplicate) as NOT being a duplicate of the previously selected identity.
     * 
     * @param request
     * @return
     */
    @Action( ACTION_EXCLUDE_DUPLICATE )
    public String doExcludeDuplicate( HttpServletRequest request )
    {
        final String identityId = request.getParameter( "customer-id" );
        final String duplicateId = request.getParameter( "duplicate-id" );
        if ( StringUtils.isAnyBlank( identityId, duplicateId ) )
        {
            throw new RuntimeException( "error" ); // TODO
        }
        // TODO send exclude duplicate request

        addInfo(MESSAGE_EXCLUDE_DUPLICATES_SUCCESS, getLocale());
        return getSearchDuplicates(request);
    }

    /**
     * Send an acknolegement to the backend to mark the identity as being currently resolved.
     * 
     * @param identity
     */
    private void sendAcknoledgement( final QualifiedIdentity identity )
    {
        // FIXME mock for now
    }

    /**
     * Sort the duplicate list by quality (highest quality first)
     *
     * @param duplicates
     */
    private void sortDuplicatesByQuality( List<QualifiedIdentity> duplicates )
    {
        duplicates.sort( Comparator.comparing( QualifiedIdentity::getQuality ).reversed( ) );
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
                    + "\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durand\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"}]}",
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
                    "{\"scoring\":1,\"quality\":79,\"coverage\":66,\"connection_id\":\"mock-connection-id-4\",\"customer_id\":\"mock-cuid-4\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"22/11/1940\",\"type\":\"string\",\"certificationLevel\":300,\"certifier\":\"mail\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"family_name\",\"value\":\"Durant\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"r2p\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"first_name\",\"value\":\"Gilles\",\"type\":\"string\",\"certificationLevel\":500,\"certifier\":\"agent\",\"certificationDate\":\"2023-05-03\"},{\"key\":\"mobile_phone\",\"value\":\"06.12.23.34.45\",\"type\":\"string\",\"certificationLevel\":600,\"certifier\":\"sms\",\"certificationDate\":\"2023-05-03\"}]}",
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
                // _serviceContract = _serviceContractCache.get( clientCode );
                // FIXME mock for now
                _serviceContract = new ObjectMapper( ).readValue(
                        "{\"attributeDefinitions\":[{\"attributeRequirement\":{\"level\":\"100\",\"name\":\"Aucune certification - auto déclaratif\",\"description\":\"Juste une identité sans compte\"},\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"gender\",\"description\":\"0:Non défini /  1:Homme / 2:Femme\",\"type\":\"STRING\",\"name\":\"Genre\"},{\"attributeRequirement\":{\"level\":\"100\",\"name\":\"Aucune certification - auto déclaratif\",\"description\":\"Juste une identité sans compte\"},\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"family_name\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Nom de famille de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"100\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"preferred_username\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Nom usuel\"},{\"attributeRequirement\":{\"level\":\"300\",\"name\":\"Données saisies par utilisateur connecté\",\"description\":\"On a un compte associé\"},\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"first_name\",\"description\":\"Prénoms usuels\",\"type\":\"STRING\",\"name\":\"Prénoms\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"birthdate\",\"description\":\"au format DD/MM/YYYY\",\"type\":\"STRING\",\"name\":\"Date de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"birthplace_code\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Code INSEE commune de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"birthcountry_code\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Code INSEE pays de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"birthplace\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Libellé INSEE commune de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"r2p\",\"label\":\"Certifiable R2P\",\"level\":\"700\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"birthcountry\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Libellé INSEE pays de naissance\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"mail\",\"label\":\"Certifiable Mail\",\"level\":\"600\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"email\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Email\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mail\",\"label\":\"Certifiable Mail\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"login\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Login de connexion (email utilisé, 0)\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"sms\",\"label\":\"Certifiable SMS\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"mobile_phone\",\"description\":\"Réservé pour l'envoi de SMS\",\"type\":\"STRING\",\"name\":\"Téléphone portable\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"fixed_phone\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Téléphone fixe\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"courrier\",\"label\":\"Certifiable Courrier\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"address\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Adresse\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"courrier\",\"label\":\"Certifiable Courrier\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"address_postal_code\",\"description\":\"Champ d'adresse : code postal\",\"type\":\"STRING\",\"name\":\"Code postal\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"courrier\",\"label\":\"Certifiable Courrier\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"address_city\",\"description\":\"Champ d'adresse : ville\",\"type\":\"STRING\",\"name\":\"Ville\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"fc\",\"label\":\"Certfiable France Connect\",\"level\":\"700\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"fc_key\",\"description\":\"Format Pivot FranceConnect - Key\",\"type\":\"STRING\",\"name\":\"(FC, 0) Key\"},{\"attributeRequirement\":null,\"attributeRight\":{\"searchable\":true,\"readable\":true,\"writable\":true},\"attributeCertifications\":[{\"code\":\"mon_paris\",\"label\":\"Mon Paris\",\"level\":\"300\"},{\"code\":\"pj\",\"label\":\"Certifiable PJ\",\"level\":\"400\"},{\"code\":\"agent\",\"label\":\"Certifiable Agent pièce originale\",\"level\":\"500\"},{\"code\":\"courrier\",\"label\":\"Certifiable Courrier\",\"level\":\"600\"}],\"certifiable\":false,\"pivot\":false,\"keyWeight\":0,\"keyName\":\"address_detail\",\"description\":\"\",\"type\":\"STRING\",\"name\":\"Complément d'adresse\"}],\"isAuthorizedDeleteCertificate\":false,\"isAuthorizedDeleteValue\":false,\"authorizedMerge\":true,\"authorizedAccountUpdate\":true,\"authorizedDeletion\":true,\"authorizedImport\":true,\"authorizedExport\":true,\"organizationalEntity\":\"Mairie de Paris\",\"responsibleName\":\"Sébastien Leridon\",\"endingDate\":null,\"contactName\":\"Sébastien Leclerc\",\"serviceType\":\"FO Lutèce\",\"startingDate\":1677448800000,\"name\":\"Contract de service pour l'application de test\"}",
                        ServiceContractDto.class );
            }
            catch( final Exception e )
            {
                AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
                addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * Build the update request in case of attributes taken from a duplicate.
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
        identity.setCustomerId( request.getParameter( "customer-id" ) );
        identity.setConnectionId( request.getParameter( "connection-id" ) );

        request.getParameterMap( ).entrySet( ).stream( ).filter( entry -> entry.getKey( ).startsWith( "override-" ) && !entry.getKey( ).endsWith( "-certif" )
                && !entry.getKey( ).endsWith( "-certiftimestamp" ) ).forEach( entry -> {
                    final String key = entry.getKey( );
                    final String value = entry.getValue( ) [0];
                    final String certif = request.getParameter( key + "-certif" );
                    final String timestamp = request.getParameter( key + "-certiftimestamp" );

                    final CertifiedAttribute attr = new CertifiedAttribute( );
                    attr.setKey( StringUtils.removeStart( key, "override-" ) );
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
