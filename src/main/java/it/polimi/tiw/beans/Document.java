package it.polimi.tiw.beans;

import java.util.Date;

public class Document {
    private String username;
    private String folderName;
    private String subFolderName;
    private String documentName;
    private String type;
    private String summary;
    private Date date;
    private byte [] body;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getSubFolderName() {
        return subFolderName;
    }

    public void setSubFolderName(String subFolderName) {
        this.subFolderName = subFolderName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummury(String summury) {
        this.summary = summury;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
