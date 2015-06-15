import java.rmi.Remote;
        import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public interface ClientRemote extends Remote {

    boolean checkClient() throws RemoteException;
    boolean setNeighbour(String ip, String lookupName) throws RemoteException;
    void initClient(int seats, int allSeats, int philosopher, int allPhilosopher, int hungryPhilosopher, int allHungryPhilosopher,
                    int philosopherOffset, int hungryPhilosopherOffset, int eatTime, int meditationTime, int sleepTime,
                    int runTimeInSeconds, String leftneighbourIP,
                    String leftneighbourLookupName , String rightneighbourIP,
                    String rightneighbourLookupName, boolean debugging, long startTime) throws RemoteException;

    SeatProposal searchSeat(String startingClientName) throws RemoteException;

    void updatePhilosophers(HashMap<Integer, Integer> philsophersUpdate) throws RemoteException;

    boolean takeForkIfAvailable() throws RemoteException;

    void awakePhilosopherAddToQueue(int philosopherId, int seatNumber, int mealsEaten) throws RemoteException;

    void lastForkWait() throws RemoteException;

    void releaseLastFork() throws RemoteException;

    void addSeats(int diff, int newAmount) throws RemoteException;

    void removeSeats(int diff, int newAmount) throws RemoteException;


    void removePhilosopher(int ident) throws RemoteException;


    void addPhilosopher(boolean hungry, boolean active) throws RemoteException;
}

