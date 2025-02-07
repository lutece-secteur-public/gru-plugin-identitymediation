package fr.paris.lutece.plugins.identitymediation.web;

import fr.paris.lutece.plugins.identitymediation.buisness.LocalIdentityDto;
import fr.paris.lutece.plugins.identitymediation.buisness.MediationIdentity;
import fr.paris.lutece.plugins.identitymediation.cache.ServiceContractCache;
import fr.paris.lutece.plugins.identitymediation.service.MediationService;
import fr.paris.lutece.plugins.identityquality.v3.web.service.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.QualityDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentitySearchResponse;
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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityServiceExtended;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AbstractIdentityDuplicateJspBean extends MVCAdminJspBean {

    // Messages
    protected static final String MESSAGE_FETCH_DUPLICATE_RULES_ERROR = "identitymediation.message.fetch_duplicate_rules.error";
    protected static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitymediation.message.get_service_contract.error";
    protected static final String MESSAGE_FETCH_ERROR = "identitymediation.message.fetch.error";
    protected static final String MESSAGE_FETCH_IDENTITY_LOCK_STATUS_ERROR = "identitymediation.message.fetch.lock.status";

    // Beans
    protected static final IdentityQualityService _serviceQuality = SpringContextService.getBean( "identitymediation.identityQualityService.rest.httpAccess" );
    protected static final IdentityServiceExtended _serviceIdentity = SpringContextService.getBean( "identitymediation.identityService.rest.httpAccess" );
    protected static final MediationService _mediationService = MediationService.instance();
    protected static final ServiceContractCache _serviceContractCache = SpringContextService.getBean( "identitymediation.serviceContractCache" );

    // Properties
    protected static final String MEDIATION_CLIENT_CODE = AppPropertiesService.getProperty( "identitymediation.default.client.code" );
    protected String _currentClientCode = MEDIATION_CLIENT_CODE;
    protected static final String PROPERTY_RULE_PRIORITY_MINIMUM = "identitymediation.rules.priority.minimum";

    // Parameters
    final String [ ] PARAMETERS_DUPLICATE_SEARCH = {
            Constants.PARAM_FIRST_NAME, Constants.PARAM_FAMILY_NAME, Constants.PARAM_BIRTH_DATE
    };
    final String PARAMETER_PAGE = "page";
    final String PARAMETER_CLIENT_CODE = "client_code";
    final String PARAMETER_ONLY_ONE_DUPLICATE = "only_one";

    // Session variable to store working values
    protected ServiceContractDto _serviceContract;
    protected final Integer _rulePriorityMin = AppPropertiesService.getPropertyInt( PROPERTY_RULE_PRIORITY_MINIMUM, 100 );
    protected String _previousRuleCode;
    protected String _currentRuleCode;
    protected LocalIdentityDto _identityToKeep;
    protected LocalIdentityDto _identityToMerge;
    protected LocalIdentityDto _suspiciousIdentity;
    protected final List<DuplicateRuleSummaryDto> _duplicateRules = new ArrayList<>( );
    protected final List<MediationIdentity> _mediationIdentities = new ArrayList<>( );
    protected Integer _totalPages;
    protected Integer _currentPage;
    protected Map<String, Integer> _totalRecordByRule;
    protected int _totalRecords = 0;
    protected Map<String, String> _ruleBySuspiciousIdentity;
    protected RequestAuthor _agentAuthor;
    protected RequestAuthor _applicationAuthor;

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
        _totalRecordByRule = new HashMap<>( );
        _ruleBySuspiciousIdentity = new HashMap<>( );
        _totalRecords = 0;
        this.initClientCode( request );
        this.initServiceContract( _currentClientCode );
        this.initDuplicateRules( forceRefresh );
        _previousRuleCode = _currentRuleCode;
        _mediationIdentities.clear( );
        final String pageParam = request.getParameter( PARAMETER_PAGE );
        _currentPage = Optional.ofNullable( StringUtils.isNotBlank( pageParam ) ? pageParam : null ).map( Integer::parseInt ).orElse( 1 );

        //on va vérifier si on fait une recherche de doublon par règle
        final String ruleParam = request.getParameter( Constants.PARAM_RULE_CODE );
        final Optional<String> ruleCodeOpt = Optional.ofNullable( StringUtils.isNotBlank( ruleParam ) ? ruleParam : null );

        //on va vérifier si on fait une recherche de doublon par attributs
        final List<Optional<String>> identitySearch = new ArrayList<>( );
        final String firstName = request.getParameter( Constants.PARAM_FIRST_NAME );
        identitySearch.add(Optional.ofNullable( StringUtils.isNotBlank( firstName )  ? firstName : null ) ) ;
        final String familyName = request.getParameter( Constants.PARAM_FAMILY_NAME );
        identitySearch.add(Optional.ofNullable( StringUtils.isNotBlank( familyName )  ? familyName : null ) );
        final String birthDate = request.getParameter( Constants.PARAM_BIRTH_DATE );
        identitySearch.add(Optional.ofNullable(StringUtils.isNotBlank( birthDate )  ? birthDate : null ) );
        final boolean attributeSearchPresent = identitySearch.stream( ).anyMatch( Optional::isPresent );

        if( attributeSearchPresent || ruleCodeOpt.isPresent( ) )
        {
            final List<String> rulesList = new ArrayList<>( );
            boolean search = false;
            if( ruleCodeOpt.isPresent( ) )
            {
                rulesList.add( ruleCodeOpt.get( ) );
                _currentRuleCode = ruleCodeOpt.get( );
                if( attributeSearchPresent )
                {
                    search = true;
                    // sert à initialiser la liste des doublons par attributs sélectionnés
                    _totalRecordByRule.putAll( _duplicateRules.stream( ).collect( Collectors.toMap( DuplicateRuleSummaryDto::getCode, rule -> 0) ) );
                    this.getSuspiciousSummary( request );
                }
            }
            else
            {
                search = true;
                _totalRecordByRule.putAll( _duplicateRules.stream( ).collect( Collectors.toMap( DuplicateRuleSummaryDto::getCode, rule -> 0) ) );
                rulesList.addAll( _duplicateRules.stream( ).map( DuplicateRuleSummaryDto::getCode ).collect( Collectors.toList( ) ) );
            }

            final boolean finalSearch = search;
            rulesList.stream( ).parallel( ).forEach( rule -> {
                final String currentRuleCode = ruleCodeOpt.orElse( rule );
                try
                {
                    this.initMediationIdentities( request, forceRefresh, finalSearch, currentRuleCode );
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.error( "Error while retrieving mediation identities.", e );
                    this.addError( MESSAGE_FETCH_ERROR, getLocale( ) );
                }
            });
            _totalRecords = _totalRecordByRule.values( ).stream( ).mapToInt( Integer::intValue ).sum( );
        }
        //si aucun code n'est présent on met _currentRuleCode à "" pour éviter un plantage sur la jsp
        if( !ruleCodeOpt.isPresent( ) )
        {
            _currentRuleCode = "";
        }
    }

    protected void getSuspiciousSummary( final HttpServletRequest request )
    {
        for ( final DuplicateRuleSummaryDto rule : _duplicateRules)
        {
            final String currentRuleCode = rule.getCode( );
            try
            {
                this.fetchPotentialDuplicateHolders( request, currentRuleCode );
            }
            catch( final IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving mediation identities.", e );
                this.addError( MESSAGE_FETCH_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * Initializes duplicate rules based on the provided forceRefresh flag.
     *
     * @param forceRefresh
     *            A boolean flag indicating whether to forcibly fetch duplicate rules or not.
     */
    protected void initDuplicateRules( final boolean forceRefresh )
    {
        if ( _duplicateRules.isEmpty( ) || forceRefresh )
        {
            _duplicateRules.clear( );
            try
            {
                final DuplicateRuleSummarySearchResponse response = _serviceQuality.getAllDuplicateRules( _currentClientCode, this.buildAgentAuthor( ), _rulePriorityMin );
                if ( _mediationService.isSuccess( response ) )
                {
                    _duplicateRules.addAll( response.getDuplicateRuleSummaries( ).stream( ).filter( DuplicateRuleSummaryDto::isActive ).collect( Collectors.toList( ) ) );
                }
                else
                {
                    this.logAndDisplayStatusErrorMessage( response );
                }
            }
            catch( final Exception e )
            {
                AppLogService.error( "Error while retrieving duplicate rules.", e );
                this.addError( MESSAGE_FETCH_DUPLICATE_RULES_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * init client code * get client code from request, * or keep default client code set in properties
     *
     * @param request
     */
    protected void initClientCode( final HttpServletRequest request )
    {
        final String clientCode = request.getParameter( PARAMETER_CLIENT_CODE );
        if ( !StringUtils.isBlank( clientCode ) )
        {
            _currentClientCode = clientCode;
        }
        else
        {
            _currentClientCode = MEDIATION_CLIENT_CODE;
        }
    }

    /**
     * Initializes the service contract for a given client code.
     *
     * @param clientCode
     *            The client code for which to fetch the active service contract.
     */
    protected void initServiceContract( final String clientCode )
    {
        if ( _serviceContract == null )
        {
            _serviceContract = _serviceContractCache.get( clientCode );
            if( _serviceContract == null )
            {
                this.addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }



    /**
     * Fetches identities that are likely to have duplicates.
     *
     * @return the list of identities
     * @throws IdentityStoreException
     */
    protected List<LocalIdentityDto> fetchPotentialDuplicateHolders( final HttpServletRequest request, final String currentRuleCode ) throws IdentityStoreException
    {
        final List<LocalIdentityDto> identities = new ArrayList<>( );
        final ArrayList<SearchAttribute> searchAttributes = new ArrayList<>( );
        final SuspiciousIdentitySearchRequest searchRequest = new SuspiciousIdentitySearchRequest( );
        searchRequest.setRuleCode( currentRuleCode );

        for ( final String searchKey : PARAMETERS_DUPLICATE_SEARCH )
        {
            final String value = request.getParameter( searchKey );
            if ( value != null && !StringUtils.isBlank( value ) )
            {
                final SearchAttribute searchAttribute = new SearchAttribute( );
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
        if ( _mediationService.isSuccess( response ) && response.getSuspiciousIdentities( ) != null )
        {
            for ( final SuspiciousIdentityDto suspiciousIdentity : response.getSuspiciousIdentities( ) )
            {
                if(suspiciousIdentity.getLock() != null)
                {
                    identities.add( this.getQualifiedIdentityFromCustomerId( suspiciousIdentity.getCustomerId( ), suspiciousIdentity.getLock().isLocked() ) );
                }
                else
                {
                    identities.add( this.getQualifiedIdentityFromCustomerId( suspiciousIdentity.getCustomerId( ) ) );
                }
            }
            _totalPages = response.getPagination( ).getTotalPages( );
            _totalRecordByRule.replace( currentRuleCode, response.getPagination( ).getTotalRecords( ) );
        }
        else
        {
            this.logAndDisplayStatusErrorMessage( response );
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
     * @throws IdentityStoreException
     *             If an error occurs during retrieval.
     */
    protected void initMediationIdentities( final HttpServletRequest request, boolean forceRefresh, boolean search, final String currentRuleCode) throws IdentityStoreException
    {
        if ( currentRuleCode.equals( _previousRuleCode ) && !_mediationIdentities.isEmpty( ) && !forceRefresh )
        {
            return;
        }
        final List<LocalIdentityDto> potentialDuplicateHolders = this.fetchPotentialDuplicateHolders( request, currentRuleCode );
        final List<MediationIdentity> fetchedMediationIdentities = this.fetchMediationIdentities( potentialDuplicateHolders, currentRuleCode );
        if( !search ) {
            _mediationIdentities.clear();
        }
        if ( !fetchedMediationIdentities.isEmpty( ) )
        {
            _mediationIdentities.addAll( fetchedMediationIdentities );
            for( final MediationIdentity mediationIdentity : fetchedMediationIdentities )
            {
                _ruleBySuspiciousIdentity.put( mediationIdentity.getSuspiciousIdentity( ).getCustomerId( ), currentRuleCode );
            }
        }
    }

    /**
     * Send an acknowledgement to the backend to mark current suspiciousIdentity as being currently resolved.
     *
     * @param suspiciousIdentity
     */
    protected void sendAcknowledgement( final LocalIdentityDto suspiciousIdentity ) throws IdentityStoreException
    {
        final SuspiciousIdentityLockRequest lockRequest = new SuspiciousIdentityLockRequest( );
        lockRequest.setCustomerId( suspiciousIdentity.getCustomerId( ) );
        lockRequest.setLocked( true );
        final SuspiciousIdentityLockResponse response = _serviceQuality.lockIdentity( lockRequest, _currentClientCode, this.buildAgentAuthor( ) );
        if ( !_mediationService.isSuccess( response ) )
        {
            throw new IdentityStoreException( I18nService.getLocalizedString( response.getStatus( ).getMessageKey( ), this.getLocale( ) ) );
        }
    }

    /**
     * Send an acknowledgement release to the backend for current suspiciousIdentity.
     *
     * @param suspiciousIdentity
     */
    protected void releaseAcknowledgement( final LocalIdentityDto suspiciousIdentity ) throws IdentityStoreException
    {
        final SuspiciousIdentityLockRequest lockRequest = new SuspiciousIdentityLockRequest( );
        lockRequest.setCustomerId( suspiciousIdentity.getCustomerId( ) );
        lockRequest.setLocked( false );
        final SuspiciousIdentityLockResponse response = _serviceQuality.lockIdentity( lockRequest, _currentClientCode, buildAgentAuthor( ) );
        logAndDisplayStatusErrorMessage( response );
    }

    /**
     * Sort the duplicate list by quality (highest quality first)
     *
     * @param identityList
     */
    protected void sortByQuality( final List<LocalIdentityDto> identityList )
    {
        final Comparator<QualityDefinition> qualityComparator = Comparator.comparingDouble( QualityDefinition::getQuality ).reversed( );
        identityList.sort( Comparator.comparing( LocalIdentityDto::getQuality, qualityComparator ) );
    }

    /**
     * Init worked identities for resolve duplicate screen. Identity to keep is : - the identity having an active "Mon Paris" account if there's only one
     * identity having a "Mon Paris" account - the identity having the better quality score otherwise
     *
     * @param identity1
     * @param identity2
     */
    protected void sortWorkedIdentities( final LocalIdentityDto identity1, final LocalIdentityDto identity2 )
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
    protected LocalIdentityDto getQualifiedIdentityFromCustomerId( final String customerId ) throws IdentityStoreException
    {
        if ( StringUtils.isNotBlank( customerId ) )
        {
            final IdentitySearchResponse identityResponse = _serviceIdentity.getIdentityByCustomerId( customerId, _currentClientCode, buildAgentAuthor( ) );
            if ( _mediationService.isSuccess( identityResponse ) )
            {
                if ( identityResponse.getIdentities( ).size( ) == 1 )
                {
                    LocalIdentityDto localIdentityDto = LocalIdentityDto.toLocalIdentityDto(identityResponse.getIdentities().get(0));
                    localIdentityDto.setCanNotify( _mediationService.canSendEmail( localIdentityDto ) && _mediationService.validateIdentityCertification( localIdentityDto ) );
                    localIdentityDto.setLocked(false);
                    return localIdentityDto;
                }
            }
            else
            {
                this.logAndDisplayStatusErrorMessage( identityResponse );
            }
        }
        return null;
    }

    /**
     * get QualifiedIdentity From CustomerId
     *
     * @param customerId
     * @return the QualifiedIdentity , null otherwise
     * @throws IdentityStoreException
     */
    protected LocalIdentityDto getQualifiedIdentityFromCustomerId( final String customerId, boolean locked ) throws IdentityStoreException
    {
        if ( StringUtils.isNotBlank( customerId ) )
        {
            final IdentitySearchResponse identityResponse = _serviceIdentity.getIdentityByCustomerId( customerId, _currentClientCode, buildAgentAuthor( ) );
            if ( _mediationService.isSuccess( identityResponse ) )
            {
                if ( identityResponse.getIdentities( ).size( ) == 1 )
                {
                    LocalIdentityDto localIdentityDto = LocalIdentityDto.toLocalIdentityDto(identityResponse.getIdentities().get(0));
                    localIdentityDto.setCanNotify( _mediationService.canSendEmail( localIdentityDto ) && _mediationService.validateIdentityCertification( localIdentityDto ) );
                    localIdentityDto.setLocked(locked);
                    return localIdentityDto;
                }
            }
            else
            {
                this.logAndDisplayStatusErrorMessage( identityResponse );
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
    protected List<LocalIdentityDto> fetchPotentialDuplicates( final LocalIdentityDto identity, final String currentRuleCode, final Boolean showError ) throws IdentityStoreException
    {
        final DuplicateSearchResponse response = _serviceQuality.getDuplicates( identity.getCustomerId( ), currentRuleCode, _currentClientCode,
                this.buildAgentAuthor( ) );
        if ( _mediationService.isSuccess( response ) && !response.getIdentities( ).isEmpty( ) )
        {
            return response.getIdentities( ).stream().map( LocalIdentityDto::toLocalIdentityDto )
                    .peek( localIdentityDto -> localIdentityDto.setCanNotify( _mediationService.canSendEmail( localIdentityDto ) && _mediationService.validateIdentityCertification( localIdentityDto ) ) )
                    .peek( localIdentityDto -> localIdentityDto.setLocked( this.getLockedStatus(localIdentityDto) ) )
                    .collect( Collectors.toList( ) );
        }
        if ( showError ) {
            this.logAndDisplayStatusErrorMessage( response );
        }
        return Collections.emptyList( );
    }


    protected Map<DuplicateRuleSummaryDto, List<LocalIdentityDto>> fetchPotentialDuplicates( final LocalIdentityDto identity ) throws IdentityStoreException {
        if (identity == null) {
            return Collections.emptyMap();
        }

        this.initDuplicateRules(false);

        final Map<DuplicateRuleSummaryDto, List<LocalIdentityDto>> duplicates = new ConcurrentHashMap<>();
        final LinkedHashMap<DuplicateRuleSummaryDto, List<LocalIdentityDto>> sortedDuplicatesByRulePriority;

        final CompletableFuture<Void> allOf = CompletableFuture.allOf(_duplicateRules.stream()
                .map(rule -> CompletableFuture.runAsync(() -> {
                    try {
                        final List<LocalIdentityDto> potentialDuplicates = this.fetchPotentialDuplicates(identity, rule.getCode(), false);
                        if (!potentialDuplicates.isEmpty()) {
                            duplicates.put(rule, potentialDuplicates);
                        }
                    } catch (IdentityStoreException e) {
                        AppLogService.error("Error while fetching potential duplicates", e);
                    }
                })).toArray(CompletableFuture[]::new));
        try
        {
            allOf.get();
            sortedDuplicatesByRulePriority =
                    duplicates.entrySet()
                              .stream()
                              .sorted(Comparator.comparingInt(entry -> entry.getKey().getPriority()))
                              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }
        catch ( final InterruptedException | ExecutionException e )
        {
            throw new IdentityStoreException("Error fetching potential duplicates", e);
        }

        return sortedDuplicatesByRulePriority;
    }


    /**
     * buil merge reques
     *
     * @param request
     * @return the IdentityMergeRequests
     */
    protected IdentityMergeRequest buildMergeRequest(final HttpServletRequest request )
    {
        final IdentityMergeRequest req = new IdentityMergeRequest( );

        req.setPrimaryCuid( _identityToKeep.getCustomerId( ) );
        req.setPrimaryLastUpdateDate( _identityToKeep.getLastUpdateDate( ) );

        req.setSecondaryCuid( _identityToMerge.getCustomerId( ) );
        req.setSecondaryLastUpdateDate( _identityToMerge.getLastUpdateDate( ) );

        req.setDuplicateRuleCode( _currentRuleCode );

        if ( request.getParameterMap( ).entrySet( ).stream( ).anyMatch( entry -> entry.getKey( ).startsWith( "override-" ) ) )
        {
            final LocalIdentityDto identity = new LocalIdentityDto( );
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

    protected RequestAuthor buildAgentAuthor( )
    {
        if ( _agentAuthor == null )
        {
            _agentAuthor = new RequestAuthor( );
            _agentAuthor.setName( getUser( ).getEmail( ) );
            _agentAuthor.setType( AuthorType.agent );
        }
        return _agentAuthor;
    }

    protected RequestAuthor buildApplicationAuthor( )
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
    protected List<MediationIdentity> fetchMediationIdentities(final List<LocalIdentityDto> identities, final String currentRuleCode ) throws IdentityStoreException
    {
        final List<MediationIdentity> listIdentityToMerge = new ArrayList<>( );

        final List<LocalIdentityDto> identitiesCopy = new ArrayList<>( identities );
        for ( final LocalIdentityDto suspiciousIdentity : identitiesCopy )
        {
            boolean locked = suspiciousIdentity.isLocked();
            final List<LocalIdentityDto> duplicateList = new ArrayList<>( this.fetchPotentialDuplicates( suspiciousIdentity, currentRuleCode, true ) );
            duplicateList.add( suspiciousIdentity );
            duplicateList.sort( Comparator.comparing(o -> o.getQuality( ).getQuality( ), Comparator.reverseOrder( ) ) );
            final LocalIdentityDto bestIdentity = duplicateList.get( 0 );

            final MediationIdentity mediationIdentity = new MediationIdentity( );
            mediationIdentity.setSuspiciousIdentity( suspiciousIdentity );
            mediationIdentity.setBestIdentity( bestIdentity );
            mediationIdentity.setDuplicatesToMergeAttributes( new HashMap<>( ) );

            for ( final LocalIdentityDto duplicate : duplicateList )
            {
                if(!locked)
                    locked = duplicate.isLocked( );

                if ( duplicate.equals( bestIdentity ) )
                    continue;

                final List<String> attrToMergeList = mediationIdentity.getDuplicatesToMergeAttributes( ).computeIfAbsent( duplicate, k -> new ArrayList<>( ) );
                for ( final AttributeDto attrToKeep : bestIdentity.getAttributes( ) )
                {
                    for ( final AttributeDto attrToMerge : duplicate.getAttributes( ) )
                    {
                        if ( attrToKeep.getKey( ).equals( attrToMerge.getKey( ) )
                                && attrToKeep.getCertificationLevel( ) < attrToMerge.getCertificationLevel( ) )
                        {
                            attrToMergeList.add( attrToKeep.getKey( ) );
                        }
                    }
                }
            }
            mediationIdentity.setLocked(locked);

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
    protected Map<Long, Map<LocalIdentityDto, List<AttributeChange>>> fetchItentityHistoryByDate(Integer nDaysFrom ) throws IdentityStoreException
    {
        final long currentTime = new Date( ).getTime( );
        final long nDaysInMillis = nDaysFrom * 24 * 60 * 60 * 1000L;
        final Map<Long, Map<LocalIdentityDto, List<AttributeChange>>> groupedAttributes = new HashMap<>( );

        final IdentityHistorySearchRequest request = new IdentityHistorySearchRequest( );
        request.setClientCode( _currentClientCode );
        request.setNbDaysFrom( 30 );
        request.setIdentityChangeType( IdentityChangeType.CONSOLIDATED );
        if ( _currentRuleCode != null )
        {
            Map<String, String> metadata = new HashMap<>( );
            metadata.put( Constants.METADATA_DUPLICATE_RULE_CODE, _currentRuleCode );
            request.setMetadata( metadata );
        }
        final IdentityHistorySearchResponse response = _serviceIdentity.searchIdentityHistory( request, _currentClientCode, buildAgentAuthor( ) );
        if ( _mediationService.isSuccess( response ) && response.getHistories( ) != null )
        {
            for ( IdentityHistory h : response.getHistories( ) )
            {
                final LocalIdentityDto identity = this.getQualifiedIdentityFromCustomerId( h.getCustomerId( ) );
                if ( identity == null )
                {
                    continue;
                }
                for ( final AttributeHistory ah : h.getAttributeHistories( ) )
                {
                    for ( final AttributeChange ac : ah.getAttributeChanges( ) )
                    {
                        final long modTime = Optional.ofNullable( ac.getModificationDate( ) ).map( Date::getTime ).orElse( 0L );
                        if ( Math.abs( currentTime - modTime ) <= nDaysInMillis )
                        {
                            long key = ( modTime / ( 1000 * 60 ) ) * ( 1000 * 60 );
                            groupedAttributes.computeIfAbsent( key, k -> new HashMap<>( ) ).computeIfAbsent( identity, k -> new ArrayList<>( ) ).add( ac );
                        }
                    }
                }
            }
        }
        else
        {
            this.logAndDisplayStatusErrorMessage( response );
        }
        return groupedAttributes;
    }

    /**
     * log and display in IHM the localized status message if apiResponse is in error
     *
     * @param apiResponse
     *            the API response
     */
    protected void logAndDisplayStatusErrorMessage( final ResponseDto apiResponse )
    {
        if ( apiResponse != null && apiResponse.getStatus( ).getType( ) != ResponseStatusType.OK
                && apiResponse.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
        {
            if ( apiResponse.getStatus( ).getType( ) == ResponseStatusType.INCOMPLETE_SUCCESS )
            {
                addWarning( apiResponse.getStatus( ).getMessageKey( ), getLocale());
                AppLogService.info( apiResponse.getStatus( ).getMessage( ) );
            }
            else
            {
                addError( apiResponse.getStatus( ).getMessageKey( ), getLocale( ) );
                AppLogService.error( apiResponse.getStatus( ).getMessage( ) );
            }
        }
    }

    private boolean getLockedStatus(LocalIdentityDto localIdentityDto)
    {
        try
        {
            return _serviceQuality.checkLock( localIdentityDto.getCustomerId( ), _currentClientCode,
                    this.buildAgentAuthor( ) ).isLocked();
        } catch (IdentityStoreException e)
        {
            AppLogService.error( "Error while retrieving suspicious identity lock status.", e );
            this.addError( MESSAGE_FETCH_IDENTITY_LOCK_STATUS_ERROR, getLocale( ) );
        }
        return false;
    }
}
