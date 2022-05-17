package it.polimi.tiw.controllers.Create;

import it.polimi.tiw.DAO.DocumentDAO;
import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.SubFolder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class CreateDocument extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        boolean creationOK = true;
        //redirect to login if not logged in
        String path = getServletContext().getContextPath() + "/login.html";
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("currentUser") == null) {
            response.sendRedirect(path);
            return;
        }

        String username = request.getParameter("username");
        String folderName = request.getParameter("folderName");
        String subFolderName = request.getParameter("subFolderName");
        String documentName = request.getParameter("documentName");
        String type = request.getParameter("type");
        String summury = request.getParameter("summury");
        // byte[] body = request.getParameter("body");
        //CONTROLLI MANCANTII

        //to repeat client side
        if (subFolderName == null || subFolderName.length() <= 3) {
            ctx.setVariable("subFolderNameError", "sub folder name too short");
            creationOK = false;
        }

        boolean exists=true;
        DocumentDAO dao = new DocumentDAO(connection);
        Document doc = new Document();
        doc.setUsername(((User) session.getAttribute("currentUser")).getUsername());
        doc.setFolderName(folderName);
        doc.setSubFolderName(subFolderName);
        doc.setDocumentName(documentName);
        doc.setDate(new Date());
        doc.setSummury(summury);
        //doc.setBody(body);

        //check folder name univocity
        if(creationOK) {
            try {
                exists = dao.exists(doc);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
            }

            if(exists){
                ctx.setVariable("subFolderNameError", "you already have a similar document in this sub folder");
                creationOK=false;
            }
        }

        if(creationOK) {
            //insert in database and return to HomePage
            try {
                dao.insertDocument(doc);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database update documents");
            }
            ctx.setVariable("creationOK", "new document added");
            path="/GoToHomePage";
        }else{
            path="/WEB-INF//ContentManagerPage.html";
        }
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
