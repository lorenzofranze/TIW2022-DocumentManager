package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Folder;
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

        /*
        HttpSession session = request.getSession(false);
        if (session == null) {
            String path = getServletContext().getContextPath();
            response.sendRedirect(path);
        }
        */

        List<Folder> allfolders = new ArrayList<Folder>();
        for (int i = 0; i < 3; i++) {
            Folder folder = new Folder();
            folder.setFolderName(String.valueOf(i));
            folder.setUsername("pushi");
            allfolders.add(folder);
        }

        /*
        FolderDAO folderService = new FolderDAO(connection);
        try {
            assert session != null;
            allfolders = folderService.getAllFolderOfUser(String.valueOf(session.getAttribute("currentUser")));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error in retrieving folders from the database");
            return;
        }
        */

        // Redirect to the Home page and add folders to the parameters
        String path = "/WEB-INF/HomePage.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("allfolders", allfolders);
        templateEngine.process(path, ctx, response.getWriter());
    }
}
