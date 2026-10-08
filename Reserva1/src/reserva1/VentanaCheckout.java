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

public class VentanaCheckout extends JPanel {

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
    private final JLabel lblErrorBuscar = new JLabel(" ");
    private final JButton btnBuscar = new JButton("Buscar reservas");

    private final JComboBox<Activa> cbReservas = new JComboBox<>();
    private final JLabel lblAyuda = new JLabel(" ");
    private final JLabel lblErrorCheckout = new JLabel(" ");
    private final JButton btnAtras = new JButton("Atrás");
    private final JButton btnCheckout = new JButton("Hacer checkout");
    private boolean esperandoConfirmacion = false;

    private final JLabel lblExitoId = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoMsg = new JLabel(" ", SwingConstants.CENTER);

    private static class Activa {
        final int idReserva;
        final int idHabitacion;
        final int numeroHabitacion;
        final String texto;

        Activa(int idReserva, int idHabitacion, int numeroHabitacion, String texto) {
            this.idReserva = idReserva;
            this.idHabitacion = idHabitacion;
            this.numeroHabitacion = numeroHabitacion;
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

    public VentanaCheckout(Runnable alVolver) {
        this.alVolver = alVolver;
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(26, 32, 24, 32));
        tarjeta.setPreferredSize(new Dimension(700, 400));

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

        JLabel titulo = new JLabel("HACER CHECKOUT");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel columna = nuevaColumna();

        JLabel intro = new JLabel("Ingresa la cédula del cliente que va a dejar la habitación.");
        intro.setFont(new Font("SansSerif", Font.PLAIN, 13));
        intro.setForeground(GRIS_TEXTO);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(intro);
        columna.add(Box.createRigidArea(new Dimension(0, 20)));

        agregar(columna, "Cédula", estilizarCampo(tfCedula));
        tfCedula.addActionListener(e -> buscar());

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

        JLabel titulo = new JLabel("HACER CHECKOUT");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel columna = nuevaColumna();
        agregar(columna, "Reservas activas del cliente", estilizarCombo(cbReservas));

        lblAyuda.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblAyuda.setForeground(new Color(224, 170, 110));
        lblAyuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(lblAyuda);

        lblErrorCheckout.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblErrorCheckout.setForeground(ROJO_ERROR);

        estilizarBotonSecundario(btnAtras);
        btnAtras.addActionListener(e -> atras());

        estilizarBotonPrincipal(btnCheckout);
        btnCheckout.addActionListener(e -> pedirCheckout());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnAtras);
        botones.add(btnCheckout);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblErrorCheckout, BorderLayout.NORTH);
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

        JLabel titulo = new JLabel("CHECKOUT REALIZADO");
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
        campo.setMaximumSize(new Dimension(640, 36));
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

    private JComboBox<Activa> estilizarCombo(JComboBox<Activa> combo) {
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
        estilizarBotonSecundario(b);
        return b;
    }

    private void estilizarBotonSecundario(JButton b) {
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(GRIS_TEXTO);
        b.setBackground(new Color(24, 24, 26));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        if (cedula.isEmpty()) {
            lblErrorBuscar.setText("Ingresa la cédula del cliente.");
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("Buscando...");

        new SwingWorker<List<Activa>, Void>() {
            @Override
            protected List<Activa> doInBackground() throws Exception {
                return consultarActivas(cedula);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar reservas");
                try {
                    List<Activa> lista = get();
                    if (lista.isEmpty()) {
                        lblErrorBuscar.setText("<html>No se encontró una reserva activa para esa cédula.</html>");
                        return;
                    }
                    cbReservas.removeAllItems();
                    for (Activa a : lista) {
                        cbReservas.addItem(a);
                    }
                    lblErrorCheckout.setText(" ");
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
            lblErrorCheckout.setText(" ");
            vistas.show(contenido, "buscar");
        }
    }

    private void pedirCheckout() {
        if (!esperandoConfirmacion) {
            esperandoConfirmacion = true;
            cbReservas.setEnabled(false);
            lblAyuda.setText("Se registrará la salida y la habitación quedará libre. ¿Confirmas?");
            btnCheckout.setText("Sí, hacer checkout");
            btnAtras.setText("No, volver");
            return;
        }

        final Activa a = (Activa) cbReservas.getSelectedItem();
        if (a == null) {
            return;
        }

        btnCheckout.setEnabled(false);
        btnCheckout.setText("Procesando...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                hacerCheckout(a);
                return null;
            }

            @Override
            protected void done() {
                btnCheckout.setEnabled(true);
                try {
                    get();
                    lblExitoId.setText("Reserva #" + a.idReserva);
                    lblExitoMsg.setText("Salida registrada. La habitación " + a.numeroHabitacion + " quedó libre.");
                    vistas.show(contenido, "exito");
                } catch (Exception ex) {
                    lblErrorCheckout.setText("<html>" + mensajeDe(ex) + "</html>");
                    reiniciarConfirmacion();
                }
            }
        }.execute();
    }

    private void reiniciarConfirmacion() {
        esperandoConfirmacion = false;
        cbReservas.setEnabled(true);
        lblAyuda.setText(" ");
        btnCheckout.setText("Hacer checkout");
        btnAtras.setText("Atrás");
    }

    private String mensajeDe(Exception ex) {
        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
        return causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.";
    }

    private List<Activa> consultarActivas(String cedula) throws Exception {
        List<Activa> lista = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String sql =
            "SELECT r.id_reserva, r.id_habitacion, c.nombre, h.numero, h.tipo, r.fecha_entrada, r.fecha_salida " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE c.cedula = ? AND r.estado = 'Confirmada' " +
            "ORDER BY r.fecha_entrada";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cedula);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String texto = "Reserva #" + rs.getInt("id_reserva")
                                + " — " + rs.getString("nombre")
                                + " — Hab " + rs.getInt("numero") + " (" + rs.getString("tipo") + ")"
                                + " — " + rs.getDate("fecha_entrada").toLocalDate().format(f)
                                + " → " + rs.getDate("fecha_salida").toLocalDate().format(f);
                        lista.add(new Activa(rs.getInt("id_reserva"), rs.getInt("id_habitacion"),
                                rs.getInt("numero"), texto));
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar las reservas: " + e.getMessage());
        }

        return lista;
    }

    private void hacerCheckout(Activa a) throws Exception {
        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Reservas SET estado = 'Finalizada' WHERE id_reserva = ? AND estado = 'Confirmada'")) {
                    ps.setInt(1, a.idReserva);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("Esa reserva ya no está activa.");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Habitaciones SET disponible = 1 WHERE id_habitacion = ?")) {
                    ps.setInt(1, a.idHabitacion);
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al hacer el checkout: " + e.getMessage());
        }
    }
}
