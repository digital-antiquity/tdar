--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.2
-- Dumped by pg_dump version 9.1.2
-- Started on 2012-07-26 10:36:54

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 289 (class 3079 OID 11639)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2671 (class 0 OID 0)
-- Dependencies: 289
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 161 (class 1259 OID 574969)
-- Dependencies: 6
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
-- TOC entry 162 (class 1259 OID 574972)
-- Dependencies: 161 6
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
-- TOC entry 2672 (class 0 OID 0)
-- Dependencies: 162
-- Name: authorized_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE authorized_user_id_seq OWNED BY authorized_user.id;


--
-- TOC entry 163 (class 1259 OID 574974)
-- Dependencies: 6
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
-- TOC entry 164 (class 1259 OID 574976)
-- Dependencies: 2295 6
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
-- TOC entry 165 (class 1259 OID 574980)
-- Dependencies: 6
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
-- TOC entry 166 (class 1259 OID 574986)
-- Dependencies: 6
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
-- TOC entry 167 (class 1259 OID 574988)
-- Dependencies: 165 6
-- Name: category_variable_new_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE category_variable_new_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_variable_new_id_seq OWNER TO tdar;

--
-- TOC entry 2673 (class 0 OID 0)
-- Dependencies: 167
-- Name: category_variable_new_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE category_variable_new_id_seq OWNED BY category_variable.id;


SET default_with_oids = true;

--
-- TOC entry 168 (class 1259 OID 574990)
-- Dependencies: 2297 6
-- Name: category_variable_old; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE category_variable_old (
    id bigint DEFAULT nextval('category_variable_id_seq'::regclass) NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    parent_id bigint,
    encoded_parent_ids character varying(255),
    label character varying(255),
    new_id bigint
);


ALTER TABLE public.category_variable_old OWNER TO tdar;

--
-- TOC entry 169 (class 1259 OID 574997)
-- Dependencies: 6
-- Name: category_variable_synonyms; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE category_variable_synonyms (
    categoryvariable_id integer NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.category_variable_synonyms OWNER TO tdar;

--
-- TOC entry 170 (class 1259 OID 575000)
-- Dependencies: 6
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
-- TOC entry 171 (class 1259 OID 575002)
-- Dependencies: 2298 6
-- Name: coding_rule; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE coding_rule (
    id bigint DEFAULT nextval('coding_rule_id_seq'::regclass) NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(2000),
    term character varying(255) NOT NULL,
    coding_sheet_id bigint
);


ALTER TABLE public.coding_rule OWNER TO tdar;

--
-- TOC entry 172 (class 1259 OID 575009)
-- Dependencies: 6
-- Name: coding_sheet; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE coding_sheet (
    id bigint NOT NULL,
    category_variable_id integer
);


ALTER TABLE public.coding_sheet OWNER TO tdar;

SET default_with_oids = false;

--
-- TOC entry 173 (class 1259 OID 575012)
-- Dependencies: 2299 6
-- Name: collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE collection (
    id bigint NOT NULL,
    description text,
    name character varying(255),
    owner_id bigint,
    parent_id bigint,
    collection_type character varying(255),
    visible boolean DEFAULT false NOT NULL,
    date_created timestamp without time zone,
    sort_order character varying(25)
);


ALTER TABLE public.collection OWNER TO tdar;

--
-- TOC entry 174 (class 1259 OID 575019)
-- Dependencies: 6 173
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
-- TOC entry 2674 (class 0 OID 0)
-- Dependencies: 174
-- Name: collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE collection_id_seq OWNED BY collection.id;


--
-- TOC entry 175 (class 1259 OID 575021)
-- Dependencies: 6
-- Name: collection_resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE collection_resource (
    collection_id bigint NOT NULL,
    resource_id bigint NOT NULL
);


ALTER TABLE public.collection_resource OWNER TO tdar;

--
-- TOC entry 176 (class 1259 OID 575024)
-- Dependencies: 6
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
-- TOC entry 177 (class 1259 OID 575026)
-- Dependencies: 2301 6
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
-- TOC entry 178 (class 1259 OID 575033)
-- Dependencies: 2302 2303 6
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
-- TOC entry 179 (class 1259 OID 575041)
-- Dependencies: 6 178
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
-- TOC entry 2675 (class 0 OID 0)
-- Dependencies: 179
-- Name: coverage_date_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE coverage_date_id_seq OWNED BY coverage_date.id;


--
-- TOC entry 180 (class 1259 OID 575043)
-- Dependencies: 6
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
-- TOC entry 181 (class 1259 OID 575045)
-- Dependencies: 2305 6
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
-- TOC entry 182 (class 1259 OID 575052)
-- Dependencies: 6
-- Name: creator_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE creator_synonym (
    creator_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.creator_synonym OWNER TO tdar;

--
-- TOC entry 183 (class 1259 OID 575055)
-- Dependencies: 6
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
-- TOC entry 184 (class 1259 OID 575061)
-- Dependencies: 6 183
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
-- TOC entry 2676 (class 0 OID 0)
-- Dependencies: 184
-- Name: culture_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE culture_keyword_id_seq OWNED BY culture_keyword.id;


--
-- TOC entry 185 (class 1259 OID 575063)
-- Dependencies: 6
-- Name: culture_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE culture_keyword_synonym (
    culturekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.culture_keyword_synonym OWNER TO tdar;

--
-- TOC entry 186 (class 1259 OID 575066)
-- Dependencies: 6
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
-- TOC entry 187 (class 1259 OID 575068)
-- Dependencies: 2307 6
-- Name: data_table; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_table (
    id bigint DEFAULT nextval('data_table_id_seq'::regclass) NOT NULL,
    aggregated boolean NOT NULL,
    name character varying(255) NOT NULL,
    category_variable_id integer,
    dataset_id bigint,
    description text,
    display_name character varying(255)
);


ALTER TABLE public.data_table OWNER TO tdar;

--
-- TOC entry 188 (class 1259 OID 575075)
-- Dependencies: 6
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
-- TOC entry 189 (class 1259 OID 575077)
-- Dependencies: 2308 2309 2310 2311 6
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
-- TOC entry 190 (class 1259 OID 575087)
-- Dependencies: 6
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
-- TOC entry 191 (class 1259 OID 575090)
-- Dependencies: 190 6
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
-- TOC entry 2677 (class 0 OID 0)
-- Dependencies: 191
-- Name: data_table_column_relationship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE data_table_column_relationship_id_seq OWNED BY data_table_column_relationship.id;


--
-- TOC entry 192 (class 1259 OID 575092)
-- Dependencies: 6
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
-- TOC entry 193 (class 1259 OID 575095)
-- Dependencies: 192 6
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
-- TOC entry 2678 (class 0 OID 0)
-- Dependencies: 193
-- Name: data_table_relationship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE data_table_relationship_id_seq OWNED BY data_table_relationship.id;


--
-- TOC entry 194 (class 1259 OID 575097)
-- Dependencies: 6
-- Name: data_value_ontology_node_mapping; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE data_value_ontology_node_mapping (
    id bigint NOT NULL,
    data_table_column_id bigint,
    data_value text,
    ontology_node_id bigint
);


ALTER TABLE public.data_value_ontology_node_mapping OWNER TO tdar;

--
-- TOC entry 195 (class 1259 OID 575103)
-- Dependencies: 6 194
-- Name: data_value_ontology_node_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: tdar
--

CREATE SEQUENCE data_value_ontology_node_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_value_ontology_node_mapping_id_seq OWNER TO tdar;

--
-- TOC entry 2679 (class 0 OID 0)
-- Dependencies: 195
-- Name: data_value_ontology_node_mapping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE data_value_ontology_node_mapping_id_seq OWNED BY data_value_ontology_node_mapping.id;


SET default_with_oids = true;

--
-- TOC entry 196 (class 1259 OID 575105)
-- Dependencies: 6
-- Name: dataset; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE dataset (
    id bigint NOT NULL
);


ALTER TABLE public.dataset OWNER TO tdar;

SET default_with_oids = false;

--
-- TOC entry 197 (class 1259 OID 575108)
-- Dependencies: 6
-- Name: document; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE document (
    document_type character varying(255),
    edition character varying(255),
    isbn character varying(255),
    number_of_pages integer,
    number_of_volumes integer,
    publisher character varying(255),
    publisher_location character varying(255),
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
    end_page character varying(10)
);


ALTER TABLE public.document OWNER TO tdar;

--
-- TOC entry 198 (class 1259 OID 575114)
-- Dependencies: 6
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
-- TOC entry 199 (class 1259 OID 575116)
-- Dependencies: 6
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
-- TOC entry 200 (class 1259 OID 575122)
-- Dependencies: 6 199
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
-- TOC entry 2680 (class 0 OID 0)
-- Dependencies: 200
-- Name: geographic_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE geographic_keyword_id_seq OWNED BY geographic_keyword.id;


--
-- TOC entry 201 (class 1259 OID 575124)
-- Dependencies: 6
-- Name: geographic_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE geographic_keyword_synonym (
    geographickeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.geographic_keyword_synonym OWNER TO tdar;

--
-- TOC entry 202 (class 1259 OID 575127)
-- Dependencies: 6
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
-- TOC entry 203 (class 1259 OID 575130)
-- Dependencies: 6 202
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
-- TOC entry 2681 (class 0 OID 0)
-- Dependencies: 203
-- Name: homepage_cache_geographic_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE homepage_cache_geographic_keyword_id_seq OWNED BY homepage_cache_geographic_keyword.id;


--
-- TOC entry 204 (class 1259 OID 575132)
-- Dependencies: 6
-- Name: homepage_cache_resource_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE homepage_cache_resource_type (
    id bigint NOT NULL,
    resource_type character varying(100) NOT NULL,
    resource_count bigint
);


ALTER TABLE public.homepage_cache_resource_type OWNER TO tdar;

--
-- TOC entry 205 (class 1259 OID 575135)
-- Dependencies: 6 204
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
-- TOC entry 2682 (class 0 OID 0)
-- Dependencies: 205
-- Name: homepage_cache_resource_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE homepage_cache_resource_type_id_seq OWNED BY homepage_cache_resource_type.id;


--
-- TOC entry 206 (class 1259 OID 575137)
-- Dependencies: 6
-- Name: image; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE image (
    id bigint NOT NULL
);


ALTER TABLE public.image OWNER TO tdar;

--
-- TOC entry 207 (class 1259 OID 575140)
-- Dependencies: 2318 2319 2320 2321 2322 2323 2324 2325 2326 2327 2328 6
-- Name: information_resource; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource (
    id bigint NOT NULL,
    available_to_public boolean,
    date_created integer,
    date_made_public timestamp without time zone,
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
    inheriting_collection_information boolean DEFAULT false
);


ALTER TABLE public.information_resource OWNER TO tdar;

--
-- TOC entry 208 (class 1259 OID 575157)
-- Dependencies: 6
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
-- TOC entry 209 (class 1259 OID 575159)
-- Dependencies: 2329 6
-- Name: information_resource_file; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_file (
    id bigint DEFAULT nextval('information_resource_file_seq'::regclass) NOT NULL,
    general_type character varying(255),
    latest_version integer,
    sequence_number integer,
    information_resource_id bigint,
    confidential boolean,
    status character varying(32)
);


ALTER TABLE public.information_resource_file OWNER TO tdar;

--
-- TOC entry 210 (class 1259 OID 575163)
-- Dependencies: 6
-- Name: information_resource_file_download_statistics; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_file_download_statistics (
    id bigint NOT NULL,
    date_accessed timestamp without time zone,
    information_resource_file_id bigint
);


ALTER TABLE public.information_resource_file_download_statistics OWNER TO tdar;

--
-- TOC entry 211 (class 1259 OID 575166)
-- Dependencies: 6 210
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
-- TOC entry 2683 (class 0 OID 0)
-- Dependencies: 211
-- Name: information_resource_file_download_statistics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE information_resource_file_download_statistics_id_seq OWNED BY information_resource_file_download_statistics.id;


--
-- TOC entry 212 (class 1259 OID 575168)
-- Dependencies: 6
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
-- TOC entry 213 (class 1259 OID 575170)
-- Dependencies: 2331 6
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
    information_resource_file_id bigint
);


ALTER TABLE public.information_resource_file_version OWNER TO tdar;

--
-- TOC entry 214 (class 1259 OID 575177)
-- Dependencies: 6
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
-- TOC entry 215 (class 1259 OID 575179)
-- Dependencies: 6
-- Name: information_resource_related_citation; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_related_citation (
    information_resource_id bigint NOT NULL,
    document_id bigint NOT NULL
);


ALTER TABLE public.information_resource_related_citation OWNER TO tdar;

--
-- TOC entry 216 (class 1259 OID 575182)
-- Dependencies: 6
-- Name: information_resource_source_citation; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE information_resource_source_citation (
    information_resource_id bigint NOT NULL,
    document_id bigint NOT NULL
);


ALTER TABLE public.information_resource_source_citation OWNER TO tdar;

SET default_with_oids = true;

--
-- TOC entry 217 (class 1259 OID 575185)
-- Dependencies: 6
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
-- TOC entry 218 (class 1259 OID 575191)
-- Dependencies: 217 6
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
-- TOC entry 2684 (class 0 OID 0)
-- Dependencies: 218
-- Name: institution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE institution_id_seq OWNED BY institution.id;


SET default_with_oids = false;

--
-- TOC entry 219 (class 1259 OID 575193)
-- Dependencies: 6
-- Name: institution_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE institution_synonym (
    institution_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.institution_synonym OWNER TO tdar;

--
-- TOC entry 220 (class 1259 OID 575196)
-- Dependencies: 6
-- Name: investigation_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE investigation_type (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.investigation_type OWNER TO tdar;

--
-- TOC entry 221 (class 1259 OID 575202)
-- Dependencies: 220 6
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
-- TOC entry 2685 (class 0 OID 0)
-- Dependencies: 221
-- Name: investigation_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE investigation_type_id_seq OWNED BY investigation_type.id;


--
-- TOC entry 222 (class 1259 OID 575204)
-- Dependencies: 6
-- Name: investigation_type_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE investigation_type_synonym (
    investigationtype_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.investigation_type_synonym OWNER TO tdar;

--
-- TOC entry 223 (class 1259 OID 575207)
-- Dependencies: 6
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
-- TOC entry 224 (class 1259 OID 575209)
-- Dependencies: 2334 6
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
-- TOC entry 225 (class 1259 OID 575213)
-- Dependencies: 6
-- Name: material_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE material_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.material_keyword OWNER TO tdar;

--
-- TOC entry 226 (class 1259 OID 575219)
-- Dependencies: 6 225
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
-- TOC entry 2686 (class 0 OID 0)
-- Dependencies: 226
-- Name: material_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE material_keyword_id_seq OWNED BY material_keyword.id;


--
-- TOC entry 227 (class 1259 OID 575221)
-- Dependencies: 6
-- Name: material_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE material_keyword_synonym (
    materialkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.material_keyword_synonym OWNER TO tdar;

SET default_with_oids = true;

--
-- TOC entry 228 (class 1259 OID 575224)
-- Dependencies: 6
-- Name: ontology; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE ontology (
    id bigint NOT NULL,
    category_variable_id integer
);


ALTER TABLE public.ontology OWNER TO tdar;

SET default_with_oids = false;

--
-- TOC entry 229 (class 1259 OID 575227)
-- Dependencies: 6
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
-- TOC entry 230 (class 1259 OID 575233)
-- Dependencies: 229 6
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
-- TOC entry 2687 (class 0 OID 0)
-- Dependencies: 230
-- Name: ontology_node_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE ontology_node_id_seq OWNED BY ontology_node.id;


--
-- TOC entry 231 (class 1259 OID 575235)
-- Dependencies: 6
-- Name: ontology_node_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE ontology_node_synonym (
    ontologynode_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.ontology_node_synonym OWNER TO tdar;

--
-- TOC entry 232 (class 1259 OID 575238)
-- Dependencies: 6
-- Name: other_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE other_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.other_keyword OWNER TO tdar;

--
-- TOC entry 233 (class 1259 OID 575244)
-- Dependencies: 232 6
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
-- TOC entry 2688 (class 0 OID 0)
-- Dependencies: 233
-- Name: other_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE other_keyword_id_seq OWNED BY other_keyword.id;


--
-- TOC entry 234 (class 1259 OID 575246)
-- Dependencies: 6
-- Name: other_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE other_keyword_synonym (
    otherkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.other_keyword_synonym OWNER TO tdar;

--
-- TOC entry 235 (class 1259 OID 575249)
-- Dependencies: 6
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
-- TOC entry 236 (class 1259 OID 575251)
-- Dependencies: 2338 2339 2340 2341 2342 6
-- Name: person; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE person (
    id bigint DEFAULT nextval('person_id_seq'::regclass) NOT NULL,
    contributor boolean NOT NULL,
    email character varying(255),
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    privileged boolean NOT NULL,
    registered boolean NOT NULL,
    rpa boolean DEFAULT false NOT NULL,
    rpa_number character varying(255),
    phone character varying(255),
    password character varying(255),
    contributor_reason character varying(512),
    institution_id bigint,
    total_login bigint DEFAULT 0,
    last_login timestamp without time zone,
    penultimate_login timestamp without time zone,
    phone_public boolean DEFAULT false NOT NULL,
    email_public boolean DEFAULT false NOT NULL
);


ALTER TABLE public.person OWNER TO tdar;

SET default_with_oids = false;

--
-- TOC entry 237 (class 1259 OID 575262)
-- Dependencies: 6
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
-- TOC entry 238 (class 1259 OID 575268)
-- Dependencies: 6 237
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
-- TOC entry 2689 (class 0 OID 0)
-- Dependencies: 238
-- Name: personal_filestore_ticket_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE personal_filestore_ticket_id_seq OWNED BY personal_filestore_ticket.id;


SET default_with_oids = true;

--
-- TOC entry 239 (class 1259 OID 575270)
-- Dependencies: 6
-- Name: project; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE project (
    id bigint NOT NULL
);


ALTER TABLE public.project OWNER TO tdar;

--
-- TOC entry 240 (class 1259 OID 575273)
-- Dependencies: 6
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
-- TOC entry 241 (class 1259 OID 575275)
-- Dependencies: 6
-- Name: related_comparative_collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE related_comparative_collection (
    id bigint NOT NULL,
    text character varying(1024),
    resource_id bigint
);


ALTER TABLE public.related_comparative_collection OWNER TO tdar;

--
-- TOC entry 242 (class 1259 OID 575281)
-- Dependencies: 241 6
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
-- TOC entry 2690 (class 0 OID 0)
-- Dependencies: 242
-- Name: related_comparative_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE related_comparative_collection_id_seq OWNED BY related_comparative_collection.id;


SET default_with_oids = true;

--
-- TOC entry 243 (class 1259 OID 575283)
-- Dependencies: 2345 6
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
    external_id character varying(255)
);


ALTER TABLE public.resource OWNER TO tdar;

SET default_with_oids = false;

--
-- TOC entry 244 (class 1259 OID 575290)
-- Dependencies: 6
-- Name: resource_access_statistics; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_access_statistics (
    id bigint NOT NULL,
    date_accessed timestamp without time zone,
    resource_id bigint
);


ALTER TABLE public.resource_access_statistics OWNER TO tdar;

--
-- TOC entry 245 (class 1259 OID 575293)
-- Dependencies: 244 6
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
-- TOC entry 2691 (class 0 OID 0)
-- Dependencies: 245
-- Name: resource_access_statistics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_access_statistics_id_seq OWNED BY resource_access_statistics.id;


--
-- TOC entry 246 (class 1259 OID 575295)
-- Dependencies: 6
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
-- TOC entry 247 (class 1259 OID 575301)
-- Dependencies: 6 246
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
-- TOC entry 2692 (class 0 OID 0)
-- Dependencies: 247
-- Name: resource_annotation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_annotation_id_seq OWNED BY resource_annotation.id;


--
-- TOC entry 248 (class 1259 OID 575303)
-- Dependencies: 6
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
-- TOC entry 249 (class 1259 OID 575309)
-- Dependencies: 248 6
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
-- TOC entry 2693 (class 0 OID 0)
-- Dependencies: 249
-- Name: resource_annotation_key_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_annotation_key_id_seq OWNED BY resource_annotation_key.id;


--
-- TOC entry 250 (class 1259 OID 575311)
-- Dependencies: 6
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
-- TOC entry 251 (class 1259 OID 575314)
-- Dependencies: 250 6
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
-- TOC entry 2694 (class 0 OID 0)
-- Dependencies: 251
-- Name: resource_creator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_creator_id_seq OWNED BY resource_creator.id;


--
-- TOC entry 252 (class 1259 OID 575316)
-- Dependencies: 6
-- Name: resource_culture_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_culture_keyword (
    resource_id bigint NOT NULL,
    culture_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_culture_keyword OWNER TO tdar;

--
-- TOC entry 253 (class 1259 OID 575320)
-- Dependencies: 6
-- Name: resource_geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_geographic_keyword (
    resource_id bigint NOT NULL,
    geographic_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_geographic_keyword OWNER TO tdar;

--
-- TOC entry 254 (class 1259 OID 575323)
-- Dependencies: 6
-- Name: resource_investigation_type; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_investigation_type (
    resource_id bigint NOT NULL,
    investigation_type_id bigint NOT NULL
);


ALTER TABLE public.resource_investigation_type OWNER TO tdar;

--
-- TOC entry 255 (class 1259 OID 575326)
-- Dependencies: 6
-- Name: resource_managed_geographic_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_managed_geographic_keyword (
    resource_id bigint NOT NULL,
    geographic_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_managed_geographic_keyword OWNER TO tdar;

--
-- TOC entry 256 (class 1259 OID 575329)
-- Dependencies: 6
-- Name: resource_material_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_material_keyword (
    resource_id bigint NOT NULL,
    material_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_material_keyword OWNER TO tdar;

--
-- TOC entry 257 (class 1259 OID 575332)
-- Dependencies: 6
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
-- TOC entry 258 (class 1259 OID 575338)
-- Dependencies: 6 257
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
-- TOC entry 2695 (class 0 OID 0)
-- Dependencies: 258
-- Name: resource_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE resource_note_id_seq OWNED BY resource_note.id;


--
-- TOC entry 259 (class 1259 OID 575340)
-- Dependencies: 6
-- Name: resource_other_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_other_keyword (
    resource_id bigint NOT NULL,
    other_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_other_keyword OWNER TO tdar;

--
-- TOC entry 260 (class 1259 OID 575343)
-- Dependencies: 6
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
-- TOC entry 261 (class 1259 OID 575345)
-- Dependencies: 2351 6
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
-- TOC entry 262 (class 1259 OID 575352)
-- Dependencies: 6
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
-- TOC entry 263 (class 1259 OID 575354)
-- Dependencies: 6
-- Name: resource_site_name_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_site_name_keyword (
    resource_id bigint NOT NULL,
    site_name_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_site_name_keyword OWNER TO tdar;

--
-- TOC entry 264 (class 1259 OID 575357)
-- Dependencies: 6
-- Name: resource_site_type_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_site_type_keyword (
    resource_id bigint NOT NULL,
    site_type_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_site_type_keyword OWNER TO tdar;

--
-- TOC entry 265 (class 1259 OID 575360)
-- Dependencies: 6
-- Name: resource_temporal_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE resource_temporal_keyword (
    resource_id bigint NOT NULL,
    temporal_keyword_id bigint NOT NULL
);


ALTER TABLE public.resource_temporal_keyword OWNER TO tdar;

--
-- TOC entry 266 (class 1259 OID 575363)
-- Dependencies: 2352 2353 2354 2355 2356 2357 2358 2359 2360 2361 2362 2363 6
-- Name: sensory_data; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE sensory_data (
    id bigint NOT NULL,
    additional_project_notes character varying(255),
    company_name character varying(255),
    decimated_mesh_dataset character varying(255),
    decimated_mesh_original_triangle_count bigint,
    decimated_mesh_triangle_count bigint,
    final_dataset_description character varying(255),
    final_registration_points bigint,
    turntable_used boolean DEFAULT false NOT NULL,
    mesh_adjustment_matrix character varying(255),
    mesh_dataset_name character varying(255),
    mesh_holes_filled boolean DEFAULT false NOT NULL,
    mesh_processing_notes character varying(255),
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
    rgb_data_capture_info character varying(1024),
    rgb_preserved_from_original boolean DEFAULT false NOT NULL,
    scanner_details character varying(255),
    scans_total_acquired integer,
    scans_used integer,
    survey_conditions character varying(255),
    survey_date_begin timestamp without time zone,
    survey_location character varying(255),
    estimated_data_resolution character varying(255),
    total_scans_in_project bigint,
    point_deletion_summary character varying(1024),
    mesh_color_editions boolean DEFAULT false NOT NULL,
    mesh_healing_despiking boolean DEFAULT false NOT NULL,
    survey_date_end timestamp without time zone,
    planimetric_map_filename character varying(255),
    control_data_filename character varying(255),
    registration_method character varying(255)
);


ALTER TABLE public.sensory_data OWNER TO tdar;

--
-- TOC entry 267 (class 1259 OID 575381)
-- Dependencies: 2364 6
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
-- TOC entry 268 (class 1259 OID 575388)
-- Dependencies: 267 6
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
-- TOC entry 2696 (class 0 OID 0)
-- Dependencies: 268
-- Name: sensory_data_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE sensory_data_image_id_seq OWNED BY sensory_data_image.id;


--
-- TOC entry 269 (class 1259 OID 575390)
-- Dependencies: 2366 2367 6
-- Name: sensory_data_scan; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE sensory_data_scan (
    id bigint NOT NULL,
    filename character varying(255) NOT NULL,
    monument_name character varying(255),
    points_in_scan bigint,
    resolution character varying(255),
    scan_notes character varying(255),
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
-- TOC entry 270 (class 1259 OID 575398)
-- Dependencies: 6 269
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
-- TOC entry 2697 (class 0 OID 0)
-- Dependencies: 270
-- Name: sensory_data_scan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE sensory_data_scan_id_seq OWNED BY sensory_data_scan.id;


--
-- TOC entry 271 (class 1259 OID 575400)
-- Dependencies: 6
-- Name: site_name_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_name_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.site_name_keyword OWNER TO tdar;

--
-- TOC entry 272 (class 1259 OID 575406)
-- Dependencies: 6 271
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
-- TOC entry 2698 (class 0 OID 0)
-- Dependencies: 272
-- Name: site_name_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE site_name_keyword_id_seq OWNED BY site_name_keyword.id;


--
-- TOC entry 273 (class 1259 OID 575408)
-- Dependencies: 6
-- Name: site_name_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_name_keyword_synonym (
    sitenamekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.site_name_keyword_synonym OWNER TO tdar;

--
-- TOC entry 274 (class 1259 OID 575411)
-- Dependencies: 6
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
-- TOC entry 275 (class 1259 OID 575417)
-- Dependencies: 274 6
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
-- TOC entry 2699 (class 0 OID 0)
-- Dependencies: 275
-- Name: site_type_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE site_type_keyword_id_seq OWNED BY site_type_keyword.id;


--
-- TOC entry 276 (class 1259 OID 575419)
-- Dependencies: 6
-- Name: site_type_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE site_type_keyword_synonym (
    sitetypekeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.site_type_keyword_synonym OWNER TO tdar;

--
-- TOC entry 277 (class 1259 OID 575422)
-- Dependencies: 6
-- Name: source_collection; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE source_collection (
    id bigint NOT NULL,
    text character varying(1024),
    resource_id bigint
);


ALTER TABLE public.source_collection OWNER TO tdar;

--
-- TOC entry 278 (class 1259 OID 575428)
-- Dependencies: 6 277
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
-- TOC entry 2700 (class 0 OID 0)
-- Dependencies: 278
-- Name: source_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE source_collection_id_seq OWNED BY source_collection.id;


--
-- TOC entry 279 (class 1259 OID 575430)
-- Dependencies: 6
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
-- TOC entry 280 (class 1259 OID 575436)
-- Dependencies: 6 279
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
-- TOC entry 2701 (class 0 OID 0)
-- Dependencies: 280
-- Name: stats_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE stats_id_seq OWNED BY stats.id;


--
-- TOC entry 281 (class 1259 OID 575438)
-- Dependencies: 6
-- Name: temporal_keyword; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE temporal_keyword (
    id bigint NOT NULL,
    definition text,
    label character varying(255) NOT NULL
);


ALTER TABLE public.temporal_keyword OWNER TO tdar;

--
-- TOC entry 282 (class 1259 OID 575444)
-- Dependencies: 6 281
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
-- TOC entry 2702 (class 0 OID 0)
-- Dependencies: 282
-- Name: temporal_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE temporal_keyword_id_seq OWNED BY temporal_keyword.id;


--
-- TOC entry 283 (class 1259 OID 575446)
-- Dependencies: 6
-- Name: temporal_keyword_synonym; Type: TABLE; Schema: public; Owner: tdar; Tablespace: 
--

CREATE TABLE temporal_keyword_synonym (
    temporalkeyword_id bigint NOT NULL,
    synonyms character varying(255)
);


ALTER TABLE public.temporal_keyword_synonym OWNER TO tdar;

--
-- TOC entry 284 (class 1259 OID 575449)
-- Dependencies: 6
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
-- TOC entry 285 (class 1259 OID 575455)
-- Dependencies: 6 284
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
-- TOC entry 2703 (class 0 OID 0)
-- Dependencies: 285
-- Name: upgradetask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE upgradetask_id_seq OWNED BY upgrade_task.id;


SET default_with_oids = true;

--
-- TOC entry 286 (class 1259 OID 575457)
-- Dependencies: 6
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
-- TOC entry 287 (class 1259 OID 575460)
-- Dependencies: 286 6
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
-- TOC entry 2704 (class 0 OID 0)
-- Dependencies: 287
-- Name: user_session_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tdar
--

ALTER SEQUENCE user_session_id_seq OWNED BY user_session.id;


--
-- TOC entry 288 (class 1259 OID 575462)
-- Dependencies: 2293 6
-- Name: vw_resource_creator; Type: VIEW; Schema: public; Owner: tdar
--

CREATE VIEW vw_resource_creator AS
    SELECT rc.id AS rcid, r.id AS resource_id, r.title AS resource_title, rc.creator_id, CASE WHEN (p.id IS NOT NULL) THEN 'PERSON'::text WHEN (i.id IS NOT NULL) THEN 'INSTITUTION'::text ELSE NULL::text END AS rc_type, CASE WHEN (p.id IS NOT NULL) THEN ((((((COALESCE(p.first_name, ''::character varying))::text || ' '::text) || (COALESCE(p.last_name, ''::character varying))::text) || '('::text) || (COALESCE(p.email, ''::character varying))::text) || ')'::text) WHEN (i.id IS NOT NULL) THEN (i.name)::text ELSE NULL::text END AS rc_name, r.resource_type, rc.role FROM (((resource_creator rc JOIN resource r ON ((r.id = rc.resource_id))) LEFT JOIN person p ON ((rc.creator_id = p.id))) LEFT JOIN institution i ON ((rc.creator_id = i.id)));


ALTER TABLE public.vw_resource_creator OWNER TO tdar;

--
-- TOC entry 2294 (class 2604 OID 575467)
-- Dependencies: 162 161
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE authorized_user ALTER COLUMN id SET DEFAULT nextval('authorized_user_id_seq'::regclass);


--
-- TOC entry 2296 (class 2604 OID 575468)
-- Dependencies: 167 165
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE category_variable ALTER COLUMN id SET DEFAULT nextval('category_variable_new_id_seq'::regclass);


--
-- TOC entry 2300 (class 2604 OID 575469)
-- Dependencies: 174 173
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE collection ALTER COLUMN id SET DEFAULT nextval('collection_id_seq'::regclass);


--
-- TOC entry 2304 (class 2604 OID 575470)
-- Dependencies: 179 178
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE coverage_date ALTER COLUMN id SET DEFAULT nextval('coverage_date_id_seq'::regclass);


--
-- TOC entry 2306 (class 2604 OID 575471)
-- Dependencies: 184 183
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE culture_keyword ALTER COLUMN id SET DEFAULT nextval('culture_keyword_id_seq'::regclass);


--
-- TOC entry 2312 (class 2604 OID 575472)
-- Dependencies: 191 190
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE data_table_column_relationship ALTER COLUMN id SET DEFAULT nextval('data_table_column_relationship_id_seq'::regclass);


--
-- TOC entry 2313 (class 2604 OID 575473)
-- Dependencies: 193 192
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE data_table_relationship ALTER COLUMN id SET DEFAULT nextval('data_table_relationship_id_seq'::regclass);


--
-- TOC entry 2314 (class 2604 OID 575474)
-- Dependencies: 195 194
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE data_value_ontology_node_mapping ALTER COLUMN id SET DEFAULT nextval('data_value_ontology_node_mapping_id_seq'::regclass);


--
-- TOC entry 2315 (class 2604 OID 575475)
-- Dependencies: 200 199
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE geographic_keyword ALTER COLUMN id SET DEFAULT nextval('geographic_keyword_id_seq'::regclass);


--
-- TOC entry 2316 (class 2604 OID 575476)
-- Dependencies: 203 202
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE homepage_cache_geographic_keyword ALTER COLUMN id SET DEFAULT nextval('homepage_cache_geographic_keyword_id_seq'::regclass);


--
-- TOC entry 2317 (class 2604 OID 575477)
-- Dependencies: 205 204
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE homepage_cache_resource_type ALTER COLUMN id SET DEFAULT nextval('homepage_cache_resource_type_id_seq'::regclass);


--
-- TOC entry 2330 (class 2604 OID 575478)
-- Dependencies: 211 210
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE information_resource_file_download_statistics ALTER COLUMN id SET DEFAULT nextval('information_resource_file_download_statistics_id_seq'::regclass);


--
-- TOC entry 2332 (class 2604 OID 575479)
-- Dependencies: 218 217
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE institution ALTER COLUMN id SET DEFAULT nextval('institution_id_seq'::regclass);


--
-- TOC entry 2333 (class 2604 OID 575480)
-- Dependencies: 221 220
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE investigation_type ALTER COLUMN id SET DEFAULT nextval('investigation_type_id_seq'::regclass);


--
-- TOC entry 2335 (class 2604 OID 575481)
-- Dependencies: 226 225
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE material_keyword ALTER COLUMN id SET DEFAULT nextval('material_keyword_id_seq'::regclass);


--
-- TOC entry 2336 (class 2604 OID 575482)
-- Dependencies: 230 229
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE ontology_node ALTER COLUMN id SET DEFAULT nextval('ontology_node_id_seq'::regclass);


--
-- TOC entry 2337 (class 2604 OID 575483)
-- Dependencies: 233 232
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE other_keyword ALTER COLUMN id SET DEFAULT nextval('other_keyword_id_seq'::regclass);


--
-- TOC entry 2343 (class 2604 OID 575484)
-- Dependencies: 238 237
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE personal_filestore_ticket ALTER COLUMN id SET DEFAULT nextval('personal_filestore_ticket_id_seq'::regclass);


--
-- TOC entry 2344 (class 2604 OID 575485)
-- Dependencies: 242 241
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE related_comparative_collection ALTER COLUMN id SET DEFAULT nextval('related_comparative_collection_id_seq'::regclass);


--
-- TOC entry 2346 (class 2604 OID 575486)
-- Dependencies: 245 244
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE resource_access_statistics ALTER COLUMN id SET DEFAULT nextval('resource_access_statistics_id_seq'::regclass);


--
-- TOC entry 2347 (class 2604 OID 575487)
-- Dependencies: 247 246
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE resource_annotation ALTER COLUMN id SET DEFAULT nextval('resource_annotation_id_seq'::regclass);


--
-- TOC entry 2348 (class 2604 OID 575488)
-- Dependencies: 249 248
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE resource_annotation_key ALTER COLUMN id SET DEFAULT nextval('resource_annotation_key_id_seq'::regclass);


--
-- TOC entry 2349 (class 2604 OID 575489)
-- Dependencies: 251 250
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE resource_creator ALTER COLUMN id SET DEFAULT nextval('resource_creator_id_seq'::regclass);


--
-- TOC entry 2350 (class 2604 OID 575490)
-- Dependencies: 258 257
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE resource_note ALTER COLUMN id SET DEFAULT nextval('resource_note_id_seq'::regclass);


--
-- TOC entry 2365 (class 2604 OID 575491)
-- Dependencies: 268 267
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE sensory_data_image ALTER COLUMN id SET DEFAULT nextval('sensory_data_image_id_seq'::regclass);


--
-- TOC entry 2368 (class 2604 OID 575492)
-- Dependencies: 270 269
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE sensory_data_scan ALTER COLUMN id SET DEFAULT nextval('sensory_data_scan_id_seq'::regclass);


--
-- TOC entry 2369 (class 2604 OID 575493)
-- Dependencies: 272 271
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE site_name_keyword ALTER COLUMN id SET DEFAULT nextval('site_name_keyword_id_seq'::regclass);


--
-- TOC entry 2370 (class 2604 OID 575494)
-- Dependencies: 275 274
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE site_type_keyword ALTER COLUMN id SET DEFAULT nextval('site_type_keyword_id_seq'::regclass);


--
-- TOC entry 2371 (class 2604 OID 575495)
-- Dependencies: 278 277
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE source_collection ALTER COLUMN id SET DEFAULT nextval('source_collection_id_seq'::regclass);


--
-- TOC entry 2372 (class 2604 OID 575496)
-- Dependencies: 280 279
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE stats ALTER COLUMN id SET DEFAULT nextval('stats_id_seq'::regclass);


--
-- TOC entry 2373 (class 2604 OID 575497)
-- Dependencies: 282 281
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE temporal_keyword ALTER COLUMN id SET DEFAULT nextval('temporal_keyword_id_seq'::regclass);


--
-- TOC entry 2374 (class 2604 OID 575498)
-- Dependencies: 285 284
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE upgrade_task ALTER COLUMN id SET DEFAULT nextval('upgradetask_id_seq'::regclass);


--
-- TOC entry 2375 (class 2604 OID 575499)
-- Dependencies: 287 286
-- Name: id; Type: DEFAULT; Schema: public; Owner: tdar
--

ALTER TABLE user_session ALTER COLUMN id SET DEFAULT nextval('user_session_id_seq'::regclass);


--
-- TOC entry 2379 (class 2606 OID 603966)
-- Dependencies: 161 161
-- Name: authorized_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY authorized_user
    ADD CONSTRAINT authorized_user_pkey PRIMARY KEY (id);


--
-- TOC entry 2381 (class 2606 OID 603968)
-- Dependencies: 164 164
-- Name: bookmarked_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT bookmarked_resource_pkey PRIMARY KEY (id);


--
-- TOC entry 2383 (class 2606 OID 603970)
-- Dependencies: 165 165
-- Name: category_variable_new_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY category_variable
    ADD CONSTRAINT category_variable_new_pkey PRIMARY KEY (id);


--
-- TOC entry 2388 (class 2606 OID 603972)
-- Dependencies: 171 171
-- Name: coding_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coding_rule
    ADD CONSTRAINT coding_rule_pkey PRIMARY KEY (id);


--
-- TOC entry 2392 (class 2606 OID 603974)
-- Dependencies: 172 172
-- Name: coding_sheet_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT coding_sheet_pkey PRIMARY KEY (id);


--
-- TOC entry 2394 (class 2606 OID 603976)
-- Dependencies: 173 173
-- Name: collection_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY collection
    ADD CONSTRAINT collection_pkey PRIMARY KEY (id);


--
-- TOC entry 2396 (class 2606 OID 603978)
-- Dependencies: 175 175 175
-- Name: collection_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT collection_resource_pkey PRIMARY KEY (collection_id, resource_id);


--
-- TOC entry 2398 (class 2606 OID 603980)
-- Dependencies: 177 177
-- Name: contributor_request_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_pkey PRIMARY KEY (id);


--
-- TOC entry 2400 (class 2606 OID 603982)
-- Dependencies: 178 178
-- Name: coverage_date_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY coverage_date
    ADD CONSTRAINT coverage_date_pkey PRIMARY KEY (id);


--
-- TOC entry 2461 (class 2606 OID 603984)
-- Dependencies: 224 224
-- Name: coverage_longitude_latitude_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY latitude_longitude
    ADD CONSTRAINT coverage_longitude_latitude_pkey PRIMARY KEY (id);


--
-- TOC entry 2403 (class 2606 OID 603986)
-- Dependencies: 181 181
-- Name: creator_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY creator
    ADD CONSTRAINT creator_pkey PRIMARY KEY (id);


--
-- TOC entry 2407 (class 2606 OID 603988)
-- Dependencies: 183 183
-- Name: culture_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT culture_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2413 (class 2606 OID 603990)
-- Dependencies: 189 189
-- Name: data_table_column_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT data_table_column_pkey PRIMARY KEY (id);


--
-- TOC entry 2415 (class 2606 OID 603992)
-- Dependencies: 190 190
-- Name: data_table_column_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT data_table_column_relationship_pkey PRIMARY KEY (id);


--
-- TOC entry 2411 (class 2606 OID 603994)
-- Dependencies: 187 187
-- Name: data_table_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT data_table_pkey PRIMARY KEY (id);


--
-- TOC entry 2417 (class 2606 OID 603996)
-- Dependencies: 192 192
-- Name: data_table_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT data_table_relationship_pkey PRIMARY KEY (id);


--
-- TOC entry 2419 (class 2606 OID 603998)
-- Dependencies: 194 194
-- Name: data_value_ontology_node_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY data_value_ontology_node_mapping
    ADD CONSTRAINT data_value_ontology_node_mapping_pkey PRIMARY KEY (id);


--
-- TOC entry 2421 (class 2606 OID 604000)
-- Dependencies: 196 196
-- Name: dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (id);


--
-- TOC entry 2423 (class 2606 OID 604002)
-- Dependencies: 197 197
-- Name: document_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);


--
-- TOC entry 2428 (class 2606 OID 604004)
-- Dependencies: 199 199
-- Name: geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY geographic_keyword
    ADD CONSTRAINT geographic_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2432 (class 2606 OID 604006)
-- Dependencies: 202 202
-- Name: homepage_cache_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_geographic_keyword
    ADD CONSTRAINT homepage_cache_geographic_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2434 (class 2606 OID 604008)
-- Dependencies: 204 204
-- Name: homepage_cache_resource_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_resource_type
    ADD CONSTRAINT homepage_cache_resource_type_pkey PRIMARY KEY (id);


--
-- TOC entry 2436 (class 2606 OID 604010)
-- Dependencies: 204 204
-- Name: homepage_cache_resource_type_unique; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY homepage_cache_resource_type
    ADD CONSTRAINT homepage_cache_resource_type_unique UNIQUE (resource_type);


--
-- TOC entry 2438 (class 2606 OID 604012)
-- Dependencies: 206 206
-- Name: image_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);


--
-- TOC entry 2444 (class 2606 OID 604014)
-- Dependencies: 209 209
-- Name: information_resource_file_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file
    ADD CONSTRAINT information_resource_file_pkey PRIMARY KEY (id);


--
-- TOC entry 2446 (class 2606 OID 604016)
-- Dependencies: 213 213 213 213
-- Name: information_resource_file_vers_information_resource_file_id_key; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT information_resource_file_vers_information_resource_file_id_key UNIQUE (information_resource_file_id, file_version, internal_type);


--
-- TOC entry 2448 (class 2606 OID 604018)
-- Dependencies: 213 213
-- Name: information_resource_file_version_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT information_resource_file_version_pkey PRIMARY KEY (id);


--
-- TOC entry 2442 (class 2606 OID 604020)
-- Dependencies: 207 207
-- Name: information_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_pkey PRIMARY KEY (id);


--
-- TOC entry 2450 (class 2606 OID 604022)
-- Dependencies: 215 215 215
-- Name: information_resource_related_citation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citation_pkey PRIMARY KEY (information_resource_id, document_id);


--
-- TOC entry 2452 (class 2606 OID 604024)
-- Dependencies: 216 216 216
-- Name: information_resource_source_citation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citation_pkey PRIMARY KEY (information_resource_id, document_id);


--
-- TOC entry 2454 (class 2606 OID 604026)
-- Dependencies: 217 217
-- Name: institution_name_key; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_name_key UNIQUE (name);


--
-- TOC entry 2457 (class 2606 OID 604028)
-- Dependencies: 217 217
-- Name: institution_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_pkey PRIMARY KEY (id);


--
-- TOC entry 2459 (class 2606 OID 604030)
-- Dependencies: 220 220
-- Name: investigation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY investigation_type
    ADD CONSTRAINT investigation_type_pkey PRIMARY KEY (id);


--
-- TOC entry 2386 (class 2606 OID 604032)
-- Dependencies: 168 168
-- Name: master_ontology_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY category_variable_old
    ADD CONSTRAINT master_ontology_pkey PRIMARY KEY (id);


--
-- TOC entry 2464 (class 2606 OID 604034)
-- Dependencies: 225 225
-- Name: material_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY material_keyword
    ADD CONSTRAINT material_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2472 (class 2606 OID 604036)
-- Dependencies: 229 229
-- Name: ontology_node_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY ontology_node
    ADD CONSTRAINT ontology_node_pkey PRIMARY KEY (id);


--
-- TOC entry 2467 (class 2606 OID 604038)
-- Dependencies: 228 228
-- Name: ontology_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT ontology_pkey PRIMARY KEY (id);


--
-- TOC entry 2475 (class 2606 OID 604040)
-- Dependencies: 232 232
-- Name: other_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY other_keyword
    ADD CONSTRAINT other_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2483 (class 2606 OID 604042)
-- Dependencies: 236 236
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 2485 (class 2606 OID 604044)
-- Dependencies: 237 237
-- Name: personal_filestore_ticket_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY personal_filestore_ticket
    ADD CONSTRAINT personal_filestore_ticket_pkey PRIMARY KEY (id);


--
-- TOC entry 2562 (class 2606 OID 604046)
-- Dependencies: 279 279
-- Name: pk_stats; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY stats
    ADD CONSTRAINT pk_stats PRIMARY KEY (id);


--
-- TOC entry 2487 (class 2606 OID 604048)
-- Dependencies: 239 239
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- TOC entry 2498 (class 2606 OID 604050)
-- Dependencies: 248 248
-- Name: resource_annotation_key_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_annotation_key
    ADD CONSTRAINT resource_annotation_key_pkey PRIMARY KEY (id);


--
-- TOC entry 2495 (class 2606 OID 604052)
-- Dependencies: 246 246
-- Name: resource_annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT resource_annotation_pkey PRIMARY KEY (id);


--
-- TOC entry 2502 (class 2606 OID 604054)
-- Dependencies: 250 250
-- Name: resource_creator_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT resource_creator_pkey PRIMARY KEY (id);


--
-- TOC entry 2506 (class 2606 OID 604056)
-- Dependencies: 252 252 252
-- Name: resource_culture_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_pkey PRIMARY KEY (resource_id, culture_keyword_id);


--
-- TOC entry 2509 (class 2606 OID 604058)
-- Dependencies: 253 253 253
-- Name: resource_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_pkey PRIMARY KEY (resource_id, geographic_keyword_id);


--
-- TOC entry 2513 (class 2606 OID 604060)
-- Dependencies: 254 254 254
-- Name: resource_investigation_type_pkey1; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_pkey1 PRIMARY KEY (resource_id, investigation_type_id);


--
-- TOC entry 2517 (class 2606 OID 604062)
-- Dependencies: 255 255 255
-- Name: resource_managed_geographic_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_pkey PRIMARY KEY (resource_id, geographic_keyword_id);


--
-- TOC entry 2520 (class 2606 OID 604064)
-- Dependencies: 256 256 256
-- Name: resource_material_keyword_pkey1; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_pkey1 PRIMARY KEY (resource_id, material_keyword_id);


--
-- TOC entry 2524 (class 2606 OID 604066)
-- Dependencies: 257 257
-- Name: resource_note_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_note
    ADD CONSTRAINT resource_note_pkey PRIMARY KEY (id);


--
-- TOC entry 2527 (class 2606 OID 604068)
-- Dependencies: 259 259 259
-- Name: resource_other_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_pkey PRIMARY KEY (resource_id, other_keyword_id);


--
-- TOC entry 2491 (class 2606 OID 604070)
-- Dependencies: 243 243
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);


--
-- TOC entry 2530 (class 2606 OID 604072)
-- Dependencies: 261 261
-- Name: resource_revised_date_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_revision_log
    ADD CONSTRAINT resource_revised_date_pkey PRIMARY KEY (id);


--
-- TOC entry 2533 (class 2606 OID 604074)
-- Dependencies: 263 263 263
-- Name: resource_site_name_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_pkey PRIMARY KEY (resource_id, site_name_keyword_id);


--
-- TOC entry 2537 (class 2606 OID 604076)
-- Dependencies: 264 264 264
-- Name: resource_site_type_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_pkey PRIMARY KEY (resource_id, site_type_keyword_id);


--
-- TOC entry 2541 (class 2606 OID 604078)
-- Dependencies: 265 265 265
-- Name: resource_temporal_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_pkey PRIMARY KEY (resource_id, temporal_keyword_id);


--
-- TOC entry 2547 (class 2606 OID 604080)
-- Dependencies: 267 267
-- Name: sensory_data_image_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data_image
    ADD CONSTRAINT sensory_data_image_pkey PRIMARY KEY (id);


--
-- TOC entry 2544 (class 2606 OID 604082)
-- Dependencies: 266 266
-- Name: sensory_data_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data
    ADD CONSTRAINT sensory_data_pkey PRIMARY KEY (id);


--
-- TOC entry 2550 (class 2606 OID 604084)
-- Dependencies: 269 269
-- Name: sensory_data_scan_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY sensory_data_scan
    ADD CONSTRAINT sensory_data_scan_pkey PRIMARY KEY (id);


--
-- TOC entry 2553 (class 2606 OID 604086)
-- Dependencies: 271 271
-- Name: site_name_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_name_keyword
    ADD CONSTRAINT site_name_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2559 (class 2606 OID 604088)
-- Dependencies: 274 274
-- Name: site_type_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_type_keyword
    ADD CONSTRAINT site_type_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2564 (class 2606 OID 604090)
-- Dependencies: 281 281
-- Name: temporal_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY temporal_keyword
    ADD CONSTRAINT temporal_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2409 (class 2606 OID 604092)
-- Dependencies: 183 183
-- Name: unique_culture_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT unique_culture_keyword UNIQUE (label);


--
-- TOC entry 2430 (class 2606 OID 604094)
-- Dependencies: 199 199
-- Name: unique_geographic_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY geographic_keyword
    ADD CONSTRAINT unique_geographic_keyword UNIQUE (label);


--
-- TOC entry 2477 (class 2606 OID 604096)
-- Dependencies: 232 232
-- Name: unique_other_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY other_keyword
    ADD CONSTRAINT unique_other_keyword UNIQUE (label);


--
-- TOC entry 2555 (class 2606 OID 604098)
-- Dependencies: 271 271
-- Name: unique_site_name_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY site_name_keyword
    ADD CONSTRAINT unique_site_name_keyword UNIQUE (label);


--
-- TOC entry 2567 (class 2606 OID 604100)
-- Dependencies: 281 281
-- Name: unique_temporal_keyword; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY temporal_keyword
    ADD CONSTRAINT unique_temporal_keyword UNIQUE (label);


--
-- TOC entry 2569 (class 2606 OID 604102)
-- Dependencies: 284 284
-- Name: upgradetask_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY upgrade_task
    ADD CONSTRAINT upgradetask_pkey PRIMARY KEY (id);


--
-- TOC entry 2571 (class 2606 OID 604104)
-- Dependencies: 286 286
-- Name: user_session_pkey; Type: CONSTRAINT; Schema: public; Owner: tdar; Tablespace: 
--

ALTER TABLE ONLY user_session
    ADD CONSTRAINT user_session_pkey PRIMARY KEY (id);


--
-- TOC entry 2376 (class 1259 OID 604105)
-- Dependencies: 161 161
-- Name: authorized_user_cid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX authorized_user_cid ON authorized_user USING btree (id, resource_collection_id);


--
-- TOC entry 2377 (class 1259 OID 604106)
-- Dependencies: 161 161 161
-- Name: authorized_user_perm; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX authorized_user_perm ON authorized_user USING btree (general_permission_int, user_id, resource_collection_id);


--
-- TOC entry 2404 (class 1259 OID 604107)
-- Dependencies: 183 183
-- Name: cltkwd_appr; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX cltkwd_appr ON culture_keyword USING btree (approved, id);


--
-- TOC entry 2390 (class 1259 OID 604108)
-- Dependencies: 172
-- Name: coding_catvar_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coding_catvar_id ON coding_sheet USING btree (category_variable_id);


--
-- TOC entry 2389 (class 1259 OID 604109)
-- Dependencies: 171
-- Name: coding_rule_term_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coding_rule_term_index ON coding_rule USING btree (term);


--
-- TOC entry 2401 (class 1259 OID 604110)
-- Dependencies: 178 178
-- Name: coverage_resid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX coverage_resid ON coverage_date USING btree (resource_id, id);


--
-- TOC entry 2499 (class 1259 OID 604111)
-- Dependencies: 250 250 250
-- Name: creator_sequence; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX creator_sequence ON resource_creator USING btree (resource_id, sequence_number, creator_id);


--
-- TOC entry 2405 (class 1259 OID 604112)
-- Dependencies: 183 183
-- Name: culture_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX culture_keyword_label_lc ON culture_keyword USING btree (lower((label)::text));


--
-- TOC entry 2424 (class 1259 OID 604113)
-- Dependencies: 197
-- Name: document_type_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX document_type_index ON document USING btree (document_type);


--
-- TOC entry 2478 (class 1259 OID 604114)
-- Dependencies: 236
-- Name: email_unique; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE UNIQUE INDEX email_unique ON person USING btree (email);


--
-- TOC entry 2384 (class 1259 OID 604115)
-- Dependencies: 168
-- Name: encoded_parent_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX encoded_parent_index ON category_variable_old USING btree (encoded_parent_ids);


--
-- TOC entry 2425 (class 1259 OID 604116)
-- Dependencies: 199 199 199
-- Name: geog_label_level_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX geog_label_level_id ON geographic_keyword USING btree (level, label, id);


--
-- TOC entry 2426 (class 1259 OID 604117)
-- Dependencies: 199 199
-- Name: geographic_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX geographic_keyword_label_lc ON geographic_keyword USING btree (lower((label)::text));


--
-- TOC entry 2439 (class 1259 OID 604118)
-- Dependencies: 207 207
-- Name: infores_projid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX infores_projid ON information_resource USING btree (project_id, id);


--
-- TOC entry 2440 (class 1259 OID 604119)
-- Dependencies: 207
-- Name: infores_provid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX infores_provid ON information_resource USING btree (provider_institution_id);


--
-- TOC entry 2455 (class 1259 OID 604120)
-- Dependencies: 217 217
-- Name: institution_name_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX institution_name_lc ON institution USING btree (lower((name)::text), id);


--
-- TOC entry 2515 (class 1259 OID 604121)
-- Dependencies: 255 255
-- Name: mgd_geogr_res; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX mgd_geogr_res ON resource_managed_geographic_keyword USING btree (resource_id, geographic_keyword_id);


--
-- TOC entry 2465 (class 1259 OID 604122)
-- Dependencies: 228
-- Name: ontology_catvar_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_catvar_id ON ontology USING btree (category_variable_id);


--
-- TOC entry 2468 (class 1259 OID 604123)
-- Dependencies: 229
-- Name: ontology_node_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_index ON ontology_node USING btree (index);


--
-- TOC entry 2469 (class 1259 OID 604124)
-- Dependencies: 229
-- Name: ontology_node_interval_end_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_interval_end_index ON ontology_node USING btree (interval_end);


--
-- TOC entry 2470 (class 1259 OID 604125)
-- Dependencies: 229
-- Name: ontology_node_interval_start_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX ontology_node_interval_start_index ON ontology_node USING btree (interval_start);


--
-- TOC entry 2473 (class 1259 OID 604126)
-- Dependencies: 232 232
-- Name: other_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX other_keyword_label_lc ON other_keyword USING btree (lower((label)::text));


--
-- TOC entry 2479 (class 1259 OID 604127)
-- Dependencies: 236
-- Name: person_email_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_email_index ON person USING btree (email);


--
-- TOC entry 2480 (class 1259 OID 604128)
-- Dependencies: 236 236
-- Name: person_instid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_instid ON person USING btree (id, institution_id);


--
-- TOC entry 2481 (class 1259 OID 604129)
-- Dependencies: 236 236 236
-- Name: person_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX person_lc ON person USING btree (lower((first_name)::text), lower((last_name)::text), id);


--
-- TOC entry 2503 (class 1259 OID 604130)
-- Dependencies: 252
-- Name: rck_culture_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rck_culture_keyword_id ON resource_culture_keyword USING btree (culture_keyword_id);


--
-- TOC entry 2488 (class 1259 OID 604131)
-- Dependencies: 243
-- Name: res_submitterid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX res_submitterid ON resource USING btree (submitter_id);


--
-- TOC entry 2489 (class 1259 OID 604132)
-- Dependencies: 243
-- Name: res_updaterid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX res_updaterid ON resource USING btree (updater_id);


--
-- TOC entry 2500 (class 1259 OID 604133)
-- Dependencies: 250
-- Name: rescreator_resid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rescreator_resid ON resource_creator USING btree (resource_id);


--
-- TOC entry 2504 (class 1259 OID 604134)
-- Dependencies: 252 252
-- Name: resid_cultkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_cultkwdid ON resource_culture_keyword USING btree (resource_id, culture_keyword_id);


--
-- TOC entry 2507 (class 1259 OID 604135)
-- Dependencies: 253 253
-- Name: resid_geogkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_geogkwdid ON resource_geographic_keyword USING btree (resource_id, geographic_keyword_id);


--
-- TOC entry 2511 (class 1259 OID 604136)
-- Dependencies: 254 254
-- Name: resid_invtypeid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_invtypeid ON resource_investigation_type USING btree (resource_id, investigation_type_id);


--
-- TOC entry 2518 (class 1259 OID 604137)
-- Dependencies: 256 256
-- Name: resid_matkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_matkwdid ON resource_material_keyword USING btree (resource_id, material_keyword_id);


--
-- TOC entry 2522 (class 1259 OID 604138)
-- Dependencies: 257 257
-- Name: resid_noteid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_noteid ON resource_note USING btree (resource_id, id);


--
-- TOC entry 2525 (class 1259 OID 604139)
-- Dependencies: 259 259
-- Name: resid_otherkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_otherkwdid ON resource_other_keyword USING btree (resource_id, other_keyword_id);


--
-- TOC entry 2545 (class 1259 OID 604140)
-- Dependencies: 267 267
-- Name: resid_sesory_data_img; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sesory_data_img ON sensory_data_image USING btree (sensory_data_id, id);


--
-- TOC entry 2548 (class 1259 OID 604141)
-- Dependencies: 269 269
-- Name: resid_sesory_data_scan; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sesory_data_scan ON sensory_data_scan USING btree (sensory_data_id, id);


--
-- TOC entry 2531 (class 1259 OID 604142)
-- Dependencies: 263 263
-- Name: resid_sitenamekwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sitenamekwdid ON resource_site_name_keyword USING btree (resource_id, site_name_keyword_id);


--
-- TOC entry 2535 (class 1259 OID 604143)
-- Dependencies: 264 264
-- Name: resid_sitetypekwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_sitetypekwdid ON resource_site_type_keyword USING btree (resource_id, site_type_keyword_id);


--
-- TOC entry 2539 (class 1259 OID 604144)
-- Dependencies: 265 265
-- Name: resid_temporalkwdid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resid_temporalkwdid ON resource_temporal_keyword USING btree (resource_id, temporal_keyword_id);


--
-- TOC entry 2496 (class 1259 OID 604145)
-- Dependencies: 246 246 246
-- Name: resource_id_keyid; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_id_keyid ON resource_annotation USING btree (resource_id, id, resourceannotationkey_id);


--
-- TOC entry 2462 (class 1259 OID 604146)
-- Dependencies: 224 224
-- Name: resource_latlong; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_latlong ON latitude_longitude USING btree (resource_id, id);


--
-- TOC entry 2492 (class 1259 OID 604147)
-- Dependencies: 243
-- Name: resource_title_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_title_index ON resource USING btree (title);


--
-- TOC entry 2493 (class 1259 OID 604148)
-- Dependencies: 243
-- Name: resource_type_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX resource_type_index ON resource USING btree (resource_type);


--
-- TOC entry 2510 (class 1259 OID 604149)
-- Dependencies: 253
-- Name: rgk_geographic_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rgk_geographic_keyword_id ON resource_geographic_keyword USING btree (geographic_keyword_id);


--
-- TOC entry 2514 (class 1259 OID 604150)
-- Dependencies: 254
-- Name: rit_investigation_type_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rit_investigation_type_id ON resource_investigation_type USING btree (investigation_type_id);


--
-- TOC entry 2521 (class 1259 OID 604151)
-- Dependencies: 256
-- Name: rmk_material_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rmk_material_keyword_id ON resource_material_keyword USING btree (material_keyword_id);


--
-- TOC entry 2528 (class 1259 OID 604152)
-- Dependencies: 259
-- Name: rok_other_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rok_other_keyword_id ON resource_other_keyword USING btree (other_keyword_id);


--
-- TOC entry 2534 (class 1259 OID 604153)
-- Dependencies: 263
-- Name: rsnk_site_name_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rsnk_site_name_keyword_id ON resource_site_name_keyword USING btree (site_name_keyword_id);


--
-- TOC entry 2538 (class 1259 OID 604154)
-- Dependencies: 264
-- Name: rstk_site_type_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rstk_site_type_keyword_id ON resource_site_type_keyword USING btree (site_type_keyword_id);


--
-- TOC entry 2542 (class 1259 OID 604155)
-- Dependencies: 265
-- Name: rtk_temporal_keyword_id; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX rtk_temporal_keyword_id ON resource_temporal_keyword USING btree (temporal_keyword_id);


--
-- TOC entry 2551 (class 1259 OID 604156)
-- Dependencies: 271 271
-- Name: site_name_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_name_keyword_label_lc ON site_name_keyword USING btree (lower((label)::text));


--
-- TOC entry 2556 (class 1259 OID 604157)
-- Dependencies: 274
-- Name: site_type_keyword_index; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_type_keyword_index ON site_type_keyword USING btree (label);


--
-- TOC entry 2557 (class 1259 OID 604158)
-- Dependencies: 274 274
-- Name: site_type_keyword_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX site_type_keyword_label_lc ON site_type_keyword USING btree (lower((label)::text));


--
-- TOC entry 2560 (class 1259 OID 604159)
-- Dependencies: 274 274
-- Name: sitetype_appr; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX sitetype_appr ON site_type_keyword USING btree (approved, id);


--
-- TOC entry 2565 (class 1259 OID 604160)
-- Dependencies: 281 281
-- Name: temporal_label_lc; Type: INDEX; Schema: public; Owner: tdar; Tablespace: 
--

CREATE INDEX temporal_label_lc ON temporal_keyword USING btree (lower((label)::text));


--
-- TOC entry 2575 (class 2606 OID 604161)
-- Dependencies: 165 2382 165
-- Name: category_variable_new_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable
    ADD CONSTRAINT category_variable_new_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES category_variable(id);


--
-- TOC entry 2579 (class 2606 OID 604166)
-- Dependencies: 2441 207 172
-- Name: coding_sheet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT coding_sheet_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- TOC entry 2583 (class 2606 OID 604171)
-- Dependencies: 177 2482 236
-- Name: contributor_request_applicant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- TOC entry 2584 (class 2606 OID 604176)
-- Dependencies: 236 177 2482
-- Name: contributor_request_approver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY contributor_request
    ADD CONSTRAINT contributor_request_approver_id_fkey FOREIGN KEY (approver_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- TOC entry 2587 (class 2606 OID 604181)
-- Dependencies: 183 183 2406
-- Name: culture_keyword_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY culture_keyword
    ADD CONSTRAINT culture_keyword_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES culture_keyword(id);


--
-- TOC entry 2592 (class 2606 OID 604186)
-- Dependencies: 2391 189 172
-- Name: data_table_column_default_coding_sheet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT data_table_column_default_coding_sheet_id_fkey FOREIGN KEY (default_coding_sheet_id) REFERENCES coding_sheet(id);


--
-- TOC entry 2602 (class 2606 OID 604191)
-- Dependencies: 194 189 2412
-- Name: data_value_ontology_node_mapping_data_table_column_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_value_ontology_node_mapping
    ADD CONSTRAINT data_value_ontology_node_mapping_data_table_column_id_fkey FOREIGN KEY (data_table_column_id) REFERENCES data_table_column(id);


--
-- TOC entry 2603 (class 2606 OID 604196)
-- Dependencies: 194 229 2471
-- Name: data_value_ontology_node_mapping_ontology_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_value_ontology_node_mapping
    ADD CONSTRAINT data_value_ontology_node_mapping_ontology_node_id_fkey FOREIGN KEY (ontology_node_id) REFERENCES ontology_node(id);


--
-- TOC entry 2604 (class 2606 OID 604201)
-- Dependencies: 207 2441 196
-- Name: dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- TOC entry 2605 (class 2606 OID 604206)
-- Dependencies: 207 2441 197
-- Name: document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2649 (class 2606 OID 604211)
-- Dependencies: 257 243 2490
-- Name: fk11beb35032793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_note
    ADD CONSTRAINT fk11beb35032793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2612 (class 2606 OID 604216)
-- Dependencies: 2443 213 209
-- Name: fk276ff6d3ff692808; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_file_version
    ADD CONSTRAINT fk276ff6d3ff692808 FOREIGN KEY (information_resource_file_id) REFERENCES information_resource_file(id);


--
-- TOC entry 2611 (class 2606 OID 604221)
-- Dependencies: 207 209 2441
-- Name: fk2bc70d3a7b2d0e85; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_file
    ADD CONSTRAINT fk2bc70d3a7b2d0e85 FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- TOC entry 2660 (class 2606 OID 604226)
-- Dependencies: 271 273 2552
-- Name: fk2dbbbfe9a52c1e3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_name_keyword_synonym
    ADD CONSTRAINT fk2dbbbfe9a52c1e3 FOREIGN KEY (sitenamekeyword_id) REFERENCES site_name_keyword(id);


--
-- TOC entry 2662 (class 2606 OID 604231)
-- Dependencies: 274 276 2558
-- Name: fk31359698e9ef2043; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_type_keyword_synonym
    ADD CONSTRAINT fk31359698e9ef2043 FOREIGN KEY (sitetypekeyword_id) REFERENCES site_type_keyword(id);


--
-- TOC entry 2659 (class 2606 OID 604236)
-- Dependencies: 266 207 2441
-- Name: fk32612bea51d71f47; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY sensory_data
    ADD CONSTRAINT fk32612bea51d71f47 FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- TOC entry 2599 (class 2606 OID 604241)
-- Dependencies: 196 192 2420
-- Name: fk344b15be19e50a8c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15be19e50a8c FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- TOC entry 2600 (class 2606 OID 604246)
-- Dependencies: 2410 192 187
-- Name: fk344b15be80a27383; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15be80a27383 FOREIGN KEY (localtable_id) REFERENCES data_table(id);


--
-- TOC entry 2601 (class 2606 OID 604251)
-- Dependencies: 192 187 2410
-- Name: fk344b15bee6f89bec; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_relationship
    ADD CONSTRAINT fk344b15bee6f89bec FOREIGN KEY (foreigntable_id) REFERENCES data_table(id);


--
-- TOC entry 2620 (class 2606 OID 604256)
-- Dependencies: 2458 220 222
-- Name: fk5511d21363e4a1e3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY investigation_type_synonym
    ADD CONSTRAINT fk5511d21363e4a1e3 FOREIGN KEY (investigationtype_id) REFERENCES investigation_type(id);


--
-- TOC entry 2637 (class 2606 OID 604261)
-- Dependencies: 243 2490 250
-- Name: fk5b43fcfb32793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT fk5b43fcfb32793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2638 (class 2606 OID 604266)
-- Dependencies: 250 181 2402
-- Name: fk5b43fcfb67ffc561; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_creator
    ADD CONSTRAINT fk5b43fcfb67ffc561 FOREIGN KEY (creator_id) REFERENCES creator(id);


--
-- TOC entry 2607 (class 2606 OID 604271)
-- Dependencies: 207 206 2441
-- Name: fk5faa95b51d71f47; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY image
    ADD CONSTRAINT fk5faa95b51d71f47 FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- TOC entry 2589 (class 2606 OID 604276)
-- Dependencies: 196 187 2420
-- Name: fk608fa6f919e50a8c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT fk608fa6f919e50a8c FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- TOC entry 2590 (class 2606 OID 604281)
-- Dependencies: 243 187 2490
-- Name: fk608fa6f96ac97cbe; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT fk608fa6f96ac97cbe FOREIGN KEY (dataset_id) REFERENCES resource(id);


--
-- TOC entry 2622 (class 2606 OID 604286)
-- Dependencies: 225 227 2463
-- Name: fk7175948de769ea23; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY material_keyword_synonym
    ADD CONSTRAINT fk7175948de769ea23 FOREIGN KEY (materialkeyword_id) REFERENCES material_keyword(id);


--
-- TOC entry 2581 (class 2606 OID 604291)
-- Dependencies: 173 175 2393
-- Name: fk74b2d88f53679086; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT fk74b2d88f53679086 FOREIGN KEY (collection_id) REFERENCES collection(id);


--
-- TOC entry 2582 (class 2606 OID 604296)
-- Dependencies: 243 175 2490
-- Name: fk74b2d88fd20877f1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY collection_resource
    ADD CONSTRAINT fk74b2d88fd20877f1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2578 (class 2606 OID 604301)
-- Dependencies: 2391 172 171
-- Name: fk7680deb1eab2d817; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_rule
    ADD CONSTRAINT fk7680deb1eab2d817 FOREIGN KEY (coding_sheet_id) REFERENCES coding_sheet(id);


--
-- TOC entry 2606 (class 2606 OID 604306)
-- Dependencies: 2427 201 199
-- Name: fk7e5a05dd644f9de3; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY geographic_keyword_synonym
    ADD CONSTRAINT fk7e5a05dd644f9de3 FOREIGN KEY (geographickeyword_id) REFERENCES geographic_keyword(id);


--
-- TOC entry 2630 (class 2606 OID 604311)
-- Dependencies: 2482 237 236
-- Name: fk7f736747fc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY personal_filestore_ticket
    ADD CONSTRAINT fk7f736747fc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- TOC entry 2627 (class 2606 OID 604316)
-- Dependencies: 232 234 2474
-- Name: fk893dbc76521c07d1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY other_keyword_synonym
    ADD CONSTRAINT fk893dbc76521c07d1 FOREIGN KEY (otherkeyword_id) REFERENCES other_keyword(id);


--
-- TOC entry 2621 (class 2606 OID 604321)
-- Dependencies: 243 2490 224
-- Name: fk8c6540b332793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY latitude_longitude
    ADD CONSTRAINT fk8c6540b332793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2576 (class 2606 OID 604326)
-- Dependencies: 168 2385 168
-- Name: fk96f59e8c2dd1b509; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable_old
    ADD CONSTRAINT fk96f59e8c2dd1b509 FOREIGN KEY (parent_id) REFERENCES category_variable_old(id);


--
-- TOC entry 2573 (class 2606 OID 604331)
-- Dependencies: 2490 243 164
-- Name: fk9923c8b832793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT fk9923c8b832793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2574 (class 2606 OID 604336)
-- Dependencies: 2482 164 236
-- Name: fk9923c8b8bca96193; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY bookmarked_resource
    ADD CONSTRAINT fk9923c8b8bca96193 FOREIGN KEY (person_id) REFERENCES person(id);


--
-- TOC entry 2626 (class 2606 OID 604341)
-- Dependencies: 231 229 2471
-- Name: fk99dda7aed6698fa8; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology_node_synonym
    ADD CONSTRAINT fk99dda7aed6698fa8 FOREIGN KEY (ontologynode_id) REFERENCES ontology_node(id);


--
-- TOC entry 2619 (class 2606 OID 604346)
-- Dependencies: 219 217 2456
-- Name: fk99dda7aed6698fa9; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution_synonym
    ADD CONSTRAINT fk99dda7aed6698fa9 FOREIGN KEY (institution_id) REFERENCES institution(id);


--
-- TOC entry 2636 (class 2606 OID 604351)
-- Dependencies: 248 2482 236
-- Name: fk9b5fd5a0fc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation_key
    ADD CONSTRAINT fk9b5fd5a0fc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- TOC entry 2577 (class 2606 OID 604356)
-- Dependencies: 2382 169 165
-- Name: fk_category_variable_synonyms__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY category_variable_synonyms
    ADD CONSTRAINT fk_category_variable_synonyms__category_variable FOREIGN KEY (categoryvariable_id) REFERENCES category_variable(id);


--
-- TOC entry 2580 (class 2606 OID 604361)
-- Dependencies: 172 165 2382
-- Name: fk_coding_sheet__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coding_sheet
    ADD CONSTRAINT fk_coding_sheet__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- TOC entry 2591 (class 2606 OID 604366)
-- Dependencies: 165 2382 187
-- Name: fk_data_table__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table
    ADD CONSTRAINT fk_data_table__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- TOC entry 2593 (class 2606 OID 604371)
-- Dependencies: 189 2382 165
-- Name: fk_data_table_column__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fk_data_table_column__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- TOC entry 2596 (class 2606 OID 604376)
-- Dependencies: 190 2412 189
-- Name: fk_data_table_column_relationship_foreign_column; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_foreign_column FOREIGN KEY (foreign_column_id) REFERENCES data_table_column(id);


--
-- TOC entry 2597 (class 2606 OID 604381)
-- Dependencies: 190 2412 189
-- Name: fk_data_table_column_relationship_local_column; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_local_column FOREIGN KEY (local_column_id) REFERENCES data_table_column(id);


--
-- TOC entry 2598 (class 2606 OID 604386)
-- Dependencies: 190 2416 192
-- Name: fk_data_table_column_relationship_relationship; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column_relationship
    ADD CONSTRAINT fk_data_table_column_relationship_relationship FOREIGN KEY (relationship_id) REFERENCES data_table_relationship(id);


--
-- TOC entry 2623 (class 2606 OID 604391)
-- Dependencies: 165 2382 228
-- Name: fk_ontology__category_variable; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT fk_ontology__category_variable FOREIGN KEY (category_variable_id) REFERENCES category_variable(id);


--
-- TOC entry 2572 (class 2606 OID 604396)
-- Dependencies: 173 2393 161
-- Name: fkb234f7ef1e1b5338; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY authorized_user
    ADD CONSTRAINT fkb234f7ef1e1b5338 FOREIGN KEY (resource_collection_id) REFERENCES collection(id);


--
-- TOC entry 2664 (class 2606 OID 604401)
-- Dependencies: 2563 281 283
-- Name: fkdf4940889bebcc03; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY temporal_keyword_synonym
    ADD CONSTRAINT fkdf4940889bebcc03 FOREIGN KEY (temporalkeyword_id) REFERENCES temporal_keyword(id);


--
-- TOC entry 2588 (class 2606 OID 604406)
-- Dependencies: 2406 185 183
-- Name: fke42e97145ff8f2d1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY culture_keyword_synonym
    ADD CONSTRAINT fke42e97145ff8f2d1 FOREIGN KEY (culturekeyword_id) REFERENCES culture_keyword(id);


--
-- TOC entry 2594 (class 2606 OID 604411)
-- Dependencies: 2466 189 228
-- Name: fke5d0f5c2d0884ca; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fke5d0f5c2d0884ca FOREIGN KEY (default_ontology_id) REFERENCES ontology(id);


--
-- TOC entry 2595 (class 2606 OID 604416)
-- Dependencies: 189 187 2410
-- Name: fke5d0f5c8072b1b7; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY data_table_column
    ADD CONSTRAINT fke5d0f5c8072b1b7 FOREIGN KEY (data_table_id) REFERENCES data_table(id);


--
-- TOC entry 2586 (class 2606 OID 604421)
-- Dependencies: 2402 182 181
-- Name: fke74a76e867ffc561; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY creator_synonym
    ADD CONSTRAINT fke74a76e867ffc561 FOREIGN KEY (creator_id) REFERENCES creator(id);


--
-- TOC entry 2633 (class 2606 OID 604426)
-- Dependencies: 236 243 2482
-- Name: fkebabc40efc7475f; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT fkebabc40efc7475f FOREIGN KEY (submitter_id) REFERENCES person(id);


--
-- TOC entry 2631 (class 2606 OID 604431)
-- Dependencies: 2490 243 239
-- Name: fked904b19e8e3bf97; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fked904b19e8e3bf97 FOREIGN KEY (id) REFERENCES resource(id);


--
-- TOC entry 2585 (class 2606 OID 604436)
-- Dependencies: 178 2490 243
-- Name: fkf246a3a532793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY coverage_date
    ADD CONSTRAINT fkf246a3a532793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2634 (class 2606 OID 604441)
-- Dependencies: 243 246 2490
-- Name: fkfdf7080032793d68; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT fkfdf7080032793d68 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2635 (class 2606 OID 604446)
-- Dependencies: 2497 246 248
-- Name: fkfdf70800a279d68c; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_annotation
    ADD CONSTRAINT fkfdf70800a279d68c FOREIGN KEY (resourceannotationkey_id) REFERENCES resource_annotation_key(id);


--
-- TOC entry 2608 (class 2606 OID 604451)
-- Dependencies: 2490 207 243
-- Name: information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_id_fkey FOREIGN KEY (id) REFERENCES resource(id);


--
-- TOC entry 2609 (class 2606 OID 604456)
-- Dependencies: 2456 217 207
-- Name: information_resource_institution_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_institution_fk FOREIGN KEY (provider_institution_id) REFERENCES institution(id);


--
-- TOC entry 2610 (class 2606 OID 604461)
-- Dependencies: 239 207 2486
-- Name: information_resource_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource
    ADD CONSTRAINT information_resource_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(id);


--
-- TOC entry 2613 (class 2606 OID 604466)
-- Dependencies: 2441 215 207
-- Name: information_resource_related_citat_information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citat_information_resource_id_fkey FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- TOC entry 2614 (class 2606 OID 604471)
-- Dependencies: 2422 215 197
-- Name: information_resource_related_citation_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_related_citation
    ADD CONSTRAINT information_resource_related_citation_document_id_fkey FOREIGN KEY (document_id) REFERENCES document(id);


--
-- TOC entry 2615 (class 2606 OID 604476)
-- Dependencies: 216 207 2441
-- Name: information_resource_source_citati_information_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citati_information_resource_id_fkey FOREIGN KEY (information_resource_id) REFERENCES information_resource(id);


--
-- TOC entry 2616 (class 2606 OID 604481)
-- Dependencies: 197 216 2422
-- Name: information_resource_source_citation_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY information_resource_source_citation
    ADD CONSTRAINT information_resource_source_citation_document_id_fkey FOREIGN KEY (document_id) REFERENCES document(id);


--
-- TOC entry 2617 (class 2606 OID 604486)
-- Dependencies: 217 181 2402
-- Name: institution_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_creator_fk FOREIGN KEY (id) REFERENCES creator(id);


--
-- TOC entry 2618 (class 2606 OID 604491)
-- Dependencies: 217 217 2456
-- Name: institution_parentinstitution_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY institution
    ADD CONSTRAINT institution_parentinstitution_id_fkey FOREIGN KEY (parentinstitution_id) REFERENCES institution(id);


--
-- TOC entry 2624 (class 2606 OID 604496)
-- Dependencies: 2441 228 207
-- Name: ontology_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT ontology_id_fkey FOREIGN KEY (id) REFERENCES information_resource(id);


--
-- TOC entry 2625 (class 2606 OID 604501)
-- Dependencies: 229 228 2466
-- Name: ontology_node_ontology_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY ontology_node
    ADD CONSTRAINT ontology_node_ontology_id_fkey FOREIGN KEY (ontology_id) REFERENCES ontology(id);


--
-- TOC entry 2628 (class 2606 OID 604506)
-- Dependencies: 2402 236 181
-- Name: person_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_creator_fk FOREIGN KEY (id) REFERENCES creator(id);


--
-- TOC entry 2629 (class 2606 OID 604511)
-- Dependencies: 217 236 2456
-- Name: person_institution_fk; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_institution_fk FOREIGN KEY (institution_id) REFERENCES institution(id);


--
-- TOC entry 2632 (class 2606 OID 604516)
-- Dependencies: 243 241 2490
-- Name: related_comparative_collection_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY related_comparative_collection
    ADD CONSTRAINT related_comparative_collection_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2639 (class 2606 OID 604521)
-- Dependencies: 183 252 2406
-- Name: resource_culture_keyword_culture_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_culture_keyword_id_fkey FOREIGN KEY (culture_keyword_id) REFERENCES culture_keyword(id);


--
-- TOC entry 2640 (class 2606 OID 604526)
-- Dependencies: 2490 243 252
-- Name: resource_culture_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_culture_keyword
    ADD CONSTRAINT resource_culture_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2641 (class 2606 OID 604531)
-- Dependencies: 2427 253 199
-- Name: resource_geographic_keyword_geographic_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_geographic_keyword_id_fkey FOREIGN KEY (geographic_keyword_id) REFERENCES geographic_keyword(id);


--
-- TOC entry 2642 (class 2606 OID 604536)
-- Dependencies: 2490 253 243
-- Name: resource_geographic_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_geographic_keyword
    ADD CONSTRAINT resource_geographic_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2643 (class 2606 OID 604541)
-- Dependencies: 220 254 2458
-- Name: resource_investigation_type_investigation_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_investigation_type_id_fkey FOREIGN KEY (investigation_type_id) REFERENCES investigation_type(id);


--
-- TOC entry 2644 (class 2606 OID 604546)
-- Dependencies: 2490 243 254
-- Name: resource_investigation_type_resource_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_investigation_type
    ADD CONSTRAINT resource_investigation_type_resource_id_fkey1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2645 (class 2606 OID 604551)
-- Dependencies: 199 255 2427
-- Name: resource_managed_geographic_keyword_geographic_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_geographic_keyword_id_fkey FOREIGN KEY (geographic_keyword_id) REFERENCES geographic_keyword(id);


--
-- TOC entry 2646 (class 2606 OID 604556)
-- Dependencies: 2490 255 243
-- Name: resource_managed_geographic_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_managed_geographic_keyword
    ADD CONSTRAINT resource_managed_geographic_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2647 (class 2606 OID 604561)
-- Dependencies: 256 2463 225
-- Name: resource_material_keyword_material_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_material_keyword_id_fkey FOREIGN KEY (material_keyword_id) REFERENCES material_keyword(id);


--
-- TOC entry 2648 (class 2606 OID 604566)
-- Dependencies: 2490 243 256
-- Name: resource_material_keyword_resource_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_material_keyword
    ADD CONSTRAINT resource_material_keyword_resource_id_fkey1 FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2650 (class 2606 OID 604571)
-- Dependencies: 259 2474 232
-- Name: resource_other_keyword_other_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_other_keyword_id_fkey FOREIGN KEY (other_keyword_id) REFERENCES other_keyword(id);


--
-- TOC entry 2651 (class 2606 OID 604576)
-- Dependencies: 2490 259 243
-- Name: resource_other_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_other_keyword
    ADD CONSTRAINT resource_other_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2652 (class 2606 OID 604581)
-- Dependencies: 2482 261 236
-- Name: resource_revision_log_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_revision_log
    ADD CONSTRAINT resource_revision_log_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- TOC entry 2653 (class 2606 OID 604586)
-- Dependencies: 2490 263 243
-- Name: resource_site_name_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2654 (class 2606 OID 604591)
-- Dependencies: 263 2552 271
-- Name: resource_site_name_keyword_site_name_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_name_keyword
    ADD CONSTRAINT resource_site_name_keyword_site_name_keyword_id_fkey FOREIGN KEY (site_name_keyword_id) REFERENCES site_name_keyword(id);


--
-- TOC entry 2655 (class 2606 OID 604596)
-- Dependencies: 264 243 2490
-- Name: resource_site_type_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2656 (class 2606 OID 604601)
-- Dependencies: 2558 264 274
-- Name: resource_site_type_keyword_site_type_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_site_type_keyword
    ADD CONSTRAINT resource_site_type_keyword_site_type_keyword_id_fkey FOREIGN KEY (site_type_keyword_id) REFERENCES site_type_keyword(id);


--
-- TOC entry 2657 (class 2606 OID 604606)
-- Dependencies: 265 243 2490
-- Name: resource_temporal_keyword_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2658 (class 2606 OID 604611)
-- Dependencies: 281 265 2563
-- Name: resource_temporal_keyword_temporal_keyword_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY resource_temporal_keyword
    ADD CONSTRAINT resource_temporal_keyword_temporal_keyword_id_fkey FOREIGN KEY (temporal_keyword_id) REFERENCES temporal_keyword(id);


--
-- TOC entry 2661 (class 2606 OID 604616)
-- Dependencies: 274 274 2558
-- Name: site_type_keyword_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY site_type_keyword
    ADD CONSTRAINT site_type_keyword_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES site_type_keyword(id);


--
-- TOC entry 2663 (class 2606 OID 604621)
-- Dependencies: 277 243 2490
-- Name: source_collection_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY source_collection
    ADD CONSTRAINT source_collection_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resource(id);


--
-- TOC entry 2665 (class 2606 OID 604626)
-- Dependencies: 2482 286 236
-- Name: user_session_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tdar
--

ALTER TABLE ONLY user_session
    ADD CONSTRAINT user_session_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id) ON UPDATE CASCADE;


--
-- TOC entry 2670 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-07-26 10:37:00

--
-- PostgreSQL database dump complete
--

