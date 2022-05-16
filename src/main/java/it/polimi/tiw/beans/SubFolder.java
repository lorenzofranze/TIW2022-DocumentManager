package it.polimi.tiw.beans;

import java.util.Date;

public class SubFolder {
    private String username;
    private String subFolderName;
    private String FolderName;
    private Date date;

    public String getUsername() {
        return username;
    }

    public String getSubFolderName() {
        return subFolderName;
    }

    public Date getDate() {
        return date;
    }

    public String getFolderName() {
        return FolderName;
    }

    public void setUsername(String ownerUsername) {
        this.username = ownerUsername;
    }

    public void setSubFolderName(String subFolderName) {
        this.subFolderName = subFolderName;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFolderName(String folderName) {
        FolderName = folderName;
    }
}
