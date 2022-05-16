package it.polimi.tiw.beans;

import java.util.Date;

public class Folder {
    private String username;
    private String folderName;
    private Date date;

    public String getUsername() {
        return username;
    }

    public String getFolderName() {
        return folderName;
    }

    public Date getDate() {
        return date;
    }

    public void setUsername(String ownerUsername) {
        this.username = ownerUsername;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
