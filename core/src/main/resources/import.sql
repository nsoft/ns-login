SET FOREIGN_KEY_CHECKS=0;
DROP TABLE if exists AppUserRef;
CREATE OR REPLACE VIEW AppUserRef AS SELECT id, username, created, modified,  modifiedBy_id, createdBy_id, owner_id, version, 'readonly' as ro FROM AppUser;
SET FOREIGN_KEY_CHECKS=1;