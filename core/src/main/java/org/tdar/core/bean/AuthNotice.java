package org.tdar.core.bean;

/**
 * Classification of system notices that require some form of acknowledgement and/or acceptance from the user
 * acknowledges the notice.
 * User: jimdevos
 * Date: 9/5/13
 * Time: 3:48 PM
 */
public enum AuthNotice {
    /**
     * TOS has changed; user must accept new agreement in order to perform "logged-in" actions that require authentication
     */
    TOS_AGREEMENT,

    /**
     * Contributor agreement has changed; user must accept new terms in order to perform "logged-in" actions
     */
    CONTRIBUTOR_AGREEMENT
//    ,
//
//    /**
//     * user is logged-in with 'guest' account; The system should notify user of limitations (e.g. 'contributions will
//     * be deleted after 24hrs'), but does not require explicit acceptance
//     */
//    GUEST_ACCOUNT
}
