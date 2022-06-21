package it.polimi.tiw.controllers.Create;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.SubFolder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/CreateDocument")
@MultipartConfig(maxFileSize = 16177215)
public class CreateDocument extends HttpServlet {
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
        String documentName = request.getParameter("documentName");
        String summury = request.getParameter("summury");
        Part filePart = request.getPart("body");
        String type ="none";
        InputStream inputStream = null; // input stream of the uploaded file
        String filename;
        if (filePart != null) {
            filename = filePart.getSubmittedFileName();
            type = FilenameUtils.getExtension(filename);
            inputStream = filePart.getInputStream();
        }
        if(type==null || type.equals("")){
            type="none";
        }

        if (inputStream == null || (inputStream.available()==0) ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("empty file");
            return;
        }

        //controlls to repeat client side

        if (subFolderName == null || subFolderName.length() <= 3 ||
                folderName == null || folderName.length() <= 3 ||
                documentName==null || documentName.length()<=3 ||
                summury==null || summury.isEmpty() ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        //controlls on folders and subfolders
        boolean exists=true;
        FolderDAO folderDAO = new FolderDAO(connection);
        SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
        //check if folder exists
        try{
            exists=folderDAO.existsFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName);
        }catch(SQLException e ){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in database");
            return;
        }
        if(exists==false){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }
        //check if sub folder exists
        try{
            exists=subFolderDAO.existsSubFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName);
        }catch(SQLException e ){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in database");
            return;
        }
        if(exists==false){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        DocumentDAO dao = new DocumentDAO(connection);
        //check folder name univocity
        try {
            exists = dao.exists(((User) session.getAttribute("currentUser")).getUsername(), folderName,
                    subFolderName, documentName, type);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
            return;
        }

        if(exists){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().println("you already have a similar document in this sub folder");
            return;
        }

        //insert in database and return to HomePage
        try {
            dao.insertDocument(((User) session.getAttribute("currentUser")).getUsername(), folderName,
                    subFolderName, documentName, type, summury, new Date(), inputStream);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
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
