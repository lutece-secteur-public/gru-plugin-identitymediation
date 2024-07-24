package fr.paris.lutece.plugins.identitymediation.rbac;

import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

public class AccessDuplicateResourceIdService extends ResourceIdService {

    private static final String PLUGIN_NAME = "identitymediation";
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "identitymediation.rbac.access.duplicate.label";
    private static final String PROPERTY_LABEL_READ = "identitymediation.rbac.access.duplicate.permission.read";
    private static final String PROPERTY_LABEL_WRITE = "identitymediation.rbac.access.duplicate.permission.write";

    @Override
    public void register() {
        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( AccessDuplicateResourceIdService.class.getName( ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( AccessDuplicateResource.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission permRead = new Permission( );
        permRead.setPermissionKey( AccessDuplicateResource.PERMISSION_READ );
        permRead.setPermissionTitleKey( PROPERTY_LABEL_READ );
        rt.registerPermission( permRead );

        Permission permWrite = new Permission( );
        permWrite.setPermissionKey( AccessDuplicateResource.PERMISSION_WRITE );
        permWrite.setPermissionTitleKey( PROPERTY_LABEL_WRITE );
        rt.registerPermission( permWrite );

        ResourceTypeManager.registerResourceType(rt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList getResourceIdList( final Locale locale )
    {
        // No resources to control : return an empty list
        return new ReferenceList( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( final String s, final Locale locale )
    {
        // No resources to control : return an empty String
        return "";
    }
}
