@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
            name = "query.dataone_list_logs",
            query = " from LogEntryImpl lg where (lg.dateLogged between :start and :end) and (:idFilter is NULL or lg.identifier=:idFilter) and (:event is NULL or lg.event=:event)"
    )
})

package org.tdar.dataone.dao;

