package reserva1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Habitacion {

    private int idHabitacion;
    private int numero;
    private String tipo;
    private int piso;
    private int capacidad;
    private double precio;
    private boolean disponible;

    public Habitacion(int idHabitacion, int numero, String tipo, int piso,
                       int capacidad, double precio, boolean disponible) {
        this.idHabitacion = idHabitacion;
        this.numero = numero;
        this.tipo = tipo;
        this.piso = piso;
        this.capacidad = capacidad;
        this.precio = precio;
        this.disponible = disponible;
    }

    public int getIdHabitacion() {
        return idHabitacion;
    }

    public int getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public int getPiso() {
        return piso;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;

        String sql = "UPDATE Habitaciones SET disponible = ? WHERE id_habitacion = ?";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, disponible);
            ps.setInt(2, idHabitacion);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar disponibilidad: " + e.getMessage());
        }
    }

    public static List<Habitacion> cargarTodas() {

        List<Habitacion> lista = new ArrayList<>();
        String sql =
    "SELECT h.id_habitacion, h.numero, h.tipo, h.piso, h.capacidad, h.precio, " +
    "  CASE WHEN EXISTS (SELECT 1 FROM Reservas r WHERE r.id_habitacion = h.id_habitacion " +
    "AND r.estado = 'Confirmada' " +
    "AND r.fecha_entrada <= CAST(GETDATE() AS DATE) " +
    "AND r.fecha_salida > CAST(GETDATE() AS DATE)) " +
    "THEN 0 ELSE 1 END AS disponible " +
    "FROM Habitaciones h";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Habitacion(
                    rs.getInt("id_habitacion"),
                    rs.getInt("numero"),
                    rs.getString("tipo"),
                    rs.getInt("piso"),
                    rs.getInt("capacidad"),
                    rs.getDouble("precio"),
                    rs.getBoolean("disponible")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar habitaciones: " + e.getMessage());
        }

        return lista;
    }
    
    public static List<Habitacion> cargarDisponibles(String entrada, String salida) {

    List<Habitacion> lista = new ArrayList<>();
    String sql =
        "SELECT h.id_habitacion, h.numero, h.tipo, h.piso, h.capacidad, h.precio " +
        "FROM Habitaciones h " +
        "WHERE NOT EXISTS (SELECT 1 FROM Reservas r WHERE r.id_habitacion = h.id_habitacion " +
        "AND r.estado = 'Confirmada' AND r.fecha_entrada < ? AND r.fecha_salida > ?) " +
        "ORDER BY h.numero";

    try (Connection con = Main.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setDate(1, java.sql.Date.valueOf(salida));
        ps.setDate(2, java.sql.Date.valueOf(entrada));

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Habitacion(
                    rs.getInt("id_habitacion"),
                    rs.getInt("numero"),
                    rs.getString("tipo"),
                    rs.getInt("piso"),
                    rs.getInt("capacidad"),
                    rs.getDouble("precio"),
                    true
                ));
            }
        }

    } catch (SQLException e) {
        System.out.println("Error al cargar habitaciones disponibles: " + e.getMessage());
    }

    return lista;
}
}