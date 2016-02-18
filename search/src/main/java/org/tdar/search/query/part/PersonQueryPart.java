package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;
import org.tdar.search.query.QueryFieldNames;


public class PersonQueryPart extends FieldQueryPart<Person> {

    public PersonQueryPart() {
        setAllowInvalid(true);
    }

    private boolean registered = false;

    @Override
    public String generateQueryString() {
        List<String> fns = new ArrayList<>();
        List<String> lns = new ArrayList<>();
        List<String> ems = new ArrayList<>();
        List<String> insts = new ArrayList<>();
        List<String> wildcards = new ArrayList<>();
        QueryPartGroup group = new QueryPartGroup();
        for (Person pers : getFieldValues()) {
            boolean hasName = false;
            if (StringUtils.isNotBlank(pers.getFirstName())) {
                fns.add(pers.getFirstName().trim());
                hasName = true;
            }
            if (StringUtils.isNotBlank(pers.getLastName())) {
                lns.add(pers.getLastName().trim());
                hasName = true;
            }
            if (!hasName && StringUtils.isNotBlank(pers.getWildcardName())) {
                String wildcardName = pers.getWildcardName().trim();
                wildcards.add(wildcardName);
                group.setOperator(Operator.OR);
                StringAutocompletePart auto = new StringAutocompletePart(QueryFieldNames.PROPER_AUTO, Arrays.asList(wildcardName));
                String wildcard = StringUtils.trim(new String(wildcardName));
                wildcard = PhraseFormatter.ESCAPED.format(wildcard);
                if (!wildcard.contains(" ")) {
                    wildcard = PhraseFormatter.WILDCARD.format(wildcard);
                } else {
                    wildcard = PhraseFormatter.QUOTED.format(wildcard);
                }
                auto.setBoost(6f);
                group.append(auto);
                if (!wildcard.contains(" ")) {
                    FieldQueryPart<String> username = new FieldQueryPart<>(QueryFieldNames.USERNAME, wildcard);
                    username.setBoost(4f);
                    group.append(username);
                }
                FieldQueryPart<String> email = new FieldQueryPart<>(QueryFieldNames.EMAIL, wildcard);
                email.setBoost(4f);
                group.append(email);
                setOperator(Operator.OR);
            }

            if (StringUtils.isNotBlank(pers.getEmail())) {
                ems.add(StringUtils.trim(pers.getEmail()));
            }

            if (StringUtils.isNotBlank(pers.getInstitutionName())) {
                String institution = StringUtils.trim(pers.getInstitutionName());
                institution = PhraseFormatter.ESCAPED.format(institution);
                // institution = PhraseFormatter.WILDCARD.format(institution);
                if (institution.contains(" ")) {
                    institution = PhraseFormatter.QUOTED.format(institution);
                }
                insts.add(institution);
            }
        }
        fns.addAll(wildcards);
        lns.addAll(wildcards);
        insts.addAll(wildcards);

        group.append(createFieldGroup(fns, QueryFieldNames.FIRST_NAME, QueryFieldNames.FIRST_NAME_AUTO));
        group.append(createFieldGroup(lns, QueryFieldNames.LAST_NAME, QueryFieldNames.LAST_NAME_AUTO));

        if (CollectionUtils.isNotEmpty(ems)) {
            FieldQueryPart<String> emls = new FieldQueryPart<String>(QueryFieldNames.EMAIL, ems);
            emls.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group.append(emls);
        }
        if (CollectionUtils.isNotEmpty(insts)) {
            QueryPartGroup group1 = new QueryPartGroup(Operator.OR);
            group1.append(new FieldQueryPart<String>(QueryFieldNames.INSTITUTION_NAME, insts));
//            group1.append(new FieldQueryPart<String>(QueryFieldNames.INSTITUTION_NAME_AUTO, insts));
            group.append(group1);
        }

        if (registered) {
            // adding wildcard search for username too
            if (CollectionUtils.isNotEmpty(wildcards)) {
                FieldQueryPart<String> fqp = new FieldQueryPart<String>(QueryFieldNames.USERNAME, wildcards);
                fqp.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
                fqp.setOperator(Operator.OR);
                group.append(fqp);
            }

            QueryPartGroup qpg = new QueryPartGroup(Operator.AND);
            qpg.append(group);
            qpg.append(new FieldQueryPart<Boolean>(QueryFieldNames.REGISTERED, Boolean.TRUE));
            return qpg.generateQueryString();
        }
        return group.toString();
    }

    private QueryPartGroup createFieldGroup(List<String> terms, String norm, String auto) {
        if (CollectionUtils.isNotEmpty(terms)) {
            QueryPartGroup group1 = new QueryPartGroup(Operator.OR);
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(norm, terms);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group1.append(fqp);
            StringAutocompletePart autoField = new StringAutocompletePart(auto, terms);
//            autoField.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            autoField.setBoost(2.0f);
            group1.append(autoField);
            return group1;
        }
        return null;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
