package it.polimi.tiw.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.*;

public class SubFolderDAO {
    private final Connection connection;

    public SubFolderDAO(Connection con) {
        this.connection = con;
    }

    public List<SubFolder> getAllSubFolderOfFolder(String username, String folderName) throws SQLException {

        List<SubFolder> allSubFoldersOfFolder = new ArrayList<>();

        try (PreparedStatement pstatement = connection.prepareStatement(
                "SELECT * FROM subfolder WHERE username = ? AND folderName = ?")) {
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    SubFolder newSubFolder = new SubFolder();
                    newSubFolder.setUsername(result.getString("username"));
                    newSubFolder.setFolderName(result.getString("foldername"));
                    newSubFolder.setSubFolderName(result.getString("subfoldername"));
                    newSubFolder.setDate(result.getDate("date"));
                    allSubFoldersOfFolder.add(newSubFolder);
                }
            }
        }
        return allSubFoldersOfFolder;
    }

    public boolean insertSubFolder(SubFolder subFolder) throws SQLException {
        String query = "INSERT into subfolder VALUES(?, ?, ?, ?)";
        PreparedStatement pstatement = null;
        int code;

        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, subFolder.getUsername());
            pstatement.setString(2, subFolder.getFolderName());
            pstatement.setString(3, subFolder.getSubFolderName());
            pstatement.setDate(4, new Date(subFolder.getDate().getTime()));
            code = pstatement.executeUpdate();
        } finally {
            assert pstatement != null;
            pstatement.close();
        }
        return (code == 1);
    }

    public boolean existsSubFolder(String username, String folderName, String subFolderName) throws SQLException{
        String query = "SELECT username FROM subfolder WHERE username = ? and folderName = ? and subFoldername = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        boolean status;

        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
            pstatement.setString(3, subFolderName);
            result = pstatement.executeQuery();
            if (!result.isBeforeFirst())
                status = false;
            else {
                status = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);

        } finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            try {
                pstatement.close();
            } catch (Exception e2) {
                throw new SQLException(e2);
            }
        }
        return status;
    }
}
