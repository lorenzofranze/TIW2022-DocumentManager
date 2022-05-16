package it.polimi.tiw.DAO;

import it.polimi.tiw.beans.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    private Connection con;

    public DocumentDAO(Connection connection){
        this.con = connection;
    }


    public boolean exists(Document document) throws SQLException{
        String query = "SELECT * FROM document WHERE username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        boolean status;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, document.getUsername());
            pstatement.setString(2, document.getFolderName());
            pstatement.setString(3, document.getSubFolderName());
            pstatement.setString(4, document.getDocumentName());
            pstatement.setString(5, document.getType());
            result = pstatement.executeQuery();

            if (!result.isBeforeFirst())
                status = false;
            else {
                status =  true;
            }
        } catch (SQLException e) {
            throw new SQLException(e);

        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw e;
            }
            try {
                pstatement.close();
            } catch (Exception e) {
                throw e;
            }
        }
        return status;
    }

    public boolean insertDocument(Document document) throws SQLException {
        String query = "INSERT into document VALUES(?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstatement = null;
        int code = 0;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, document.getUsername());
            pstatement.setString(2, document.getFolderName());
            pstatement.setString(3, document.getSubFolderName());
            pstatement.setString(4, document.getDocumentName());
            pstatement.setString(5, document.getType());
            pstatement.setString(6, document.getSummary());
            pstatement.setDate(7, (java.sql.Date) document.getDate()); // verificare
            code = pstatement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw e1;
            }
        }
        return (code==1 ? true : false);

    }

    /** this method edits document's attributes in document when the document is moved */
    //sql exeption if in the destination subfolder there is a document with same primary key
    public void moveDocumentFromSubFolder(Document document, String folderName, String subFolderName) throws SQLException{
        String query = "UPDATE document set folderName = ? , subFolderName = ? WHERE username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";

        PreparedStatement pstatement = null;
        int code = 0;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, folderName);
            pstatement.setString(2, subFolderName);
            pstatement.setString(3, document.getUsername());
            pstatement.setString(4, document.getFolderName());
            pstatement.setString(5, document.getSubFolderName());
            pstatement.setString(6, document.getDocumentName());
            pstatement.setString(7, document.getType());

            code = pstatement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                pstatement.close();
            } catch (SQLException e1) {
                throw e1;
            }
        }
    }

    public List<Document> getAllDocumentsOfSubFolder (String username, String folderName, String subFolderName) throws SQLException{
        String query = "select * from document where username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";
        PreparedStatement pstatement = null;
        ResultSet result = null;

        List <Document> allDocuments = new ArrayList<>();

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            pstatement.setString(3, subFolderName);
            result = pstatement.executeQuery();

            if (!result.isBeforeFirst())
                return allDocuments;  //empty
            while(result.next()){
                Document toAdd = new Document();
                toAdd.setUsername(result.getString("username"));
                toAdd.setFolderName(result.getString("folderName"));
                toAdd.setSubFolderName(result.getString("subFolderName"));
                toAdd.setDocumentName(result.getString("documetName"));
                toAdd.setType(result.getString("type"));
                toAdd.setSummury(result.getString("summury"));
                toAdd.setDate(result.getDate("date"));
                byte[] body = result.getBytes("body");
                toAdd.setBody(body);
                allDocuments.add(toAdd);
            }
        } catch (SQLException e) {
            throw new SQLException(e);

        } finally {
            try {
                result.close();
            } catch (SQLException e) {
                throw e;
            }
            try {
                pstatement.close();
            } catch (SQLException e) {
                throw e;
            }
        }

        return allDocuments;

    }

    public Document getDocumentByKey(String username, String folderName, String subFolderName, String documentName, String type) throws SQLException{
        String query = "select * from document where username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        Document doc;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            pstatement.setString(3, subFolderName);
            pstatement.setString(4, documentName);
            pstatement.setString(5, type);

            result = pstatement.executeQuery();

            if (!result.isBeforeFirst())
                return null;  //empty

            result.next();
            doc = new Document();
            doc.setUsername(result.getString("username"));
            doc.setFolderName(result.getString("folderName"));
            doc.setSubFolderName(result.getString("subFolderName"));
            doc.setDocumentName(result.getString("documetName"));
            doc.setType(result.getString("type"));
            doc.setSummury(result.getString("summury"));
            doc.setDate(result.getDate("date"));
            byte[] body = result.getBytes("body");
            doc.setBody(body);

        } catch (SQLException e) {
            throw new SQLException(e);

        } finally {
            try {
                result.close();
            } catch (SQLException e) {
                throw e;
            }
            try {
                pstatement.close();
            } catch (SQLException e) {
                throw e;
            }
        }
        return doc;
    }
}
