package com.example.farmaventa;

import Usuarios.Permisos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label     lblUsuario;
    @FXML private Label     lblFecha;
    @FXML private Label     lblModuloActual;
    @FXML private Label     lblSubtituloActual;

    @FXML private Button btnInicio;
    @FXML private Button btnVentas;
    @FXML private Button btnCompras;
    @FXML private Button btnPagos;
    @FXML private Button btnNomina;
    @FXML private Button btnPersonas;
    @FXML private Button btnInventario;
    @FXML private Button btnFidelizacion;
    @FXML private Button btnConvenios;
    @FXML private Button btnEnvios;
    @FXML private Button btnUsuarios; // ← NUEVO

    @FXML private Button btnReclamacionesToggle;
    @FXML private Button btnDevolucionesToggle;
    @FXML private VBox   vboxReclamaciones;
    @FXML private VBox   vboxDevoluciones;
    @FXML private Button btnReclamaciones;
    @FXML private Button btnReclamacionesCompra;
    @FXML private Button btnDevoluciones;
    @FXML private Button btnDevolucionesCompra;

    private Button  btnActivo;
    private boolean reclamacionesExpandido = false;
    private boolean devolucionesExpandido  = false;

    private static final String S_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: #BBF7D0; " +
                    "-fx-font-size: 12.5px; -fx-background-radius: 7; " +
                    "-fx-cursor: hand; -fx-padding: 8 10 8 10;";

    private static final String S_ACTIVO =
            "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-background-radius: 7; " +
                    "-fx-cursor: hand; -fx-padding: 8 10 8 10; " +
                    "-fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 7; -fx-border-width: 1;";

    private static final String S_TOGGLE_ABIERTO =
            "-fx-background-color: rgba(0,0,0,0.15); -fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 12.5px; -fx-background-radius: 7; " +
                    "-fx-cursor: hand; -fx-padding: 8 10 8 10;";

    private static final String S_SUB_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: #6EE7B7; " +
                    "-fx-font-size: 11.5px; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-padding: 6 10 6 10;";

    private static final String S_SUB_ACTIVO =
            "-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-padding: 6 10 6 10;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblFecha != null)
            lblFecha.setText(LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        resetarTodos();
        aplicarPermisos();
        cargarVista("Dashboard.fxml", btnInicio, "Inicio", "Panel principal del sistema");
    }

    private void aplicarPermisos() {
        ocultarSi(btnVentas,              !Permisos.tieneAcceso(Permisos.Modulo.VENTAS));
        ocultarSi(btnCompras,             !Permisos.tieneAcceso(Permisos.Modulo.COMPRAS));
        ocultarSi(btnPagos,               !Permisos.tieneAcceso(Permisos.Modulo.PAGOS));
        ocultarSi(btnNomina,              !Permisos.tieneAcceso(Permisos.Modulo.NOMINA));
        ocultarSi(btnPersonas,            !Permisos.tieneAcceso(Permisos.Modulo.PERSONAS));
        ocultarSi(btnInventario,          !Permisos.tieneAcceso(Permisos.Modulo.INVENTARIO));
        ocultarSi(btnFidelizacion,        !Permisos.tieneAcceso(Permisos.Modulo.FIDELIZACION));
        ocultarSi(btnConvenios,           !Permisos.tieneAcceso(Permisos.Modulo.CONVENIOS));
        ocultarSi(btnEnvios,              !Permisos.tieneAcceso(Permisos.Modulo.ENVIOS));
        ocultarSi(btnReclamacionesToggle, !Permisos.tieneAcceso(Permisos.Modulo.RECLAMACIONES));
        ocultarSi(btnDevolucionesToggle,  !Permisos.tieneAcceso(Permisos.Modulo.DEVOLUCIONES));
        ocultarSi(btnUsuarios,            !Permisos.tieneAcceso(Permisos.Modulo.USUARIOS)); // ← NUEVO
    }

    private void ocultarSi(Button btn, boolean ocultar) {
        if (btn == null) return;
        btn.setVisible(!ocultar);
        btn.setManaged(!ocultar);
    }

    public void setUsuario(String nombreCompleto) {
        if (lblUsuario != null && nombreCompleto != null && !nombreCompleto.isBlank())
            lblUsuario.setText(nombreCompleto);
    }

    // ── Navegación ────────────────────────────────────────────────────────
    @FXML private void onMenuInicio() {
        cargarVista("Dashboard.fxml", btnInicio, "Inicio", "Panel principal del sistema"); }
    @FXML private void onMenuVentas() {
        cargarVista("Ventas.fxml", btnVentas, "Ventas", "Registra y gestiona ventas"); }
    @FXML private void onMenuCompras() {
        cargarVista("Compra.fxml", btnCompras, "Compras", "Gestiona compras a proveedores"); }
    @FXML private void onMenuPagos() {
        cargarVista("Pagos.fxml", btnPagos, "Pagos", "Cuentas por pagar, cobrar y seguros"); }
    @FXML private void onMenuNomina() {
        cargarVista("Nomina.fxml", btnNomina, "Nómina", "Creación, pago e historial de nóminas"); }
    @FXML private void onMenuPersonas() {
        cargarVista("Personas.fxml", btnPersonas, "Personas", "Clientes, empleados y proveedores"); }
    @FXML private void onMenuInventario() {
        cargarVista("Inventario.fxml", btnInventario, "Inventario", "Control de productos y stock"); }
    @FXML private void onMenuFidelizacion() {
        cargarVista("fidelizacion.fxml", btnFidelizacion, "Fidelización", "Programa de puntos y beneficios"); }
    @FXML private void onMenuConvenios() {
        cargarVista("Convenio.fxml", btnConvenios, "Convenios", "Acuerdos con proveedores"); }
    @FXML private void onMenuEnvios() {
        cargarVista("Envio.fxml", btnEnvios, "Envíos", "Seguimiento y gestión de envíos"); }

    @FXML private void onMenuUsuarios() {
        try {
            URL ruta = getClass().getResource("/Usuarios/Registro.fxml");
            if (ruta == null) { System.err.println("FXML no encontrado: Usuarios/Registro.fxml"); return; }
            Node vista = FXMLLoader.load(ruta);
            contentArea.getChildren().setAll(vista);
            if (lblModuloActual    != null) lblModuloActual.setText("Usuarios");
            if (lblSubtituloActual != null) lblSubtituloActual.setText("Gestión de accesos al sistema");
            if (btnActivo != null) btnActivo.setStyle(S_NORMAL);
            btnUsuarios.setStyle(S_ACTIVO);
            btnActivo = btnUsuarios;
            limpiarSubs();
        } catch (IOException e) {
            System.err.println("Error cargando Usuarios/Registro.fxml");
            e.printStackTrace();
        }
    }

    // ── Submenús ──────────────────────────────────────────────────────────
    @FXML
    private void onToggleReclamaciones() {
        reclamacionesExpandido = !reclamacionesExpandido;
        vboxReclamaciones.setVisible(reclamacionesExpandido);
        vboxReclamaciones.setManaged(reclamacionesExpandido);
        btnReclamacionesToggle.setText(reclamacionesExpandido
                ? "📋  Reclamaciones  ∨" : "📋  Reclamaciones  ›");
        btnReclamacionesToggle.setStyle(reclamacionesExpandido ? S_TOGGLE_ABIERTO : S_NORMAL);
    }

    @FXML private void onMenuReclamaciones() {
        cargarVista("Reclamacionventa.fxml", null,
                "Reclamaciones · Ventas", "Gestión de reclamaciones de ventas");
        activarSub(btnReclamaciones);
    }
    @FXML private void onMenuReclamacionesCompra() {
        cargarVista("ReclamacionCompra.fxml", null,
                "Reclamaciones · Compras", "Gestión de reclamaciones de compras");
        activarSub(btnReclamacionesCompra);
    }

    @FXML
    private void onToggleDevoluciones() {
        devolucionesExpandido = !devolucionesExpandido;
        vboxDevoluciones.setVisible(devolucionesExpandido);
        vboxDevoluciones.setManaged(devolucionesExpandido);
        btnDevolucionesToggle.setText(devolucionesExpandido
                ? "↩  Devoluciones  ∨" : "↩  Devoluciones  ›");
        btnDevolucionesToggle.setStyle(devolucionesExpandido ? S_TOGGLE_ABIERTO : S_NORMAL);
    }

    @FXML private void onMenuDevoluciones() {
        cargarVista("DevolucionVenta.fxml", null,
                "Devoluciones · Ventas", "Procesamiento de devoluciones de ventas");
        activarSub(btnDevoluciones);
    }
    @FXML private void onMenuDevolucionesCompra() {
        cargarVista("DevolucionCompra.fxml", null,
                "Devoluciones · Compras", "Procesamiento de devoluciones de compras");
        activarSub(btnDevolucionesCompra);
    }

    // ── Cerrar sesión ─────────────────────────────────────────────────────
    @FXML
    private void onCerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Usuarios/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnInicio.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("FarmaVenta — Iniciar Sesión");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void cargarVista(String fxml, Button boton, String titulo, String subtitulo) {
        try {
            URL ruta = getClass().getResource("/com/example/farmaventa/" + fxml);
            if (ruta == null) { System.err.println("FXML no encontrado: " + fxml); return; }
            Node vista = FXMLLoader.load(ruta);
            contentArea.getChildren().setAll(vista);
            if (lblModuloActual    != null) lblModuloActual.setText(titulo);
            if (lblSubtituloActual != null) lblSubtituloActual.setText(subtitulo);
            if (boton != null) {
                if (btnActivo != null) btnActivo.setStyle(S_NORMAL);
                boton.setStyle(S_ACTIVO);
                btnActivo = boton;
                limpiarSubs();
            }
        } catch (IOException e) {
            System.err.println("Error cargando: " + fxml);
            e.printStackTrace();
        }
    }

    private void activarSub(Button sub) {
        if (btnActivo != null) { btnActivo.setStyle(S_NORMAL); btnActivo = null; }
        limpiarSubs();
        sub.setStyle(S_SUB_ACTIVO);
    }

    private void limpiarSubs() {
        List.of(btnReclamaciones, btnReclamacionesCompra,
                        btnDevoluciones, btnDevolucionesCompra)
                .forEach(b -> b.setStyle(S_SUB_NORMAL));
    }

    private void resetarTodos() {
        List.of(btnInicio, btnVentas, btnCompras, btnPagos, btnNomina, btnPersonas,
                        btnInventario, btnFidelizacion, btnConvenios, btnEnvios,
                        btnUsuarios, // ← NUEVO
                        btnReclamacionesToggle, btnDevolucionesToggle)
                .forEach(b -> { if (b != null) b.setStyle(S_NORMAL); });
        limpiarSubs();
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public void navegarA(String fxml, Button boton) {
        cargarVista(fxml, boton != null ? boton : btnInicio, "", ""); }
    public Button getBtnInicio()              { return btnInicio; }
    public Button getBtnVentas()              { return btnVentas; }
    public Button getBtnCompras()             { return btnCompras; }
    public Button getBtnPagos()               { return btnPagos; }
    public Button getBtnNomina()              { return btnNomina; }
    public Button getBtnPersonas()            { return btnPersonas; }
    public Button getBtnInventario()          { return btnInventario; }
    public Button getBtnReclamaciones()       { return btnReclamaciones; }
    public Button getBtnReclamacionesCompra() { return btnReclamacionesCompra; }
    public Button getBtnDevoluciones()        { return btnDevoluciones; }
    public Button getBtnDevolucionesCompra()  { return btnDevolucionesCompra; }
    public Button getBtnFidelizacion()        { return btnFidelizacion; }
    public Button getBtnConvenios()           { return btnConvenios; }
    public Button getBtnEnvios()              { return btnEnvios; }
    public Button getBtnUsuarios()            { return btnUsuarios; } // ← NUEVO
}