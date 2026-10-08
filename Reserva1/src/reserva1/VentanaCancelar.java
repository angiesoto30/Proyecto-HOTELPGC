package reserva1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentanaCancelar extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);
    private static final Color ROJO_ERROR = new Color(224, 120, 120);

    private final Runnable alVolver;

    private final CardLayout vistas = new CardLayout();
    private final JPanel contenido = new JPanel(vistas);

    private final JTextField tfCedula = new JTextField();
    private final JTextField tfNombre = new JTextField();
    private final JLabel lblErrorBuscar = new JLabel(" ");
    private final JButton btnBuscar = new JButton("Buscar reservas");

    private final JComboBox<Reserva> cbReservas = new JComboBox<>();
    private final JLabel lblAyuda = new JLabel(" ");
    private final JLabel lblErrorCancelar = new JLabel(" ");
    private final JButton btnAtras = new JButton("Atrás");
    private final JButton btnCancelar = new JButton("Cancelar reserva seleccionada");
    private boolean esperandoConfirmacion = false;

    private final JLabel lblExitoId = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoMsg = new JLabel(" ", SwingConstants.CENTER);

    private static class Reserva {
        final int idReserva;
        final int idHabitacion;
        final String texto;

        Reserva(int idReserva, int idHabitacion, String texto) {
            this.idReserva = idReserva;
            this.idHabitacion = idHabitacion;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
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

    public VentanaCancelar(Runnable alVolver) {
        this.alVolver = alVolver;
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(26, 32, 24, 32));
        tarjeta.setPreferredSize(new Dimension(580, 420));

        contenido.setOpaque(false);
        contenido.add(construirBuscar(), "buscar");
        contenido.add(construirResultado(), "resultado");
        contenido.add(construirExito(), "exito");
        tarjeta.add(contenido, BorderLayout.CENTER);

        add(tarjeta);
    }

    private JPanel construirBuscar() {
        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setOpaque(false);

        JLabel titulo = new JLabel("CANCELAR RESERVA");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel columna = nuevaColumna();

        JLabel intro = new JLabel("Ingresa los datos con los que hiciste la reserva.");
        intro.setFont(new Font("SansSerif", Font.PLAIN, 13));
        intro.setForeground(GRIS_TEXTO);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(intro);
        columna.add(Box.createRigidArea(new Dimension(0, 20)));

        agregar(columna, "Cédula", estilizarCampo(tfCedula));
        agregar(columna, "Nombre completo", estilizarCampo(tfNombre));

        lblErrorBuscar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblErrorBuscar.setForeground(ROJO_ERROR);

        JButton btnVolver = botonSecundario("Volver");
        btnVolver.addActionListener(e -> alVolver.run());

        estilizarBotonPrincipal(btnBuscar);
        btnBuscar.addActionListener(e -> buscar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnVolver);
        botones.add(btnBuscar);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblErrorBuscar, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);

        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(columna, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirResultado() {
        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setOpaque(false);

        JLabel titulo = new JLabel("CANCELAR RESERVA");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel columna = nuevaColumna();
        agregar(columna, "Reservas activas encontradas", estilizarCombo(cbReservas));

        lblAyuda.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblAyuda.setForeground(new Color(224, 170, 110));
        lblAyuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(lblAyuda);

        lblErrorCancelar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblErrorCancelar.setForeground(ROJO_ERROR);

        btnAtras.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnAtras.setForeground(GRIS_TEXTO);
        btnAtras.setBackground(new Color(24, 24, 26));
        btnAtras.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        btnAtras.setFocusPainted(false);
        btnAtras.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtras.addActionListener(e -> atras());

        estilizarBotonPrincipal(btnCancelar);
        btnCancelar.addActionListener(e -> pedirCancelacion());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnAtras);
        botones.add(btnCancelar);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblErrorCancelar, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);

        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(columna, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirExito() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("RESERVA CANCELADA");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblExitoId.setFont(new Font("Serif", Font.BOLD, 38));
        lblExitoId.setForeground(BLANCO_HUESO);
        lblExitoId.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblExitoMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblExitoMsg.setForeground(GRIS_TEXTO);
        lblExitoMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnVolver = new JButton("Volver al panel");
        estilizarBotonPrincipal(btnVolver);
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> alVolver.run());

        p.add(Box.createVerticalGlue());
        p.add(titulo);
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(lblExitoId);
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(lblExitoMsg);
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(btnVolver);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel nuevaColumna() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private void agregar(JPanel columna, String etiqueta, JComponent campo) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(GRIS_TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(520, 36));
        campo.setPreferredSize(new Dimension(380, 36));

        columna.add(l);
        columna.add(Box.createRigidArea(new Dimension(0, 4)));
        columna.add(campo);
        columna.add(Box.createRigidArea(new Dimension(0, 14)));
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

    private JComboBox<Reserva> estilizarCombo(JComboBox<Reserva> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setForeground(BLANCO_HUESO);
        combo.setBackground(FONDO_CAMPO);
        combo.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        combo.putClientProperty("FlatLaf.style",
                "buttonBackground: #222224; buttonArrowColor: #C9A869");
        return combo;
    }

    private JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(GRIS_TEXTO);
        b.setBackground(new Color(24, 24, 26));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void estilizarBotonPrincipal(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(OSCURO);
        b.setBackground(DORADO);
        b.setBorder(new EmptyBorder(11, 26, 11, 26));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void buscar() {
        lblErrorBuscar.setText(" ");

        String cedula = tfCedula.getText().trim();
        String nombre = tfNombre.getText().trim();

        if (cedula.isEmpty() || nombre.isEmpty()) {
            lblErrorBuscar.setText("Completa la cédula y el nombre.");
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("Buscando...");

        new SwingWorker<List<Reserva>, Void>() {
            @Override
            protected List<Reserva> doInBackground() throws Exception {
                return consultarReservas(cedula, nombre);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar reservas");
                try {
                    List<Reserva> lista = get();
                    if (lista.isEmpty()) {
                        lblErrorBuscar.setText("<html>No se encontraron reservas activas con esos datos. "
                                + "Verifica la cédula y el nombre.</html>");
                        return;
                    }
                    cbReservas.removeAllItems();
                    for (Reserva r : lista) {
                        cbReservas.addItem(r);
                    }
                    lblErrorCancelar.setText(" ");
                    reiniciarConfirmacion();
                    vistas.show(contenido, "resultado");
                } catch (Exception ex) {
                    lblErrorBuscar.setText("<html>" + mensajeDe(ex) + "</html>");
                }
            }
        }.execute();
    }

    private void atras() {
        if (esperandoConfirmacion) {
            reiniciarConfirmacion();
        } else {
            lblErrorCancelar.setText(" ");
            vistas.show(contenido, "buscar");
        }
    }

    private void pedirCancelacion() {
        if (!esperandoConfirmacion) {
            esperandoConfirmacion = true;
            cbReservas.setEnabled(false);
            lblAyuda.setText("Esto libera la habitación y no se puede deshacer. ¿Confirmas?");
            btnCancelar.setText("Sí, cancelar reserva");
            btnAtras.setText("No, volver");
            return;
        }

        final Reserva r = (Reserva) cbReservas.getSelectedItem();
        if (r == null) {
            return;
        }

        btnCancelar.setEnabled(false);
        btnCancelar.setText("Cancelando...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                cancelarReserva(r);
                return null;
            }

            @Override
            protected void done() {
                btnCancelar.setEnabled(true);
                try {
                    get();
                    lblExitoId.setText("Reserva #" + r.idReserva);
                    lblExitoMsg.setText("La reserva quedó cancelada y la habitación está libre otra vez.");
                    vistas.show(contenido, "exito");
                } catch (Exception ex) {
                    lblErrorCancelar.setText("<html>" + mensajeDe(ex) + "</html>");
                    reiniciarConfirmacion();
                }
            }
        }.execute();
    }

    private void reiniciarConfirmacion() {
        esperandoConfirmacion = false;
        cbReservas.setEnabled(true);
        lblAyuda.setText(" ");
        btnCancelar.setText("Cancelar reserva seleccionada");
        btnAtras.setText("Atrás");
    }

    private String mensajeDe(Exception ex) {
        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
        return causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.";
    }

    private List<Reserva> consultarReservas(String cedula, String nombre) throws Exception {
        List<Reserva> lista = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String sql =
            "SELECT r.id_reserva, r.id_habitacion, r.fecha_entrada, r.fecha_salida, h.numero, h.tipo " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE c.cedula = ? AND LOWER(c.nombre) = LOWER(?) AND r.estado = 'Confirmada' " +
            "ORDER BY r.fecha_entrada";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cedula);
                ps.setString(2, nombre);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String texto = "Reserva #" + rs.getInt("id_reserva")
                                + " — Hab " + rs.getInt("numero") + " (" + rs.getString("tipo") + ")"
                                + " — " + rs.getDate("fecha_entrada").toLocalDate().format(f)
                                + " → " + rs.getDate("fecha_salida").toLocalDate().format(f);
                        lista.add(new Reserva(rs.getInt("id_reserva"), rs.getInt("id_habitacion"), texto));
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar las reservas: " + e.getMessage());
        }

        return lista;
    }

    private void cancelarReserva(Reserva r) throws Exception {
        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Reservas SET estado = 'Cancelada' WHERE id_reserva = ? AND estado = 'Confirmada'")) {
                    ps.setInt(1, r.idReserva);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("Esa reserva ya no está activa.");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Habitaciones SET disponible = 1 WHERE id_habitacion = ?")) {
                    ps.setInt(1, r.idHabitacion);
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al cancelar la reserva: " + e.getMessage());
        }
    }
}
