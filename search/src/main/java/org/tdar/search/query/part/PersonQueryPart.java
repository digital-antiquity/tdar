package org.tdar.search.query.part;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.query.QueryFieldNames;

public class PersonQueryPart extends FieldQueryPart<Person> {

    public PersonQueryPart() {
        setAllowInvalid(true);
    }

    private boolean registered = false;
    private boolean includeEmail   = false;

    @Override
    public String generateQueryString() {
        Set<String> fns = new HashSet<>();
        Set<String> lns = new HashSet<>();
        Set<String> ems = new HashSet<>();
        Set<String> insts = new HashSet<>();
        Set<String> wildcards = new HashSet<>();
        QueryPartGroup group = new QueryPartGroup(getOperator());
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
                if (isIncludeEmail()) {
                    FieldQueryPart<String> email = new FieldQueryPart<>(QueryFieldNames.EMAIL, wildcard);
                    email.setBoost(4f);
                    group.append(email);
                }
                setOperator(Operator.OR);
            }

            if(pers instanceof TdarUser && StringUtils.isNotBlank(((TdarUser)pers).getUsername())){
            		FieldQueryPart<String> username = new FieldQueryPart<>(QueryFieldNames.USERNAME, ((TdarUser)pers).getUsername());
            		username.setBoost(4f);
                group.append(username);
            }
            
            
            if (StringUtils.isNotBlank(pers.getEmail())) {
                if (isIncludeEmail()) {
                    ems.add(StringUtils.trim(pers.getEmail()));
                } else {
                    // right now, throwing the exception is too disruptive, ignoring query component instead
                    // throw new TdarRecoverableRuntimeException("personQueryPart.not_allowed_email");
                }
            }

            if (StringUtils.isNotBlank(pers.getInstitutionName())) {
                String institution = StringUtils.trim(pers.getInstitutionName());
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
            Set<String> formatted = new HashSet<>();
            for (String inst : insts) {
                String institution = PhraseFormatter.ESCAPED.format(inst);
                if (institution.contains(" ")) {
                    institution = PhraseFormatter.QUOTED.format(institution);
                }
                formatted.add(institution);
            }
            FieldQueryPart<String> inst = new FieldQueryPart<String>(QueryFieldNames.INSTITUTION_NAME, formatted);
//            inst.setPhraseFormatters(PhraseFormatter.ESCAPED);
            group.append(inst);
            // group1.append(new FieldQueryPart<String>(QueryFieldNames.INSTITUTION_NAME_AUTO, insts));
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

    private QueryPartGroup createFieldGroup(Set<String> terms, String norm, String auto) {
        if (CollectionUtils.isNotEmpty(terms)) {
            QueryPartGroup group1 = new QueryPartGroup(Operator.OR);
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(norm, terms);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group1.append(fqp);
            StringAutocompletePart autoField = new StringAutocompletePart(auto, terms);
            // autoField.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
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

    public boolean isIncludeEmail() {
        return includeEmail;
    }

    public void setIncludeEmail(boolean includeEmail) {
        this.includeEmail = includeEmail;
    }
}
