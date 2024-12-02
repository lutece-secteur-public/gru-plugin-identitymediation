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
package fr.paris.lutece.plugins.identitymediation.cache;

import fr.paris.lutece.plugins.identitymediation.service.MediationService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ServiceContractService;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceContractCache extends AbstractCacheableService
{
    protected static final String SERVICE_NAME = "Mediation_ServiceContractCache";
    protected static final String MEDIATION_CLIENT_CODE = AppPropertiesService.getProperty( "identitymediation.default.client.code" );

    protected final ServiceContractService _serviceContractService;
    protected final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitymediation.attribute.order" ).split( "," ) );
    protected final List<String> _attributeKeyToShowList = Arrays.asList( AppPropertiesService.getProperty( "identitymediation.attribute.show" ).split( "," ) );

    public ServiceContractCache( ServiceContractService serviceContractService )
    {
        this.initCache( );
        _serviceContractService = serviceContractService;
    }

    private void put( final String clientCode, final ServiceContractDto serviceContract )
    {
        if ( this.getKeys( ).contains( clientCode ) )
        {
            this.removeKey( clientCode );
        }
        this.putInCache( clientCode, serviceContract );
        AppLogService.debug( "ServiceContractDto added to cache: " + clientCode );
    }

    public ServiceContractDto get( final String clientCode )
    {
        ServiceContractDto serviceContract = ( ServiceContractDto ) this.getFromCache( clientCode );
        if ( serviceContract == null )
        {
            serviceContract = this.getFromAPI( clientCode );
            this.put( clientCode, serviceContract );
        }
        return serviceContract;
    }

    private ServiceContractDto getFromAPI( final String clientCode )
    {
        try
        {
            final ServiceContractSearchResponse response = _serviceContractService.getActiveServiceContract( clientCode, MEDIATION_CLIENT_CODE, new RequestAuthor( SERVICE_NAME, AuthorType.application.name( ) ) );
            if ( MediationService.instance().isSuccess( response ) && response.getServiceContract( ) != null )
            {
                final ServiceContractDto serviceContract = response.getServiceContract( );
                this.sortServiceContractAttributes( serviceContract );
                this.filterServiceContractAttributes( serviceContract );
                return serviceContract;
            }
            else
            {
                if ( response != null && response.getStatus( ).getType( ) != ResponseStatusType.OK
                        && response.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
                {
                    if ( response.getStatus( ).getType( ) == ResponseStatusType.INCOMPLETE_SUCCESS )
                    {
                        AppLogService.info( response.getStatus( ).getMessage( ) );
                    }
                    else
                    {
                        AppLogService.error( response.getStatus( ).getMessage( ) );
                    }
                }
            }
        }
        catch( final Exception e )
        {
            AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
        }
        return null;
    }

    /**
     * Sorts the attributes of the given ServiceContractDto based on their key names and their order of appearance in the _sortedAttributeKeyList.
     *
     * @param contract
     *            The ServiceContractDto whose attributes need to be sorted.
     */
    protected void sortServiceContractAttributes( final ServiceContractDto contract )
    {
        if ( contract != null )
        {
            contract.getAttributeDefinitions( ).sort( ( a1, a2 ) -> {
                final int index1 = _sortedAttributeKeyList.indexOf( a1.getKeyName( ) );
                final int index2 = _sortedAttributeKeyList.indexOf( a2.getKeyName( ) );
                final int i1 = index1 == -1 ? 999 : index1;
                final int i2 = index2 == -1 ? 999 : index2;
                if ( i1 == i2 )
                {
                    return a1.getKeyName( ).compareTo( a2.getKeyName( ) );
                }
                return Integer.compare( i1, i2 );
            } );
        }
    }

    /**
     * Filters the attributes of the given ServiceContractDto based on the _attributeKeyToShowList.
     *
     * @param contract
     *            The ServiceContractDto whose attributes need to be filtered.
     */
    protected void filterServiceContractAttributes( final ServiceContractDto contract )
    {
        if ( contract != null && !_attributeKeyToShowList.isEmpty( ) )
        {
            contract.setAttributeDefinitions( contract.getAttributeDefinitions( ).stream( ).filter( a -> _attributeKeyToShowList.contains( a.getKeyName( ) ) )
                    .collect( Collectors.toList( ) ) );
        }
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
