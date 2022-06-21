package it.polimi.tiw.controllers;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/Subscribe")
@MultipartConfig
public class Subscribe extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password1 = request.getParameter("password1");
        String password2 = request.getParameter("password2");
        UserDAO dao = new UserDAO(connection);
        ServletContext servletContext = getServletContext();

        //to repeat client side
        if(username == null || username.length()<=3 ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parameters invalid");
            return;
        }
        if(email == null || email.length()<=3 || !email.contains("@") || !email.contains(".")){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Email format invalid");
            return;
        }
        if(name == null || name.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parameters invalid");
            return;
        }
        if(password1 == null || password1.length()<=3 ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parameters invalid");
            return;
        }
        if(password1!=null && !password1.equals(password2)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parameters invalid5");
            return;
        }

        boolean exists=true;
        //check username
        try {
            exists = dao.existsUser(username);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Failure in database checking username");
            return;
        }
        //there is an other user with same username
        if(exists){
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getWriter().println("Username already in use");
            return;
        }
        //check email
        try {
            exists = dao.existsEmail(email);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Failure in database checking email");
            return;
        }
        //there is an other user with same email
        if(exists){
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getWriter().println("Email already in use");
            return;
        }
        //insert in database and return to login
        try{
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setName(name);
            dao.createUser(user,password1);
        }catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Failure in database subscribing");
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
}

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
