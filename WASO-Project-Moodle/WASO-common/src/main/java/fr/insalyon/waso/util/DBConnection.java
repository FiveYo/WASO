package fr.insalyon.waso.util;

import fr.insalyon.waso.util.exception.DBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    protected Connection connection;

    public DBConnection(String url, String user, String password, String... tables) throws DBException {

        //String url = "jdbc:mysql://localhost:3306/WASO-DB-Client";
        //String url = "jdbc:mysql://localhost:8889/WASO-DB-Client";
        //String user = "waso";
        //String password = "waso";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            this.connection = DriverManager.getConnection(url, user, password);
            
            for (String table : tables) {
                checkDB(table);
            }
        } catch (ClassNotFoundException ex) {
            throw new DBException("DB Driver not found.", ex);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new DBException("Could not connect to DB " + url, ex);
        } catch (IOException ex) {
            throw new DBException("Could not get DB scripts", ex);
        }

    }
    
    protected final void checkDB(String name) throws SQLException, IOException {
        DatabaseMetaData metadata = this.connection.getMetaData();
        ResultSet result = metadata.getTables(null, null, name, null);
        if (!result.next()) {
            Statement statement = this.connection.createStatement();
                        
            String createTableStatement = readSqlResource(name + ".structure");
            String insertDataStatement = readSqlResource(name + ".data");
            
            statement.executeUpdate(createTableStatement);
            statement.executeUpdate(insertDataStatement);
        }
    }

    public PreparedStatement buildPrepareStatement(String query) throws DBException {

        try {
            return this.connection.prepareStatement(query);

        } catch (SQLException ex) {
            throw new DBException("DB Query Exception", ex);
        }

    }

    public List<Object[]> launchQuery(String query, Object... parameters) throws DBException {

        try {
            PreparedStatement pstmt = this.connection.prepareStatement(query);
            int paramIndex = 0;
            //pstmt.setInt(1, ...);
            for (Object parameter : parameters) {
                pstmt.setObject(++paramIndex, parameter);
            }

            ResultSet results = pstmt.executeQuery();
            ResultSetMetaData columnMetaData = results.getMetaData();
            int columns = columnMetaData.getColumnCount();            
            
//            List<String[]> resultList = new ArrayList<String[]>();
//
//            while (results.next()) {
//                String[] row = new String[columns];
//                for (int col = 0; col < columns; col++) {
//                    row[col] = results.getString(col);
//                }
//                resultList.add(row);
//            }

            List<Object[]> resultList = new ArrayList<Object[]>();
            while (results.next()) {
                Object[] row = new Object[columns];
                for (int col = 1; col <= columns; col++) {
                    results.getObject(col);
                    switch (columnMetaData.getColumnType(col)) {
                        case Types.INTEGER:
                            row[col - 1] = results.wasNull() ? null : results.getInt(col);
                            break;
                        case Types.VARCHAR:
                            row[col - 1] = results.wasNull() ? null : results.getString(col);
                            break;
                        case Types.DOUBLE:
                            row[col - 1] = results.wasNull() ? null : results.getDouble(col);
                            break;
                        case Types.FLOAT:
                            row[col - 1] = results.wasNull() ? null : results.getFloat(col);
                            break;
                        case Types.BOOLEAN:
                            row[col - 1] = results.wasNull() ? null : results.getBoolean(col);
                            break;
                        case Types.TIMESTAMP:
                            row[col - 1] = results.wasNull() ? null : results.getTimestamp(col).getTime();
                            break;
                        case Types.NULL:
                            row[col - 1] = null;
                            break;
                    }
                }
                resultList.add(row);
            }

            pstmt.close();

            return resultList;

        } catch (SQLException ex) {
            throw new DBException("DB Query Exception", ex);
        }

    }
    
    protected static String readSqlResource(String name) throws IOException {
            InputStream fileContent = DBConnection.class.getResourceAsStream("/sql/" +  name + ".sql");
            BufferedReader input = new BufferedReader(new InputStreamReader(fileContent,JsonServletHelper.ENCODING_UTF8));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
    }

}
