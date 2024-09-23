/*
 * 
 * ATTENZIONE: il codice di questa classe dipende dalla corretta definizione delle
 * risorse presente nei file context.xml (Resource) e web.xml (resource-ref).
 * Si veda la servlet principale per informazioni sulla configurazione del database
 * 
 * WARNING: this class needs the definition of an external data source to work correctly.
 * See the Resource element in context.xml and the resource-ref element in web.xml
 * See the application main servlet for information of the database configuration
 * 
 */
package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.result.StreamResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Giuseppe Della Penna
 */
public class Download extends HttpServlet {

    private static final String GET_FILE_QUERY = "SELECT * FROM files WHERE ID=?";

    @Resource(name = "jdbc/webdb")
    private DataSource ds;

    private void action_download(HttpServletRequest request, HttpServletResponse response) throws IOException, NamingException, SQLException {
        Integer res = (Integer) request.getAttribute("resid");
        if (res != null) {
            StreamResult result = new StreamResult(getServletContext());
            try (Connection c = ds.getConnection();
                    PreparedStatement s = c.prepareStatement(GET_FILE_QUERY)) {
                s.setInt(1, res);
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        try (InputStream is = new FileInputStream(
                                getServletContext().getInitParameter("uploads.directory") + File.separatorChar + rs.getString("localfile"))) {
                            request.setAttribute("contentType", rs.getString("type"));
                            result.setResource(is, rs.getLong("size"), rs.getString("name"));
                            result.activate(request, response);
                        }
                    } else {
                        ServletHelpers.handleError("Resurce not found in file database", request, response, getServletContext());
                    }
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        try {
            int res = SecurityHelpers.checkNumeric(request.getParameter("res"));
            request.setAttribute("resid", res);
            action_download(request, response);
        } catch (NumberFormatException ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        } catch (IOException ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        } catch (NamingException ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        } catch (SQLException ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
