package fr.paris.lutece.plugins.identitymediation.service;

import fr.paris.lutece.plugins.identitymediation.cache.ReferentialCache;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeKeyDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationLevelDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationProcessusDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediationService {
    private final ReferentialCache _referentialCache = SpringContextService.getBean( "identitymediation.ReferentialCache" );
    private final String _minCertificationCode = AppPropertiesService.getProperty( "identitymediation.merge.minimum.certification" );
    private static MediationService instance = null;

    private MediationService( )
    {
        _referentialCache.load();
    }

    public static MediationService instance( ) {
        if( instance == null )
        {
            instance = new MediationService();
        }
        return instance;
    }

    public boolean canSendEmail( final IdentityDto identity )
    {
        return identity.getAttributes( ).stream( ).anyMatch( attributeDto -> Objects.equals(attributeDto.getKey( ), Constants.PARAM_EMAIL ) );
    }

    public boolean validateIdentityCertification( final IdentityDto identityDto )
    {
        try
        {
            final List<String> pivotKeys = _referentialCache.getAttributeKeyList().stream( )
                    .filter( AttributeKeyDto::isPivot )
                    .map( AttributeKeyDto::getKeyName )
                    .collect( Collectors.toList( ) );
            final List<AttributeDto> pivotAttributes = identityDto.getAttributes( ).stream( ).filter(a -> pivotKeys.contains( a.getKey( ) ) )
                    .collect( Collectors.toList( ) );

            // Born in France
            if ( pivotAttributes.size( ) == pivotKeys.size( ) )
            {
                return this.validateAttributesCertification( pivotAttributes );
            }

            // Not born in france
            else
            if ( pivotAttributes.size( ) == pivotKeys.size( ) - 1
                    && pivotAttributes.stream( ).map( AttributeDto::getKey ).noneMatch( s -> s.equals( Constants.PARAM_BIRTH_PLACE_CODE ) )
                    && pivotAttributes.stream( ).anyMatch( attributeDto -> attributeDto.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE )
                    && !attributeDto.getValue( ).equals( "99100" ) ) )
            {
                return this.validateAttributesCertification( pivotAttributes );
            }

            // invalid
            else
            {
                return false;
            }
        }
        catch ( final Exception e )
        {
            return false;
        }
    }

    private boolean validateAttributesCertification( final List<AttributeDto> pivotAttributes )
    {
        try
        {
            for ( final AttributeDto attributeDto : pivotAttributes )
            {
                final AttributeCertificationProcessusDto processusDto = _referentialCache.getProcessList( ).stream( )
                        .filter( p -> Objects.equals(p.getCode(), _minCertificationCode ) )
                        .findFirst()
                        .orElse(null);

                if( processusDto != null )
                {
                    final AttributeCertificationLevelDto certificationLevelDto = processusDto.getAttributeCertificationLevels().stream().filter(a -> Objects.equals(attributeDto.getKey(), a.getAttributeKey())).findFirst().orElse(null);
                    if( certificationLevelDto != null )
                    {
                        final Integer minRequiredLevel = Integer.valueOf( certificationLevelDto.getLevel().getLevel() );
                        if( attributeDto.getCertificationLevel( ) < minRequiredLevel )
                        {
                            return false;
                        }
                    }
                    else //invalid
                    {
                        return false;
                    }
                }
                else //invalid
                {
                    return false;
                }
            }
            return true;
        }
        catch ( final Exception e )
        {
            return false;
        }
    }
}
