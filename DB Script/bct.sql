CREATE TABLE method (
  idMethod INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  methodDeclaration TEXT NULL,
  PRIMARY KEY(idMethod)
);

CREATE TABLE session (
  idSession INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  dataSession TEXT NULL,
  PRIMARY KEY(idSession)
);

CREATE TABLE efsa (
  idEFSA INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  efsa BLOB NULL,
  PRIMARY KEY(idEFSA),
  INDEX efsa_FKIndex1(method_idMethod)
);

CREATE TABLE fsa (
  idFSA INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  fsa BLOB NULL,
  PRIMARY KEY(idFSA),
  INDEX fsa_FKIndex1(method_idMethod)
);

CREATE TABLE thread (
  idThread INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  session_idSession INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(idThread),
  INDEX thread_FKIndex1(session_idSession)
);

CREATE TABLE beginenddeclaration (
  idBeginEndDeclaration INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  beginDeclaration TEXT NULL,
  endDeclaration TEXT NULL,
  PRIMARY KEY(idBeginEndDeclaration),
  INDEX beginenddeclaration_FKIndex1(method_idMethod)
);

CREATE TABLE datamodel (
  idDataModel INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  modelIN TEXT NULL,
  modelOUT TEXT NULL,
  PRIMARY KEY(idDataModel),
  INDEX datamodel_FKIndex1(method_idMethod)
);

CREATE TABLE gktaildatamodel (
  idGKTailDataModel INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  gktailmethodcall_idGKTailMethodCall INTEGER UNSIGNED NOT NULL,
  modelIN TEXT NULL,
  modelOUT TEXT NULL,
  PRIMARY KEY(idGKTailDataModel),
  INDEX gktaildatamodel_FKIndex1(gktailmethodcall_idGKTailMethodCall)
);

CREATE TABLE beginendexecmethod (
  idBeginEndExecMethod INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  thread_idThread INTEGER UNSIGNED NOT NULL,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  occurrence INTEGER UNSIGNED NOT NULL,
  beginEnd VARCHAR(2) NOT NULL,
  startMethod INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(idBeginEndExecMethod),
  INDEX beginendexecmethod_FKIndex1(method_idMethod),
  INDEX beginendexecmethod_FKIndex2(thread_idThread)
);

CREATE TABLE interactiontrace (
  idInteractionTrace INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  thread_idThread INTEGER UNSIGNED NOT NULL,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(idInteractionTrace),
  INDEX interactiontrace_FKIndex1(method_idMethod),
  INDEX interactiontrace_FKIndex2(thread_idThread)
);

CREATE TABLE gktailinteractiontrace (
  idGKTailInteractionTrace INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  thread_idThread INTEGER UNSIGNED NOT NULL,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(idGKTailInteractionTrace),
  INDEX gktailinteractiontrace_FKIndex1(method_idMethod),
  INDEX gktailinteractiontrace_FKIndex2(thread_idThread)
);

CREATE TABLE methodcall (
  idMethodCall INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  interactiontrace_idInteractionTrace INTEGER UNSIGNED NOT NULL,
  beginendexecmethod_idBeginEndExecMethod INTEGER UNSIGNED NOT NULL,
  occurrence INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(idMethodCall),
  INDEX methodcall_FKIndex1(interactiontrace_idInteractionTrace),
  INDEX methodcall_FKIndex2(method_idMethod),
  INDEX methodcall_FKIndex3(beginendexecmethod_idBeginEndExecMethod)
);

CREATE TABLE gktailmethodcall (
  idGKTailMethodCall INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  method_idMethod INTEGER UNSIGNED NOT NULL,
  gktailinteractiontrace_idGKTailInteractionTrace INTEGER UNSIGNED NOT NULL,
  occurrence INTEGER UNSIGNED NOT NULL,
  marker TEXT NULL,
  PRIMARY KEY(idGKTailMethodCall),
  INDEX gktailmethodcall_FKIndex1(method_idMethod),
  INDEX gktailmethodcall_FKIndex2(gktailinteractiontrace_idGKTailInteractionTrace)
);

CREATE TABLE datum (
  idDatum INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  beginendexecmethod_idBeginEndExecMethod INTEGER UNSIGNED NOT NULL,
  dataDefinition TEXT NULL,
  PRIMARY KEY(idDatum),
  INDEX datum_FKIndex1(beginendexecmethod_idBeginEndExecMethod)
);

CREATE TABLE normalizeddata (
  idNormalizedData INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  beginendexecmethod_idBeginEndExecMethod INTEGER UNSIGNED NOT NULL,
  methodCall_idMethodCall INTEGER UNSIGNED,
  normalizedDataDefinition TEXT NULL,
  PRIMARY KEY(idNormalizedData),
  INDEX normalizeddata_FKIndex1(methodcall_idMethodCall),
  INDEX normalizeddata_FKIndex2(beginendexecmethod_idBeginEndExecMethod)
);

CREATE TABLE gktailnormalizeddata (
  idGKTailNormalizedData INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  gktailmethodCall_idGKTailMethodCall INTEGER UNSIGNED,
  normalizedDataDefinition TEXT NULL,
  PRIMARY KEY(idGKTailNormalizedData),
  INDEX normalizeddata_FKIndex1(gktailmethodcall_idGKTailMethodCall)
);


