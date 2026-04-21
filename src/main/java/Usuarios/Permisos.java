package Usuarios;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Permisos {

    public enum Modulo {
        INICIO, VENTAS, COMPRAS, PAGOS, PERSONAS, INVENTARIO,
        RECLAMACIONES, DEVOLUCIONES, FIDELIZACION, CONVENIOS, ENVIOS,
        USUARIOS
    }

    public enum Rol {
        ADMIN, SUPERVISOR, FARMACEUTICO, CAJERO;

        public static Rol fromString(String s) {
            try { return valueOf(s.toUpperCase()); }
            catch (Exception e) { return CAJERO; }
        }
    }

    private static final Map<Rol, Set<Modulo>> TABLA = new EnumMap<>(Rol.class);

    static {
        TABLA.put(Rol.ADMIN, EnumSet.allOf(Modulo.class));

        TABLA.put(Rol.SUPERVISOR, EnumSet.of(
                Modulo.INICIO, Modulo.VENTAS, Modulo.COMPRAS, Modulo.PAGOS,
                Modulo.PERSONAS, Modulo.INVENTARIO, Modulo.RECLAMACIONES,
                Modulo.DEVOLUCIONES, Modulo.FIDELIZACION, Modulo.CONVENIOS,
                Modulo.ENVIOS
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

    public static boolean tieneAcceso(String rolStr, Modulo modulo) {
        Rol rol = Rol.fromString(rolStr);
        Set<Modulo> modulos = TABLA.get(rol);
        return modulos != null && modulos.contains(modulo);
    }

    public static boolean tieneAcceso(Modulo modulo) {
        Usuario u = SesionUsuario.getInstance().getUsuarioActual();
        if (u == null) return false;
        return tieneAcceso(u.getRol(), modulo);
    }
}