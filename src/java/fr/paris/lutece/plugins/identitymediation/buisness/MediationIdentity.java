package fr.paris.lutece.plugins.identitymediation.buisness;

import java.util.List;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;

public class MediationIdentity
{
    private IdentityDto suspiciousIdentity;
    private IdentityDto bestIdentity;
    private Map<IdentityDto, List<String>> duplicatesToMergeAttributes;

    public IdentityDto getSuspiciousIdentity( )
    {
        return suspiciousIdentity;
    }

    public void setSuspiciousIdentity( IdentityDto suspiciousIdentity )
    {
        this.suspiciousIdentity = suspiciousIdentity;
    }

    public IdentityDto getBestIdentity( )
    {
        return bestIdentity;
    }

    public void setBestIdentity( IdentityDto bestIdentity )
    {
        this.bestIdentity = bestIdentity;
    }

    public Map<IdentityDto, List<String>> getDuplicatesToMergeAttributes( )
    {
        return duplicatesToMergeAttributes;
    }

    public void setDuplicatesToMergeAttributes( Map<IdentityDto, List<String>> duplicatesToMergeAttributes )
    {
        this.duplicatesToMergeAttributes = duplicatesToMergeAttributes;
    }
}
