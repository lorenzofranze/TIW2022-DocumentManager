package it.polimi.tiw.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/GetDocumentsList")
@MultipartConfig
public class GetDocumentsList extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException{
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String username = request.getParameter("username");
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");

        if(checkIncorrect(username) || checkIncorrect(folderName) || checkIncorrect(subFolderName) ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            // response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        }

        //user changes parameters to access resources of other users:
        if(!username.equals(sessionUser.getUsername())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("User not allowed");
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
            return;
        }
        //check if user has that folder and subfoler(maybe he manipulated the link)
        boolean valid;
        SubFolderDAO subDao = new SubFolderDAO(connection);
        try{
            valid = subDao.existsSubFolder(username, folderName, subFolderName);
        }catch (SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in retrieving documents from the database");
            // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            //        "Error in retrieving documents from the database");
            return;
        }
        if(valid==false){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("The resource doesn't exist");
            return;
        }

        List<Document> allDocumentsOf = new ArrayList<>();
        DocumentDAO dService = new DocumentDAO(connection);
        try {
            allDocumentsOf = dService.getAllDocumentsOfSubFolder(username, folderName, subFolderName);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in retrieving documents from the database");
            // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            //        "Error in retrieving documents from the database");
            return;
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy MMM dd").create();
        String json = gson.toJson(allDocumentsOf);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** check string parameter isn't null and lenght greater than zero */
    private boolean checkIncorrect(String string){
        return (string==null || string.isEmpty() );
    }
}
