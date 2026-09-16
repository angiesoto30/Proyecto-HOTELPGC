package reserva1;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static int idHabitacionSeleccionada;
    static int numeroHabitacion;
    static String tipoHabitacion;
    static double precioHabitacion;

    static String nombre;
    static String cedula;
    static int idCliente;

    static int idReserva;

 
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=HotelReserva;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "hotelapp";
    private static final String PASSWORD = "HotelApp2026!";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== HOTEL =====");
            System.out.println("1. Reservar");
            System.out.println("2. Ver mis reservas (por cedula)");
            System.out.println("3. Cancelar una reserva (por cedula)");
            System.out.println("4. Ver todas las reservas");
            System.out.println("5. Ver estado de habitaciones");
            System.out.println("6. Generar reporte de ventas");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            int op = leerNumero();

            if (op == 1) {
                seleccionarHabitacion();
                pedirDatos();
                crearReserva();
                restaurante();

            } else if (op == 2) {
                System.out.print("\nIngrese su cedula: ");
                String ced = sc.nextLine();
                new Hotel().verReserva(ced);

            } else if (op == 3) {
                System.out.print("\nIngrese la cedula de la reserva a cancelar: ");
                String ced = sc.nextLine();
                new Hotel().cancelarReserva(ced);

            } else if (op == 4) {
                new Hotel().mostrarTodasReservas();

            } else if (op == 5) {
                new Hotel().mostrarEstado();
                
                } else if (op == 6) {
              System.out.print("Fecha inicial (AAAA-MM-DD): ");
              String fechaInicio = sc.nextLine();

              System.out.print("Fecha final (AAAA-MM-DD): ");
              String fechaFin = sc.nextLine();

            Reserva1.generarReporteVentas(fechaInicio, fechaFin);

     
            } else if (op == 0) {
                break;
            }
        }
    }

    public static int leerNumero() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print(" Ingrese numero valido: ");
            }
        }
    }

  
    public static void mostrarHabitaciones() {
        System.out.println("\nHABITACIONES DISPONIBLES:");
        String sql = "SELECT numero, tipo, precio FROM Habitaciones WHERE disponible = 1";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                    rs.getInt("numero") + " - " +
                    rs.getString("tipo") + " - $" +
                    rs.getDouble("precio")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar habitaciones: " + e.getMessage());
        }
    }

    
    public static void seleccionarHabitacion() {

        while (true) {

            mostrarHabitaciones();
            System.out.print("Elige el numero de habitacion: ");
            int op = leerNumero();

            String sql = "SELECT id_habitacion, tipo, precio FROM Habitaciones "
                       + "WHERE numero = ? AND disponible = 1";

            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, op);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idHabitacionSeleccionada = rs.getInt("id_habitacion");
                        numeroHabitacion = op;
                        tipoHabitacion = rs.getString("tipo");
                        precioHabitacion = rs.getDouble("precio");
                        return;
                    } else {
                        System.out.println(" Habitacion invalida o no disponible.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Error al validar habitacion: " + e.getMessage());
            }
        }
    }

    public static void pedirDatos() {

        System.out.print("\nNombre: ");
        nombre = sc.nextLine();

        System.out.print("Cedula: ");
        cedula = sc.nextLine();

        idCliente = buscarOCrearCliente(nombre, cedula);
    }

   
    public static int buscarOCrearCliente(String nombre, String cedula) {

        String buscar = "SELECT id_cliente FROM Clientes WHERE cedula = ?";
        String insertar = "INSERT INTO Clientes (nombre, cedula) VALUES (?, ?)";

        try (Connection con = conectar()) {

            try (PreparedStatement ps = con.prepareStatement(buscar)) {
                ps.setString(1, cedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id_cliente");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, cedula);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar/crear cliente: " + e.getMessage());
        }

        return -1;
    }

    public static void crearReserva() {

        System.out.print("\nFecha de entrada (AAAA-MM-DD): ");
        String entrada = sc.nextLine();

        System.out.print("Fecha de salida (AAAA-MM-DD): ");
        String salida = sc.nextLine();

        String insertar = "INSERT INTO Reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado) "
                         + "VALUES (?, ?, ?, ?, 'Confirmada')";
        String actualizar = "UPDATE Habitaciones SET disponible = 0 WHERE id_habitacion = ?";

        try (Connection con = conectar()) {

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idHabitacionSeleccionada);
                ps.setDate(3, Date.valueOf(entrada));
                ps.setDate(4, Date.valueOf(salida));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        idReserva = keys.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(actualizar)) {
                ps.setInt(1, idHabitacionSeleccionada);
                ps.executeUpdate();
            }

            System.out.println("\n Reserva registrada con exito (ID " + idReserva + ")");

        } catch (SQLException e) {
            System.out.println("Error al crear la reserva: " + e.getMessage());
        }
    }

    public static void restaurante() {

        pedirConsumo("Desayuno");
        pedirConsumo("Almuerzo");
        pedirConsumo("Cena");

        mostrarFactura();
    }

    
    public static void pedirConsumo(String tipo) {

        System.out.print("\n¿Desea " + tipo.toLowerCase() + "? (1=Si / 0=No): ");
        int quiere = leerNumero();

        if (quiere != 1) {
            return;
        }

        String sql = "SELECT id_menu, nombre, precio FROM Menus WHERE tipo = ?";
        List<Integer> ids = new ArrayList<>();

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                int contador = 1;
                System.out.println("\nMenus de " + tipo + ":");
                while (rs.next()) {
                    ids.add(rs.getInt("id_menu"));
                    System.out.println(contador + ". " + rs.getString("nombre") + " - $" + rs.getDouble("precio"));
                    contador++;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar menus: " + e.getMessage());
            return;
        }

        System.out.print("Elige una opcion (0 = ninguna): ");
        int op = leerNumero();

        if (op >= 1 && op <= ids.size()) {
            guardarConsumo(ids.get(op - 1));
        }
    }

    public static void guardarConsumo(int idMenu) {

        String sql = "INSERT INTO Consumos (id_reserva, id_menu, cantidad) VALUES (?, ?, 1)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);
            ps.setInt(2, idMenu);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar consumo: " + e.getMessage());
        }
    }

   
    public static void mostrarFactura() {

        String sql =
            "SELECT c.nombre, c.cedula, h.numero, h.tipo, h.precio AS precio_habitacion, " +
            "       r.fecha_entrada, r.fecha_salida, " +
            "       ISNULL(SUM(m.precio * co.cantidad), 0) AS total_consumo " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "LEFT JOIN Consumos co ON co.id_reserva = r.id_reserva " +
            "LEFT JOIN Menus m ON co.id_menu = m.id_menu " +
            "WHERE r.id_reserva = ? " +
            "GROUP BY c.nombre, c.cedula, h.numero, h.tipo, h.precio, r.fecha_entrada, r.fecha_salida";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalConsumo = rs.getDouble("total_consumo");
                    double precioHab = rs.getDouble("precio_habitacion");

                    System.out.println("\n========= FACTURA =========");
                    System.out.println("Cliente: " + rs.getString("nombre"));
                    System.out.println("Cedula: " + rs.getString("cedula"));
                    System.out.println("Habitacion: " + rs.getInt("numero") + " (" + rs.getString("tipo") + ")");
                    System.out.println("Entrada: " + rs.getDate("fecha_entrada"));
                    System.out.println("Salida: " + rs.getDate("fecha_salida"));
                    System.out.println("Precio habitacion: $" + precioHab);
                    System.out.println("Consumo restaurante: $" + totalConsumo);
                    System.out.println("TOTAL: $" + (precioHab + totalConsumo));
                    System.out.println("===========================");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al generar la factura: " + e.getMessage());
        }
    }
}