package it.polimi.tiw.DAO;

import it.polimi.tiw.beans.Document;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static java.lang.System.out;

public class DocumentDAO {
    private Connection con;

    public DocumentDAO(Connection connection){
        this.con = connection;
    }


    public boolean exists(String username, String folderName, String subFolderName, String documentName, String type) throws SQLException{
        String query = "SELECT * FROM document WHERE username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        boolean status;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            pstatement.setString(3, subFolderName);
            pstatement.setString(4, documentName);
            pstatement.setString(5, type);
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

    public boolean insertDocument(String username, String folderName, String subFolderName, String documentName, String type, String summury, Date date, InputStream body) throws SQLException {
        String query = "INSERT into document VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstatement = null;
        int code = 0;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            pstatement.setString(3, subFolderName);
            pstatement.setString(4, documentName);
            pstatement.setString(5, type);
            pstatement.setString(6, summury);
            pstatement.setDate(7, new java.sql.Date(date.getTime()));
            pstatement.setBlob(8, body);
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
    public void moveDocumentFromSubFolder(String username, String folderName, String subFolderName, String documentName, String type, String folderTarget, String subFolderTarget) throws SQLException{
        String query = "UPDATE document set folderName = ? , subFolderName = ? WHERE username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";

        PreparedStatement pstatement = null;
        int code = 0;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, folderTarget);
            pstatement.setString(2, subFolderTarget);
            pstatement.setString(3, username);
            pstatement.setString(4, folderName);
            pstatement.setString(5, subFolderName);
            pstatement.setString(6, documentName);
            pstatement.setString(7, type);

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
                "and folderName = ? and subFolderName = ?";
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
                toAdd.setDocumentName(result.getString("documentName"));
                toAdd.setType(result.getString("type"));
                toAdd.setSummury(result.getString("summary"));
                toAdd.setDate(result.getDate("date"));
                /*
                byte[] data = result.getBytes("body");
                String encodedData= Base64.getEncoder().encodeToString(data);
                toAdd.setBody(encodedData);
                 */
                allDocuments.add(toAdd);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
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
            doc.setDocumentName(result.getString("documentName"));
            doc.setType(result.getString("type"));
            doc.setSummury(result.getString("summary"));
            doc.setDate(result.getDate("date"));

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

    public InputStream getDocumentData(String username, String folderName, String subFolderName, String documentName, String type) throws SQLException{
        String query = "select body from document where username = ? " +
                "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        InputStream input = null;
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
            input = result.getBinaryStream("body");

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
        return input;
    }

}
