import java.rmi.Naming;

/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Main {

    public static void main(String[] args){


        try{

            MasterServiceImpl masterService = new MasterServiceImpl();
            Naming.rebind("//192.168.178.32/MasterRemote", masterService);

            System.out.println("SharedPhilosopherMaster erfolgreich gestartet");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
