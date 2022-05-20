package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public void init() {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.connection = ConnectionHandler.getConnection(getServletContext());
        HttpSession session = request.getSession(false);
        if (session == null) {
            String path = getServletContext().getContextPath();
            response.sendRedirect(path);
        }

        List<Folder> allfolders = new ArrayList<>();
        // List<SubFolder> allsubfolders = new ArrayList<>();

        FolderDAO fService = new FolderDAO(connection);
        // SubFolderDAO sService = new SubFolderDAO(connection);

        try {
            if (session != null) {
                User user = (User) session.getAttribute("currentUser");
                String username = user.getUsername();
                allfolders = fService.getAllFolderOfUser(username);
                // allsubfolders = sService.getAllSubFolderOfFolder(username, "Prova1");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error in retrieving products from the database");
            return;
        }

        // Redirect to the Home page and add folders to the parameters
        String path = "/WEB-INF/HomePage.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("allfolders", allfolders);
        // ctx.setVariable("allsubfolders", allsubfolders);
        templateEngine.process(path, ctx, response.getWriter());
    }
}
