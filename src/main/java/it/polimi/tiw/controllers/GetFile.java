package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/GetFile")
public class GetFile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        User sessionUser = (User) session.getAttribute("currentUser");
        if (session == null || sessionUser == null) {
            String path = getServletContext().getContextPath();
            response.sendRedirect(path);
            return;
        }

        //controlls
        String username = request.getParameter("username");
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String type = request.getParameter("documentType");

        if(checkIncorrect(username) || checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type) ){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        }

        //user changes parameters to access resources of other users:
        if(!username.equals(sessionUser.getUsername())){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
            return;
        }
        byte[] body = null;
        DocumentDAO docDAO = new DocumentDAO(connection);

        try{
            body = docDAO.getDocumentData(username, folderName, subFolderName, documentName, type);
            if(body == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                return;
            }
        }catch(SQLException e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover document");
            return;
        }
        System.out.println(body);

        response.setHeader("Content-disposition","attachment; filename="+documentName+"."+type);

        File tmpFile = File.createTempFile(documentName, type, new File("C:\\Users\\loren\\OneDrive\\Desktop"));

        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(tmpFile);

        int length;
        while ((length = in.read(body)) > 0){
            out.write(body, 0, length);
        }
        in.close();
        out.flush();
        tmpFile.delete();
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
