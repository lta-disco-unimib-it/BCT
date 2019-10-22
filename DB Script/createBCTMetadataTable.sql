CREATE TABLE methodcallmetadata (
  idMetadata INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  beginendexecmethod_idBeginEndExecMethod INTEGER UNSIGNED NOT NULL,
  data TEXT NULL,
  PRIMARY KEY(idMetadata),
  INDEX datum_FKIndex1(beginendexecmethod_idBeginEndExecMethod)
);