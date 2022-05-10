package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.DAO.SubFolderDAO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/ContentManager")
public class ContentManager extends HttpServlet {
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

        //redirect to login if not logged in
        String path = getServletContext().getContextPath() + "/login.html";
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(path);
            return;
        }

        Map<Folder, List<SubFolder>> subFolderMap = new HashMap<>();
        // get all folders of user
        User user = (User) session.getAttribute("currentUser");
        List<Folder> folders= new ArrayList<>();
        FolderDAO foDAO = new FolderDAO(connection);
        try {
            folders = foDAO.getAllFolderOfUser(user.getUsername());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover folders");
            return;
        }

        //TODO: getsubfolders and submit to page html
        SubFolderDAO subDAO = new SubFolderDAO(connection);
        for(Folder folder : folders) {
            try {
                subFolderMap.put(folder, subDAO.getAllSubFolderOfFolder(user.getUsername(), folder.getFolderName()));
            } catch(SQLException ex){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover sub folders");
                return;
            }
        }
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("subFolderMap", subFolderMap);
        path = "/WEB-INF/contentManagerPage.html";
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
