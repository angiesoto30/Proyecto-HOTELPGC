package reserva1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentanaVerReservas extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);
    private static final Color AVISO = new Color(224, 170, 110);

    private static final String[] COLUMNAS = {"Reserva", "Habitación", "Entrada", "Salida", "Estado"};
    private static final int COL_ESTADO = 4;

    private final JTextField tfCedula = new JTextField();
    private final JTextField tfNombre = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JLabel lblEstado = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

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

    public VentanaVerReservas(Runnable alVolver) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(26, 32, 22, 32));
        tarjeta.setPreferredSize(new Dimension(780, 500));

        JLabel titulo = new JLabel("VER MIS RESERVAS");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fila = new JPanel(new GridLayout(1, 3, 14, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        fila.add(campoConEtiqueta("Cédula", estilizarCampo(tfCedula)));
        fila.add(campoConEtiqueta("Nombre completo", estilizarCampo(tfNombre)));
        fila.add(columnaBoton());

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setBorder(new EmptyBorder(0, 0, 16, 0));
        arriba.add(titulo);
        arriba.add(Box.createRigidArea(new Dimension(0, 16)));
        arriba.add(fila);

        tfCedula.addActionListener(e -> buscar());
        tfNombre.addActionListener(e -> buscar());

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        scroll.getViewport().setBackground(FONDO_CAMPO);

        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setForeground(GRIS_TEXTO);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnVolver.setForeground(GRIS_TEXTO);
        btnVolver.setBackground(new Color(24, 24, 26));
        btnVolver.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> alVolver.run());

        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(14, 0, 0, 0));
        pie.add(lblEstado, BorderLayout.CENTER);
        pie.add(btnVolver, BorderLayout.EAST);

        tarjeta.add(arriba, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);

        add(tarjeta);
    }

    private JPanel campoConEtiqueta(String etiqueta, JTextField campo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(etiqueta);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(GRIS_TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campo.setPreferredSize(new Dimension(200, 36));

        p.add(l);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(campo);
        return p;
    }

    private JPanel columnaBoton() {
        btnBuscar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnBuscar.setForeground(OSCURO);
        btnBuscar.setBackground(DORADO);
        btnBuscar.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuscar.setPreferredSize(new Dimension(150, 36));
        btnBuscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnBuscar.addActionListener(e -> buscar());

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());
        p.add(btnBuscar);
        return p;
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

    private void configurarTabla() {
        tabla.setRowHeight(34);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(55, 55, 58));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(FONDO_CAMPO);
        tabla.setForeground(BLANCO_HUESO);
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

        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(230);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
    }

    private void mostrarMensaje(String texto, boolean esAviso) {
        lblEstado.setForeground(esAviso ? AVISO : GRIS_TEXTO);
        lblEstado.setText(texto);
    }

    private void buscar() {
        String cedula = tfCedula.getText().trim();
        String nombre = tfNombre.getText().trim();

        if (cedula.isEmpty() || nombre.isEmpty()) {
            mostrarMensaje("Completa la cédula y el nombre.", true);
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("Buscando...");

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return consultar(cedula, nombre);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar");
                modelo.setRowCount(0);

                try {
                    List<Object[]> filas = get();

                    if (filas.isEmpty()) {
                        mostrarMensaje("No se encontraron reservas con esos datos. Verifica la cédula y el nombre.", true);
                        return;
                    }

                    for (Object[] fila : filas) {
                        modelo.addRow(fila);
                    }
                    mostrarMensaje(filas.size() == 1
                            ? "1 reserva encontrada."
                            : filas.size() + " reservas encontradas.", false);

                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarMensaje(causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.", true);
                }
            }
        }.execute();
    }

    private List<Object[]> consultar(String cedula, String nombre) throws Exception {
        List<Object[]> filas = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String sql =
            "SELECT r.id_reserva, h.numero, h.tipo, r.fecha_entrada, r.fecha_salida, r.estado " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE c.cedula = ? AND LOWER(c.nombre) = LOWER(?) " +
            "ORDER BY r.fecha_entrada DESC, r.id_reserva DESC";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cedula);
                ps.setString(2, nombre);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        filas.add(new Object[] {
                            "#" + rs.getInt("id_reserva"),
                            "Hab " + rs.getInt("numero") + " — " + rs.getString("tipo"),
                            rs.getDate("fecha_entrada").toLocalDate().format(f),
                            rs.getDate("fecha_salida").toLocalDate().format(f),
                            rs.getString("estado")
                        });
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar las reservas: " + e.getMessage());
        }

        return filas;
    }
}
