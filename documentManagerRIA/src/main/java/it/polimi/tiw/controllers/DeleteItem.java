package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/DeleteItem")
@MultipartConfig
public class DeleteItem extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String username = request.getParameter("username");
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String type = request.getParameter("type");

        if(checkIncorrect(username) || checkIncorrect(folderName) ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        //user changes parameters to access resources of other users:
        if(!username.equals(sessionUser.getUsername())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //type of element check
        if(subFolderName==null && documentName == null && type == null){
            //delete folder
            FolderDAO dao = new FolderDAO(connection);
            boolean status;
            try{
                status = dao.deleteFolder(username, folderName);
                if(status!=true)
                    throw new SQLException();
            }catch(SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error in database");
                return;
            }

        }else if(!checkIncorrect(subFolderName) && documentName==null && type ==null){
            //delete subfolder
            SubFolderDAO dao = new SubFolderDAO(connection);
            boolean status;
            try{
                status = dao.deleteSubFolder(username, folderName, subFolderName);
                if(status!=true)
                    throw new SQLException();
            }catch(SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error in database");
                return;
            }
        }else if(!checkIncorrect(subFolderName) && !checkIncorrect(documentName) && !checkIncorrect(type)){
            //delete document
            DocumentDAO dao = new DocumentDAO(connection);
            boolean status;
            try{
                status = dao.deleteDocument(username, folderName, subFolderName, documentName, type);
                if(status!=true)
                    throw new SQLException();
            }catch(SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error in database");
                return;
            }
        }else{
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /** check string parameter isn't null and lenght greater than zero */
    private boolean checkIncorrect(String string){
        return (string==null || string.isEmpty() );
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

