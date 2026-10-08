package reserva1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class VentanaTodasReservas extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);
    private static final Color AVISO = new Color(224, 170, 110);

    private static final String[] COLUMNAS = {"Reserva", "Cliente", "Cédula", "Habitación", "Entrada", "Salida", "Estado"};
    private static final int COL_ESTADO = 6;

    private static final DateTimeFormatter VISIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter[] FORMATOS = {
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
    };

    private final boolean esJefe;
    private final Runnable alVolver;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<Fila> filas = new ArrayList<>();

    private final JLabel lblEstado = new JLabel(" ");

    private final CardLayout vistasAcc = new CardLayout();
    private final JPanel acciones = new JPanel(vistasAcc);
    private final JTextField tfEntrada = new JTextField();
    private final JTextField tfSalida = new JTextField();
    private final JLabel lblConfirmarEliminar = new JLabel(" ");

    private Fila enAccion;
    private boolean ocupado = false;

    private static class Fila {
        final int id;
        final String estado;
        final LocalDate entrada;
        final LocalDate salida;
        final Object[] celdas;

        Fila(int id, String estado, LocalDate entrada, LocalDate salida, Object[] celdas) {
            this.id = id;
            this.estado = estado;
            this.entrada = entrada;
            this.salida = salida;
            this.celdas = celdas;
        }
    }

    private interface Tarea {
        void ejecutar() throws Exception;
    }

    private static class Tarjeta extends JPanel {
        Tarjeta() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(22, 22, 24, 215));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g2.setColor(new Color(201, 168, 105, 140));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public VentanaTodasReservas(Runnable alVolver, boolean esJefe) {
        this.alVolver = alVolver;
        this.esJefe = esJefe;
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 14));
        tarjeta.setBorder(new EmptyBorder(26, 32, 22, 32));
        tarjeta.setPreferredSize(new Dimension(880, 540));

        JLabel titulo = new JLabel("TODAS LAS RESERVAS");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        scroll.getViewport().setBackground(FONDO_CAMPO);

        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setForeground(GRIS_TEXTO);

        construirAcciones();

        JButton btnVolver = botonSecundario("Volver");
        btnVolver.addActionListener(e -> alVolver.run());

        JPanel filaInferior = new JPanel(new BorderLayout(12, 0));
        filaInferior.setOpaque(false);
        filaInferior.add(acciones, BorderLayout.CENTER);
        filaInferior.add(btnVolver, BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout(0, 10));
        pie.setOpaque(false);
        pie.add(lblEstado, BorderLayout.NORTH);
        pie.add(filaInferior, BorderLayout.SOUTH);

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);

        add(tarjeta);

        cargar(null);
    }

    private void construirAcciones() {
        acciones.setOpaque(false);

        JPanel normal = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        normal.setOpaque(false);

        if (esJefe) {
            JButton btnEditar = botonPrincipal("Editar fechas");
            btnEditar.addActionListener(e -> iniciarEdicion());
            JButton btnEliminar = botonSecundario("Eliminar reserva");
            btnEliminar.addActionListener(e -> iniciarEliminar());
            normal.add(btnEditar);
            normal.add(btnEliminar);
        } else {
            JLabel nota = new JLabel("Solo el Jefe puede editar o eliminar reservas.");
            nota.setFont(new Font("SansSerif", Font.PLAIN, 12));
            nota.setForeground(GRIS_TEXTO);
            normal.add(nota);
        }

        JPanel editar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editar.setOpaque(false);
        estilizarCampo(tfEntrada);
        estilizarCampo(tfSalida);
        tfEntrada.setPreferredSize(new Dimension(115, 38));
        tfSalida.setPreferredSize(new Dimension(115, 38));

        JButton btnGuardar = botonPrincipal("Guardar");
        btnGuardar.addActionListener(e -> guardarEdicion());
        JButton btnCancelarEdicion = botonSecundario("Cancelar");
        btnCancelarEdicion.addActionListener(e -> cancelarAccion());

        editar.add(etiqueta("Entrada"));
        editar.add(tfEntrada);
        editar.add(etiqueta("Salida"));
        editar.add(tfSalida);
        editar.add(btnGuardar);
        editar.add(btnCancelarEdicion);

        JPanel eliminar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        eliminar.setOpaque(false);
        lblConfirmarEliminar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblConfirmarEliminar.setForeground(AVISO);

        JButton btnSi = new JButton("Sí, eliminar");
        estilizarBoton(btnSi, new Color(190, 80, 80), BLANCO_HUESO, true);
        btnSi.addActionListener(e -> confirmarEliminar());
        JButton btnNo = botonSecundario("No, volver");
        btnNo.addActionListener(e -> cancelarAccion());

        eliminar.add(lblConfirmarEliminar);
        eliminar.add(btnSi);
        eliminar.add(btnNo);

        acciones.add(normal, "normal");
        acciones.add(editar, "editar");
        acciones.add(eliminar, "eliminar");
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(GRIS_TEXTO);
        return l;
    }

    private JTextField estilizarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setForeground(BLANCO_HUESO);
        campo.setBackground(FONDO_CAMPO);
        campo.setCaretColor(BLANCO_HUESO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(8, 10, 8, 10)));
        return campo;
    }

    private void estilizarBoton(JButton b, Color fondo, Color texto, boolean negrita) {
        b.setFont(new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(texto);
        b.setBackground(fondo);
        b.setBorder(new EmptyBorder(11, 20, 11, 20));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton botonPrincipal(String texto) {
        JButton b = new JButton(texto);
        estilizarBoton(b, DORADO, OSCURO, true);
        return b;
    }

    private JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        estilizarBoton(b, new Color(24, 24, 26), GRIS_TEXTO, false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 20, 10, 20)));
        return b;
    }

    private void configurarTabla() {
        tabla.setRowHeight(32);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(55, 55, 58));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(FONDO_CAMPO);
        tabla.setForeground(BLANCO_HUESO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new Dimension(100, 34));

        DefaultTableCellRenderer cabecera = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, valor, false, false, fila, col);
                setBackground(new Color(30, 30, 32));
                setForeground(DORADO);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
        tabla.getTableHeader().setDefaultRenderer(cabecera);

        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, valor, sel, false, fila, col);
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (sel) {
                    setBackground(new Color(70, 62, 45));
                } else {
                    setBackground(fila % 2 == 0 ? FONDO_CAMPO : new Color(40, 40, 43));
                }

                setForeground(BLANCO_HUESO);
                if (col == COL_ESTADO && valor != null) {
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                    switch (valor.toString()) {
                        case "Confirmada":
                            setForeground(new Color(130, 205, 150));
                            break;
                        case "Cancelada":
                            setForeground(new Color(224, 120, 120));
                            break;
                        default:
                            setForeground(GRIS_TEXTO);
                    }
                }
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celdas);

        int[] anchos = {70, 170, 105, 150, 95, 95, 100};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    private void mostrarMensaje(String texto, boolean esAviso) {
        lblEstado.setForeground(esAviso ? AVISO : GRIS_TEXTO);
        lblEstado.setText(texto);
    }

    private Fila seleccionada() {
        int i = tabla.getSelectedRow();
        if (i < 0 || i >= filas.size()) {
            mostrarMensaje("Selecciona primero una reserva de la tabla.", true);
            return null;
        }
        return filas.get(i);
    }

    private void iniciarEdicion() {
        Fila f = seleccionada();
        if (f == null) {
            return;
        }
        if (!f.estado.equals("Confirmada")) {
            mostrarMensaje("Solo se pueden editar reservas confirmadas.", true);
            return;
        }

        enAccion = f;
        tfEntrada.setText(f.entrada.format(VISIBLE));
        tfSalida.setText(f.salida.format(VISIBLE));
        tabla.setEnabled(false);
        mostrarMensaje("Editando la reserva #" + f.id + ". Usa el formato dd/mm/aaaa.", false);
        vistasAcc.show(acciones, "editar");
    }

    private void guardarEdicion() {
        if (ocupado || enAccion == null) {
            return;
        }

        LocalDate entrada = parsearFecha(tfEntrada.getText().trim());
        LocalDate salida = parsearFecha(tfSalida.getText().trim());

        if (entrada == null || salida == null) {
            mostrarMensaje("Fecha inválida. Usa dd/mm/aaaa, por ejemplo 07/10/2026.", true);
            return;
        }
        if (!salida.isAfter(entrada)) {
            mostrarMensaje("La fecha de salida debe ser posterior a la de entrada.", true);
            return;
        }

        final int id = enAccion.id;
        ejecutar(() -> actualizarFechas(id, entrada, salida), "Reserva #" + id + " actualizada.");
    }

    private void iniciarEliminar() {
        Fila f = seleccionada();
        if (f == null) {
            return;
        }

        enAccion = f;
        tabla.setEnabled(false);
        lblConfirmarEliminar.setText("¿Eliminar definitivamente la reserva #" + f.id + "? No se puede deshacer.");
        mostrarMensaje(" ", false);
        vistasAcc.show(acciones, "eliminar");
    }

    private void confirmarEliminar() {
        if (ocupado || enAccion == null) {
            return;
        }

        final int id = enAccion.id;
        ejecutar(() -> eliminarReserva(id), "Reserva #" + id + " eliminada.");
    }

    private void cancelarAccion() {
        if (ocupado) {
            return;
        }
        enAccion = null;
        tabla.setEnabled(true);
        mostrarMensaje(" ", false);
        vistasAcc.show(acciones, "normal");
    }

    private void ejecutar(Tarea tarea, String mensajeExito) {
        ocupado = true;
        mostrarMensaje("Procesando...", false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                tarea.ejecutar();
                return null;
            }

            @Override
            protected void done() {
                ocupado = false;
                try {
                    get();
                    cancelarAccion();
                    cargar(mensajeExito);
                } catch (Exception ex) {
                    mostrarMensaje(mensajeDe(ex), true);
                }
            }
        }.execute();
    }

    private String mensajeDe(Exception ex) {
        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
        return causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.";
    }

    private static LocalDate parsearFecha(String texto) {
        for (DateTimeFormatter f : FORMATOS) {
            try {
                return LocalDate.parse(texto, f);
            } catch (DateTimeParseException e) {
            }
        }
        return null;
    }

    private void cargar(String mensajeFinal) {
        new SwingWorker<List<Fila>, Void>() {
            @Override
            protected List<Fila> doInBackground() throws Exception {
                return consultarTodas();
            }

            @Override
            protected void done() {
                try {
                    List<Fila> lista = get();
                    filas.clear();
                    filas.addAll(lista);
                    modelo.setRowCount(0);
                    for (Fila f : lista) {
                        modelo.addRow(f.celdas);
                    }

                    if (mensajeFinal != null) {
                        mostrarMensaje(mensajeFinal, false);
                    } else if (lista.isEmpty()) {
                        mostrarMensaje("No hay reservas registradas.", true);
                    } else {
                        mostrarMensaje(lista.size() == 1 ? "1 reserva." : lista.size() + " reservas.", false);
                    }
                } catch (Exception ex) {
                    mostrarMensaje(mensajeDe(ex), true);
                }
            }
        }.execute();
    }

    private List<Fila> consultarTodas() throws Exception {
        List<Fila> lista = new ArrayList<>();

        String sql =
            "SELECT r.id_reserva, c.nombre, c.cedula, h.numero, h.tipo, " +
            "       r.fecha_entrada, r.fecha_salida, r.estado " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "ORDER BY r.id_reserva DESC";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id_reserva");
                    LocalDate entrada = rs.getDate("fecha_entrada").toLocalDate();
                    LocalDate salida = rs.getDate("fecha_salida").toLocalDate();
                    String estado = rs.getString("estado");

                    Object[] celdas = {
                        "#" + id,
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        "Hab " + rs.getInt("numero") + " — " + rs.getString("tipo"),
                        entrada.format(VISIBLE),
                        salida.format(VISIBLE),
                        estado
                    };
                    lista.add(new Fila(id, estado, entrada, salida, celdas));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar las reservas: " + e.getMessage());
        }

        return lista;
    }

    private void actualizarFechas(int id, LocalDate entrada, LocalDate salida) throws Exception {
        String sql = "UPDATE Reservas SET fecha_entrada = ?, fecha_salida = ?, recordatorio_enviado = 0 "
                   + "WHERE id_reserva = ? AND estado = 'Confirmada'";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(entrada));
                ps.setDate(2, Date.valueOf(salida));
                ps.setInt(3, id);

                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("La reserva no existe o ya no está confirmada.");
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al editar la reserva: " + e.getMessage());
        }
    }

    private void eliminarReserva(int id) throws Exception {
        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            con.setAutoCommit(false);
            try {
                String estado = "";
                int idHabitacion = -1;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT estado, id_habitacion FROM Reservas WHERE id_reserva = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("La reserva ya no existe.");
                        }
                        estado = rs.getString("estado");
                        idHabitacion = rs.getInt("id_habitacion");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM Consumos WHERE id_reserva = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM Reservas WHERE id_reserva = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                if (estado.equals("Confirmada")) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE Habitaciones SET disponible = 1 WHERE id_habitacion = ?")) {
                        ps.setInt(1, idHabitacion);
                        ps.executeUpdate();
                    }
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar la reserva: " + e.getMessage());
        }
    }
}
