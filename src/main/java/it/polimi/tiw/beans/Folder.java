package it.polimi.tiw.beans;

import java.util.Date;
import java.util.List;

public class Folder {
    private String username;
    private String folderName;
    private List<SubFolder> childList = null;
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

    public List<SubFolder> getChildList() {
        return childList;
    }

    public void setChildList(List<SubFolder> childList) {
        this.childList = childList;
    }
}
