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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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

        boolean creationOK = true;

        //redirect to login if not logged in
        String path = getServletContext().getContextPath();
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("currentUser") == null) {
            response.sendRedirect(path);
            return;
        }

        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String summury = request.getParameter("summury");
        Part filePart = request.getPart("body");
        String type ="";
        InputStream inputStream = null; // input stream of the uploaded file
        String mimeType = null;
        String filename = filePart.getSubmittedFileName();
        if (filePart != null) {
            filename = filePart.getSubmittedFileName();
            type = FilenameUtils.getExtension(filename);
            inputStream = filePart.getInputStream();
            mimeType = getServletContext().getMimeType(filePart.getSubmittedFileName());
        }

        if (inputStream == null || (inputStream.available()==0) ) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty file uploaded");
            return;
        }

        //controlls to repeat client side

        if (subFolderName == null || subFolderName.length() <= 3 ||
                folderName == null || folderName.length() <= 3 ||
                documentName==null || documentName.length()<=3 ||
                summury==null || summury.isEmpty() ) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name format error");
            return;
        }
        System.out.println("ok controlli iniziali fatti, type: "+type);

        //controlls on folders and subfolders
        boolean exists=true;
        FolderDAO folderDAO = new FolderDAO(connection);
        SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
        //check if folder exists
        try{
            exists=folderDAO.existsFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName);
        }catch(SQLException e ){
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
        }
        if(exists==false){
            creationOK=false;
            request.setAttribute("inexistentFolderFromDocument", "you are creating a document in an inexistent folder");
        }
        //check if sub folder exists
        try{
            exists=subFolderDAO.existsSubFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName);
        }catch(SQLException e ){
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking sub folders");
        }
        if(exists==false){
            creationOK=false;
            request.setAttribute("inexistentSubFolderFromDocument", "you are creating a document in an inexistent sub folder");
        }

        DocumentDAO dao = new DocumentDAO(connection);
        //check folder name univocity
        if(creationOK) {
            try {
                exists = dao.exists(((User) session.getAttribute("currentUser")).getUsername(), folderName,
                        subFolderName, documentName, type);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
            }

            if(exists){
                request.setAttribute("documentNameError", "you already have a similar document in this sub folder");
                creationOK=false;
            }
        }

        if(creationOK) {
            //insert in database and return to HomePage
            try {
                dao.insertDocument(((User) session.getAttribute("currentUser")).getUsername(), folderName,
                        subFolderName, documentName, type, summury, new Date(), inputStream);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database update documents");
            }
            path = getServletContext().getContextPath() + "/GoToHomePage";
            session.setAttribute("creationOK", "new document uploaded");
            response.sendRedirect(path);
        }else{
            RequestDispatcher dispatcher = getServletContext()
                    .getRequestDispatcher("/ContentManager");
            dispatcher.forward(request,response);
        }
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
