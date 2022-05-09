package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.beans.*;

//vedere gestione duplicati se eccezione o meglio verificare
public class UserDAO {
    private Connection con;

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    public User checkUser(String username, String password) throws SQLException {
        User user = null;
        String query = "SELECT username FROM user WHERE username = ? and password = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, password);
            result = pstatement.executeQuery();
            if (!result.isBeforeFirst())
                return null;
            else {
                result.next();
                user = new User();
                user.setUsername(result.getString("username"));
                user.setName(result.getString("name"));
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
        return user;
    }

    public boolean existsUser(String username) throws SQLException {
        String query = "SELECT username FROM user WHERE username = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            result = pstatement.executeQuery();
            if (!result.isBeforeFirst())
                return false;
            else {
                return true;
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
    }

    public boolean createUser(User user, String password) throws SQLException {
        int code = 0;
        String query = "INSERT into user (username, password, name)   VALUES(?, ?, ?)";
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, user.getUsername());
            pstatement.setString(2, password);
            pstatement.setString(3, user.getName());
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
        return (code==1 ? true : false);
    }
}
