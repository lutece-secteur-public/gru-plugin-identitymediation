package fr.paris.lutece.plugins.identitymediation.web;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.identitymediation.rbac.AccessDuplicateResource;
import fr.paris.lutece.plugins.identityquality.v3.web.service.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.SuspiciousIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummaryDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class provides the user interface to manage identity lock (search, resolve)
 */
@Controller( controllerJsp = "IdentityLocks.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_LOCKS_MANAGEMENT" )
public class IdentityLocksJspBean extends MVCAdminJspBean
{
    // Messages


    // Beans
    protected static final IdentityQualityService _serviceQuality = SpringContextService.getBean( "identitymediation.identityQualityService.rest.httpAccess" );

    // Properties
    protected static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    protected static final String MEDIATION_CLIENT_CODE = AppPropertiesService.getProperty( "identitymediation.default.client.code" );
    protected String _currentClientCode = MEDIATION_CLIENT_CODE;

    // Parameters
    protected static final String PARAM_SUSPICIOUS_IDENTITIES_LIST = "suspicious_identities_list";

    //Views
    protected static final String VIEW_SEARCH_LOCKS = "searchLocks";

    //Actions

    // Templates
    protected static final String TEMPLATE_SEARCH_LOCKS = "/admin/plugins/identitymediation/search_locks.html";

    // Session variable to store working values
    protected RequestAuthor _agentAuthor;


    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_LOCKS, defaultView = true )
    public String getSearchLocks( final HttpServletRequest request ) throws AccessDeniedException
    {
        if( !RBACService.isAuthorized( new AccessDuplicateResource( ), AccessDuplicateResource.PERMISSION_READ, (User) this.getUser( ) ) )
        {
            throw new AccessDeniedException( "You don't have the right to read duplicates" );
        }

        try
        {
            List<SuspiciousIdentityDto> suspiciousIdentityDtoList = _serviceQuality.getAllLocks(_currentClientCode, this.buildAgentAuthor()).getSuspiciousIdentityDtoList();
            suspiciousIdentityDtoList.sort(new Comparator<SuspiciousIdentityDto>()
            {
                public int compare(SuspiciousIdentityDto o1, SuspiciousIdentityDto o2)
                {
                    return o1.getLock().getLockEndDate().compareTo(o2.getLock().getLockEndDate());
                }
            });
            Collections.reverse(suspiciousIdentityDtoList);
        final Map<String, Object> model = getModel( );

        model.put( PARAM_SUSPICIOUS_IDENTITIES_LIST, suspiciousIdentityDtoList);

        return this.getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_LOCKS, model );
        } catch (IdentityStoreException e)
        {
            throw new RuntimeException(e);
        }
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

}
