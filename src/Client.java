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

        if (!ip.equals(client.ip)) return false;
        return lookupName.equals(client.lookupName);

    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + lookupName.hashCode();
        return result;
    }
}
