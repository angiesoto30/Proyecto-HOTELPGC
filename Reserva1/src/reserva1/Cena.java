
package reserva1;

public class Cena extends Reserva1 {

    
    private Reserva1 reserva;

    
    public Cena(String nombreCliente,
                int numeroHabitacion,
                String tipoHabitacion,
                Reserva1 reserva) {

        super();

        this.reserva = reserva;
    }



    public void mostrarMenus() {

        System.out.println("========================================");
        System.out.println("             CENA EJECUTIVA");
        System.out.println("========================================");

        
        System.out.println("DATOS DE LA RESERVA");
        reserva.mostrarInfo();

        System.out.println("========================================");

        System.out.println("Menu A - Grand Gourmet");
        System.out.println("- Carpaccio de res con alcaparras");
        System.out.println("- Filete mignon al romero");
        System.out.println("- Vegetales salteados al ajo");
        System.out.println("- Creme brulee");
        System.out.println("- Copa de vino Cabernet Sauvignon");
        System.out.println("Precio: $110.000");

        System.out.println("========================================");

        System.out.println("Menu B - Noche Elegante");
        System.out.println("- Veloute de esparragos");
        System.out.println("- Salmon a las finas hierbas");
        System.out.println("- Pure trufado");
        System.out.println("- Panna cotta de vainilla");
        System.out.println("- Copa de vino Chardonnay");
        System.out.println("Precio: $88.000");

        System.out.println("========================================");
    }

    public double calcularPrecio(int opcion) {

        if (opcion == 1) {
            return 110000;
        }

        if (opcion == 2) {
            return 88000;
        }

        return 0;
    }
}