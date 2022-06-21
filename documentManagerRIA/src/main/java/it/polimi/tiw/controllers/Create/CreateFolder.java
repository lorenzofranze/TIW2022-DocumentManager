package it.polimi.tiw.controllers.Create;

import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.beans.Folder;
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

@WebServlet("/CreateFolder")
@MultipartConfig
public class CreateFolder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String folderName = request.getParameter("folderName");

        //to repeat client side
        if(folderName == null || folderName.length()<=3 ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        FolderDAO dao = new FolderDAO(connection);
        boolean exists;
        //check folder name univocity
        try {
            exists = dao.existsFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in database");
            return;
        }
        //there is an other folder with same name
        if(exists){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().println("you already have a folder with this name");
            return;
        }
        //insert in database and return to HomePage
        try {
            Folder folder = new Folder();
            folder.setUsername(((User) session.getAttribute("currentUser")).getUsername());
            folder.setFolderName(folderName);
            folder.setDate(new Date());
            dao.insertFolder(folder);
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
