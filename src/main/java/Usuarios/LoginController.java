package Usuarios;

import com.example.farmaventa.MainController;
import com.example.farmaventa.database.Conexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    Conexion conexion = new Conexion();

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label         lblError;
    @FXML private Button        btnIngresar;
    @FXML private Hyperlink     linkRegistro;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        // Permitir login con Enter desde el campo contraseña
        txtContrasena.setOnAction(e -> onLogin(null));
    }

    // ── Acción Login ──────────────────────────────────────────────────────
    @FXML
    public void onLogin(ActionEvent event) {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String usuario    = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Ingresa tu usuario y contraseña.");
            return;
        }

        try (Connection con = conexion.establecerConexion()) {

            String sql = "SELECT c.id_credencial, c.usuario, c.rol, c.estado, " +
                    "       c.id_empleado, " +
                    "       p.nombre + ' ' + p.apellido AS nombre_completo " +
                    "FROM TBL_CREDENCIAL c " +
                    "JOIN TBL_EMPLEADO e ON e.id_empleado = c.id_empleado " +
                    "JOIN TBL_PERSONA  p ON p.id_persona  = e.id_persona " +
                    "WHERE c.usuario = ? AND c.contrasena = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usuario);
            ps.setString(2, contrasena);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (!rs.getBoolean("estado")) {
                    mostrarError("Tu cuenta está desactivada. Contacta al administrador.");
                    return;
                }

                // Guardar sesión
                Usuario u = new Usuario(
                        rs.getInt("id_credencial"),
                        rs.getString("usuario"),
                        rs.getString("rol"),
                        rs.getBoolean("estado"),
                        rs.getInt("id_empleado"),
                        rs.getString("nombre_completo")
                );
                SesionUsuario.getInstance().setUsuarioActual(u);

                // Registrar último acceso
                actualizarUltimoAcceso(con, u.getIdCredencial());

                // Ir al sistema principal pasando el nombre completo
                abrirVentanaPrincipal(u.getNombreCompleto());

            } else {
                mostrarError("Usuario o contraseña incorrectos.");
                txtContrasena.clear();
            }

        } catch (SQLException e) {
            mostrarError("Error de conexión: " + e.getMessage());
        }
    }

    // ── Ir a registro ─────────────────────────────────────────────────────
    @FXML
    public void onIrRegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Usuarios/Registro.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtUsuario.getScene().getWindow();

            // Asignar escena sin tamaño fijo y dejar que sizeToScene la ajuste
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("FarmaVenta — Registro de Usuario");
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo abrir el registro: " + e.getMessage());
        }
    }

    // ── Abrir sistema principal ───────────────────────────────────────────
    private void abrirVentanaPrincipal(String nombreCompleto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/MainLayout.fxml"));
            Parent root = loader.load();

            // Pasar el nombre del usuario al controlador principal
            MainController mainController = loader.getController();
            mainController.setUsuario(nombreCompleto);

            Stage stage = (Stage) txtUsuario.getScene().getWindow();

            // Asignar escena, maximizar y mostrar
            stage.setScene(new Scene(root));
            stage.setTitle("FarmaVenta — " + nombreCompleto);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el sistema: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void mostrarError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void actualizarUltimoAcceso(Connection con, int idCredencial) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_CREDENCIAL SET ultimo_acceso = GETDATE() WHERE id_credencial = ?");
            ps.setInt(1, idCredencial);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}