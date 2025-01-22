package fr.paris.lutece.plugins.identitymediation.rbac;

import fr.paris.lutece.portal.service.rbac.RBACResource;

public class AccessDuplicateResource implements RBACResource {

    // RBAC management
    public static final String RESOURCE_TYPE = "ACCESS_DUPLICATE";

    // Perimissions
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_NOTIFICATION = "NOTIFICATION";
    public static final String PERMISSION_EXCLUDE = "EXCLUDE";

    @Override
    public String getResourceTypeCode() {
        return RESOURCE_TYPE;
    }

    @Override
    public String getResourceId() {
        return null;
    }
}
