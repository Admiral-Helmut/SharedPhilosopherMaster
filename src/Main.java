import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Main {


    private static String masterIP = "192.168.1.3";
    private static final int clientAmount = 2;
    private static Object monitor = new Object();

    //Felder aus VSS Projekt
    public static final int eatTime = 1;
    public static final int meditationTime = 5;
    public static final int sleepTime = 10;
    public static final int philosopherAmount = 4;
    public static final int hungryPhilosopherAmount = 2;
    public static final int runTimeInSeconds = 100;
    public static final int seatAmount = 10;
    public static long endTime;
    public static boolean debugging = true;


    public static void main(String[] args) {


        if(args.length>0){
            masterIP = args[0];
        }

        try{

            LocateRegistry.createRegistry(1099);
            System.out.println("Start der Registry erfolgreich!");

        }catch(Exception e){
            System.out.println("Start der Registry fehlgeschlagen!");
        }

        MasterServiceImpl masterService = null;
        try{

            masterService = new MasterServiceImpl();
            Naming.rebind("//"+masterIP+"/MasterRemote", masterService);

            System.out.println("SharedPhilosopherMaster erfolgreich gestartet");

        }catch(Exception e){
            e.printStackTrace();
        }

        while(masterService.getClientListSize()<clientAmount){

            try {
                synchronized (monitor) {
                    monitor.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        int[] seats = getResultPerClient(seatAmount);
        int[] philosophers = getResultPerClient(philosopherAmount);
        int[] hungryPhilosophers = getResultPerClient(hungryPhilosopherAmount);
        int remoteMapCounter = 0;


        ArrayList<Client> clientList = masterService.getClientList();
        int philosopherOffset = 1;
        int hungryPhilosopherOffset = 1;

        for(int i = 0; i<masterService.getClientListSize();i++){
            Client client = clientList.get(i);
            Client rightClient = clientList.get((i+1)%clientList.size());
            Client leftClient = clientList.get((i-1+clientList.size())%clientList.size());

            try {
                masterService.getRemoteMap().get(client.getLookupName()).initClient(seats[remoteMapCounter], seatAmount, philosophers[remoteMapCounter], philosopherAmount, hungryPhilosophers[remoteMapCounter], hungryPhilosopherAmount, philosopherOffset, hungryPhilosopherOffset, eatTime, meditationTime, sleepTime, runTimeInSeconds ,leftClient.getIp(), leftClient.getLookupName(), rightClient.getIp(), rightClient.getLookupName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            philosopherOffset += philosophers[i];
            hungryPhilosopherOffset += hungryPhilosophers[i];
        }


    }

    private static int[] getResultPerClient(int count){

        int[] results = new int[clientAmount];
        int resultPerClient = count / clientAmount;
        int remainingResults = count - (resultPerClient*clientAmount);
        for(int i = 0; i<clientAmount; i++, remainingResults--){
            results[i] =  (remainingResults>0) ? resultPerClient+1 :resultPerClient;
        }

        return results;
    }

    public static Object getMonitor(){
        return monitor;
    }

}
