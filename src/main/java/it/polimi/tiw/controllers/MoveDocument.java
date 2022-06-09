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


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        //redirect to login if not logged in
        String path = getServletContext().getContextPath();
        HttpSession session = request.getSession();

        User sessionUser = (User) session.getAttribute("currentUser");

        if (session.isNew() || sessionUser == null) {
            response.sendRedirect(path);
            return;
        }

        String requestAction = request.getParameter("requestAction");

        DocumentDAO docDAO = new DocumentDAO(connection);
        Document doc = null;

        if (requestAction.equals("chooseMovement")) {

            String folderName = request.getParameter("folderName");
            String subFolderName = request.getParameter("subFolderName");
            String documentName = request.getParameter("documentName");
            String type = request.getParameter("documentType");

            if( checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type) ){response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }

            try{
                doc = docDAO.getDocumentByKey(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName, documentName, type);
                if(doc == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                    return;
                }
            }catch(SQLException e){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover document");
                return;
            }

            List<Folder> allfolders = GoToHomePage.getFolderTree(response, session);

            path = "/WEB-INF/moveToHomePage.html";
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            ctx.setVariable("allfolders", allfolders );
            ctx.setVariable("doc", doc );
            ctx.setVariable("page", "Documents?"+request.getQueryString());
            ctx.setVariable("info", "you are moving document "+
                    doc.getDocumentName()+"."+doc.getType()+" from sub folder: "+doc.getSubFolderName()+
                    " select new destination: ");
            templateEngine.process(path, ctx, response.getWriter());

        } else if (requestAction.equals("updateFolder")) {

            String folderTarget = request.getParameter("folderTarget");
            String subFolderTarget = request.getParameter("subFolderTarget");

            //document's key origin

            String folderName = request.getParameter("folderName");
            String subFolderName = request.getParameter("subFolderName");
            String documentName = request.getParameter("documentName");
            String type = request.getParameter("documentType");

            if(checkIncorrect(folderName) || checkIncorrect(subFolderName) || checkIncorrect(documentName) || checkIncorrect(type)
                || checkIncorrect(folderTarget) || checkIncorrect(subFolderTarget)){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }


            //check if target is different from origin
            if(folderName.equals(folderTarget) && subFolderName.equals(subFolderTarget)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "target equals origin");
                return;
            }
            //check univocity
            boolean exists;
            try {
                exists = docDAO.exists(((User) session.getAttribute("currentUser")).getUsername(), folderTarget, subFolderTarget, documentName, type);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
                return;
            }
            if(exists){
                response.sendError(HttpServletResponse.SC_CONFLICT, "document already present");
                return;
            }

            //move
            try {
                docDAO.moveDocumentFromSubFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName, subFolderName, documentName, type, folderTarget, subFolderTarget);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to move document");
                return;
            }

            response.sendRedirect(getServletContext().getContextPath() + "/"+"Documents"+"?"+"folderName="+folderTarget+"&subFolderName="+subFolderTarget);

        // incorrect action
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