package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.*;
import it.polimi.tiw.beans.*;
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

@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet{
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            String path = getServletContext().getContextPath();
            response.sendRedirect(path);
        }

        List<Folder> allFolders = null;
        List<SubFolder> allSubFolders = null;

        FolderDAO fService = new FolderDAO(connection);
        SubFolderDAO sService = new SubFolderDAO(connection);

        String username = ((User) session.getAttribute("username")).getUsername();

        try {
            allFolders = fService.getAllFolderOfUser(username);
            for (Folder folder : allFolders) {
                allSubFolders = sService.getAllSubFolderOfFolder(username, folder.getFolderName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error in retrieving folders and subFolders from the database");
            return;
        }
        // TODO: actual implementation of methods
        String path = "/WEB-INF/Home.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("allproducts", allFolders);
        ctx.setVariable("topproducts", allSubFolders);
        templateEngine.process(path, ctx, response.getWriter());
    }
}
