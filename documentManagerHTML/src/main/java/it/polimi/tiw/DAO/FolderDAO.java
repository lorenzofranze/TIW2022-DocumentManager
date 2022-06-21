package it.polimi.tiw.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.*;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.http.HttpSession;

public class FolderDAO {
    private final Connection connection;

    public FolderDAO(Connection con) {
        this.connection = con;
    }

    public List<Folder> getAllFolderOfUser(String username) throws SQLException {

        List<Folder> allFolders = new ArrayList<>();

        try (PreparedStatement pstatement = connection.prepareStatement(
                "SELECT * FROM folder WHERE username = ? ")) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Folder newFolder = new Folder();
                    newFolder.setUsername(result.getString("username"));
                    newFolder.setFolderName(result.getString("foldername"));
                    newFolder.setDate(result.getDate("date"));
                    newFolder.setChildList(getAllChildOfFolder(newFolder, username));
                    allFolders.add(newFolder);
                }
            }
        }
        return allFolders;
    }

    public List<SubFolder> getAllChildOfFolder(Folder folder, String username) throws SQLException {

        List<SubFolder> allSubFoldersOf = new ArrayList<>();
        SubFolderDAO dao = new SubFolderDAO(connection);
        allSubFoldersOf = dao.getAllSubFolderOfFolder(username, folder.getFolderName());
        return allSubFoldersOf;
    }

    public boolean insertFolder(Folder folder) throws SQLException {
        String query = "INSERT into folder VALUES(?, ?, ?)";
        PreparedStatement pstatement = null;
        int code;

        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, folder.getUsername());
            pstatement.setString(2, folder.getFolderName());
            pstatement.setDate(3, new Date(folder.getDate().getTime()));
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
        return (code == 1);
    }

    public boolean existsFolder(String username, String folderName) throws SQLException{
        String query = "SELECT username FROM folder WHERE username = ? and folderName = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        boolean status;

        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, folderName);
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
