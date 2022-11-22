/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.Strabon;
import eu.earthobservatory.utils.Format;
import eu.earthobservatory.utils.StrabonDBEngine;

public class StrabonBeanWrapper implements org.springframework.beans.factory.DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper.class);

    private static final String FILE_PROTOCOL = "file";

    private String serverName;
    private int port;
    private String databaseName;
    private String user;
    private String password;
    private String dbBackend;
    private String googlemapskey;
    private int maxLimit;
    private boolean loadFromFile;
    private String prefixes;
    private Strabon strabon = null;
    private String gChartString = " ";
    private boolean checkForLockTable;
    private List<StrabonBeanWrapperConfiguration> entries;
    private boolean strabonConnectionDetailsHaveBeenModified = false;

    public StrabonBeanWrapper(String databaseName, String user, String password,
            int port, String serverName, boolean checkForLockTable, String dbBackend,
            String googlemapskey, int maxLimit, boolean loadFromFile, String prefixes, List<List<String>> args) {
        setConnectionDetails(databaseName, user, password, String.valueOf(port), serverName, dbBackend, googlemapskey);
        this.checkForLockTable = checkForLockTable;
        this.maxLimit = maxLimit;
        this.loadFromFile = loadFromFile;
        this.prefixes = prefixes;
        this.entries = new ArrayList<StrabonBeanWrapperConfiguration>(args.size());

        Iterator<List<String>> entryit = args.iterator();

        while (entryit.hasNext()) {
            List<String> list = entryit.next();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                int items = 0;
                //Header:label        
                //Bean  :label      bean         
                //Entry :label      bean         statement    format       title      handle
                String param1 = "", param2 = "", param3 = "", param4 = "", param5 = "", param6 = "";

                if (it.hasNext()) {
                    param1 = it.next();
                    items++;
                }
                if (it.hasNext()) {
                    param2 = it.next();
                    items++;
                }
                if (it.hasNext()) {
                    param3 = it.next();
                    items++;
                }
                if (it.hasNext()) {
                    param4 = it.next();
                    items++;
                }
                if (it.hasNext()) {
                    param5 = it.next();
                    items++;
                }
                if (it.hasNext()) {
                    param6 = it.next();
                    items++;
                }

                if (items == 1) {
                    //the first element corresponds to the label
                    StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param1);
                    this.entries.add(entry);
                } else if (items == 2) {
                    //the first element corresponds to the label
                    StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param1, param2);
                    this.entries.add(entry);
                } else if (items == 6) {
                    StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param3, param1, param4, param2, param5, param6);
                    this.entries.add(entry);
                }
            }
        }
        init();
    }

    public boolean init() {
        /* if the connection details have been modified
        ** we should close the current Strabon (if any)
        ** and create a new one
         */
        if (this.strabonConnectionDetailsHaveBeenModified) {
            this.closeConnection();
            try {
                logger.warn("[StrabonEndpoint] Strabon not initialized yet.");
                logger.warn("[StrabonEndpoint] Initializing Strabon.");
                //logger.info("[StrabonEndpoint] Connection details:\n" + this.getDetails());

                // initialize Strabon according to user preference
                switch (StrabonDBEngine.getStrabonDBEngine(dbBackend)) {
                    case MonetDB : this.strabon = new eu.earthobservatory.runtime.monetdb.Strabon(databaseName, user, password, port, serverName, checkForLockTable);
                                   break;
                    case PostGIS : // use PostGIS as the default database backend
                    default :
                                    this.strabon = new eu.earthobservatory.runtime.postgis.Strabon(databaseName, user, password, port, serverName, checkForLockTable);
                }

                logger.info("[StrabonEndpoint] Created new connection with details:\n" + this.getDetails());
                /* we should clear the <strabonConnectionDetailsHaveBeenModified> flag
                ** since the connection to Strabon has been made
                 */
                this.strabonConnectionDetailsHaveBeenModified = false;

                installSIGTERMHandler(this.strabon);

            } catch (Exception e) {
                logger.error("[StrabonEndpoint] Exception occured while creating Strabon. {}\n{}", e.getMessage(), this.getDetails());
                return false;
            }
        }
        return true;
    }

    /**
     * Registers a handler for SIGTERM signals, like Ctrl-C. One may send such a
     * signal at the command prompt, when running Strabon Endpoint from the
     * command line, i.e., using the endpoint-exec module.
     *
     * @param strabon The strabon instance
     */
    private static void installSIGTERMHandler(final Strabon strabon) {
        if (logger.isDebugEnabled()) {
            logger.info("[StrabonEndpoint] Installing handler for SIGTERM signals...");
        }

        // register the handler
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                // just call the Strabon.close() method
                strabon.close();
            }
        });

        if (logger.isDebugEnabled()) {
            logger.info("[StrabonEndpoint] Handler for SIGTERM signals installed successfully.");
        }
    }

    public Strabon getStrabon() {
        return strabon;
    }

    public void setStrabon(Strabon strabon) {
        this.strabon = strabon;
    }

    public void closeConnection() {
        if (strabon != null) {
            logger.info("[StrabonEndpoint] Closing existing connection with details:\n" + this.getDetails());
            strabon.close();
            strabon = null;
        }
    }

    public void destroy() throws Exception {
        if (strabon != null) {
            strabon.close();

            // deregister jdbc driver
            strabon.deregisterDriver();
        }
    }

    public void query(String queryString, String answerFormatStrabon, OutputStream out)
            throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
        logger.info("[StrabonEndpoint] Received SELECT query.");
        if ((this.strabon == null) && (!init())) {
            throw new RepositoryException("Could not connect to Strabon.");
        }
        if (answerFormatStrabon.equalsIgnoreCase(Format.PIECHART.toString()) || answerFormatStrabon.equalsIgnoreCase(Format.AREACHART.toString())
                || answerFormatStrabon.equalsIgnoreCase(Format.COLUMNCHART.toString())) {
            TupleQueryResult result = (TupleQueryResult) strabon.query(queryString, Format.fromString(answerFormatStrabon), strabon.getSailRepoConnection(), out);
            List<String> bindingNames = result.getBindingNames();
            if (bindingNames.size() != 2 && answerFormatStrabon.equalsIgnoreCase(Format.PIECHART.toString())) {
                logger.error("Strabon endpoint: to display results in a pie chart, exactly TWO variables must be projected");
            } else {
                if (answerFormatStrabon.equalsIgnoreCase(Format.PIECHART.toString())) {

                    ArrayList<String> arr = new ArrayList<String>(2);
                    arr.add(0, bindingNames.get(0));
                    arr.add(1, bindingNames.get(1));

                    gChartString = "var data = new google.visualization.DataTable();";
                    gChartString += "data.addColumn('string',\'" + arr.get(0) + "');\n";
                    gChartString += "data.addColumn('number',\'" + arr.get(1) + "');\n";

                    while (result.hasNext()) {
                        BindingSet bindings = result.next();
                        arr.add(0, bindings.getValue(bindingNames.get(0)).stringValue());
                        arr.add(1, bindings.getValue(bindingNames.get(1)).stringValue());

                        gChartString += "data.addRow([\'" + withoutPrefix(arr.get(0)) + "\', "
                                + arr.get(1).replace("\"", "").replace("^^", "").replace("<http://www.w3.org/2001/XMLSchema#integer>", "") + "]);\n";
                    }
                    gChartString += "var options = {'title':'','width':1000, 'height':1000, is3D: true};\n";
                    gChartString += "var chart = new google.visualization.PieChart(document.getElementById('chart_div'));\n";

                } else {

                    String chartType;
                    int varNum = bindingNames.size();

                    gChartString = "var data = google.visualization.arrayToDataTable([[";
                    for (int j = 0; j < varNum; j++) {
                        String chartValue = bindingNames.get(j);
                        gChartString += "'" + chartValue + "'";

                        if (j != varNum - 1) {
                            gChartString += ",";
                        }
                    }
                    gChartString += "],";

                    while (result.hasNext()) {
                        BindingSet bindings = result.next();
                        gChartString += "[";
                        for (int j = 0; j < varNum; j++) {

                            String chartValue = bindings.getValue(bindingNames.get(j)).stringValue();
                            if (j == 0) { //the first variable is a string variable.
                                gChartString += "'" + withoutPrefix(chartValue).replace("\"", "") + "'";
                            } else { //numeric value
                                gChartString += withoutPrefix(chartValue).replace("\"", "");
                            }
                            if (j != varNum - 1) {
                                gChartString += ",";
                            }
                        }
                        gChartString += "],";
                    }
                    if (answerFormatStrabon.equalsIgnoreCase(Format.AREACHART.toString())) {
                        chartType = "AreaChart";
                    } else {
                        chartType = "ColumnChart";
                    }
                    gChartString += "]);";
                    gChartString += " var options = {title: '', hAxis: {title:'" + bindingNames.get(0) + "',  titleTextStyle: {color: \'red\'}}};";
                    gChartString += "var chart = new google.visualization." + chartType + "(document.getElementById('chart_div')); \n";

                }

            }
        } else {
            strabon.query(queryString, Format.fromString(answerFormatStrabon), strabon.getSailRepoConnection(), out);
        }

    }

    /**
     * Wrapper around Strabon.describeOp which takes an OutputStream to use for
     * writing the answer to a DESCRIBE query.
     *
     * @param queryString
     * @param answerFormatStrabon
     * @param out
     * @throws MalformedQueryException
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws TupleQueryResultHandlerException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void describe(String queryString, String answerFormatStrabon, OutputStream out)
            throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
        logger.info("[StrabonEndpoint] Received DESCRIBE query.");
        if ((this.strabon == null) && (!init())) {
            throw new RepositoryException("Could not connect to Strabon.");
        }

        strabon.describe(queryString, answerFormatStrabon, strabon.getSailRepoConnection(), out);
    }

    public Object update(String updateString, String answerFormatStrabon)
            throws MalformedQueryException, RepositoryException, QueryEvaluationException,
            TupleQueryResultHandlerException, IOException, ClassNotFoundException {
        logger.info("[StrabonEndpoint] Received UPDATE query.");
        logger.info("[StrabonEndpoint] Answer format: " + answerFormatStrabon);

        if ((this.strabon == null) && (!init())) {
            throw new RepositoryException("Could not connect to Strabon.");
        }

        strabon.update(updateString, strabon.getSailRepoConnection());
        return "OK!";
    }

    /**
     * Store the given data in the given format into Strabon repository. If url
     * is true, then input comes from a URL.
     *
     * Returns true on success, false otherwise.
     *
     * @param source_data
     * @param format
     * @param url
     * @return
     * @throws MalformedQueryException
     * @throws RepositoryException
     * @throws InvalidDatasetFormatFault
     * @throws RDFHandlerException
     * @throws RDFParseException
     * @throws QueryEvaluationException
     * @throws TupleQueryResultHandlerException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean store(String src, String context, String format, Boolean inference, Boolean url) throws RepositoryException, RDFParseException, RDFHandlerException, IOException, InvalidDatasetFormatFault {
        logger.info("[StrabonEndpoint] Received STORE request.");

        if ((this.strabon == null) && (!init())) {
            throw new RepositoryException("Could not connect to Strabon.");
        }

        if (url) {
            URL source = new URL(src);
            if (source.getProtocol().equalsIgnoreCase(FILE_PROTOCOL) && !loadFromFile) {
                // it would be a security issue if we read from the server's filesystem
                throw new IllegalArgumentException("The protocol of the URL should be one of http or ftp.");
            }

        }

        strabon.storeInRepo(src, null, context, format, inference);

        logger.info("[StrabonEndpoint] STORE was successful.");

        return true;
    }

    public void setConnectionDetails(String databaseName, String user, String password, String port, String serverName, String dbBackend, String googlemapskey) {
        /* validate-sanitize certain Strabon connection properties
        ** dbBackend - must be one of the supported Db engines, or set it to PostGIS
        ** port - must be an integer, or set it to dbBackend's default port
         */
        if (StrabonDBEngine.getStrabonDBEngine(dbBackend) == null) {
            logger.warn("[StrabonEndpoint] Unknown database backend \"" + dbBackend + "\". Assuming PostGIS.");
            dbBackend = StrabonDBEngine.PostGIS.getName();
        }
        try {
            int tmp_port = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = String.valueOf(StrabonDBEngine.getStrabonDBEngine(this.dbBackend).getDefaultPort());
        }

        /* set the <strabonConnectionDetailsHaveBeenModified> flag if
        ** any of the Strabon connection properties have changed
         */
        this.strabonConnectionDetailsHaveBeenModified = !(dbBackend.equalsIgnoreCase(this.dbBackend)
                && serverName.equalsIgnoreCase(this.serverName)
                && port.equalsIgnoreCase(String.valueOf(this.port))
                && databaseName.equals(this.databaseName)
                && user.equalsIgnoreCase(this.user)
                && password.equals(this.password));
        this.dbBackend = dbBackend;
        this.serverName = serverName;
        this.port = Integer.parseInt(port);
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.googlemapskey = googlemapskey;
        this.checkForLockTable = true;
    }

    public String getUsername() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDBEngine() {
        return dbBackend;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return serverName;
    }

    private String getDetails() {
        String details = "-----------------------------------------\n";
        details += "host     : " + serverName + "\n";
        details += "port     : " + port + "\n";
        details += "database : " + databaseName + "\n";
        details += "user     : " + user + "\n";
        details += "password : " + password + "\n";
        details += "dbengine : " + dbBackend + "\n";
        details += "googlemapskey : " + googlemapskey + "\n";
        details += "-----------------------------------------\n";

        return details;
    }

    public List<StrabonBeanWrapperConfiguration> getEntries() {
        return this.entries;
    }

    public void setEntries(List<StrabonBeanWrapperConfiguration> entries) {
        this.entries = entries;
    }

    public StrabonBeanWrapperConfiguration getEntry(int i) {
        if (i < 0 || i >= this.entries.size()) {
            return null;
        }

        return this.entries.get(i);
    }

    /*
	 * Limit the number of solutions returned.
	 * */
    public String addLimit(String queryString, String maxLimit) {
        String limitedQuery = queryString;
        String lowerLimit = null;
        int max;

        if (maxLimit == null) {
            max = this.maxLimit;
        } else {
            max = Integer.valueOf(maxLimit);
        }

        if (max > 0) {
            queryString = queryString.trim();
            Pattern limitPattern = Pattern.compile("limit(\\s*)(\\d+)(\\s*)(offset(\\s*)\\d+)?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher limitMatcher = limitPattern.matcher(queryString);

            // check whether the query contains a limit clause
            if (limitMatcher.find()) {
                Pattern rowsNumberPattern = Pattern.compile("\\d+");
                Matcher rowsNumberMatcher = rowsNumberPattern.matcher(limitMatcher.group());
                rowsNumberMatcher.find();

                // if the initial limit is greater than the maximum, set it to the maximum
                if (Integer.valueOf(rowsNumberMatcher.group()) > max) {
                    lowerLimit = rowsNumberMatcher.replaceFirst(String.valueOf(max));
                    limitedQuery = limitMatcher.replaceFirst(lowerLimit);
                }
            } else // add a limit to the query 
            {
                limitedQuery = queryString + " limit " + max;
            }
        }
        return limitedQuery;
    }

    public String getPrefixes() {
        return prefixes;
    }

    public String getgChartString() {
        return gChartString;
    }

    public void setgChartString(String gChartString) {
        this.gChartString = gChartString;
    }

    public String withoutPrefix(String inputURI) {
        int index;

        if (!inputURI.contains("http")) { //plain literal case- no prefixes to remove
            return inputURI;
        } else { //URI case
            //removing prefixes so that they will not be displayed in the chart
            if (inputURI.lastIndexOf('#') > inputURI.lastIndexOf('/')) {
                index = inputURI.lastIndexOf('#') + 1;
            } else {
                index = inputURI.lastIndexOf("/") + 1;
            }

            int endIndex = inputURI.length();
            return inputURI.substring(index, endIndex);

        }
    }

    public String getGooglemapskey() {
        return googlemapskey;
    }

    public String googleMapsAPIScriptSourceForJSP() {
        /* returns the correct script source string to use in JSP
           i.e. query.jsp
           If the googlemapskey is null then assumes this is an already
           registered host for Google, therefore uses the old script source,
           otherwise uses the new script source
         */
        String scriptSource;
        if (googlemapskey.equals("") || googlemapskey.equals("null")) {
            scriptSource = "http://maps.googleapis.com/maps/api/js?sensor=false";
        } else {
            // scriptSource = "https://maps.googleapis.com/maps/api/js?key=" + googlemapskey + "&amp;callback=initMap";
            scriptSource = "https://maps.googleapis.com/maps/api/js?key=" + googlemapskey;
        }
        return scriptSource;
    }
}
