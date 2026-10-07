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

    public void buscarConFiltros(Double precioMin, Double precioMax, String tipo) {
        System.out.println("\n== RESULTADOS DE BUSQUEDA ==");
        boolean hay = false;
        for (Habitacion h : habitaciones) {
            if (!h.isDisponible()) continue;
            if (precioMin != null && h.getPrecio() < precioMin) continue;
            if (precioMax != null && h.getPrecio() > precioMax) continue;
            if (tipo != null && !h.getTipo().equalsIgnoreCase(tipo)) continue;

            System.out.println(
                "Num: " + h.getNumero() +
                " | Tipo: " + h.getTipo() +
                " | Piso: " + h.getPiso() +
                " | Precio: $" + h.getPrecio()
            );
            hay = true;
        }
        if (!hay) System.out.println("No hay habitaciones que coincidan con los filtros.");
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

    public void verReserva(String cedula, String nombreCompleto) {
        List<Reserva1> reservas = buscarReservasPorCedulaYNombre(cedula, nombreCompleto);
        if (reservas.isEmpty()) {
            System.out.println("No se encontraron reservas con esos datos. Verifique cedula y nombre.");
            return;
        }
        System.out.println("\n== SU RESERVA ==");
        for (Reserva1 r : reservas) {
            r.mostrarInfo();
        }
    }

    public void cancelarReserva(String cedula, String nombreCompleto) {
        List<Reserva1> reservas = buscarReservasPorCedulaYNombre(cedula, nombreCompleto);
        if (reservas.isEmpty()) {
            System.out.println("No se encontro reserva con esos datos. Verifique cedula y nombre.");
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

    public void hacerCheckoutManual(String cedula) {

        String buscarReserva =
            "SELECT r.id_reserva, r.id_habitacion " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "WHERE c.cedula = ? AND r.estado = 'Confirmada'";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(buscarReserva)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No se encontro una reserva activa para esa cedula.");
                    return;
                }

                int idReservaEncontrada = rs.getInt("id_reserva");
                int idHabitacion = rs.getInt("id_habitacion");

                try (PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE Reservas SET estado = 'Finalizada' WHERE id_reserva = ?")) {
                    ps2.setInt(1, idReservaEncontrada);
                    ps2.executeUpdate();
                }

                try (PreparedStatement ps3 = con.prepareStatement(
                        "UPDATE Habitaciones SET disponible = 1 WHERE id_habitacion = ?")) {
                    ps3.setInt(1, idHabitacion);
                    ps3.executeUpdate();
                }

                System.out.println("Check-out realizado con exito. Habitacion liberada.");
            }

        } catch (SQLException e) {
            System.out.println("Error al hacer check-out: " + e.getMessage());
        }
    }

    public void editarReserva(int idReserva, String nuevaFechaEntrada, String nuevaFechaSalida) {

        String sql = "UPDATE Reservas SET fecha_entrada = ?, fecha_salida = ?, recordatorio_enviado = 0 " +
                     "WHERE id_reserva = ?";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaFechaEntrada);
            ps.setString(2, nuevaFechaSalida);
            ps.setInt(3, idReserva);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Reserva #" + idReserva + " actualizada correctamente.");
            } else {
                System.out.println("No se encontro ninguna reserva con el ID " + idReserva + ".");
            }

        } catch (SQLException e) {
            System.out.println("Error al editar la reserva: " + e.getMessage());
        }
    }

    public void eliminarReservaDefinitivamente(int idReserva) {

        String buscarHabitacion = "SELECT id_habitacion FROM Reservas WHERE id_reserva = ?";
        String eliminarConsumos = "DELETE FROM Consumos WHERE id_reserva = ?";
        String eliminarReserva = "DELETE FROM Reservas WHERE id_reserva = ?";
        String liberarHabitacion = "UPDATE Habitaciones SET disponible = 1 WHERE id_habitacion = ?";

        try (Connection con = Main.conectar()) {

            int idHabitacion = -1;

            try (PreparedStatement ps = con.prepareStatement(buscarHabitacion)) {
                ps.setInt(1, idReserva);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idHabitacion = rs.getInt("id_habitacion");
                    } else {
                        System.out.println("No se encontro ninguna reserva con el ID " + idReserva + ".");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(eliminarConsumos)) {
                ps.setInt(1, idReserva);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(eliminarReserva)) {
                ps.setInt(1, idReserva);
                ps.executeUpdate();
            }

            if (idHabitacion != -1) {
                try (PreparedStatement ps = con.prepareStatement(liberarHabitacion)) {
                    ps.setInt(1, idHabitacion);
                    ps.executeUpdate();
                }
            }

            System.out.println("Reserva #" + idReserva + " eliminada definitivamente y habitacion liberada.");

        } catch (SQLException e) {
            System.out.println("Error al eliminar la reserva: " + e.getMessage());
        }
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

    private List<Reserva1> buscarReservasPorCedulaYNombre(String cedula, String nombreCompleto) {
        List<Reserva1> lista = new ArrayList<>();

        String sql =
            "SELECT r.id_reserva, r.fecha_entrada, r.fecha_salida, r.estado, " +
            "       c.id_cliente, c.nombre, c.cedula, " +
            "       h.id_habitacion, h.numero, h.tipo, h.piso, h.capacidad, h.precio, h.disponible " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE c.cedula = ? AND LOWER(c.nombre) = LOWER(?) AND r.estado = 'Confirmada'";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula);
            ps.setString(2, nombreCompleto);

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