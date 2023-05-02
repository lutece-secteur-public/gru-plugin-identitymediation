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

import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage identity duplicates (search, resolve)
 */
@Controller( controllerJsp = "IdentityDuplicate.jsp", controllerPath = "jsp/admin/plugins/identitymediation/", right = "IDENTITYMEDIATION_MANAGEMENT" )
public class IdentityDuplicateJspBean extends MVCAdminJspBean
{
    // Views
    private static final String VIEW_SEARCH_DUPLICATES = "searchDuplicates";
    private static final String VIEW_RESOLVE_DUPLICATES = "resolveDuplicates";

    // Templates
    private static final String TEMPLATE_SEARCH_DUPLICATES = "/admin/plugins/identitymediation/search_duplicates.html";
    private static final String TEMPLATE_RESOLVE_DUPLICATES = "/admin/plugins/identitymediation/resolve_duplicates.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES = "identitymediation.resolve_duplicates.pageTitle";

    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_DUPLICATES, defaultView = true )
    public String getSearchDuplicates( HttpServletRequest request )
    {
        return getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_DUPLICATES );
    }

    /**
     * Returns the form to manually resolve an identity duplicate
     *
     * @param request
     *            The Http request
     * @return the html code of the form
     */
    @View( value = VIEW_RESOLVE_DUPLICATES )
    public String getResolveDuplicates( HttpServletRequest request )
    {
        return getPage( PROPERTY_PAGE_TITLE_RESOLVE_DUPLICATES, TEMPLATE_RESOLVE_DUPLICATES );
    }

}
