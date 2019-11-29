package space.gcy.androidmqtt.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MqttConnection {
    @Id(autoincrement = true)
    private Long id;
    private String name;
    private String address;
    private int port;
    private String username;
    private String password;
    private String clientId;
    @Generated(hash = 856714475)
    public MqttConnection(Long id, String name, String address, int port,
            String username, String password, String clientId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getClientId() {
        return this.clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MqttConnection(String name, String address, int port,
                          String username, String password, String clientId) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
    }
    @Generated(hash = 809948974)
    public MqttConnection() {
    }
}
