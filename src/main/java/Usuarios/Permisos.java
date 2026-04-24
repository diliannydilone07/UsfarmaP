package Usuarios;

import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Permisos {

    // ── Módulos del sistema ───────────────────────────────────────────────
    public enum Modulo {
        INICIO, VENTAS, COMPRAS, PAGOS, NOMINA,
        PERSONAS, INVENTARIO,
        RECLAMACIONES, DEVOLUCIONES, FIDELIZACION, CONVENIOS, ENVIOS,
        USUARIOS
    }

    // ── Acciones posibles ─────────────────────────────────────────────────
    public enum Accion {
        REGISTRAR, EDITAR, ELIMINAR, VER
    }

    // ── Roles ─────────────────────────────────────────────────────────────
    public enum Rol {
        ADMIN, SUPERVISOR, FARMACEUTICO, CAJERO;

        public static Rol fromString(String s) {
            try { return valueOf(s.toUpperCase()); }
            catch (Exception e) { return CAJERO; }
        }
    }

    // ── Tabla de módulos por rol ──────────────────────────────────────────
    private static final Map<Rol, Set<Modulo>> TABLA = new EnumMap<>(Rol.class);

    static {
        TABLA.put(Rol.ADMIN, EnumSet.allOf(Modulo.class));

        TABLA.put(Rol.SUPERVISOR, EnumSet.of(
                Modulo.INICIO, Modulo.VENTAS, Modulo.COMPRAS, Modulo.PAGOS,
                Modulo.NOMINA, Modulo.PERSONAS, Modulo.INVENTARIO,
                Modulo.RECLAMACIONES, Modulo.DEVOLUCIONES,
                Modulo.FIDELIZACION, Modulo.CONVENIOS, Modulo.ENVIOS
        ));

        TABLA.put(Rol.FARMACEUTICO, EnumSet.of(
                Modulo.INICIO, Modulo.VENTAS, Modulo.COMPRAS,
                Modulo.INVENTARIO, Modulo.RECLAMACIONES,
                Modulo.DEVOLUCIONES, Modulo.ENVIOS
        ));

        TABLA.put(Rol.CAJERO, EnumSet.of(
                Modulo.INICIO, Modulo.VENTAS, Modulo.RECLAMACIONES,
                Modulo.DEVOLUCIONES, Modulo.FIDELIZACION, Modulo.ENVIOS
        ));
    }

    // ── Tabla de acciones por rol ─────────────────────────────────────────
    private static final Map<Rol, Set<Accion>> ACCIONES = new EnumMap<>(Rol.class);

    static {
        ACCIONES.put(Rol.ADMIN,        EnumSet.allOf(Accion.class));
        ACCIONES.put(Rol.SUPERVISOR,   EnumSet.allOf(Accion.class));
        ACCIONES.put(Rol.FARMACEUTICO, EnumSet.of(Accion.REGISTRAR, Accion.VER));
        ACCIONES.put(Rol.CAJERO,       EnumSet.of(Accion.REGISTRAR, Accion.VER));
    }

    // ── Consultas de módulo ───────────────────────────────────────────────
    public static boolean tieneAcceso(Modulo modulo) {
        Usuario u = SesionUsuario.getInstance().getUsuarioActual();
        if (u == null) return false;
        return tieneAcceso(u.getRol(), modulo);
    }

    public static boolean tieneAcceso(String rolStr, Modulo modulo) {
        Rol rol = Rol.fromString(rolStr);
        Set<Modulo> modulos = TABLA.get(rol);
        return modulos != null && modulos.contains(modulo);
    }

    // ── Consultas de acción ───────────────────────────────────────────────
    public static boolean puedeHacer(Accion accion) {
        Usuario u = SesionUsuario.getInstance().getUsuarioActual();
        if (u == null) return false;
        Rol rol = Rol.fromString(u.getRol());
        Set<Accion> acciones = ACCIONES.get(rol);
        return acciones != null && acciones.contains(accion);
    }

    // ── Aplicar permisos en botones ───────────────────────────────────────
    /**
     * Oculta o muestra un Button según si el rol activo puede hacer la acción.
     * Uso: Permisos.aplicarBtn(btnEditar, Permisos.Accion.EDITAR);
     */
    public static void aplicarBtn(Button btn, Accion accion) {
        if (btn == null) return;
        boolean puede = puedeHacer(accion);
        btn.setVisible(puede);
        btn.setManaged(puede);
        btn.setDisable(!puede);
    }

    /**
     * Oculta o muestra cualquier Node (TextField, ComboBox, Label…)
     * según si el rol activo puede hacer la acción.
     */
    public static void aplicarControl(Node nodo, Accion accion) {
        if (nodo == null) return;
        boolean puede = puedeHacer(accion);
        nodo.setVisible(puede);
        nodo.setManaged(puede);
        nodo.setDisable(!puede);
    }
}