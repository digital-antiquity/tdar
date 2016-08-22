package org.tdar.balk.bean;

import javax.persistence.Entity;

@Entity
public class DropboxDirectory extends AbstractDropboxItem {

    private static final long serialVersionUID = -4900076561727433757L;

    private DropboxDirectory() {
        setType(ItemType.DIR);
    }
}
