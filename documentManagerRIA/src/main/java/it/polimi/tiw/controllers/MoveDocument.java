package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

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

@WebServlet("/MoveDocument")
@MultipartConfig
public class MoveDocument extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //redirect to log in if not logged in
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        DocumentDAO docDAO = new DocumentDAO(connection);

        String folderTarget = request.getParameter("folderTarget");
        String subFolderTarget = request.getParameter("subFolderTarget");

        //document's key origin
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String type = request.getParameter("documentType");

        if(checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type)
            || checkIncorrect(folderTarget) || checkIncorrect(subFolderTarget)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        //check if target is different from origin
        if(folderName.equals(folderTarget) && subFolderName.equals(subFolderTarget)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().println("Target equals origin");
            return;
        }
        //check univocity
        boolean exists;
        try{
            exists = docDAO.exists(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName, documentName, type);
        }catch (SQLException e ){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to move the document");
            return;
        }
        if(exists){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().println("you already have a similar document in the selected sub folder !");
            return;
        }
        try {
            docDAO.moveDocumentFromSubFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName, documentName, type, folderTarget, subFolderTarget);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to move the document");
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

    private boolean checkIncorrect(String string){
        return (string==null || string.isEmpty() );
    }
}