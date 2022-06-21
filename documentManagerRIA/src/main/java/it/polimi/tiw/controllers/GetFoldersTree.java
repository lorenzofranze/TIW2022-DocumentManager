package it.polimi.tiw.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/GetFoldersTree")
@MultipartConfig
public class GetFoldersTree extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private static Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Folder> allfolders = getFolderTree(response, session);

        // Redirect to the Home page and add missions to the parameters

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy MMM dd").create();
        String json = gson.toJson(allfolders);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public static List<Folder> getFolderTree(HttpServletResponse response, HttpSession session) throws UnavailableException, IOException {

        List<Folder> allfolders = new ArrayList<>();

        FolderDAO fService = new FolderDAO(connection);

        try {
            if (session != null) {
                User user = (User) session.getAttribute("currentUser");
                String username = user.getUsername();
                allfolders = fService.getAllFolderOfUser(username);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in retrieving folders from the database");
        }

        return allfolders;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        doGet(request, response);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
