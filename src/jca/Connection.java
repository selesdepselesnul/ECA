package jca;

public class Connection {
    public boolean isConnect = false;
    public String mode;
    public String ip;
    public int port;

    @Override
    public String toString() {
        return "Connection{" +
                "isConnect=" + isConnect +
                ", mode='" + mode + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
