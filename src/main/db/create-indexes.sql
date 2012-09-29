create index resource_type_index on resource(resource_type);
/* FIXME: title is now getting longer, will this index cause issues?*/
/*create index resource_title_index on resource(title);*/
create index coding_rule_term_index on coding_rule(term);

create index resource_status_index on resource(status);
create index full_user_person_index on full_user(person_id);

CREATE INDEX calendar_date_res_id_id
  ON calendar_date
  USING btree
  (resource_id, id);

CREATE INDEX coding_catvar_id
  ON coding_sheet
  USING btree
  (category_variable_id);
  
CREATE INDEX ontology_catvar_id
  ON ontology
  USING btree
  (category_variable_id);

CREATE INDEX resource_id_keyid
  ON resource_annotation
  USING btree
  (resource_id, id, resourceannotationkey_id);

CREATE INDEX rescreator_resid
  ON resource_creator
  USING btree
  (resource_id);

  
CREATE INDEX resId_noteId
  ON resource_note
  USING btree
  (resource_id, id);



CREATE INDEX resId_temporalKwdId
  ON resource_temporal_keyword
  USING btree
  (resource_id, temporal_keyword_id);


CREATE INDEX resId_siteTypeKwdId
  ON resource_site_type_keyword
  USING btree
  (resource_id, site_type_keyword_id);


CREATE INDEX resId_siteNameKwdId
  ON resource_site_name_keyword
  USING btree
  (resource_id, site_name_keyword_id);


CREATE INDEX resId_otherKwdId
  ON resource_other_keyword
  USING btree
  (resource_id, other_keyword_id);


CREATE INDEX resId_matKwdId
  ON resource_material_keyword
  USING btree
  (resource_id, material_keyword_id);


CREATE INDEX resId_invTypeId
  ON resource_investigation_type
  USING btree
  (resource_id, investigation_type_id);


CREATE INDEX resId_geogKwdId
  ON resource_geographic_keyword
  USING btree
  (resource_id, geographic_keyword_id);


CREATE INDEX resId_cultKwdId
  ON resource_culture_keyword
  USING btree
  (resource_id, culture_keyword_id);


CREATE INDEX infoRes_provId
  ON information_resource
  USING btree
  (provider_institution_id);

  CREATE INDEX infoRes_ProjId
  ON information_resource
  USING btree
  (project_id, id);

CREATE INDEX person_InstId
  ON person
  USING btree
  (id, institution_id);

  CREATE INDEX res_submitterId
  ON resource
  USING btree
  (submitter_id);

  CREATE INDEX res_updaterId
  ON resource
  USING btree
  (updater_id);

  
CREATE INDEX cltKwd_appr
  ON culture_keyword
  USING btree
  (approved, id);

CREATE INDEX siteType_appr
  ON site_type_keyword
  USING btree
  (approved, id);

