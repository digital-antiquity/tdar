package org.tdar.core.bean.billing;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * $Id$
 * 
 * 
 * 
 * @author TDAR
 * @version $Rev$
 */
@Entity
@Table(name = "pos_account_group")
public class AccountGroup extends Account {

    private static final long serialVersionUID = 3939132209828344622L;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "account_group_id")
    private Set<Account> accounts = new HashSet<Account>();

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}
