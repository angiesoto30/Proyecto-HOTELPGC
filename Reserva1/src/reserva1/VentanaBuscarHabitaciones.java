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
import java.util.ArrayList;
import java.util.List;

public class VentanaBuscarHabitaciones extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);
    private static final Color AVISO = new Color(224, 170, 110);

    private static final String[] COLUMNAS = {"Habitación", "Tipo", "Piso", "Capacidad", "Precio por noche"};
    private static final String TODOS = "Todos";

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JTextField tfPrecioMin = new JTextField();
    private final JTextField tfPrecioMax = new JTextField();
    private final JComboBox<String> cbTipo = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");
    private final JButton btnBuscar = new JButton("Buscar");

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

    public VentanaBuscarHabitaciones(Runnable alVolver) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 14));
        tarjeta.setBorder(new EmptyBorder(26, 32, 22, 32));
        tarjeta.setPreferredSize(new Dimension(780, 540));

        JLabel titulo = new JLabel("BUSCAR HABITACIONES");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtros.setOpaque(false);

        estilizarCampo(tfPrecioMin);
        estilizarCampo(tfPrecioMax);
        tfPrecioMin.setPreferredSize(new Dimension(130, 38));
        tfPrecioMax.setPreferredSize(new Dimension(130, 38));
        estilizarCombo(cbTipo);
        cbTipo.setPreferredSize(new Dimension(150, 38));

        btnBuscar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnBuscar.setForeground(OSCURO);
        btnBuscar.setBackground(DORADO);
        btnBuscar.setBorder(new EmptyBorder(11, 24, 11, 24));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscar());

        filtros.add(etiqueta("Precio mínimo"));
        filtros.add(tfPrecioMin);
        filtros.add(etiqueta("Precio máximo"));
        filtros.add(tfPrecioMax);
        filtros.add(etiqueta("Tipo"));
        filtros.add(cbTipo);
        filtros.add(btnBuscar);

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        scroll.getViewport().setBackground(FONDO_CAMPO);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

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
        pie.add(lblEstado, BorderLayout.CENTER);
        pie.add(btnVolver, BorderLayout.EAST);

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(centro, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);

        add(tarjeta);

        cargarTipos();
        buscar();
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

    private JComboBox<String> estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setForeground(BLANCO_HUESO);
        combo.setBackground(FONDO_CAMPO);
        combo.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        combo.putClientProperty("FlatLaf.style",
                "buttonBackground: #222224; buttonArrowColor: #C9A869");
        return combo;
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
                setForeground(BLANCO_HUESO);
                if (sel) {
                    setBackground(new Color(70, 62, 45));
                } else {
                    setBackground(fila % 2 == 0 ? FONDO_CAMPO : new Color(40, 40, 43));
                }
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celdas);
    }

    private void mostrarMensaje(String texto, boolean esAviso) {
        lblEstado.setForeground(esAviso ? AVISO : GRIS_TEXTO);
        lblEstado.setText(texto);
    }

    private static Double leerPrecio(String texto) {
        String limpio = texto.replaceAll("[^0-9]", "");
        if (limpio.isEmpty()) {
            return null;
        }
        return Double.parseDouble(limpio);
    }

    private void cargarTipos() {
        cbTipo.addItem(TODOS);

        try (Connection con = Main.conectar()) {
            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT DISTINCT tipo FROM Habitaciones ORDER BY tipo");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    cbTipo.addItem(rs.getString("tipo"));
                }
            }

        } catch (SQLException e) {
            mostrarMensaje("Error al cargar los tipos de habitación.", true);
        }
    }

    private void buscar() {
        final Double min = leerPrecio(tfPrecioMin.getText());
        final Double max = leerPrecio(tfPrecioMax.getText());
        final String tipo = (String) cbTipo.getSelectedItem();

        if (min != null && max != null && min > max) {
            mostrarMensaje("El precio mínimo no puede ser mayor que el máximo.", true);
            return;
        }

        btnBuscar.setEnabled(false);
        mostrarMensaje("Buscando...", false);

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return consultar(min, max, tipo);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                try {
                    List<Object[]> lista = get();
                    modelo.setRowCount(0);
                    for (Object[] fila : lista) {
                        modelo.addRow(fila);
                    }

                    if (lista.isEmpty()) {
                        mostrarMensaje("No se encontraron habitaciones con esos filtros.", true);
                    } else {
                        mostrarMensaje(lista.size() == 1
                                ? "1 habitación disponible." : lista.size() + " habitaciones disponibles.", false);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarMensaje(causa.getMessage() != null
                            ? causa.getMessage() : "Ocurrió un error inesperado.", true);
                }
            }
        }.execute();
    }

    private List<Object[]> consultar(Double min, Double max, String tipo) throws Exception {
        List<Object[]> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT numero, tipo, piso, capacidad, precio FROM Habitaciones WHERE disponible = 1");
        List<Object> parametros = new ArrayList<>();

        if (min != null) {
            sql.append(" AND precio >= ?");
            parametros.add(min);
        }
        if (max != null) {
            sql.append(" AND precio <= ?");
            parametros.add(max);
        }
        if (tipo != null && !tipo.equals(TODOS)) {
            sql.append(" AND LOWER(tipo) = LOWER(?)");
            parametros.add(tipo);
        }
        sql.append(" ORDER BY precio, numero");

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    ps.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new Object[]{
                            "Hab " + rs.getInt("numero"),
                            rs.getString("tipo"),
                            rs.getInt("piso"),
                            rs.getInt("capacidad"),
                            "$" + String.format("%,.0f", rs.getDouble("precio"))
                        });
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar habitaciones: " + e.getMessage());
        }

        return lista;
    }
}