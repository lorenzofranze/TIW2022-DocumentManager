package it.polimi.tiw.controllers.Create;

import it.polimi.tiw.DAO.FolderDAO;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.RequestDispatcher;
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
import java.util.Date;

@WebServlet("/CreateFolder")
public class CreateFolder extends HttpServlet {
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

        //to repeat client side
        if(folderName == null || folderName.length()<=3 ) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name format error");
            return;
        }

        boolean exists=true;
        FolderDAO dao = new FolderDAO(connection);
        //check folder name univocity
        if(creationOK) {
            try {
                exists = dao.existsFolder(((User) session.getAttribute("currentUser")).getUsername(), folderName);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database checking folders");
            }
            //there is an other folder with same name
            if(exists){
                request.setAttribute("folderNameError", "you already have a folder with this name ");
                creationOK=false;
            }
        }
        if(creationOK) {
            //insert in database and return to HomePage
            try {
                Folder folder = new Folder();
                folder.setUsername(((User) session.getAttribute("currentUser")).getUsername());
                folder.setFolderName(folderName);
                folder.setDate(new Date());
                dao.insertFolder(folder);
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database update folders");
            }
            path = getServletContext().getContextPath() + "/GoToHomePage";
            session.setAttribute("creationOK", "new folder created");
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
