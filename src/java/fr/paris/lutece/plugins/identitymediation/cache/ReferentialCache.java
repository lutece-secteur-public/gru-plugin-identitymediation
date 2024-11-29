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

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeKeyDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationProcessusDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.LevelDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ReferentialService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;

public class ReferentialCache extends AbstractCacheableService
{
    private static final String SERVICE_NAME = "MediationReferentialCache";
    private static final String PROCESSUS_KEY = "processus_referential";
    private static final String ATTRIBUTE_KEY_KEY = "attribute_key_referential";
    private static final String LEVEL_KEY = "level_referential";
    private final ReferentialService _referentialService;
    private final String _currentClientCode = AppPropertiesService.getProperty( "identitymediation.default.client.code" );

    public ReferentialCache( ReferentialService srService )
    {
        _referentialService = srService;
        this.initCache( );
    }

    public void load( ) {
        try
        {
            this.getLevelList();
            this.getProcessList();
            this.getAttributeKeyList();
        }
        catch ( final Exception e )
        {
            AppLogService.error( "Cannot load Referential cache due to unexpected exception.", e );
        }
    }

    private void putProcessList( final List<AttributeCertificationProcessusDto> referential )
    {
        if ( this.getKeys( ).contains( PROCESSUS_KEY) )
        {
            this.removeKey( PROCESSUS_KEY);
        }
        this.putInCache( PROCESSUS_KEY, referential );
        AppLogService.debug( "Referential added to certification processus cache: " + PROCESSUS_KEY );
    }

    public List<AttributeCertificationProcessusDto> getProcessList(  ) throws IdentityStoreException
    {
        final Object cachedValue = this.getFromCache(PROCESSUS_KEY);
        if( cachedValue instanceof List<?> )
        {
            return ( List<AttributeCertificationProcessusDto> ) cachedValue;
        }
        else
        {
            final List<AttributeCertificationProcessusDto> processusDtos = _referentialService
                    .getProcessList( _currentClientCode, new RequestAuthor( "Mediation_ReferentialCache", AuthorType.application.name( ) ) )
                    .getProcessus( );
            this.putProcessList( processusDtos );
            return processusDtos;
        }
    }

    private void putAttributeKeyList( final List<AttributeKeyDto> referential )
    {
        if ( this.getKeys( ).contains( ATTRIBUTE_KEY_KEY) )
        {
            this.removeKey( ATTRIBUTE_KEY_KEY);
        }
        this.putInCache( ATTRIBUTE_KEY_KEY, referential );
        AppLogService.debug( "Referential added to attribute key cache: " + ATTRIBUTE_KEY_KEY);
    }

    public List<AttributeKeyDto> getAttributeKeyList( ) throws IdentityStoreException
    {
        final Object cachedValue = this.getFromCache(ATTRIBUTE_KEY_KEY);
        if( cachedValue instanceof List<?> )
        {
            return ( List<AttributeKeyDto> ) cachedValue;
        }
        else
        {
            final List<AttributeKeyDto> attributeKeys = _referentialService
                    .getAttributeKeyList( _currentClientCode, new RequestAuthor( "Mediation_ReferentialCache", AuthorType.application.name( ) ) )
                    .getAttributeKeys( );
            this.putAttributeKeyList( attributeKeys );
            return attributeKeys;
        }
    }

    private void putLevelList( final List<LevelDto> referential )
    {
        if ( this.getKeys( ).contains( LEVEL_KEY) )
        {
            this.removeKey( LEVEL_KEY);
        }
        this.putInCache( LEVEL_KEY, referential );
        AppLogService.debug( "Referential added to certification level cache: " + LEVEL_KEY);
    }

    public List<LevelDto> getLevelList( ) throws IdentityStoreException
    {
        final Object cachedValue = this.getFromCache(LEVEL_KEY);
        if( cachedValue instanceof List<?> )
        {
            return ( List<LevelDto> ) cachedValue;
        }
        else
        {
            final List<LevelDto> attributeKeys = _referentialService
                    .getLevelList( _currentClientCode, new RequestAuthor( "Mediation_ReferentialCache", AuthorType.application.name( ) ) )
                    .getLevels( );
            this.putLevelList( attributeKeys );
            return attributeKeys;
        }
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
