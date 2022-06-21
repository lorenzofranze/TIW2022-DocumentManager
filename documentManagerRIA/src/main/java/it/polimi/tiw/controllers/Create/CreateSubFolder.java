package it.polimi.tiw.controllers.Create;

import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.SubFolder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/CreateSubFolder")
@MultipartConfig
public class CreateSubFolder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        //redirect to login if not logged in
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");

        //to repeat client side
        if (subFolderName == null || subFolderName.length() <= 3 || folderName == null || folderName.length()<=3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        boolean exists=true;
        FolderDAO folderDAO = new FolderDAO(connection);
        SubFolderDAO subFolderDAO = new SubFolderDAO(connection);

        //check folder name univocity

        try {
            exists = subFolderDAO.existsSubFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in database");
            return;
        }

        if(exists){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().println("you already have a sub folder with this name in this folder");
            return;
        }


        //insert in database and return to HomePage
        try {
            SubFolder subFolder = new SubFolder();
            subFolder.setUsername(((User) session.getAttribute("currentUser")).getUsername());
            subFolder.setFolderName(folderName);
            subFolder.setSubFolderName(subFolderName);
            subFolder.setDate(new Date());
            subFolderDAO.insertSubFolder(subFolder);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in database");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
