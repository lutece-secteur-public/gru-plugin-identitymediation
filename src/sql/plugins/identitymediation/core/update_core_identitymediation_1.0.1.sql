
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYMEDIATION_LOCKS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('IDENTITYMEDIATION_LOCKS_MANAGEMENT','identitymediation.adminFeature.IdentityLocks.name',1,'jsp/admin/plugins/identitymediation/IdentityLocks.jsp','identitymediation.adminFeature.IdentityLocks.description',0,'identitymediation',NULL,NULL,NULL,9);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYMEDIATION_LOCKS_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYMEDIATION_LOCKS_MANAGEMENT',1);