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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class provides the user interface to manage identity duplicates (search, resolve)
 */
@Controller( controllerJsp = "IdentityDuplicate.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_MANAGEMENT" )
public class IdentityDuplicateJspBean extends MVCAdminJspBean
{
    // Messages
    private static final String MESSAGE_FETCH_POTENTIAL_DUPLICATES_ERROR = "identitymediation.message.fetch_duplicates.error";
    private static final String MESSAGE_FETCH_POTENTIAL_DUPLICATES_NORESULT = "identitymediation.message.fetch_duplicates.noresult";

    // Views
    private static final String VIEW_SEARCH_DUPLICATES = "searchDuplicates";
    private static final String VIEW_RESOLVE_DUPLICATES = "resolveDuplicates";

    // Templates
    private static final String TEMPLATE_SEARCH_DUPLICATES = "/admin/plugins/identitymediation/search_duplicates.html";
    private static final String TEMPLATE_RESOLVE_DUPLICATES = "/admin/plugins/identitymediation/resolve_duplicates.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES = "identitymediation.resolve_duplicates.pageTitle";

    // Markers
    private static final String MARK_POTENTIAL_DUPLICATE_LIST = "potential_duplicate_list";

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
        final List<QualifiedIdentity> identities = new ArrayList<>( );
        try
        {
            identities.addAll( fetchPotentialDuplicates( ) );
            if ( CollectionUtils.isEmpty( identities ) )
            {
                addInfo( MESSAGE_FETCH_POTENTIAL_DUPLICATES_NORESULT, getLocale( ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while fetching potential identity duplicates.", e );
            addError( MESSAGE_FETCH_POTENTIAL_DUPLICATES_ERROR, getLocale( ) );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_POTENTIAL_DUPLICATE_LIST, identities );

        return getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_DUPLICATES, model );
    }

    /**
     * Fetches identities that are likely to have duplicates.
     */
    private List<QualifiedIdentity> fetchPotentialDuplicates( ) throws IdentityStoreException
    {
        // FIXME mock for the time being.
        try
        {
            final ObjectMapper mapper = new ObjectMapper( );
            final QualifiedIdentity id = mapper.readValue(
                    "{\"scoring\":1,\"quality\":80,\"coverage\":80,\"connection_id\":\"mock-connection-id\",\"customer_id\":\"mock-cuid\",\"attributes\":[{\"key\":\"birthdate\",\"value\":\"01/01/1990\",\"type\":\"string\",\"certificationLevel\":400,\"certifier\":\"pj\",\"certificationDate\":\"2023-05-02\"},{\"key\":\"family_name\",\"value\":\"Dupont\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"fc\",\"certificationDate\":\"2023-05-02\"},{\"key\":\"first_name\",\"value\":\"Jean\",\"type\":\"string\",\"certificationLevel\":700,\"certifier\":\"fc\",\"certificationDate\":\"2023-05-02\"}]}",
                    QualifiedIdentity.class );
            return Collections.singletonList( id );
        }
        catch( Exception e )
        {
            throw new IdentityStoreException( "error", e );
        }
    }

    /**
     * Returns the form to manually resolve an identity duplicate
     *
     * @param request
     *            The Http request
     * @return the html code of the form
     */
    @View( value = VIEW_RESOLVE_DUPLICATES )
    public String getResolveDuplicates( final HttpServletRequest request )
    {
        return getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES );
    }

}
