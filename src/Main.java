import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Main {


    private static String masterIP = "192.168.1.3";


    public static void main(String[] args){


        if(args.length>0){
            masterIP = args[0];
        }

        try{

            LocateRegistry.createRegistry(1099);
            System.out.println("Start der Registry erfolgreich!");

        }catch(Exception e){
            System.out.println("Start der Registry fehlgeschlagen!");
        }


        try{

            MasterServiceImpl masterService = new MasterServiceImpl();
            Naming.rebind("//"+masterIP+"/MasterRemote", masterService);

            System.out.println("SharedPhilosopherMaster erfolgreich gestartet");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
