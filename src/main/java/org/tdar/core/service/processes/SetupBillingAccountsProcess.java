package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.resource.DatasetService;

/**
 * $Id$
 * 
 * ScheduledProcess to reprocess all datasets.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
public class SetupBillingAccountsProcess extends ScheduledBatchProcess<Person> {

    private static final long serialVersionUID = -2313655718394118279L;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EntityService entityService;

    @Override
    public String getDisplayName() {
        return "Setup Billing Accounts";
    }

    @Override
    public Class<Person> getPersistentClass() {
        return Person.class;
    }

    @Override
    public List<Long> findAllIds() {
        return new ArrayList(entityService.findAllContributorIds());
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public void process(Person person) {
        // Find all resources this person created
        // calculate the needed values for that person
        // setup an invoice for that size
        // create invoice and account for that person
    }

    @Override
    public int getBatchSize() {
        return 5;
    }
    
    @Override
    public boolean isEnabled() {
        return false;
    }

}
