package space.gcy.androidmqtt;

/**
 * Created by gcy on 2017/6/3.
 */

public class PostInfo {
    private String topic;
    private String data;
    private String time;

    public PostInfo() {
    }

    public PostInfo(String topic, String data, String time) {
        this.topic = topic;
        this.data = data;
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
