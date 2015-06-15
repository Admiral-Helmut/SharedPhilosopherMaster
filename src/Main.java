import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
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
    private static List<Client> clientList;
    private static boolean[] philosopherTypes;

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
        philosopherTypes = new boolean[philosopherAmount + hungryPhilosopherAmount];
        for(int i = philosopherAmount; i < philosopherAmount + hungryPhilosopherAmount; i++){
            philosopherTypes[i] = true;
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

        for(int i = 0; i < amountNormalPhilosophers; i++){
            int index = getIndexForDelete(false);
            for(int j = 0; j < masterService.getClientList().size(); j++){
                try {
                    masterService.getRemoteMap().get(masterService.getClientList().get(j).getLookupName()).removePhilosopher(index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            updatePhilosopherTypes(index);
        }

        for(int i = 0; i < amountHungryPhilosophers; i++){
            int index = getIndexForDelete(true);
            for(int j = 0; j < masterService.getClientList().size(); j++){
                try {
                    masterService.getRemoteMap().get(masterService.getClientList().get(j).getLookupName()).removePhilosopher(index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            updatePhilosopherTypes(index);
        }

        System.out.println("Remove philosopher " + amountNormalPhilosophers + "+" + amountHungryPhilosophers );
    }

    private static void updatePhilosopherTypes(int index) {
        boolean[] newPhilosopherTypes = new boolean[philosopherTypes.length-1];
        boolean found = false;
        for(int i = 0; i < philosopherTypes.length; i++){
            if(i == index)
                found = true;
            newPhilosopherTypes[i] = philosopherTypes[(found)?i+1:i];
        }
        philosopherTypes = newPhilosopherTypes;
    }

    private static int getIndexForDelete(boolean hungry){
        for(int i = 0; i < philosopherTypes.length; i++){
            if(philosopherTypes[i] == hungry)
                return i;
        }
        return -1;
    }

    private static void addPhilosophers(String philosopher) {

        int amountNormalPhilosophers = Integer.valueOf(philosopher.split(",")[0]);
        int amountHungryPhilosophers = Integer.valueOf(philosopher.split(",")[1]);

        hungryPhilosopherAmount = amountHungryPhilosophers + hungryPhilosopherAmount;
        philosopherAmount = amountNormalPhilosophers + philosopherAmount;

        boolean[] newPhilosopherTypes = new boolean[philosopherAmount + hungryPhilosopherAmount];
        System.arraycopy(philosopherTypes, 0, newPhilosopherTypes, 0, philosopherTypes.length);
        philosopherTypes = newPhilosopherTypes;

        for(int i = 0; i < amountNormalPhilosophers; i++){
            try {
                masterService.getRemoteMap().get(masterService.getClientList().get(i%masterService.getClientListSize()).getLookupName()).addPhilosopher(false, true);
                for(int j = 0; j < masterService.getRemoteMap().size(); j++){
                    if(j != i%masterService.getClientListSize()){
                        masterService.getRemoteMap().get(masterService.getClientList().get(j).getLookupName()).addPhilosopher(false, false);
                        System.out.println(i +"- "+j);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        System.out.println("AmountNormalPhilosophers: "+amountNormalPhilosophers);

        for(int i = 0; i < amountHungryPhilosophers; i++){
            try {
                masterService.getRemoteMap().get(masterService.getClientList().get(i%masterService.getClientListSize()).getLookupName()).addPhilosopher(true, true);
                for(int j = 0; j < masterService.getRemoteMap().size(); j++){
                    if(j != i%masterService.getClientListSize()){
                        masterService.getRemoteMap().get(masterService.getClientList().get(j).getLookupName()).addPhilosopher(true, false);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        System.out.println("AmountHungryPhilosophers: "+amountHungryPhilosophers);
        System.out.println("CLientListSize: "+masterService.getRemoteMap().size());

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
