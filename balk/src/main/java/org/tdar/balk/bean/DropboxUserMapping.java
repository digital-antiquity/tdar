package org.tdar.balk.bean;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.tdar.core.bean.AbstractPersistable;

@Entity(name = "dropbox_user_mapping")
public class DropboxUserMapping extends AbstractPersistable {

    private static final long serialVersionUID = -5514797652457958555L;

    @Column(name = "dropbox_user_id", length = 512)
    private String dropboxUserId;

    @Column(name = "email", length = 512)
    private String email;
    @Column(name = "token", length = 512)
    private String token;
    @Column(name = "username", length = 512)
    private String username;
    @Column(name = "tdar_user_id")
    private Long tdarId;

    public String getDropboxUserId() {
        return dropboxUserId;
    }

    public void setDropboxUserId(String dropboxUserId) {
        this.dropboxUserId = dropboxUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTdarId() {
        return tdarId;
    }

    public void setTdarId(Long tdarId) {
        this.tdarId = tdarId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
