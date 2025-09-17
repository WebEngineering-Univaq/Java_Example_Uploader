/*
 * ATTENZIONE: il codice di questa classe dipende dalla corretta definizione delle
 * risorse presente nei file context.xml (Resource) 
 *
 * ATTENZIONE: in codice fa uso del driver per MySQL versione 8. Se questo driver
 * è già presente nel vostro server, non sarà necessario aggiungerlo come libreria
 * al progetto (creandone una copia "privata"). Tuttavia, se il server disponesse solo
 * della versione 5 del driver (ancora molto comune), ricevereste un errore del tipo
 * "impossibile trovare la classe com.mysql.cj.jdbc.Driver", e in tal caso dovreste
 * inserire il jar del connector/J 8 nella vostra applicazione
 *
 * ATTENZIONE: il codice fa uso di un database configurato come segue:
 * - database 'uploader' su DBMS MySQL in esecuzione su localhost
 * - utente 'website' con password 'webpass' autorizzato nel DBMS 
 *   a leggere i dati del suddetto database
 * - la seguente tabella
 *
 * CREATE TABLE  `uploader`.`files` (
 * `name` varchar(255) NOT NULL,
 * `type` varchar(255) NOT NULL,
 * `size` int(11) NOT NULL,
 * `localfile` varchar(255) NOT NULL,
 * `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 * `ID` int(11) NOT NULL AUTO_INCREMENT,
 * `digest` varchar(255) NOT NULL,
 * PRIMARY KEY (`ID`)
 * )  
 * 
 * WARNING: this class needs the definition of an external data source to work correctly.
 * See the Resource element in context.xml 
 * 
 * WARNING: this class uses the MySQL driver version 8. If this driver is already present 
 * on your server, it will not be necessary to add it as a library to the project 
 * (creating a "private" copy). However, if the server has version 5 of the driver pre-installed
 * (still very common), you will receive an error like "class com.mysql.cj.jdbc.Driver not found", 
 * and in this case you should add the connector/J 8 jar in your application libraries.
 *
 * WARNING: the code makes use of a database configured as follows:
 * - 'uploader' database on a MySQL DBMS running on localhost
 * - user 'website' with password 'webpass' authorized in the DBMS to read the 
 *   data of the aforementioned database
 * - the following table
 *
 * CREATE TABLE  `uploader`.`files` (
 * `name` varchar(255) NOT NULL,
 * `type` varchar(255) NOT NULL,
 * `size` int(11) NOT NULL,
 * `localfile` varchar(255) NOT NULL,
 * `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 * `ID` int(11) NOT NULL AUTO_INCREMENT,
 * `digest` varchar(255) NOT NULL,
 * PRIMARY KEY (`ID`)
 * )  
 *
 */
package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.result.HTMLResult;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Giuseppe Della Penna
 */
public class EsempioUploader extends HttpServlet {

    private static final String ALL_FILES_QUERY = "SELECT ID,name,size,digest,updated,date_format(updated,'%d/%m/%Y %H:%i:%s') AS formatted_updated FROM files";

    @Resource(name = "jdbc/uploader")
    private DataSource ds;

    private String humanReadableFileSize(long size) {
        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB", "PB", "EB"};
        if (size <= 1) {
            return size + " byte";
        }
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("File Repository");

        result.appendToBody("<h1>File Repository</h1>");
        try (Connection c = ds.getConnection();
                //Il tipo TYPE_SCROLL_INSENSITIVE (o SENSITIVE, cioè non FORWARD_ONLY) è necessario per poter usare i metodi last() e getRow()
                //The TYPE_SCROLL_INSENSITIVE  (or SENSITIVE, i.e. not FORWARD_ONLY) is required to use the last() and getRow() methods
                PreparedStatement s2 = c.prepareStatement(ALL_FILES_QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            try (ResultSet r = s2.executeQuery()) {
                result.appendToBody("<h2>Current repository contents</h2>");

                //un modo per conoscere il numero di record prelevati da una query
                //a way the get the number of records fetched by a query
                int count = 0;
                if (r.last()) { //move to the last record if present
                    count = r.getRow(); //get the row number
                    r.beforeFirst(); //move to the initial position
                }
                result.appendToBody("<p>This repository holds " + count + " items.</p>");

                result.appendToBody("<table border=\"1\">");
                result.appendToBody("<thead><th>ID</th><th>Name</th><th>Size</th><th>Modified</th><th>Digest</th></thead>");
                while (r.next()) {
                    //un modo contorto per leggere il timestamp con la corretta timezone
                    //a complex way to get the timestamp with the correct timezone
                    ZonedDateTime updated = r.getTimestamp("updated").toInstant().atZone(ZoneId.of("GMT"));
                    result.appendToBody("<tr><td>" + r.getInt("ID")
                            + "</td><td>" + HTMLResult.sanitizeHTMLOutput(r.getString("name"))
                            + "</td><td>" + humanReadableFileSize(r.getInt("size"))
                            //è possibile prendere il timestamp dal DB e formattarlo localmente (ma attenzione alla time zone!)                            
                            //you can get the timestamp from the DB and format it locally (but be aware of the time zone!)
                            + "</td><td>" + updated.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                            //...o farlo formattare dal DB tramite l'SQL!
                            //...or have it formatted by the DB using appropriate SQL!
                            //+ "</td><td>" + r.getString("formatted_updated")
                            + "</td><td>" + r.getString("digest")
                            + "</td></tr>");
                }
                result.appendToBody("</table>");

            }
        }
//
        result.appendToBody("<form method='get' action='download' >");
        result.appendToBody("<p>Write the file ID to download <input type='text' name='res'/>");
        result.appendToBody("<input type='submit' name='submit' value='download'/></p>");
        result.appendToBody("</form>");
//
        result.appendToBody("<h2>Upload new content</h2>");
        result.appendToBody("<form method='post' action='upload' enctype='multipart/form-data'>");
        result.appendToBody("<p>Select the file to upload <input type='file' name='filetoupload'/>");
        result.appendToBody("<input type='submit' name='submit' value='upload'/></p>");
        result.appendToBody("</form>");

        result.activate(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            action_default(request, response);
        } catch (Exception ex) {
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
