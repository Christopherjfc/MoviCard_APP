package cat.iticbcn.clientiot;

public class ClientIoT {
    public static void main(String[] args) {
        DispositiuIot disp = new DispositiuIot();
        try {
            disp.conecta();
            disp.subscriu();  
        }catch(Exception e){
            System.err.println("Error IOT: "+e.getLocalizedMessage());
            System.exit(-1);
        }
    }
}
