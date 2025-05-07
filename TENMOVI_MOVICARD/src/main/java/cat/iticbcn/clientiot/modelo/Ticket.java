package cat.iticbcn.clientiot.modelo;

public class Ticket {
    private int id;
    private String tipo;
    private int cantidad;
    private Integer duracion_dias;
    private String fecha_inicio;
    private int id_cliente;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public Integer getDuracion_dias() { return duracion_dias; }
    public void setDuracion_dias(Integer duracion_dias) { this.duracion_dias = duracion_dias; }

    public String getFecha_inicio() { return fecha_inicio; }
    public void setFecha_inicio(String fecha_inicio) { this.fecha_inicio = fecha_inicio; }

    public int getId_cliente() { return id_cliente; }
    public void setId_cliente(int id_cliente) { this.id_cliente = id_cliente; }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", cantidad=" + cantidad +
                ", duracion_dias=" + duracion_dias +
                ", fecha_inicio='" + fecha_inicio + '\'' +
                ", id_cliente=" + id_cliente +
                '}';
    }
}

