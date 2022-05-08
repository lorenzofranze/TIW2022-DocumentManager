package it.polimi.tiw.DAO;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.SubFolder;

import java.sql.*;

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

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, document.getUsername());
            pstatement.setString(2, document.getFolderName());
            pstatement.setString(3, document.getSubFolderName());
            pstatement.setString(4, document.getDocumentName());
            pstatement.setString(5, document.getType());
            result = pstatement.executeQuery();

            if (!result.isBeforeFirst())
                return false;
            else {
                return true;
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
    }

    public boolean insertDocument(Document document) throws SQLException {
        String query = "INSERT into document VALUES(?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstatement = null;
        int code = 0;
        boolean exists;

        try{
            exists = this.exists(document);
        } catch(SQLException e){
            throw e;
        }

        if(exists==true)
            return false;


        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, document.getUsername());
            pstatement.setString(2, document.getFolderName());
            pstatement.setString(3, document.getSubFolderName());
            pstatement.setString(4, document.getDocumentName());
            pstatement.setString(5, document.getType());
            pstatement.setString(6, document.getSummury());
            pstatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // verificare
            code = pstatement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                pstatement.close();
            } catch (Exception e1) {}
        }
        return (code==1 ? true : false);

    }

    // this method edits document's attributes in document when the document is moved
    public void moveDocumentFromSubFolder(Document document, SubFolder origin, SubFolder destination) throws SQLException{
        String query = "UPDATE document set folderName = ? , subFolderName = ? WHERE username = ? " +
        "and folderName = ? and subFolderName = ? " +
                "and documentName = ? and type = ?";

        PreparedStatement pstatement = null;
        int code = 0;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, destination.getFolderName);
            pstatement.setString(2, destination.getSubFolderName());
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
            } catch (Exception e1) {}
        }

    }
}
