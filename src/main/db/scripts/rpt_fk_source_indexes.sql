-- This is a simple report that shows which FK sources have indexes and which FK's do not.  Note that we're just
-- talking about FK *sources* (FK targets are always indexed).
-- Author: jdevos
-- Caveats:  This script assumes simple, one-column keys.  I have not tested it with FK's that point to multi-column PK's

-- drop table tmp_key;
-- drop table tmp_idx;
-- drop table tmp_rpt;

--create list of fk's and pk's  (assumption: information_schema always refers to public tables?)
create temporary table tmp_key as
    select
        tc.constraint_type,
        case tc.constraint_type
            when 'FOREIGN KEY' THEN 'fk'
            when 'PRIMARY KEY' then 'pk'
            else tc.constraint_type
        end as ctype,
        tc.constraint_name,
        tc.table_name,
        kcu.column_name,
        ccu.table_name foreign_table_name,
        ccu.column_name foreign_column_name
    from
        information_schema.table_constraints tc
            join information_schema.key_column_usage kcu on (tc.constraint_name = kcu.constraint_name)
            join information_schema.constraint_column_usage ccu on (tc.constraint_name = ccu.constraint_name)
    where
        tc.constraint_type  in ('FOREIGN KEY', 'PRIMARY KEY')
    order by
        tc.table_name,
        kcu.column_name,
        tc.constraint_name
;

--create list of indexes (in public namespace)
create temporary table tmp_idx as
    select
        i.relname as index_name,
        t.relname as table_name,
        a.attname as column_name
    from
        pg_class t,
        pg_class i,
        pg_index ix,
        pg_attribute a,
        pg_namespace ns
    where
        t.oid = ix.indrelid
        and i.oid = ix.indexrelid
        and a.attrelid = t.oid
        and a.attnum = ANY(ix.indkey)
        and t.relkind = 'r'
        and t.relnamespace = ns.oid
        and ns.nspname = 'public'
    order by
        t.relname, --table_name
        i.relname; --index_name
    ;

--The report:  get a list of FK source table, column, and whether they have an index
create temporary table tmp_rpt as
    select
        table_name,
        column_name,
        exists (
            select
                *
            from
                tmp_idx i
            where
                i.table_name = k.table_name
                and i.column_name = k.column_name
        ) as has_src_idx
    from
        tmp_key k
    where
        k.ctype = 'fk'
    order by
        k.table_name,
        k.column_name
;

-- pgsql syntax below to output tmp_rpt to html  (the .xls extension makes it easy to open in excel)
/*

\H
\pset footer off
\o rpt.xls
select * from tmp_rpt;
\o
\H
\pset footer on

-- */

