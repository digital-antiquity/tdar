--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: authorized_user; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE authorized_user (
    id bigint NOT NULL,
    admin_permission character varying(255),
    user_id bigint,
    resource_collection_id bigint,
    general_permission character varying(50),
    general_permission_int integer
);


ALTER TABLE public.authorized_user OWNER TO tdar;

--
-- Name: authorized_user_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE authorized_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorized_user_id_seq OWNER TO tdar;

--
-- Name: authorized_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE authorized_user_id_seq OWNED BY authorized_user.id;


--
-- Name: bookmarked_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE bookmarked_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bookmarked_resource_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: bookmarked_resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE bookmarked_resource (
    id bigint DEFAULT nextval('bookmarked_resource_id_seq'::regclass) NOT NULL,
    name character varying(255),
    "timestamp" timestamp without time zone,
    person_id bigint,
    resource_id bigint
);


ALTER TABLE public.bookmarked_resource OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: category_variable; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE category_variable (
    id bigint NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    parent_id bigint,
    label character varying(255)
);


ALTER TABLE public.category_variable OWNER TO tdar;

--
-- Name: category_variable_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE category_variable_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_variable_id_seq OWNER TO tdar;

--
-- Name: category_variable_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE category_variable_id_seq OWNED BY category_variable.id;


SET default_with_oids = true;

--
-- Name: category_variable_synonyms; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE category_variable_synonyms (
    categoryvariable_id integer NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.category_variable_synonyms OWNER TO tdar;

--
-- Name: coding_rule_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE coding_rule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.coding_rule_id_seq OWNER TO tdar;

--
-- Name: coding_rule; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE coding_rule (
    id bigint DEFAULT nextval('coding_rule_id_seq'::regclass) NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(2000),
    term character varying(255) NOT NULL,
    coding_sheet_id bigint,
    ontology_node_id bigint
);


ALTER TABLE public.coding_rule OWNER TO tdar;

--
-- Name: coding_sheet; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE coding_sheet (
    id bigint NOT NULL,
    category_variable_id integer,
    default_ontology_id bigint,
    generated boolean DEFAULT false
);


ALTER TABLE public.coding_sheet OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE collection (
    id bigint NOT NULL,
    description text,
    name character varying(255),
    updater_id bigint,
    parent_id bigint,
    orientation character varying(50) DEFAULT 'LIST',
    collection_type character varying(255),
    visible boolean DEFAULT false NOT NULL,
    date_created timestamp without time zone DEFAULT now(),
    date_updated timestamp without time zone DEFAULT now(),
    sort_order character varying(25)
);


ALTER TABLE public.collection OWNER TO tdar;

--
-- Name: collection_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE collection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.collection_id_seq OWNER TO tdar;

--
-- Name: collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE collection_id_seq OWNED BY collection.id;


--
-- Name: collection_resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE collection_resource (
    collection_id bigint NOT NULL,
    resource_id bigint NOT NULL
);


ALTER TABLE public.collection_resource OWNER TO tdar;

--
-- Name: contributor_request_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE contributor_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contributor_request_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: contributor_request; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE contributor_request (
    id bigint DEFAULT nextval('contributor_request_id_seq'::regclass) NOT NULL,
    approved boolean NOT NULL,
    contributor_reason character varying(512),
    date_approved date,
    "timestamp" timestamp without time zone NOT NULL,
    applicant_id bigint,
    approver_id bigint,
    comments character varying
);


ALTER TABLE public.contributor_request OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: coverage_date; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE coverage_date (
    id bigint NOT NULL,
    date_type character varying(255),
    end_date integer,
    start_date integer,
    resource_id bigint,
    start_aprox boolean DEFAULT false NOT NULL,
    end_aprox boolean DEFAULT false NOT NULL,
    description character varying(255)
);


ALTER TABLE public.coverage_date OWNER TO tdar;

--
-- Name: coverage_date_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE coverage_date_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.coverage_date_id_seq OWNER TO tdar;

--
-- Name: coverage_date_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE coverage_date_id_seq OWNED BY coverage_date.id;


--
-- Name: creator_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE creator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.creator_id_seq OWNER TO tdar;

--
-- Name: creator; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE creator (
    id bigint DEFAULT nextval('creator_id_seq'::regclass) NOT NULL,
    date_created date,
    last_updated timestamp without time zone,
    location character varying(255),
    url character varying(64),
    description text
);


ALTER TABLE public.creator OWNER TO tdar;

--
-- Name: creator_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE creator_synonym (
    creator_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.creator_synonym OWNER TO tdar;

--
-- Name: culture_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE culture_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL,
    approved boolean NOT NULL,
    index character varying(255),
    selectable boolean NOT NULL,
    parent_id bigint
);


ALTER TABLE public.culture_keyword OWNER TO tdar;

--
-- Name: culture_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE culture_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.culture_keyword_id_seq OWNER TO tdar;

--
-- Name: culture_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE culture_keyword_id_seq OWNED BY culture_keyword.id;


--
-- Name: culture_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE culture_keyword_synonym (
    culturekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.culture_keyword_synonym OWNER TO tdar;

--
-- Name: data_table_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE data_table_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_table_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: data_table; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_table (
    id bigint DEFAULT nextval('data_table_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL,
    dataset_id bigint,
    description text,
    display_name character varying(255)
);


ALTER TABLE public.data_table OWNER TO tdar;

--
-- Name: data_table_column_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE data_table_column_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_table_column_id_seq OWNER TO tdar;

--
-- Name: data_table_column; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_table_column (
    id bigint DEFAULT nextval('data_table_column_id_seq'::regclass) NOT NULL,
    column_data_type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    category_variable_id integer,
    default_ontology_id bigint,
    data_table_id bigint,
    default_coding_sheet_id bigint,
    description text,
    display_name character varying(255),
    measurement_unit character varying(25),
    column_encoding_type character varying(25),
    sequence_number integer,
    delimitervalue character varying(4),
    ignorefileextension boolean DEFAULT true,
    visible boolean DEFAULT true,
    mappingcolumn boolean DEFAULT false
);


ALTER TABLE public.data_table_column OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: data_table_column_relationship; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_table_column_relationship (
    id bigint NOT NULL,
    relationship_id bigint,
    local_column_id bigint,
    foreign_column_id bigint
);


ALTER TABLE public.data_table_column_relationship OWNER TO tdar;

--
-- Name: data_table_column_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE data_table_column_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_table_column_relationship_id_seq OWNER TO tdar;

--
-- Name: data_table_column_relationship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE data_table_column_relationship_id_seq OWNED BY data_table_column_relationship.id;


--
-- Name: data_table_relationship; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_table_relationship (
    id bigint NOT NULL,
    relationship_type character varying(255),
    dataset_id bigint,
    foreigntable_id bigint,
    localtable_id bigint
);


ALTER TABLE public.data_table_relationship OWNER TO tdar;

--
-- Name: data_table_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE data_table_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_table_relationship_id_seq OWNER TO tdar;

--
-- Name: data_table_relationship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE data_table_relationship_id_seq OWNED BY data_table_relationship.id;


SET default_with_oids = true;

--
-- Name: dataset; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE dataset (
    id bigint NOT NULL
);


ALTER TABLE public.dataset OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: document; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE document (
    document_type character varying(255),
    edition character varying(255),
    isbn character varying(255),
    number_of_pages integer,
    number_of_volumes integer,
    series_name character varying(255),
    series_number character varying(255),
    volume character varying(255),
    journal_name character varying(255),
    journal_number character varying(255),
    issn character varying(255),
    book_title character varying(255),
    id bigint NOT NULL,
    doi character varying(255),
    start_page character varying(10),
    end_page character varying(10),
    degree character varying(50)
);


ALTER TABLE public.document OWNER TO tdar;

--
-- Name: explore_cache_decade; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE explore_cache_decade (
    id bigint NOT NULL,
    key integer,
    item_count bigint
);


ALTER TABLE public.explore_cache_decade OWNER TO tdar;

--
-- Name: explore_cache_decade_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE explore_cache_decade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.explore_cache_decade_id_seq OWNER TO tdar;

--
-- Name: explore_cache_decade_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE explore_cache_decade_id_seq OWNED BY explore_cache_decade.id;


--
-- Name: full_user_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE full_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.full_user_id_seq OWNER TO tdar;

--
-- Name: geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE geographic_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL,
    level character varying(50)
);


ALTER TABLE public.geographic_keyword OWNER TO tdar;

--
-- Name: geographic_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE geographic_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.geographic_keyword_id_seq OWNER TO tdar;

--
-- Name: geographic_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE geographic_keyword_id_seq OWNED BY geographic_keyword.id;


--
-- Name: geographic_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE geographic_keyword_synonym (
    geographickeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.geographic_keyword_synonym OWNER TO tdar;

--
-- Name: homepage_cache_geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE homepage_cache_geographic_keyword (
    id bigint NOT NULL,
    label character varying(255) NOT NULL,
    level character varying(50) NOT NULL,
    resource_count bigint
);


ALTER TABLE public.homepage_cache_geographic_keyword OWNER TO tdar;

--
-- Name: homepage_cache_geographic_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE homepage_cache_geographic_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.homepage_cache_geographic_keyword_id_seq OWNER TO tdar;

--
-- Name: homepage_cache_geographic_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE homepage_cache_geographic_keyword_id_seq OWNED BY homepage_cache_geographic_keyword.id;


--
-- Name: homepage_cache_resource_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE homepage_cache_resource_type (
    id bigint NOT NULL,
    resource_type character varying(100) NOT NULL,
    resource_count bigint
);


ALTER TABLE public.homepage_cache_resource_type OWNER TO tdar;

--
-- Name: homepage_cache_resource_type_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE homepage_cache_resource_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.homepage_cache_resource_type_id_seq OWNER TO tdar;

--
-- Name: homepage_cache_resource_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE homepage_cache_resource_type_id_seq OWNED BY homepage_cache_resource_type.id;


--
-- Name: homepage_featured_item_cache; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE homepage_featured_item_cache (
    id bigint NOT NULL,
    resource_id bigint
);


ALTER TABLE public.homepage_featured_item_cache OWNER TO tdar;

--
-- Name: homepage_featured_item_cache_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE homepage_featured_item_cache_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.homepage_featured_item_cache_id_seq OWNER TO tdar;

--
-- Name: homepage_featured_item_cache_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE homepage_featured_item_cache_id_seq OWNED BY homepage_featured_item_cache.id;


--
-- Name: image; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE image (
    id bigint NOT NULL
);


ALTER TABLE public.image OWNER TO tdar;

--
-- Name: information_resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource (
    id bigint NOT NULL,
    date_created integer,
    project_id bigint,
    provider_institution_id bigint,
    last_uploaded timestamp without time zone,
    external_reference boolean DEFAULT false,
    resource_language character varying(100),
    metadata_language character varying(100),
    copy_location character varying(255),
    inheriting_investigation_information boolean DEFAULT false NOT NULL,
    inheriting_site_information boolean DEFAULT false NOT NULL,
    inheriting_material_information boolean DEFAULT false NOT NULL,
    inheriting_other_information boolean DEFAULT false NOT NULL,
    inheriting_cultural_information boolean DEFAULT false NOT NULL,
    inheriting_spatial_information boolean DEFAULT false NOT NULL,
    inheriting_temporal_information boolean DEFAULT false NOT NULL,
    mappeddatakeycolumn_id bigint,
    mappeddatakeyvalue character varying(255),
    license_type character varying(128),
    license_text text,
    copyright_holder_id bigint,
    date_created_normalized integer,
    inheriting_identifier_information boolean DEFAULT false,
    inheriting_note_information boolean DEFAULT false,
    inheriting_collection_information boolean DEFAULT false,
    publisher_id bigint,
    publisher_location character varying(255)
);


ALTER TABLE public.information_resource OWNER TO tdar;

--
-- Name: information_resource_file_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE information_resource_file_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.information_resource_file_seq OWNER TO tdar;

--
-- Name: information_resource_file; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_file (
    id bigint DEFAULT nextval('information_resource_file_seq'::regclass) NOT NULL,
    general_type character varying(255),
    latest_version integer,
    sequence_number integer,
    information_resource_id bigint,
    status character varying(32),
    number_of_parts bigint,
    restriction character varying(50) DEFAULT 'PUBLIC'::character varying,
    date_made_public timestamp without time zone
);


ALTER TABLE public.information_resource_file OWNER TO tdar;

--
-- Name: information_resource_file_download_statistics; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_file_download_statistics (
    id bigint NOT NULL,
    date_accessed timestamp without time zone,
    information_resource_file_id bigint
);


ALTER TABLE public.information_resource_file_download_statistics OWNER TO tdar;

--
-- Name: information_resource_file_download_statistics_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE information_resource_file_download_statistics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.information_resource_file_download_statistics_id_seq OWNER TO tdar;

--
-- Name: information_resource_file_download_statistics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE information_resource_file_download_statistics_id_seq OWNED BY information_resource_file_download_statistics.id;


--
-- Name: information_resource_file_version_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE information_resource_file_version_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.information_resource_file_version_seq OWNER TO tdar;

--
-- Name: information_resource_file_version; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_file_version (
    id bigint DEFAULT nextval('information_resource_file_version_seq'::regclass) NOT NULL,
    checksum character varying(255),
    checksum_type character varying(255),
    date_created timestamp without time zone NOT NULL,
    extension character varying(255),
    file_type character varying(255),
    filename character varying(255),
    filestore_id character varying(255),
    format character varying(255),
    height integer,
    internal_type character varying(255),
    mime_type character varying(255),
    path character varying(255),
    premisid character varying(255),
    size bigint,
    file_version integer,
    width integer,
    information_resource_file_id bigint,
    total_time bigint
);


ALTER TABLE public.information_resource_file_version OWNER TO tdar;

--
-- Name: information_resource_format_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE information_resource_format_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.information_resource_format_id_seq OWNER TO tdar;

--
-- Name: information_resource_related_citation; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_related_citation (
    information_resource_id bigint NOT NULL,
    document_id bigint NOT NULL
);


ALTER TABLE public.information_resource_related_citation OWNER TO tdar;

--
-- Name: information_resource_source_citation; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_source_citation (
    information_resource_id bigint NOT NULL,
    document_id bigint NOT NULL
);


ALTER TABLE public.information_resource_source_citation OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: institution; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE institution (
    id bigint NOT NULL,
    location character varying(255),
    name character varying(255) NOT NULL,
    url character varying(255),
    parentinstitution_id bigint
);


ALTER TABLE public.institution OWNER TO tdar;

--
-- Name: institution_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE institution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.institution_id_seq OWNER TO tdar;

--
-- Name: institution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE institution_id_seq OWNED BY institution.id;


SET default_with_oids = false;

--
-- Name: institution_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE institution_synonym (
    institution_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.institution_synonym OWNER TO tdar;

--
-- Name: investigation_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE investigation_type (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.investigation_type OWNER TO tdar;

--
-- Name: investigation_type_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE investigation_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.investigation_type_id_seq OWNER TO tdar;

--
-- Name: investigation_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE investigation_type_id_seq OWNED BY investigation_type.id;


--
-- Name: investigation_type_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE investigation_type_synonym (
    investigationtype_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.investigation_type_synonym OWNER TO tdar;

--
-- Name: latitude_longitude_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE latitude_longitude_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.latitude_longitude_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: latitude_longitude; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE latitude_longitude (
    id bigint DEFAULT nextval('latitude_longitude_id_seq'::regclass) NOT NULL,
    maximum_latitude double precision NOT NULL,
    maximum_longitude double precision NOT NULL,
    minimum_latitude double precision NOT NULL,
    minimum_longitude double precision NOT NULL,
    resource_id bigint
);


ALTER TABLE public.latitude_longitude OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: material_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE material_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.material_keyword OWNER TO tdar;

--
-- Name: material_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE material_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.material_keyword_id_seq OWNER TO tdar;

--
-- Name: material_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE material_keyword_id_seq OWNED BY material_keyword.id;


--
-- Name: material_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE material_keyword_synonym (
    materialkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.material_keyword_synonym OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: ontology; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE ontology (
    id bigint NOT NULL,
    category_variable_id integer
);


ALTER TABLE public.ontology OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: ontology_node; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE ontology_node (
    id bigint NOT NULL,
    ontology_id bigint,
    interval_start integer,
    interval_end integer,
    iri character varying,
    uri character varying,
    index character varying,
    description character varying(2048),
    display_name character varying(255),
    import_order bigint
);


ALTER TABLE public.ontology_node OWNER TO tdar;

--
-- Name: ontology_node_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE ontology_node_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ontology_node_id_seq OWNER TO tdar;

--
-- Name: ontology_node_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE ontology_node_id_seq OWNED BY ontology_node.id;


--
-- Name: ontology_node_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE ontology_node_synonym (
    ontologynode_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.ontology_node_synonym OWNER TO tdar;

--
-- Name: other_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE other_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.other_keyword OWNER TO tdar;

--
-- Name: other_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE other_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.other_keyword_id_seq OWNER TO tdar;

--
-- Name: other_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE other_keyword_id_seq OWNED BY other_keyword.id;


--
-- Name: other_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE other_keyword_synonym (
    otherkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.other_keyword_synonym OWNER TO tdar;

--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: person; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE person (
    id bigint DEFAULT nextval('person_id_seq'::regclass) NOT NULL,
    contributor boolean NOT NULL,
    email character varying(255),
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    registered boolean NOT NULL,
    rpa_number character varying(255),
    phone character varying(255),
    contributor_reason character varying(512),
    institution_id bigint,
    total_login bigint DEFAULT 0,
    last_login timestamp without time zone,
    penultimate_login timestamp without time zone,
    phone_public boolean DEFAULT false NOT NULL,
    email_public boolean DEFAULT false NOT NULL,
    username character varying(255)
);


ALTER TABLE public.person OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: personal_filestore_ticket; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE personal_filestore_ticket (
    id bigint NOT NULL,
    date_generated timestamp without time zone NOT NULL,
    personal_file_type character varying(255) NOT NULL,
    submitter_id bigint NOT NULL,
    description character varying(2000)
);


ALTER TABLE public.personal_filestore_ticket OWNER TO tdar;

--
-- Name: personal_filestore_ticket_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE personal_filestore_ticket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_filestore_ticket_id_seq OWNER TO tdar;

--
-- Name: personal_filestore_ticket_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE personal_filestore_ticket_id_seq OWNED BY personal_filestore_ticket.id;


SET default_with_oids = true;

--
-- Name: project; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE project (
    id bigint NOT NULL,
    sort_order character varying(50) DEFAULT 'RESOURCE_TYPE'::character varying
);


ALTER TABLE public.project OWNER TO tdar;

--
-- Name: read_user_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE read_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.read_user_id_seq OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: related_comparative_collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE related_comparative_collection (
    id bigint NOT NULL,
    text character varying(1024),
    resource_id bigint
);


ALTER TABLE public.related_comparative_collection OWNER TO tdar;

--
-- Name: related_comparative_collection_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE related_comparative_collection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.related_comparative_collection_id_seq OWNER TO tdar;

--
-- Name: related_comparative_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE related_comparative_collection_id_seq OWNED BY related_comparative_collection.id;


SET default_with_oids = true;

--
-- Name: resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource (
    id bigint NOT NULL,
    date_registered timestamp without time zone NOT NULL,
    description text,
    resource_type character varying(255),
    title character varying(512) NOT NULL,
    submitter_id bigint NOT NULL,
    url character varying(255),
    updater_id bigint,
    date_updated timestamp without time zone,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    external_id character varying(255),
    uploader_id bigint
);


ALTER TABLE public.resource OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: resource_access_statistics; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_access_statistics (
    id bigint NOT NULL,
    date_accessed timestamp without time zone,
    resource_id bigint
);


ALTER TABLE public.resource_access_statistics OWNER TO tdar;

--
-- Name: resource_access_statistics_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_access_statistics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_access_statistics_id_seq OWNER TO tdar;

--
-- Name: resource_access_statistics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_access_statistics_id_seq OWNED BY resource_access_statistics.id;


--
-- Name: resource_annotation; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_annotation (
    id bigint NOT NULL,
    date_created date,
    last_updated timestamp without time zone,
    value text,
    resource_id bigint NOT NULL,
    resourceannotationkey_id bigint NOT NULL
);


ALTER TABLE public.resource_annotation OWNER TO tdar;

--
-- Name: resource_annotation_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_annotation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_annotation_id_seq OWNER TO tdar;

--
-- Name: resource_annotation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_annotation_id_seq OWNED BY resource_annotation.id;


--
-- Name: resource_annotation_key; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_annotation_key (
    id bigint NOT NULL,
    annotation_data_type character varying(255),
    format_string character varying(128),
    key character varying(128),
    resource_annotation_type character varying(255),
    submitter_id bigint
);


ALTER TABLE public.resource_annotation_key OWNER TO tdar;

--
-- Name: resource_annotation_key_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_annotation_key_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_annotation_key_id_seq OWNER TO tdar;

--
-- Name: resource_annotation_key_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_annotation_key_id_seq OWNED BY resource_annotation_key.id;


--
-- Name: resource_creator; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_creator (
    id bigint NOT NULL,
    role character varying(255),
    sequence_number integer,
    creator_id bigint NOT NULL,
    resource_id bigint NOT NULL
);


ALTER TABLE public.resource_creator OWNER TO tdar;

--
-- Name: resource_creator_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_creator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_creator_id_seq OWNER TO tdar;

--
-- Name: resource_creator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_creator_id_seq OWNED BY resource_creator.id;


--
-- Name: resource_culture_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_culture_keyword (
    resource_id bigint NOT NULL,
    culture_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_culture_keyword OWNER TO tdar;

--
-- Name: resource_geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_geographic_keyword (
    resource_id bigint NOT NULL,
    geographic_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_geographic_keyword OWNER TO tdar;

--
-- Name: resource_investigation_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_investigation_type (
    resource_id bigint NOT NULL,
    investigation_type_id bigint NOT NULL
);


ALTER TABLE public.resource_investigation_type OWNER TO tdar;

--
-- Name: resource_managed_geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_managed_geographic_keyword (
    resource_id bigint NOT NULL,
    geographic_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_managed_geographic_keyword OWNER TO tdar;

--
-- Name: resource_material_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_material_keyword (
    resource_id bigint NOT NULL,
    material_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_material_keyword OWNER TO tdar;

--
-- Name: resource_note; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_note (
    id bigint NOT NULL,
    note character varying(5000),
    note_type character varying(255),
    resource_id bigint NOT NULL,
    sequence_number integer
);


ALTER TABLE public.resource_note OWNER TO tdar;

--
-- Name: resource_note_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_note_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_note_id_seq OWNER TO tdar;

--
-- Name: resource_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_note_id_seq OWNED BY resource_note.id;


--
-- Name: resource_other_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_other_keyword (
    resource_id bigint NOT NULL,
    other_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_other_keyword OWNER TO tdar;

--
-- Name: resource_revision_log_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_revision_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_revision_log_id_seq OWNER TO tdar;

SET default_with_oids = true;

--
-- Name: resource_revision_log; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_revision_log (
    id bigint DEFAULT nextval('resource_revision_log_id_seq'::regclass) NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    resource_id bigint,
    log_message character varying(512),
    person_id bigint,
    payload text
);


ALTER TABLE public.resource_revision_log OWNER TO tdar;

--
-- Name: resource_sequence; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE resource_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_sequence OWNER TO tdar;

SET default_with_oids = false;

--
-- Name: resource_site_name_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_site_name_keyword (
    resource_id bigint NOT NULL,
    site_name_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_site_name_keyword OWNER TO tdar;

--
-- Name: resource_site_type_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_site_type_keyword (
    resource_id bigint NOT NULL,
    site_type_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_site_type_keyword OWNER TO tdar;

--
-- Name: resource_temporal_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_temporal_keyword (
    resource_id bigint NOT NULL,
    temporal_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_temporal_keyword OWNER TO tdar;

--
-- Name: sensory_data; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE sensory_data (
    id bigint NOT NULL,
    additional_project_notes character varying(255),
    company_name character varying(255),
    decimated_mesh_dataset character varying(255),
    decimated_mesh_original_triangle_count bigint,
    decimated_mesh_triangle_count bigint,
    final_dataset_description text,
    final_registration_points bigint,
    turntable_used boolean DEFAULT false NOT NULL,
    mesh_adjustment_matrix character varying(255),
    mesh_dataset_name character varying(255),
    mesh_holes_filled boolean DEFAULT false NOT NULL,
    mesh_processing_notes text,
    mesh_rgb_included boolean DEFAULT false NOT NULL,
    mesh_smoothing boolean DEFAULT false NOT NULL,
    mesh_triangle_count bigint,
    mesh_data_reduction boolean DEFAULT false NOT NULL,
    monument_number character varying(255),
    premesh_dataset_name character varying(255),
    premesh_points bigint,
    premesh_color_editions boolean DEFAULT false NOT NULL,
    premesh_overlap_reduction boolean DEFAULT false NOT NULL,
    premesh_smoothing boolean DEFAULT false NOT NULL,
    premesh_subsampling boolean DEFAULT false NOT NULL,
    registered_dataset_name character varying(255),
    registration_error_units double precision,
    rgb_data_capture_info text,
    rgb_preserved_from_original boolean DEFAULT false NOT NULL,
    scanner_details character varying(255),
    scans_total_acquired integer,
    scans_used integer,
    survey_conditions character varying(255),
    survey_date_begin timestamp without time zone,
    survey_location character varying(255),
    estimated_data_resolution character varying(255),
    total_scans_in_project bigint,
    point_deletion_summary text,
    mesh_color_editions boolean DEFAULT false NOT NULL,
    mesh_healing_despiking boolean DEFAULT false NOT NULL,
    survey_date_end timestamp without time zone,
    planimetric_map_filename character varying(255),
    control_data_filename character varying(255),
    registration_method character varying(255)
);


ALTER TABLE public.sensory_data OWNER TO tdar;

--
-- Name: sensory_data_image; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE sensory_data_image (
    id bigint NOT NULL,
    description character varying(255),
    filename character varying(255) NOT NULL,
    sensory_data_id bigint,
    sequence_number bigint DEFAULT 1 NOT NULL
);


ALTER TABLE public.sensory_data_image OWNER TO tdar;

--
-- Name: sensory_data_image_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE sensory_data_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sensory_data_image_id_seq OWNER TO tdar;

--
-- Name: sensory_data_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE sensory_data_image_id_seq OWNED BY sensory_data_image.id;


--
-- Name: sensory_data_scan; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE sensory_data_scan (
    id bigint NOT NULL,
    filename character varying(255) NOT NULL,
    monument_name character varying(255),
    points_in_scan bigint,
    resolution character varying(255),
    scan_notes text,
    scanner_technology character varying(255),
    transformation_matrix character varying(255),
    triangulation_details character varying(255),
    sensory_data_id bigint,
    sequence_number bigint DEFAULT 1 NOT NULL,
    tof_return character varying(255),
    phase_frequency_settings character varying(255),
    phase_noise_settings character varying(255),
    camera_exposure_settings character varying(255),
    scan_date timestamp without time zone,
    matrix_applied boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sensory_data_scan OWNER TO tdar;

--
-- Name: sensory_data_scan_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE sensory_data_scan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sensory_data_scan_id_seq OWNER TO tdar;

--
-- Name: sensory_data_scan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE sensory_data_scan_id_seq OWNED BY sensory_data_scan.id;


--
-- Name: site_name_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_name_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.site_name_keyword OWNER TO tdar;

--
-- Name: site_name_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE site_name_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.site_name_keyword_id_seq OWNER TO tdar;

--
-- Name: site_name_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE site_name_keyword_id_seq OWNED BY site_name_keyword.id;


--
-- Name: site_name_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_name_keyword_synonym (
    sitenamekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.site_name_keyword_synonym OWNER TO tdar;

--
-- Name: site_type_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_type_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL,
    approved boolean NOT NULL,
    index character varying(255),
    selectable boolean NOT NULL,
    parent_id bigint
);


ALTER TABLE public.site_type_keyword OWNER TO tdar;

--
-- Name: site_type_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE site_type_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.site_type_keyword_id_seq OWNER TO tdar;

--
-- Name: site_type_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE site_type_keyword_id_seq OWNED BY site_type_keyword.id;


--
-- Name: site_type_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_type_keyword_synonym (
    sitetypekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.site_type_keyword_synonym OWNER TO tdar;

--
-- Name: source_collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE source_collection (
    id bigint NOT NULL,
    text character varying(1024),
    resource_id bigint
);


ALTER TABLE public.source_collection OWNER TO tdar;

--
-- Name: source_collection_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE source_collection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.source_collection_id_seq OWNER TO tdar;

--
-- Name: source_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE source_collection_id_seq OWNED BY source_collection.id;


--
-- Name: stats; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE stats (
    id bigint NOT NULL,
    recorded_date date,
    value bigint,
    comment character varying(2048),
    stat_type character varying(255)
);


ALTER TABLE public.stats OWNER TO tdar;

--
-- Name: stats_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE stats_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stats_id_seq OWNER TO tdar;

--
-- Name: stats_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE stats_id_seq OWNED BY stats.id;


--
-- Name: temporal_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE temporal_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.temporal_keyword OWNER TO tdar;

--
-- Name: temporal_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE temporal_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.temporal_keyword_id_seq OWNER TO tdar;

--
-- Name: temporal_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE temporal_keyword_id_seq OWNED BY temporal_keyword.id;


--
-- Name: temporal_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE temporal_keyword_synonym (
    temporalkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.temporal_keyword_synonym OWNER TO tdar;

--
-- Name: upgrade_task; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE upgrade_task (
    id bigint NOT NULL,
    comment character varying(255),
    name character varying(255),
    recorded_date timestamp without time zone,
    run boolean
);


ALTER TABLE public.upgrade_task OWNER TO tdar;

--
-- Name: upgradetask_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE upgradetask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.upgradetask_id_seq OWNER TO tdar;

--
-- Name: upgradetask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE upgradetask_id_seq OWNED BY upgrade_task.id;


SET default_with_oids = true;

--
-- Name: user_session; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE user_session (
    id bigint NOT NULL,
    session_start timestamp without time zone NOT NULL,
    session_end timestamp without time zone,
    person_id bigint
);


ALTER TABLE public.user_session OWNER TO tdar;

--
-- Name: user_session_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE user_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_session_id_seq OWNER TO tdar;

--
-- Name: user_session_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE user_session_id_seq OWNED BY user_session.id;


SET default_with_oids = false;

--
-- Name: video; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE video (
    audio_channels character varying(255),
    audio_codec character varying(255),
    audio_kbps integer,
    fps integer,
    height integer,
    kbps integer,
    sample_frequency integer,
    video_codec character varying(255),
    width integer,
    id bigint NOT NULL
);


ALTER TABLE public.video OWNER TO tdar;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY authorized_user ALTER COLUMN id SET DEFAULT nextval('authorized_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable ALTER COLUMN id SET DEFAULT nextval('category_variable_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY collection ALTER COLUMN id SET DEFAULT nextval('collection_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coverage_date ALTER COLUMN id SET DEFAULT nextval('coverage_date_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY culture_keyword ALTER COLUMN id SET DEFAULT nextval('culture_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship ALTER COLUMN id SET DEFAULT nextval('data_table_column_relationship_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship ALTER COLUMN id SET DEFAULT nextval('data_table_relationship_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY explore_cache_decade ALTER COLUMN id SET DEFAULT nextval('explore_cache_decade_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY geographic_keyword ALTER COLUMN id SET DEFAULT nextval('geographic_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY homepage_cache_geographic_keyword ALTER COLUMN id SET DEFAULT nextval('homepage_cache_geographic_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY homepage_cache_resource_type ALTER COLUMN id SET DEFAULT nextval('homepage_cache_resource_type_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY homepage_featured_item_cache ALTER COLUMN id SET DEFAULT nextval('homepage_featured_item_cache_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_file_download_statistics ALTER COLUMN id SET DEFAULT nextval('information_resource_file_download_statistics_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution ALTER COLUMN id SET DEFAULT nextval('institution_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY investigation_type ALTER COLUMN id SET DEFAULT nextval('investigation_type_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY material_keyword ALTER COLUMN id SET DEFAULT nextval('material_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology_node ALTER COLUMN id SET DEFAULT nextval('ontology_node_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY other_keyword ALTER COLUMN id SET DEFAULT nextval('other_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY personal_filestore_ticket ALTER COLUMN id SET DEFAULT nextval('personal_filestore_ticket_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY related_comparative_collection ALTER COLUMN id SET DEFAULT nextval('related_comparative_collection_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_access_statistics ALTER COLUMN id SET DEFAULT nextval('resource_access_statistics_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation ALTER COLUMN id SET DEFAULT nextval('resource_annotation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation_key ALTER COLUMN id SET DEFAULT nextval('resource_annotation_key_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_creator ALTER COLUMN id SET DEFAULT nextval('resource_creator_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_note ALTER COLUMN id SET DEFAULT nextval('resource_note_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY sensory_data_image ALTER COLUMN id SET DEFAULT nextval('sensory_data_image_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY sensory_data_scan ALTER COLUMN id SET DEFAULT nextval('sensory_data_scan_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_name_keyword ALTER COLUMN id SET DEFAULT nextval('site_name_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_type_keyword ALTER COLUMN id SET DEFAULT nextval('site_type_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY source_collection ALTER COLUMN id SET DEFAULT nextval('source_collection_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY stats ALTER COLUMN id SET DEFAULT nextval('stats_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY temporal_keyword ALTER COLUMN id SET DEFAULT nextval('temporal_keyword_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY upgrade_task ALTER COLUMN id SET DEFAULT nextval('upgradetask_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY user_session ALTER COLUMN id SET DEFAULT nextval('user_session_id_seq'::regclass);


--
-- Name: authorized_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY authorized_user
    ADD CONSTRAINT authorized_user_pkey PRIMARY KEY (id);


--
-- Name: bookmarked_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT bookmarked_resource_pkey PRIMARY KEY (id);


--
-- Name: category_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY category_variable
    ADD CONSTRAINT category_variable_pkey PRIMARY KEY (id);


--
-- Name: coding_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coding_rule
    ADD CONSTRAINT coding_rule_pkey PRIMARY KEY (id);


--
-- Name: coding_sheet_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT coding_sheet_pkey PRIMARY KEY (id);


--
-- Name: collection_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY collection
    ADD CONSTRAINT collection_pkey PRIMARY KEY (id);


--
-- Name: collection_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT collection_resource_pkey PRIMARY KEY (collection_id, resource_id);


--
-- Name: contributor_request_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_pkey PRIMARY KEY (id);


--
-- Name: coverage_date_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coverage_date
    ADD CONSTRAINT coverage_date_pkey PRIMARY KEY (id);


--
-- Name: coverage_longitude_latitude_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY latitude_longitude
    ADD CONSTRAINT coverage_longitude_latitude_pkey PRIMARY KEY (id);


--
-- Name: creator_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY creator
    ADD CONSTRAINT creator_pkey PRIMARY KEY (id);


--
-- Name: culture_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT culture_keyword_pkey PRIMARY KEY (id);


--
-- Name: data_table_column_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT data_table_column_pkey PRIMARY KEY (id);


--
-- Name: data_table_column_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT data_table_column_relationship_pkey PRIMARY KEY (id);


--
-- Name: data_table_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT data_table_pkey PRIMARY KEY (id);


--
-- Name: data_table_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT data_table_relationship_pkey PRIMARY KEY (id);


--
-- Name: dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (id);


--
-- Name: document_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);


--
-- Name: explore_cache_decade_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY explore_cache_decade
    ADD CONSTRAINT explore_cache_decade_pkey PRIMARY KEY (id);


--
-- Name: geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY geographic_keyword
    ADD CONSTRAINT geographic_keyword_pkey PRIMARY KEY (id);


--
-- Name: homepage_cache_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_geographic_keyword
    ADD CONSTRAINT homepage_cache_geographic_keyword_pkey PRIMARY KEY (id);


--
-- Name: homepage_cache_resource_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_resource_type
    ADD CONSTRAINT homepage_cache_resource_type_pkey PRIMARY KEY (id);


--
-- Name: homepage_cache_resource_type_unique; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_resource_type
    ADD CONSTRAINT homepage_cache_resource_type_unique UNIQUE (resource_type);


--
-- Name: homepage_featured_item_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_featured_item_cache
    ADD CONSTRAINT homepage_featured_item_cache_pkey PRIMARY KEY (id);


--
-- Name: image_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);


--
-- Name: information_resource_file_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file
    ADD CONSTRAINT information_resource_file_pkey PRIMARY KEY (id);


--
-- Name: information_resource_file_vers_information_resource_file_id_key; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT information_resource_file_vers_information_resource_file_id_key UNIQUE (information_resource_file_id, file_version, internal_type);


--
-- Name: information_resource_file_version_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT information_resource_file_version_pkey PRIMARY KEY (id);


--
-- Name: information_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_pkey PRIMARY KEY (id);


--
-- Name: information_resource_related_citation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citation_pkey PRIMARY KEY (information_resource_id, document_id);


--
-- Name: information_resource_source_citation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citation_pkey PRIMARY KEY (information_resource_id, document_id);


--
-- Name: institution_name_key; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_name_key UNIQUE (name);


--
-- Name: institution_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_pkey PRIMARY KEY (id);


--
-- Name: investigation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY investigation_type
    ADD CONSTRAINT investigation_type_pkey PRIMARY KEY (id);


--
-- Name: material_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY material_keyword
    ADD CONSTRAINT material_keyword_pkey PRIMARY KEY (id);


--
-- Name: ontology_node_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY ontology_node
    ADD CONSTRAINT ontology_node_pkey PRIMARY KEY (id);


--
-- Name: ontology_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT ontology_pkey PRIMARY KEY (id);


--
-- Name: other_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY other_keyword
    ADD CONSTRAINT other_keyword_pkey PRIMARY KEY (id);


--
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- Name: person_username_key; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_username_key UNIQUE (username);


--
-- Name: personal_filestore_ticket_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY personal_filestore_ticket
    ADD CONSTRAINT personal_filestore_ticket_pkey PRIMARY KEY (id);

--added by jtd. FK can only be added after person defined and person.id becomes PK
ALTER TABLE collection ADD COLUMN owner_id bigint references person;


--
-- Name: pk_stats; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY stats
    ADD CONSTRAINT pk_stats PRIMARY KEY (id);


--
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- Name: resource_annotation_key_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_annotation_key
    ADD CONSTRAINT resource_annotation_key_pkey PRIMARY KEY (id);


--
-- Name: resource_annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT resource_annotation_pkey PRIMARY KEY (id);


--
-- Name: resource_creator_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT resource_creator_pkey PRIMARY KEY (id);


--
-- Name: resource_culture_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_pkey PRIMARY KEY (resource_id, culture_keyword_id);


--
-- Name: resource_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_pkey PRIMARY KEY (resource_id, geographic_keyword_id);


--
-- Name: resource_investigation_type_pkey1; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_pkey1 PRIMARY KEY (resource_id, investigation_type_id);


--
-- Name: resource_managed_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_pkey PRIMARY KEY (resource_id, geographic_keyword_id);


--
-- Name: resource_material_keyword_pkey1; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_pkey1 PRIMARY KEY (resource_id, material_keyword_id);


--
-- Name: resource_note_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_note
    ADD CONSTRAINT resource_note_pkey PRIMARY KEY (id);


--
-- Name: resource_other_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_pkey PRIMARY KEY (resource_id, other_keyword_id);


--
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);


--
-- Name: resource_revised_date_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_revision_log
    ADD CONSTRAINT resource_revised_date_pkey PRIMARY KEY (id);


--
-- Name: resource_site_name_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_pkey PRIMARY KEY (resource_id, site_name_keyword_id);


--
-- Name: resource_site_type_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_pkey PRIMARY KEY (resource_id, site_type_keyword_id);


--
-- Name: resource_temporal_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_pkey PRIMARY KEY (resource_id, temporal_keyword_id);


--
-- Name: sensory_data_image_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data_image
    ADD CONSTRAINT sensory_data_image_pkey PRIMARY KEY (id);


--
-- Name: sensory_data_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data
    ADD CONSTRAINT sensory_data_pkey PRIMARY KEY (id);


--
-- Name: sensory_data_scan_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data_scan
    ADD CONSTRAINT sensory_data_scan_pkey PRIMARY KEY (id);


--
-- Name: site_name_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_name_keyword
    ADD CONSTRAINT site_name_keyword_pkey PRIMARY KEY (id);


--
-- Name: site_type_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_type_keyword
    ADD CONSTRAINT site_type_keyword_pkey PRIMARY KEY (id);


--
-- Name: temporal_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY temporal_keyword
    ADD CONSTRAINT temporal_keyword_pkey PRIMARY KEY (id);


--
-- Name: unique_culture_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT unique_culture_keyword UNIQUE (label);


--
-- Name: unique_geographic_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY geographic_keyword
    ADD CONSTRAINT unique_geographic_keyword UNIQUE (label);


--
-- Name: unique_other_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY other_keyword
    ADD CONSTRAINT unique_other_keyword UNIQUE (label);


--
-- Name: unique_site_name_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_name_keyword
    ADD CONSTRAINT unique_site_name_keyword UNIQUE (label);


--
-- Name: unique_temporal_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY temporal_keyword
    ADD CONSTRAINT unique_temporal_keyword UNIQUE (label);


--
-- Name: upgradetask_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY upgrade_task
    ADD CONSTRAINT upgradetask_pkey PRIMARY KEY (id);


--
-- Name: user_session_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY user_session
    ADD CONSTRAINT user_session_pkey PRIMARY KEY (id);


--
-- Name: video_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY video
    ADD CONSTRAINT video_pkey PRIMARY KEY (id);


--
-- Name: authorized_user_cid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX authorized_user_cid ON authorized_user USING btree (id, resource_collection_id);


--
-- Name: authorized_user_perm; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX authorized_user_perm ON authorized_user USING btree (general_permission_int, user_id, resource_collection_id);


--
-- Name: cltkwd_appr; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX cltkwd_appr ON culture_keyword USING btree (approved, id);


--
-- Name: coding_catvar_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coding_catvar_id ON coding_sheet USING btree (category_variable_id);


--
-- Name: coding_rule_term_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coding_rule_term_index ON coding_rule USING btree (term);


--
-- Name: coverage_resid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coverage_resid ON coverage_date USING btree (resource_id, id);


--
-- Name: creator_sequence; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX creator_sequence ON resource_creator USING btree (resource_id, sequence_number, creator_id);


--
-- Name: culture_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX culture_keyword_label_lc ON culture_keyword USING btree (lower((label)::text));


--
-- Name: document_type_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX document_type_index ON document USING btree (document_type);


--
-- Name: email_unique; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE UNIQUE INDEX email_unique ON person USING btree (email);


--
-- Name: geog_label_level_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX geog_label_level_id ON geographic_keyword USING btree (level, label, id);


--
-- Name: geographic_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX geographic_keyword_label_lc ON geographic_keyword USING btree (lower((label)::text));


--
-- Name: infores_projid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX infores_projid ON information_resource USING btree (project_id, id);


--
-- Name: infores_provid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX infores_provid ON information_resource USING btree (provider_institution_id);


--
-- Name: institution_name_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX institution_name_lc ON institution USING btree (lower((name)::text), id);


--
-- Name: mgd_geogr_res; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX mgd_geogr_res ON resource_managed_geographic_keyword USING btree (resource_id, geographic_keyword_id);


--
-- Name: ontology_catvar_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_catvar_id ON ontology USING btree (category_variable_id);


--
-- Name: ontology_node_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_index ON ontology_node USING btree (index);


--
-- Name: ontology_node_interval_end_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_interval_end_index ON ontology_node USING btree (interval_end);


--
-- Name: ontology_node_interval_start_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_interval_start_index ON ontology_node USING btree (interval_start);


--
-- Name: other_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX other_keyword_label_lc ON other_keyword USING btree (lower((label)::text));


--
-- Name: person_email_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_email_index ON person USING btree (email);


--
-- Name: person_instid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_instid ON person USING btree (id, institution_id);


--
-- Name: person_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_lc ON person USING btree (lower((first_name)::text), lower((last_name)::text), id);


--
-- Name: rck_culture_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rck_culture_keyword_id ON resource_culture_keyword USING btree (culture_keyword_id);


--
-- Name: res_submitterid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX res_submitterid ON resource USING btree (submitter_id);


--
-- Name: res_updaterid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX res_updaterid ON resource USING btree (updater_id);


--
-- Name: rescreator_resid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rescreator_resid ON resource_creator USING btree (resource_id);


--
-- Name: resid_cultkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_cultkwdid ON resource_culture_keyword USING btree (resource_id, culture_keyword_id);


--
-- Name: resid_geogkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_geogkwdid ON resource_geographic_keyword USING btree (resource_id, geographic_keyword_id);


--
-- Name: resid_invtypeid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_invtypeid ON resource_investigation_type USING btree (resource_id, investigation_type_id);


--
-- Name: resid_matkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_matkwdid ON resource_material_keyword USING btree (resource_id, material_keyword_id);


--
-- Name: resid_noteid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_noteid ON resource_note USING btree (resource_id, id);


--
-- Name: resid_otherkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_otherkwdid ON resource_other_keyword USING btree (resource_id, other_keyword_id);


--
-- Name: resid_sesory_data_img; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sesory_data_img ON sensory_data_image USING btree (sensory_data_id, id);


--
-- Name: resid_sesory_data_scan; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sesory_data_scan ON sensory_data_scan USING btree (sensory_data_id, id);


--
-- Name: resid_sitenamekwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sitenamekwdid ON resource_site_name_keyword USING btree (resource_id, site_name_keyword_id);


--
-- Name: resid_sitetypekwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sitetypekwdid ON resource_site_type_keyword USING btree (resource_id, site_type_keyword_id);


--
-- Name: resid_temporalkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_temporalkwdid ON resource_temporal_keyword USING btree (resource_id, temporal_keyword_id);


--
-- Name: resource_id_keyid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_id_keyid ON resource_annotation USING btree (resource_id, id, resourceannotationkey_id);


--
-- Name: resource_latlong; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_latlong ON latitude_longitude USING btree (resource_id, id);


--
-- Name: resource_title_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_title_index ON resource USING btree (title);


--
-- Name: resource_type_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_type_index ON resource USING btree (resource_type);


--
-- Name: rgk_geographic_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rgk_geographic_keyword_id ON resource_geographic_keyword USING btree (geographic_keyword_id);


--
-- Name: rit_investigation_type_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rit_investigation_type_id ON resource_investigation_type USING btree (investigation_type_id);


--
-- Name: rmk_material_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rmk_material_keyword_id ON resource_material_keyword USING btree (material_keyword_id);


--
-- Name: rok_other_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rok_other_keyword_id ON resource_other_keyword USING btree (other_keyword_id);


--
-- Name: rsnk_site_name_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rsnk_site_name_keyword_id ON resource_site_name_keyword USING btree (site_name_keyword_id);


--
-- Name: rstk_site_type_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rstk_site_type_keyword_id ON resource_site_type_keyword USING btree (site_type_keyword_id);


--
-- Name: rtk_temporal_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rtk_temporal_keyword_id ON resource_temporal_keyword USING btree (temporal_keyword_id);


--
-- Name: site_name_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_name_keyword_label_lc ON site_name_keyword USING btree (lower((label)::text));


--
-- Name: site_type_keyword_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_type_keyword_index ON site_type_keyword USING btree (label);


--
-- Name: site_type_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_type_keyword_label_lc ON site_type_keyword USING btree (lower((label)::text));


--
-- Name: sitetype_appr; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX sitetype_appr ON site_type_keyword USING btree (approved, id);


--
-- Name: temporal_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX temporal_label_lc ON temporal_keyword USING btree (lower((label)::text));


--
-- Name: category_variable_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable
    ADD CONSTRAINT category_variable_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES category_variable(id);


--
-- Name: coding_rule_ontology_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_rule
    ADD CONSTRAINT coding_rule_ontology_node_id_fkey FOREIGN KEY (ontology_node_id) REFERENCES ontology_node(id);


--
-- Name: coding_sheet_default_ontology_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT coding_sheet_default_ontology_id_fkey FOREIGN KEY (default_ontology_id) REFERENCES ontology(id);


--
-- Name: coding_sheet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT coding_sheet_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: contributor_request_applicant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- Name: contributor_request_approver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_approver_id_fkey FOREIGN KEY (approver_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- Name: culture_keyword_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT culture_keyword_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES culture_keyword(id);


--
-- Name: data_table_column_default_coding_sheet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT data_table_column_default_coding_sheet_id_fkey FOREIGN KEY (default_coding_sheet_id) REFERENCES coding_sheet(id);


--
-- Name: dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk11beb35032793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_note
    ADD CONSTRAINT fk11beb35032793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fk276ff6d3ff692808; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT fk276ff6d3ff692808 FOREIGN KEY (information_resource_file_id) REFERENCES information_resource_file(id);


--
-- Name: fk2bc70d3a7b2d0e85; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_file
    ADD CONSTRAINT fk2bc70d3a7b2d0e85 FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- Name: fk2dbbbfe9a52c1e3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_name_keyword_synonym
    ADD CONSTRAINT fk2dbbbfe9a52c1e3 FOREIGN KEY (sitenamekeyword_id) REFERENCES site_name_keyword(id);


--
-- Name: fk31359698e9ef2043; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_type_keyword_synonym
    ADD CONSTRAINT fk31359698e9ef2043 FOREIGN KEY (sitetypekeyword_id) REFERENCES site_type_keyword(id);


--
-- Name: fk32612bea51d71f47; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY sensory_data
    ADD CONSTRAINT fk32612bea51d71f47 FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: fk344b15be19e50a8c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15be19e50a8c FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- Name: fk344b15be80a27383; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15be80a27383 FOREIGN KEY (localtable_id) REFERENCES data_table(id);


--
-- Name: fk344b15bee6f89bec; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15bee6f89bec FOREIGN KEY (foreigntable_id) REFERENCES data_table(id);


--
-- Name: fk5511d21363e4a1e3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY investigation_type_synonym
    ADD CONSTRAINT fk5511d21363e4a1e3 FOREIGN KEY (investigationtype_id) REFERENCES investigation_type(id);


--
-- Name: fk5b43fcfb32793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT fk5b43fcfb32793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fk5b43fcfb67ffc561; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT fk5b43fcfb67ffc561 FOREIGN KEY (creator_id) REFERENCES creator(id);


--
-- Name: fk5faa95b51d71f47; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY image
    ADD CONSTRAINT fk5faa95b51d71f47 FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: fk608fa6f919e50a8c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT fk608fa6f919e50a8c FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- Name: fk608fa6f96ac97cbe; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT fk608fa6f96ac97cbe FOREIGN KEY (dataset_id) REFERENCES resource(id);


--
-- Name: fk6b0147b51d71f47; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY video
    ADD CONSTRAINT fk6b0147b51d71f47 FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: fk7175948de769ea23; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY material_keyword_synonym
    ADD CONSTRAINT fk7175948de769ea23 FOREIGN KEY (materialkeyword_id) REFERENCES material_keyword(id);


--
-- Name: fk74b2d88f53679086; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT fk74b2d88f53679086 FOREIGN KEY (collection_id) REFERENCES collection(id);


--
-- Name: fk74b2d88fd20877f1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT fk74b2d88fd20877f1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fk7680deb1eab2d817; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_rule
    ADD CONSTRAINT fk7680deb1eab2d817 FOREIGN KEY (coding_sheet_id) REFERENCES coding_sheet(id);


--
-- Name: fk7e5a05dd644f9de3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY geographic_keyword_synonym
    ADD CONSTRAINT fk7e5a05dd644f9de3 FOREIGN KEY (geographickeyword_id) REFERENCES geographic_keyword(id);


--
-- Name: fk7f736747fc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY personal_filestore_ticket
    ADD CONSTRAINT fk7f736747fc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- Name: fk893dbc76521c07d1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY other_keyword_synonym
    ADD CONSTRAINT fk893dbc76521c07d1 FOREIGN KEY (otherkeyword_id) REFERENCES other_keyword(id);


--
-- Name: fk8c6540b332793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY latitude_longitude
    ADD CONSTRAINT fk8c6540b332793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fk9923c8b832793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT fk9923c8b832793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fk9923c8b8bca96193; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT fk9923c8b8bca96193 FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: fk99dda7aed6698fa8; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology_node_synonym
    ADD CONSTRAINT fk99dda7aed6698fa8 FOREIGN KEY (ontologynode_id) REFERENCES ontology_node(id);


--
-- Name: fk99dda7aed6698fa9; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution_synonym
    ADD CONSTRAINT fk99dda7aed6698fa9 FOREIGN KEY (institution_id) REFERENCES institution(id);


--
-- Name: fk9b5fd5a0fc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation_key
    ADD CONSTRAINT fk9b5fd5a0fc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- Name: fk_category_variable_synonyms__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable_synonyms
    ADD CONSTRAINT fk_category_variable_synonyms__category_variable FOREIGN KEY (categoryvariable_id) REFERENCES category_variable(id);


--
-- Name: fk_coding_sheet__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT fk_coding_sheet__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- Name: fk_data_table_column__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fk_data_table_column__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- Name: fk_data_table_column_relationship_foreign_column; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_foreign_column FOREIGN KEY (foreign_column_id) REFERENCES data_table_column(id);


--
-- Name: fk_data_table_column_relationship_local_column; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_local_column FOREIGN KEY (local_column_id) REFERENCES data_table_column(id);


--
-- Name: fk_data_table_column_relationship_relationship; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_relationship FOREIGN KEY (relationship_id) REFERENCES data_table_relationship(id);


--
-- Name: fk_ontology__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT fk_ontology__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- Name: fkb234f7ef1e1b5338; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY authorized_user
    ADD CONSTRAINT fkb234f7ef1e1b5338 FOREIGN KEY (resource_collection_id) REFERENCES collection(id);


--
-- Name: fkdf4940889bebcc03; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY temporal_keyword_synonym
    ADD CONSTRAINT fkdf4940889bebcc03 FOREIGN KEY (temporalkeyword_id) REFERENCES temporal_keyword(id);


--
-- Name: fke42e97145ff8f2d1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY culture_keyword_synonym
    ADD CONSTRAINT fke42e97145ff8f2d1 FOREIGN KEY (culturekeyword_id) REFERENCES culture_keyword(id);


--
-- Name: fke5d0f5c2d0884ca; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fke5d0f5c2d0884ca FOREIGN KEY (default_ontology_id) REFERENCES ontology(id);


--
-- Name: fke5d0f5c8072b1b7; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fke5d0f5c8072b1b7 FOREIGN KEY (data_table_id) REFERENCES data_table(id);


--
-- Name: fke74a76e867ffc561; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY creator_synonym
    ADD CONSTRAINT fke74a76e867ffc561 FOREIGN KEY (creator_id) REFERENCES creator(id);


--
-- Name: fkebabc40efc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT fkebabc40efc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- Name: fked904b19e8e3bf97; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fked904b19e8e3bf97 FOREIGN KEY (id) REFERENCES resource(id);


--
-- Name: fkf246a3a532793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coverage_date
    ADD CONSTRAINT fkf246a3a532793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fkfdf7080032793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT fkfdf7080032793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: fkfdf70800a279d68c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT fkfdf70800a279d68c FOREIGN KEY (resourceannotationkey_id) REFERENCES resource_annotation_key(id);


--
-- Name: homepage_featured_item_cache_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY homepage_featured_item_cache
    ADD CONSTRAINT homepage_featured_item_cache_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES information_resource(id);


--
-- Name: information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_id_fkey FOREIGN KEY (id) REFERENCES resource(id);


--
-- Name: information_resource_institution_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_institution_fk FOREIGN KEY (provider_institution_id) REFERENCES institution(id);


--
-- Name: information_resource_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: information_resource_publisher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_publisher_id_fkey FOREIGN KEY (publisher_id) REFERENCES institution(id);


--
-- Name: information_resource_related_citat_information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citat_information_resource_id_fkey FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- Name: information_resource_related_citation_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citation_document_id_fkey FOREIGN KEY (document_id) REFERENCES document(id);


--
-- Name: information_resource_source_citati_information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citati_information_resource_id_fkey FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- Name: information_resource_source_citation_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citation_document_id_fkey FOREIGN KEY (document_id) REFERENCES document(id);


--
-- Name: institution_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_creator_fk FOREIGN KEY (id) REFERENCES creator(id);


--
-- Name: institution_parentinstitution_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_parentinstitution_id_fkey FOREIGN KEY (parentinstitution_id) REFERENCES institution(id);


--
-- Name: ontology_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT ontology_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- Name: ontology_node_ontology_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology_node
    ADD CONSTRAINT ontology_node_ontology_id_fkey FOREIGN KEY (ontology_id) REFERENCES ontology(id);


--
-- Name: person_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_creator_fk FOREIGN KEY (id) REFERENCES creator(id);


--
-- Name: person_institution_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_institution_fk FOREIGN KEY (institution_id) REFERENCES institution(id);


--
-- Name: related_comparative_collection_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY related_comparative_collection
    ADD CONSTRAINT related_comparative_collection_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_culture_keyword_culture_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_culture_keyword_id_fkey FOREIGN KEY (culture_keyword_id) REFERENCES culture_keyword(id);


--
-- Name: resource_culture_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_geographic_keyword_geographic_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_geographic_keyword_id_fkey FOREIGN KEY (geographic_keyword_id) REFERENCES geographic_keyword(id);


--
-- Name: resource_geographic_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_investigation_type_investigation_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_investigation_type_id_fkey FOREIGN KEY (investigation_type_id) REFERENCES investigation_type(id);


--
-- Name: resource_investigation_type_resource_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_resource_id_fkey1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_managed_geographic_keyword_geographic_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_geographic_keyword_id_fkey FOREIGN KEY (geographic_keyword_id) REFERENCES geographic_keyword(id);


--
-- Name: resource_managed_geographic_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_material_keyword_material_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_material_keyword_id_fkey FOREIGN KEY (material_keyword_id) REFERENCES material_keyword(id);


--
-- Name: resource_material_keyword_resource_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_resource_id_fkey1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_other_keyword_other_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_other_keyword_id_fkey FOREIGN KEY (other_keyword_id) REFERENCES other_keyword(id);


--
-- Name: resource_other_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_revision_log_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_revision_log
    ADD CONSTRAINT resource_revision_log_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: resource_site_name_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_site_name_keyword_site_name_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_site_name_keyword_id_fkey FOREIGN KEY (site_name_keyword_id) REFERENCES site_name_keyword(id);


--
-- Name: resource_site_type_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_site_type_keyword_site_type_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_site_type_keyword_id_fkey FOREIGN KEY (site_type_keyword_id) REFERENCES site_type_keyword(id);


--
-- Name: resource_temporal_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: resource_temporal_keyword_temporal_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_temporal_keyword_id_fkey FOREIGN KEY (temporal_keyword_id) REFERENCES temporal_keyword(id);


--
-- Name: resource_uploader_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_uploader_id_fkey FOREIGN KEY (uploader_id) REFERENCES person(id);


--
-- Name: site_type_keyword_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_type_keyword
    ADD CONSTRAINT site_type_keyword_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES site_type_keyword(id);


--
-- Name: source_collection_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY source_collection
    ADD CONSTRAINT source_collection_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- Name: user_session_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY user_session
    ADD CONSTRAINT user_session_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

