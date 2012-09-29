package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;

public class PersonQueryPart extends FieldQueryPart<Person> {

    public PersonQueryPart() {
        setAllowInvalid(true);
    }

    private boolean registered = false;

    @Override
    public String generateQueryString() {
        List<String> fns = new ArrayList<String>();
        List<String> lns = new ArrayList<String>();
        List<String> ems = new ArrayList<String>();
        List<String> insts = new ArrayList<String>();
        QueryPartGroup group = new QueryPartGroup();
        for (Person pers : getFieldValues()) {
            if (StringUtils.isNotBlank(pers.getFirstName())) {
                fns.add(pers.getFirstName());
            }
            if (StringUtils.isNotBlank(pers.getLastName())) {
                lns.add(pers.getLastName());
            }
            if (StringUtils.isNotBlank(pers.getEmail())) {
                ems.add(pers.getEmail());
            }
            if (StringUtils.isNotBlank(pers.getInstitutionName())) {
                String institution = pers.getInstitutionName();
                institution = PhraseFormatter.ESCAPED.format(institution);
//                institution = PhraseFormatter.WILDCARD.format(institution);
                if (institution.contains(" ")) {
                    institution = PhraseFormatter.QUOTED.format(institution);
                }
                insts.add(institution);
            }
        }

        if (CollectionUtils.isNotEmpty(fns)) {
            FieldQueryPart fqp = new FieldQueryPart("firstName", fns);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group.append(fqp);
        }

        if (CollectionUtils.isNotEmpty(lns)) {
            FieldQueryPart ln = new FieldQueryPart("lastName", lns);
            ln.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group.append(ln);
        }
        if (CollectionUtils.isNotEmpty(ems)) {
            FieldQueryPart emls = new FieldQueryPart("email", ems);
            emls.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            group.append(emls);
        }
        if (CollectionUtils.isNotEmpty(insts)) {
            QueryPartGroup group1 = new QueryPartGroup(Operator.OR);
            group1.append(new FieldQueryPart("institution.name", insts));
            group1.append(new FieldQueryPart("institution.name_auto", insts));
            group.append(group1);
        }
        
        if (registered) {
            QueryPartGroup qpg = new QueryPartGroup(Operator.AND);
            qpg.append(group);
            qpg.append(new FieldQueryPart("registered", true));
            return qpg.generateQueryString();
        }

        return group.toString();
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
