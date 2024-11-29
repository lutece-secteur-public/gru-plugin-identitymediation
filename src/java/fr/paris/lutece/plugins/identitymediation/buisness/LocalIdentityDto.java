package fr.paris.lutece.plugins.identitymediation.buisness;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;

public class LocalIdentityDto extends IdentityDto {
    private boolean canNotify;

    public boolean isCanNotify( )
    {
        return canNotify;
    }

    public void setCanNotify( boolean canNotify )
    {
        this.canNotify = canNotify;
    }

    public static LocalIdentityDto toLocalIdentityDto( IdentityDto identity )
    {
        final LocalIdentityDto localIdentityDto = new LocalIdentityDto( );
        localIdentityDto.setAttributes( identity.getAttributes() );
        localIdentityDto.setConnectionId( identity.getConnectionId( ) );
        localIdentityDto.setConsolidate( identity.getConsolidate() );
        localIdentityDto.setCustomerId( identity.getCustomerId( ) );
        localIdentityDto.setCreationDate( identity.getCreationDate( ) );
        localIdentityDto.setDuplicateDefinition( identity.getDuplicateDefinition( ) );
        localIdentityDto.setExpiration( identity.getExpiration( ) );
        localIdentityDto.setExternalCustomerId( identity.getExternalCustomerId( ) );
        localIdentityDto.setMatchedDuplicateRuleCode( identity.getMatchedDuplicateRuleCode( ) );
        localIdentityDto.setLastUpdateDate( identity.getLastUpdateDate( ) );
        localIdentityDto.setMerge( identity.getMerge( ) );
        localIdentityDto.setMonParisActive( identity.getMonParisActive( ) );
        localIdentityDto.setQuality( identity.getQuality( ) );
        localIdentityDto.setSuspicious( identity.isSuspicious() );
        return localIdentityDto;
    }
}
