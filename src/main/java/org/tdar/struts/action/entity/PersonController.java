package org.tdar.struts.action.entity;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.ObfuscationService;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/person")
public class PersonController extends AbstractPersonController<Person> {

    private static final long serialVersionUID = -5188801652461303210L;
    @Autowired
    private transient ObfuscationService obfuscationService;

    public Person getPerson() {
        Person p = getPersistable();
        if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
            if (!authorize()) {
                obfuscationService.obfuscate(p, getAuthenticatedUser());
            }
        }
        return p;
    }

    public void setPerson(Person person) {
        setPersistable(person);
    }

    @Override
    public Class<Person> getPersistableClass() {
        return Person.class;
    }

}
