package cat.iticbcn.clientiot.modelo;

public class Ticket {
    private int id;
    private String tipo;
    private int cantidad;
    private Integer duracion_dias;
    private String fecha_inicio;
    private int id_cliente;

    public String getTipo() { return tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}