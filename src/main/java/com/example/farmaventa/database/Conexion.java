package com.example.farmaventa.database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Conexion {
    Connection connection = null;

    String usuario = "dba";
    String contrase = "noviembre74";
    String db = "farmacia";
    String server = "localhost";
    String puerto = "1433";

    String cadena = "jdbc:sqlserver://" + server + "." + puerto + "/" + db;

    public Connection establecerConexion() {
        try {
            String cadena = "jdbc:sqlserver://" + server + ":" + puerto + ";" + "databaseName=" + db + ";" + "encrypt=true" + ";" + "trustServerCertificate=true";
            connection = DriverManager.getConnection(cadena, usuario, contrase);
            JOptionPane.showMessageDialog(null, "Se conecto correctamente la conexion a la bd");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error en la conexion a la bd" + e.toString());

        }
        return connection;
    }
}