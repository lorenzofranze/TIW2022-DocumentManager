package it.polimi.tiw.beans;

import java.util.Date;

public class Folder {
    private String ownerUsername;
    private String folderName;
    private Date date;

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getFolderName() {
        return folderName;
    }

    public Date getDate() {
        return date;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
