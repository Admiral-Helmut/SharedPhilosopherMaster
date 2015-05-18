/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Client {

    private String ip;
    private String lookupName;


    public Client(String ip, String lookupName) {

        this.ip = ip;
        this.lookupName = lookupName;
    }

    public String getIp(){
        return this.ip;
    }
    public String getLookupName(){
        return this.lookupName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (ip != null ? !ip.equals(client.ip) : client.ip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ip != null ? ip.hashCode() : 0;
    }
}
