import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Main {


    private static String masterIP = "192.168.178.3";
    private static final int clientAmount = 2;
    private static Object monitor = new Object();

    //Felder aus VSS Projekt
    public static final int eatTime = 1;
    public static final int meditationTime = 5;
    public static final int sleepTime = 10;
    public static  int philosopherAmount = 30;
    public static  int hungryPhilosopherAmount = 20;
    public static final int runTimeInSeconds = 62;
    public static  int seatAmount = 5;
    public static long endTime;
    public static boolean debugging = false;
    private static int[] seats;
    private static int[] philosophers;
    private static int[] hungryPhilosophers;
    private static MasterServiceImpl masterService;
    private static ArrayList<Client> clientList;

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

        masterService = null;
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

        seats = getResultPerClient(seatAmount);
        philosophers = getResultPerClient(philosopherAmount);
        hungryPhilosophers = getResultPerClient(hungryPhilosopherAmount);


        clientList = masterService.getClientList();
        int philosopherOffset = 1;
        int hungryPhilosopherOffset = 1;

        long startTime = System.currentTimeMillis()+1000;

        for(int i = 0; i<masterService.getClientListSize();i++){
            Client client = clientList.get(i);
            Client rightClient = clientList.get((i+1)%clientList.size());
            Client leftClient = clientList.get((i-1+clientList.size())%clientList.size());

            try {
                masterService.getRemoteMap().get(client.getLookupName()).initClient(seats[i], seatAmount, philosophers[i], philosopherAmount, hungryPhilosophers[i], hungryPhilosopherAmount, philosopherOffset, hungryPhilosopherOffset, eatTime, meditationTime, sleepTime, runTimeInSeconds ,leftClient.getIp(), leftClient.getLookupName(), rightClient.getIp(), rightClient.getLookupName(), debugging, startTime);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            philosopherOffset += philosophers[i];
            hungryPhilosopherOffset += hungryPhilosophers[i];
        }


        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Um Philosophen hinzuzufügen: PA:x,y eingeben, wobei x der Anzahl normaler, und y, der Anzahl hungriger Philosophen entsprich.");
            System.out.println("PD:x,y entfernt Philosophen, wobei x der Anzahl der normalen und y der Anzahl der hungrigen Philosophen entsprich.");
            System.out.println("SA:x fügt x Plätze hinzu, SD:x entfernt x Sitze");
            String request = scanner.nextLine();
            String type = request.split(":")[0];
            if("PA".equals(type)){
                addPhilosophers(request.split(":")[1]);
            }
            else if("PD".equals(type)){
                removePhilosophers(request.split(":")[1]);
            }
            else if("SA".equals(type)){
                addSeats(request.split(":")[1]);
            }
            else if("SD".equals(type)){
                removeSeats(request.split(":")[1]);
            }
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

    private static void removePhilosophers(String s) {
        int amountNormalPhilosophers = Integer.valueOf(s.split(",")[0]);
        int amountHungryPhilosophers = Integer.valueOf(s.split(",")[1]);

        int newHungryPhilosopherAmount =  hungryPhilosopherAmount - amountHungryPhilosophers;
        int newPhilosopherAmount =  philosopherAmount - amountNormalPhilosophers;

        hungryPhilosopherAmount = newHungryPhilosopherAmount;
        philosopherAmount = newPhilosopherAmount;

        int[] newHungryPhilosopher = getResultPerClient(newHungryPhilosopherAmount);
        int[] newPhilosopher = getResultPerClient(newPhilosopherAmount);

        for(int i = 0; i < philosophers.length; i++){
            int diffHungry =  hungryPhilosophers[i] - newHungryPhilosopher[i];
            int diff =  philosophers[i] - newPhilosopher[i];
            try {
                masterService.getRemoteMap().get(clientList.get(i).getLookupName()).removePhilosophers(diff, diffHungry, newPhilosopherAmount, newHungryPhilosopherAmount);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Remove philosopher " + amountNormalPhilosophers + "+" + amountHungryPhilosophers );
    }

    private static void addPhilosophers(String philosopher) {
        int amountNormalPhilosophers = Integer.valueOf(philosopher.split(",")[0]);
        int amountHungryPhilosophers = Integer.valueOf(philosopher.split(",")[1]);

        int newHungryPhilosopherAmount = amountHungryPhilosophers + hungryPhilosopherAmount;
        int newPhilosopherAmount = amountNormalPhilosophers + philosopherAmount;

        hungryPhilosopherAmount = newHungryPhilosopherAmount;
        philosopherAmount = newPhilosopherAmount;

        int[] newHungryPhilosopher = getResultPerClient(newHungryPhilosopherAmount);
        int[] newPhilosopher = getResultPerClient(newPhilosopherAmount);

        for(int i = 0; i < philosophers.length; i++){
            int diffHungry = newHungryPhilosopher[i] - hungryPhilosophers[i];
            int diff = newPhilosopher[i] - philosophers[i];
            try {
                masterService.getRemoteMap().get(clientList.get(i).getLookupName()).addPhilosophers(diff, diffHungry, newPhilosopherAmount, newHungryPhilosopherAmount);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Add philosopher " + amountNormalPhilosophers + "+" + amountHungryPhilosophers );
    }

    private static void addSeats(String seat){
        int amountSeats = Integer.valueOf(seat);

        int newAmount = seatAmount + amountSeats;
        seatAmount = newAmount;
        int[] newSeats = getResultPerClient(newAmount);
        for(int i = 0; i < seats.length; i++){
            int diff = newSeats[i] - seats[i];
            try {
                masterService.getRemoteMap().get(clientList.get(i).getLookupName()).addSeats(diff, newAmount);
                System.out.println("Client "+clientList.get(i).getLookupName()+" kriegt "+diff+" Seats");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        seats = newSeats;

        System.out.println("Added seat " + amountSeats );
        System.out.println("Now "+seatAmount + " Seats");
    }

    private static void removeSeats(String seat) {
        int amountSeats = Integer.valueOf(seat);

        int newAmount = seatAmount - amountSeats;
        seatAmount = newAmount;
        int[] newSeats = getResultPerClient(newAmount);
        for(int i = 0; i < seats.length; i++){
            int diff = seats[i] - newSeats[i];
            if(diff > 0){
                try {
                    masterService.getRemoteMap().get(clientList.get(i).getLookupName()).removeSeats(diff, newAmount);
                    System.out.println("Client "+clientList.get(i).getLookupName()+" verliert "+diff+" Seats");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        seats=newSeats;

        System.out.println("Removed " + amountSeats  + " seats");
        System.out.println("Now "+seatAmount + " Seats");
    }
}
