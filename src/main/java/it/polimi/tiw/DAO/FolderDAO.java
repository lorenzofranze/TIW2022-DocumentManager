package it.polimi.tiw.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.*;

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
                    newFolder.setOwnerUsername(result.getString("username"));
                    newFolder.setFolderName(result.getString("foldername"));
                    newFolder.setDate(result.getDate("date"));
                    allFolders.add(newFolder);
                }
            }
        }
        return allFolders;
    }

    public boolean insertFolder(Folder folder) throws SQLException {
        String query = "INSERT into folder VALUES(?, ?, ?)";
        PreparedStatement pstatement = null;
        int code;

        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, folder.getOwnerUsername());
            pstatement.setString(2, folder.getFolderName());
            pstatement.setDate(3, (Date) folder.getDate());
            code = pstatement.executeUpdate();
        } finally {
            assert pstatement != null;
            pstatement.close();
        }
        return (code == 1);
    }
}
