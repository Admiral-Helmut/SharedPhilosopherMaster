/**
 * Created by Admiral Helmut on 01.05.2015.
 */
public class Client {

    private String ip;


    public Client(String ip) {
        this.ip = ip;
    }

    public String getIp(){
        return this.ip;
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
