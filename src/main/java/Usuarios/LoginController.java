package Usuarios;

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

        // Permitir login con Enter desde el campo contraseña
        txtContrasena.setOnAction(e -> onLogin(null));
    }

    // ── Acción Login ──────────────────────────────────────────────────────
    @FXML
    public void onLogin(ActionEvent event) {
        lblError.setVisible(false);

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
            ps.setString(2, contrasena); // En producción: hashear antes de comparar
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

                // Ir a pantalla principal
                abrirVentanaPrincipal();

            } else {
                mostrarError("Usuario o contraseña incorrectos.");
                txtContrasena.clear();
            }

        } catch (SQLException e) {
            mostrarError("Error de conexión: " + e.getMessage());
        }
    }

    // ── Ir a registro de nuevo usuario ────────────────────────────────────
    @FXML
    public void onIrRegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Usuarios/Registro.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FarmaVenta — Registro de Usuario");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo abrir el registro: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void mostrarError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
    }

    private void actualizarUltimoAcceso(Connection con, int idCredencial) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_CREDENCIAL SET ultimo_acceso = GETDATE() WHERE id_credencial = ?");
            ps.setInt(1, idCredencial);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/MainView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FarmaVenta — " +
                    SesionUsuario.getInstance().getUsuarioActual().getNombreCompleto());
            stage.setMaximized(true);
        } catch (IOException e) {
            // Si aún no tienes MainView, muestra mensaje temporal
            JOptionPane.showMessageDialog(null,
                    "✅ Login correcto como: " +
                            SesionUsuario.getInstance().getUsuarioActual().getNombreCompleto() +
                            "\n\nRol: " + SesionUsuario.getInstance().getUsuarioActual().getRol() +
                            "\n\n(Conecta a tu vista principal en abrirVentanaPrincipal())");
        }
    }
}