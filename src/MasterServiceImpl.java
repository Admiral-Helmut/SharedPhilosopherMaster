import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Admiral Helmut on 01.05.2015.
 */
public class MasterServiceImpl extends UnicastRemoteObject implements MasterRemote {


    private ArrayList<Client> clientList;
    private HashMap<String, ClientRemote> clientRemoteMap;

    protected MasterServiceImpl() throws RemoteException {
        clientList = new ArrayList<Client>();
        clientRemoteMap = new HashMap<String, ClientRemote>();
    }

    @Override
    public boolean register(String ip) throws RemoteException {

        System.out.println("# Neuer Client unter IP "+ip+" versucht sich zu registrieren!");

        Client newClient = new Client(ip);
        for(Client client : clientList){
            if(client.equals(newClient)){
                return false;
            }
        }
        clientList.add(newClient);
        ClientRemote newClientRemote = null;
        try {
            newClientRemote = (ClientRemote) Naming.lookup("rmi://"+newClient.getIp()+"/ClientRemote");
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        clientRemoteMap.put(newClient.getIp(), newClientRemote);

        boolean connectionToNewClient = checkClient(clientRemoteMap.get(newClient.getIp()), newClient.getIp());
        if(!connectionToNewClient){
            clientList.remove(newClient);
            clientRemoteMap.remove(newClient.getIp());
        }
        System.out.println("");
        System.out.println("#######################");
        System.out.println("Registrierte Clients:");
        System.out.println("");
        int mapCounter = 1;
        for(Map.Entry<String, ClientRemote> e : clientRemoteMap.entrySet()) {
            System.out.print("-- "+mapCounter+" "+e.getKey());
            mapCounter++;
        }

        // Registriere neuen Client bei allen anderen Clients als Nachbar
        for(Map.Entry<String, ClientRemote> e : clientRemoteMap.entrySet()) {
            if(e.getKey()!=ip){
                e.getValue().setNeighbour(ip);
            }
        }

        // Registriere alle alten Clients beim neuen Client als Nachbarn
        for(Map.Entry<String, ClientRemote> e : clientRemoteMap.entrySet()) {
            //TODO
            if(e.getKey()!=ip){
                clientRemoteMap.get(ip).setNeighbour(e.getKey());
            }
        }


        return connectionToNewClient;
    }

    private boolean checkClient(ClientRemote clientRemote, String ip){

        System.out.println("# Prüfe Client unter IP "+ip+"!");


        boolean clientOK;
        try {
            clientOK = clientRemote.checkClient();
        } catch (RemoteException e) {
            e.printStackTrace();
            clientOK = false;
        }

        if(clientOK){
            System.out.println("# Client unter IP "+ip+" erfolgreich geprüft!");
        }else{
            System.out.println("# FEHLER auf Client unter IP "+ip+"!");
        }

        return clientOK;
    }

}
