
package reserva1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Hotel {

    private List<Habitacion> habitaciones;

    public Hotel() {
        this.habitaciones = Habitacion.cargarTodas();
    }

    public void mostrarDisponibles() {
        System.out.println("\n== HABITACIONES DISPONIBLES ==");
        boolean hay = false;
        for (Habitacion h : habitaciones) {
            if (h.isDisponible()) {
                System.out.println(
                    "Num: " + h.getNumero() +
                    " | Tipo: " + h.getTipo() +
                    " | Piso: " + h.getPiso() +
                    " | Precio: $" + h.getPrecio()
                );
                hay = true;
            }
        }
        if (!hay) System.out.println("No hay habitaciones disponibles.");
    }

    public void mostrarEstado() {
        System.out.println("\n== ESTADO DE HABITACIONES ==");
        for (Habitacion h : habitaciones) {
            String estado = h.isDisponible() ? "LIBRE" : "OCUPADA";
            System.out.println(
                "Hab " + h.getNumero() +
                " | " + h.getTipo() +
                " | Piso " + h.getPiso() +
                " | " + estado
            );
        }
    }

    public void hacerReserva(Cliente cliente, int numHab, String entrada, String salida) {
        for (Habitacion h : habitaciones) {
            if (h.getNumero() == numHab) {
                if (h.isDisponible()) {
                    Reserva1 r = Reserva1.crear(cliente, h, entrada, salida);
                    if (r != null) {
                        System.out.println("\nReserva realizada con exito! (ID " + r.getIdReserva() + ")");
                    }
                } else {
                    System.out.println("Esa habitacion no esta disponible.");
                }
                return;
            }
        }
        System.out.println("No existe esa habitacion.");
    }

    public void verReserva(String cedula) {
        List<Reserva1> reservas = buscarReservasPorCedula(cedula);
        if (reservas.isEmpty()) {
            System.out.println("No tiene reservas registradas.");
            return;
        }
        System.out.println("\n== SU RESERVA ==");
        for (Reserva1 r : reservas) {
            r.mostrarInfo();
        }
    }

    public void cancelarReserva(String cedula) {
        List<Reserva1> reservas = buscarReservasPorCedula(cedula);
        if (reservas.isEmpty()) {
            System.out.println("No se encontro reserva con esa cedula.");
            return;
        }

        Reserva1 r = reservas.get(0);
        String sql = "UPDATE Reservas SET estado = 'Cancelada' WHERE id_reserva = ?";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdReserva());
            ps.executeUpdate();

            r.getHabitacion().setDisponible(true);
            System.out.println("Reserva cancelada correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al cancelar la reserva: " + e.getMessage());
        }
    }

    public void mostrarTodasReservas() {
        List<Reserva1> todas = cargarTodasLasReservas();
        if (todas.isEmpty()) {
            System.out.println("No hay reservas registradas.");
            return;
        }
        System.out.println("\n== TODAS LAS RESERVAS ==");
        int contador = 1;
        for (Reserva1 r : todas) {
            System.out.println("\n-- Reserva #" + contador + " --");
            r.mostrarInfo();
            contador++;
        }
    }

    public void buscarPorCedula(String cedula) {
        List<Reserva1> reservas = buscarReservasPorCedula(cedula);
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas con esa cedula.");
            return;
        }
        System.out.println("\n== RESERVA ENCONTRADA ==");
        for (Reserva1 r : reservas) {
            r.mostrarInfo();
        }
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

   

    private List<Reserva1> buscarReservasPorCedula(String cedula) {
        List<Reserva1> lista = new ArrayList<>();

        String sql =
            "SELECT r.id_reserva, r.fecha_entrada, r.fecha_salida, r.estado, " +
            "       c.id_cliente, c.nombre, c.cedula, " +
            "       h.id_habitacion, h.numero, h.tipo, h.piso, h.capacidad, h.precio, h.disponible " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE c.cedula = ? AND r.estado = 'Confirmada'";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar reservas: " + e.getMessage());
        }

        return lista;
    }

    private List<Reserva1> cargarTodasLasReservas() {
        List<Reserva1> lista = new ArrayList<>();

        String sql =
            "SELECT r.id_reserva, r.fecha_entrada, r.fecha_salida, r.estado, " +
            "       c.id_cliente, c.nombre, c.cedula, " +
            "       h.id_habitacion, h.numero, h.tipo, h.piso, h.capacidad, h.precio, h.disponible " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "ORDER BY r.id_reserva";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar reservas: " + e.getMessage());
        }

        return lista;
    }

    private Reserva1 mapearReserva(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"), rs.getString("cedula"));
        Habitacion hab = new Habitacion(
            rs.getInt("id_habitacion"), rs.getInt("numero"), rs.getString("tipo"),
            rs.getInt("piso"), rs.getInt("capacidad"), rs.getDouble("precio"), rs.getBoolean("disponible")
        );
        return new Reserva1(
            rs.getInt("id_reserva"), cliente, hab,
            rs.getDate("fecha_entrada").toString(), rs.getDate("fecha_salida").toString(),
            rs.getString("estado")
        );
    }
}