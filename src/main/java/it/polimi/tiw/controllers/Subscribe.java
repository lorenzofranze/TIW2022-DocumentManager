package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.UserDAO;
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/Subscribe")
public class Subscribe extends HttpServlet {
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
        doPost(request, response);
    }
    //todo: add 4 variables in html thymeleaf
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String name = request.getParameter("name");
        String password1 = request.getParameter("password1");
        String password2 = request.getParameter("password2");
        boolean registationOK=true;
        UserDAO dao = new UserDAO(connection);
        String path =null;
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        //to repeat client side
        if(username == null || username.length()<=3 ) {
            ctx.setVariable("usernameError", "username at least 4 characters");
            registationOK=false;
        }
        if(name == null || name.isEmpty()) {
            ctx.setVariable("nameError", "name can't be empty");
            registationOK = false;
        }
        if(password1 == null || password1.length()<=3 ){
            ctx.setVariable("passwordError", "password too short");
            registationOK=false;
        }
        if(password1!=null && !password1.equals(password2)){
            ctx.setVariable("passwordDifferentError", "passwords have different values");
            registationOK=false;
        }

        boolean exists=true;
        if(registationOK) {
            try {
                exists = dao.existsUser(username);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking user");
            }
            //there is an other user with same username
            if(exists){
                ctx.setVariable("usernameError", "username already in use");
                registationOK=false;
            }
        }
        if(registationOK){
            //insert in database and return to login
            try{
                User user = new User();
                user.setUsername(username);
                user.setName(name);
                dao.createUser(user,password1);
            }catch(SQLException e){
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database subscribing");
            }
            path = "loginPage";
            ctx.setVariable("registationOK", "registation completed");
        }else{
            //return to subscribePage
            path = "subscribePage";
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
