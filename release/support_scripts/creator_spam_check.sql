select creator. id,date_created,email,first_name,last_name,phone,contributor_reason  from creator, person
        where creator.id=person.id and contributor=true  and date_Created >= 'yesterday';
