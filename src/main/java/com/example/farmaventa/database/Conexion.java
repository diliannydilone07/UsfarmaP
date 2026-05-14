package com.example.farmaventa.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    Connection connection = null;

    String usuario = "dba";
    String contrase = "noviembre74";
    String db = "farmacia";
    String server = "DESKTOP-H0V2B5N";
    String puerto = "1433";

    public Connection establecerConexion() {
        try {
            String cadena = "jdbc:sqlserver://" + server + ":" + puerto + ";"
                    + "databaseName=" + db + ";"
                    + "encrypt=true;"
                    + "trustServerCertificate=true";
            connection = DriverManager.getConnection(cadena, usuario, contrase);
        } catch (Exception e) {
            System.err.println("Error en la conexion a la BD: " + e.getMessage());
        }
        return connection;
    }
}