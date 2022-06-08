package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.io.IOUtils;
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
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

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
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String type = request.getParameter("documentType");

        if(checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type) ){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        }

        DocumentDAO docDAO = new DocumentDAO(connection);
        InputStream dbStream =null;
        try{
            dbStream = docDAO.getDocumentData(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName, documentName, type);
            if(dbStream == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                return;
            }
        }catch(SQLException e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover document");
            return;
        }

        response.setContentType("text/plain");
        response.setHeader("Content-disposition","attachment; filename="+documentName+"."+type);


        File tmpFile = new File("tmp"+"."+type);
        OutputStream streamfile = new FileOutputStream(tmpFile);
        IOUtils.copy(dbStream, streamfile);
        dbStream.close();
        streamfile.close();
        //now file created

        //send file to client
        byte[] buffer = new byte[1024];
        OutputStream out = response.getOutputStream();
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        int length;
        while ((length = fileInputStream.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }

        fileInputStream.close();
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
