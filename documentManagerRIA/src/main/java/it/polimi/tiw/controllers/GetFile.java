package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
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
import java.util.Arrays;

@WebServlet("/GetFile")
@MultipartConfig
public class GetFile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("invocato");
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
        String type = request.getParameter("documentType");

        if(checkIncorrect(username) || checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type) ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        //user changes parameters to access resources of other users:
        if(!username.equals(sessionUser.getUsername())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("User not allowed");
            return;
        }

        DocumentDAO docDAO = new DocumentDAO(connection);
        InputStream dbStream =null;
        try{
            dbStream = docDAO.getDocumentData(username, folderName, subFolderName, documentName, type);
            if(dbStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("Resource not found");
                return;
            }
        }catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to recover document");
            return;
        }
        /*
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/octet-stream");
        //response.setCharacterEncoding("UTF-8");
         */

        response.setContentType("plain/text");
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        doGet(request, response);
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
