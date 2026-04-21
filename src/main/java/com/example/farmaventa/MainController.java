package com.example.farmaventa;

import Usuarios.Permisos;
import Usuarios.Permisos.Modulo;
import Usuarios.SesionUsuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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

    // ── Botones sidebar ───────────────────────────────────────────────────
    @FXML private Button    btnInicio;
    @FXML private Button    btnVentas;
    @FXML private Button    btnCompras;
    @FXML private Button    btnPagos;
    @FXML private Button    btnPersonas;
    @FXML private Button    btnInventario;
    @FXML private Button    btnFidelizacion;
    @FXML private Button    btnConvenios;
    @FXML private Button    btnEnvios;
    @FXML private Button    btnUsuarios;

    @FXML private Button    btnReclamacionesToggle;
    @FXML private Button    btnDevolucionesToggle;
    @FXML private VBox      vboxReclamaciones;
    @FXML private VBox      vboxDevoluciones;
    @FXML private Button    btnReclamaciones;
    @FXML private Button    btnReclamacionesCompra;
    @FXML private Button    btnDevoluciones;
    @FXML private Button    btnDevolucionesCompra;

    // ── Sección administración ────────────────────────────────────────────
    @FXML private Separator separadorAdmin;
    @FXML private Label     lblSeccionAdmin;

    private Button  btnActivo;
    private boolean reclamacionesExpandido = false;
    private boolean devolucionesExpandido  = false;

    // ── Estilos ───────────────────────────────────────────────────────────
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

    private static final String S_BLOQUEADO =
            "-fx-background-color: transparent; -fx-text-fill: rgba(165,214,167,0.30); " +
                    "-fx-font-size: 12.5px; -fx-background-radius: 7; " +
                    "-fx-cursor: default; -fx-padding: 8 10 8 10;";

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblFecha != null)
            lblFecha.setText(LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        resetarTodos();
    }

    /**
     * Llamado desde LoginController justo después de cargar el FXML.
     * Establece nombre, aplica permisos y carga el dashboard.
     */
    public void setUsuario(String nombreCompleto) {
        if (lblUsuario != null && nombreCompleto != null && !nombreCompleto.isBlank())
            lblUsuario.setText(nombreCompleto);
        aplicarPermisos();
        cargarVista("Dashboard.fxml", btnInicio, "Inicio", "Panel principal del sistema");
    }

    // ── Aplicar permisos ──────────────────────────────────────────────────
    private void aplicarPermisos() {
        aplicarPermiso(btnVentas,              Modulo.VENTAS);
        aplicarPermiso(btnCompras,             Modulo.COMPRAS);
        aplicarPermiso(btnPagos,               Modulo.PAGOS);
        aplicarPermiso(btnPersonas,            Modulo.PERSONAS);
        aplicarPermiso(btnInventario,          Modulo.INVENTARIO);
        aplicarPermiso(btnReclamacionesToggle, Modulo.RECLAMACIONES);
        aplicarPermiso(btnDevolucionesToggle,  Modulo.DEVOLUCIONES);
        aplicarPermiso(btnFidelizacion,        Modulo.FIDELIZACION);
        aplicarPermiso(btnConvenios,           Modulo.CONVENIOS);
        aplicarPermiso(btnEnvios,              Modulo.ENVIOS);

        // ── Sección Administración: solo ADMIN ────────────────────────────
        boolean esAdmin = Permisos.tieneAcceso(Modulo.USUARIOS);

        if (separadorAdmin != null) {
            separadorAdmin.setVisible(esAdmin);
            separadorAdmin.setManaged(esAdmin);
        }
        if (lblSeccionAdmin != null) {
            lblSeccionAdmin.setVisible(esAdmin);
            lblSeccionAdmin.setManaged(esAdmin);
        }
        if (btnUsuarios != null) {
            btnUsuarios.setVisible(esAdmin);
            btnUsuarios.setManaged(esAdmin);
        }

        // ── Submenús: ocultar hijos si el padre está bloqueado ────────────
        boolean puedeReclamaciones = Permisos.tieneAcceso(Modulo.RECLAMACIONES);
        vboxReclamaciones.setVisible(false);
        vboxReclamaciones.setManaged(false);
        btnReclamaciones.setVisible(puedeReclamaciones);
        btnReclamaciones.setManaged(puedeReclamaciones);
        btnReclamacionesCompra.setVisible(puedeReclamaciones);
        btnReclamacionesCompra.setManaged(puedeReclamaciones);

        boolean puedeDevoluciones = Permisos.tieneAcceso(Modulo.DEVOLUCIONES);
        vboxDevoluciones.setVisible(false);
        vboxDevoluciones.setManaged(false);
        btnDevoluciones.setVisible(puedeDevoluciones);
        btnDevoluciones.setManaged(puedeDevoluciones);
        btnDevolucionesCompra.setVisible(puedeDevoluciones);
        btnDevolucionesCompra.setManaged(puedeDevoluciones);
    }

    /**
     * Si no tiene acceso: deshabilita el botón, aplica estilo gris y
     * elimina el handler para que no responda a ningún evento.
     */
    private void aplicarPermiso(Button btn, Modulo modulo) {
        if (btn == null) return;
        boolean acceso = Permisos.tieneAcceso(modulo);
        btn.setDisable(!acceso);
        btn.setStyle(acceso ? S_NORMAL : S_BLOQUEADO);
        if (!acceso) btn.setOnAction(null);
    }

    // ── Navegación ────────────────────────────────────────────────────────
    @FXML private void onMenuInicio() {
        cargarVista("Dashboard.fxml", btnInicio,
                "Inicio", "Panel principal del sistema");
    }
    @FXML private void onMenuVentas() {
        if (!Permisos.tieneAcceso(Modulo.VENTAS)) return;
        cargarVista("Ventas.fxml", btnVentas,
                "Ventas", "Registra y gestiona ventas");
    }
    @FXML private void onMenuCompras() {
        if (!Permisos.tieneAcceso(Modulo.COMPRAS)) return;
        cargarVista("Compra.fxml", btnCompras,
                "Compras", "Gestiona compras a proveedores");
    }
    @FXML private void onMenuPagos() {
        if (!Permisos.tieneAcceso(Modulo.PAGOS)) return;
        cargarVista("Pagos.fxml", btnPagos,
                "Pagos", "Cuentas por pagar, cobrar y seguros");
    }
    @FXML private void onMenuPersonas() {
        if (!Permisos.tieneAcceso(Modulo.PERSONAS)) return;
        cargarVista("Personas.fxml", btnPersonas,
                "Personas", "Clientes, empleados y proveedores");
    }
    @FXML private void onMenuInventario() {
        if (!Permisos.tieneAcceso(Modulo.INVENTARIO)) return;
        cargarVista("Inventario.fxml", btnInventario,
                "Inventario", "Control de productos y stock");
    }
    @FXML private void onMenuFidelizacion() {
        if (!Permisos.tieneAcceso(Modulo.FIDELIZACION)) return;
        cargarVista("fidelizacion.fxml", btnFidelizacion,
                "Fidelización", "Programa de puntos y beneficios");
    }
    @FXML private void onMenuConvenios() {
        if (!Permisos.tieneAcceso(Modulo.CONVENIOS)) return;
        cargarVista("Convenio.fxml", btnConvenios,
                "Convenios", "Acuerdos con proveedores");
    }
    @FXML private void onMenuEnvios() {
        if (!Permisos.tieneAcceso(Modulo.ENVIOS)) return;
        cargarVista("Envio.fxml", btnEnvios,
                "Envíos", "Seguimiento y gestión de envíos");
    }
    @FXML private void onMenuUsuarios() {
        if (!Permisos.tieneAcceso(Modulo.USUARIOS)) return;
        cargarVista("/Usuarios/Registro.fxml", btnUsuarios,
                "Usuarios", "Registro y gestión de accesos");
    }

    // ── Submenús ──────────────────────────────────────────────────────────
    @FXML
    private void onToggleReclamaciones() {
        if (!Permisos.tieneAcceso(Modulo.RECLAMACIONES)) return;
        reclamacionesExpandido = !reclamacionesExpandido;
        vboxReclamaciones.setVisible(reclamacionesExpandido);
        vboxReclamaciones.setManaged(reclamacionesExpandido);
        btnReclamacionesToggle.setText(reclamacionesExpandido
                ? "📋  Reclamaciones  ∨" : "📋  Reclamaciones  ›");
        btnReclamacionesToggle.setStyle(reclamacionesExpandido ? S_TOGGLE_ABIERTO : S_NORMAL);
    }

    @FXML private void onMenuReclamaciones() {
        if (!Permisos.tieneAcceso(Modulo.RECLAMACIONES)) return;
        cargarVista("Reclamacionventa.fxml", null,
                "Reclamaciones · Ventas", "Gestión de reclamaciones de ventas");
        activarSub(btnReclamaciones);
    }
    @FXML private void onMenuReclamacionesCompra() {
        if (!Permisos.tieneAcceso(Modulo.RECLAMACIONES)) return;
        cargarVista("ReclamacionCompra.fxml", null,
                "Reclamaciones · Compras", "Gestión de reclamaciones de compras");
        activarSub(btnReclamacionesCompra);
    }

    @FXML
    private void onToggleDevoluciones() {
        if (!Permisos.tieneAcceso(Modulo.DEVOLUCIONES)) return;
        devolucionesExpandido = !devolucionesExpandido;
        vboxDevoluciones.setVisible(devolucionesExpandido);
        vboxDevoluciones.setManaged(devolucionesExpandido);
        btnDevolucionesToggle.setText(devolucionesExpandido
                ? "↩  Devoluciones  ∨" : "↩  Devoluciones  ›");
        btnDevolucionesToggle.setStyle(devolucionesExpandido ? S_TOGGLE_ABIERTO : S_NORMAL);
    }

    @FXML private void onMenuDevoluciones() {
        if (!Permisos.tieneAcceso(Modulo.DEVOLUCIONES)) return;
        cargarVista("DevolucionVenta.fxml", null,
                "Devoluciones · Ventas", "Procesamiento de devoluciones de ventas");
        activarSub(btnDevoluciones);
    }
    @FXML private void onMenuDevolucionesCompra() {
        if (!Permisos.tieneAcceso(Modulo.DEVOLUCIONES)) return;
        cargarVista("DevolucionCompra.fxml", null,
                "Devoluciones · Compras", "Procesamiento de devoluciones de compras");
        activarSub(btnDevolucionesCompra);
    }

    // ── Cerrar sesión ─────────────────────────────────────────────────────
    @FXML
    private void onCerrarSesion() {
        SesionUsuario.getInstance().cerrarSesion();
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
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void cargarVista(String fxml, Button boton, String titulo, String subtitulo) {
        try {
            URL ruta = fxml.startsWith("/")
                    ? getClass().getResource(fxml)
                    : getClass().getResource("/com/example/farmaventa/" + fxml);

            if (ruta == null) { System.err.println("FXML no encontrado: " + fxml); return; }

            Node vista = FXMLLoader.load(ruta);
            contentArea.getChildren().setAll(vista);
            if (lblModuloActual    != null) lblModuloActual.setText(titulo);
            if (lblSubtituloActual != null) lblSubtituloActual.setText(subtitulo);
            if (boton != null) {
                if (btnActivo != null)
                    btnActivo.setStyle(btnActivo.isDisabled() ? S_BLOQUEADO : S_NORMAL);
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
        if (btnActivo != null) {
            btnActivo.setStyle(btnActivo.isDisabled() ? S_BLOQUEADO : S_NORMAL);
            btnActivo = null;
        }
        limpiarSubs();
        sub.setStyle(S_SUB_ACTIVO);
    }

    private void limpiarSubs() {
        List.of(btnReclamaciones, btnReclamacionesCompra,
                        btnDevoluciones,  btnDevolucionesCompra)
                .forEach(b -> b.setStyle(S_SUB_NORMAL));
    }

    private void resetarTodos() {
        List.of(btnInicio, btnVentas, btnCompras, btnPagos, btnPersonas,
                        btnInventario, btnFidelizacion, btnConvenios, btnEnvios,
                        btnReclamacionesToggle, btnDevolucionesToggle)
                .forEach(b -> b.setStyle(S_NORMAL));
        limpiarSubs();
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Button getBtnInicio()              { return btnInicio; }
    public Button getBtnVentas()              { return btnVentas; }
    public Button getBtnCompras()             { return btnCompras; }
    public Button getBtnPagos()               { return btnPagos; }
    public Button getBtnPersonas()            { return btnPersonas; }
    public Button getBtnInventario()          { return btnInventario; }
    public Button getBtnReclamaciones()       { return btnReclamaciones; }
    public Button getBtnReclamacionesCompra() { return btnReclamacionesCompra; }
    public Button getBtnDevoluciones()        { return btnDevoluciones; }
    public Button getBtnDevolucionesCompra()  { return btnDevolucionesCompra; }
    public Button getBtnFidelizacion()        { return btnFidelizacion; }
    public Button getBtnConvenios()           { return btnConvenios; }
    public Button getBtnEnvios()              { return btnEnvios; }
    public Button getBtnUsuarios()            { return btnUsuarios; }
}