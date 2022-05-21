package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/MoveDocument")
public class MoveDocument extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection = null;
    private Document doc = null;

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        connection = ConnectionHandler.getConnection(getServletContext());

        //redirect to login if not logged in
        String path = getServletContext().getContextPath() + "/";
        HttpSession session = request.getSession();

        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.sendRedirect(path);
            return;
        }

        // TODO: check if it's a best practice
        String requestAction = request.getParameter("requestAction");

        DocumentDAO docDAO = new DocumentDAO(connection);

        if (requestAction.equals("chooseMovement")) {

            String username = request.getParameter("username");
            String folderName = request.getParameter("folderName");
            String subFolderName = request.getParameter("subFolderName");
            String documentName = request.getParameter("documentName");
            String type = request.getParameter("documentType");

            if(checkIncorrect(username) || checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type) ){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }

            try{
                this.doc = docDAO.getDocumentByKey(username, folderName, subFolderName, documentName, type);
                if(doc == null){
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                } else if (!(doc.getUsername().equals(sessionUser.getUsername()))){
                    //example: user changes parameters to access resources of other users
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
                    return;
                }
            }catch(SQLException e){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover document");
                return;
            }

            List<Folder> allfolders = GoToHomePage.getFolderTree(request, response, session);

            path = "/WEB-INF/moveToHomePage.html";
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            ctx.setVariable("allfolders", allfolders );
            ctx.setVariable("docToMove", doc );
            templateEngine.process(path, ctx, response.getWriter());

        } else if (requestAction.equals("updateFolder")) {

            String folderName = request.getParameter("folderName");
            String subFolderName = request.getParameter("subFolderName");

            if (this.doc != null) {
                // TODO: check if user has privileges
                try {
                    docDAO.moveDocumentFromSubFolder(doc, folderName, subFolderName);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                // TODO: check if doc already exists in folder --> subfolder

                path = "/WEB-INF/documentPage.html";
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("document", doc );
                templateEngine.process(path, ctx, response.getWriter());

            } else {
                // TODO: choose right ERROR code
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Illegal order in requests to the server (doc not initialized)");
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal request action cannot be executed ");
            return;
        }
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