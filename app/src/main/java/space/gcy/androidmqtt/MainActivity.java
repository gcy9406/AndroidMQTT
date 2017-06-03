package space.gcy.androidmqtt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    private static final int CONNECT_SUCCESS = 0;
    private static final int GET_MESSAGE = 1;
    private static final int SUBCSRIP_SUCCESS = 2;
    private static final int UNSUBCSRIP_SUCCESS = 3;
    private static final int DISCONNECT_SUCCESS = 4;
    @BindView(R.id.mqtt_address)
    EditText mqttAddress;
    @BindView(R.id.mqtt_port)
    EditText mqttPort;
    @BindView(R.id.mqtt_server)
    LinearLayout mqttServer;
    @BindView(R.id.mqtt_connect)
    Button mqttConnect;
    @BindView(R.id.mqtt_send_topic)
    EditText mqttSendTopic;
    @BindView(R.id.mqtt_send_mesg)
    EditText mqttSendMesg;
    @BindView(R.id.mqtt_button_send)
    Button mqttButtonSend;
    @BindView(R.id.mqtt_sub_topic)
    EditText mqttSubTopic;
    @BindView(R.id.mqtt_button_sub)
    Button mqttButtonSub;
    @BindView(R.id.mqtt_sub_list)
    RecyclerView mqttSubList;
    @BindView(R.id.mqtt_clear)
    Button mqttClear;
    public MQTT mqtt;
    public CallbackConnection connection;
    private Adapter adapter;
    private List<String> result;
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    mqttConnect.setText("断开连接");
                    mqttServer.setVisibility(View.GONE);
                    break;
                case GET_MESSAGE:
                    result.add((String) msg.obj);
                    adapter.setData(result);
                    if (result.size() > 0)
                        mqttSubList.smoothScrollToPosition(result.size() - 1);
                    break;
                case SUBCSRIP_SUCCESS:

                    mqttButtonSub.setText("取消订阅");
                    mqttSubTopic.setEnabled(false);
                    break;
                case UNSUBCSRIP_SUCCESS:
                    mqttButtonSub.setText("订阅");
                    mqttSubTopic.setEnabled(true);
                    break;
                case DISCONNECT_SUCCESS:
                    mqttServer.setVisibility(View.VISIBLE);
                    mqttConnect.setText("连接");
                    mqttButtonSub.setText("订阅");
                    mqttSubTopic.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initDatas();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            disconnect();
        }
    }

    private void initDatas() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        edit = sp.edit();

        mqtt = new MQTT();
        mqttSubList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this);
        mqttSubList.setAdapter(adapter);
        mqttSubList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        result = new ArrayList<>();
        adapter.setOnItemClickListener(this);
        mqttAddress.setText(sp.getString("mqtt_address", ""));
        mqttPort.setText(sp.getString("mqtt_port", "1883"));
        mqttSendTopic.setText(sp.getString("mqtt_send_topic", ""));
        mqttSendMesg.setText(sp.getString("mqtt_send_mesg", ""));
        mqttSubTopic.setText(sp.getString("mqtt_sub_topic", ""));

    }

    @OnClick({R.id.mqtt_connect, R.id.mqtt_button_send, R.id.mqtt_button_sub, R.id.mqtt_clear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mqtt_connect:
                if (!mqttAddress.getText().toString().trim().equals("")) {
                    if (mqttConnect.getText().equals("连接")) {
                        connect(mqttAddress.getText().toString().trim(), mqttPort.getText().toString().trim());
                    } else {
                        disconnect();
                    }
                } else {
                    Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mqtt_button_send:
                Log.d("@@@", "onViewClicked: " + mqttSendTopic.getText().toString().trim() + "  " + mqttSendMesg.getText().toString().trim());
                publish(mqttSendTopic.getText().toString().trim(), mqttSendMesg.getText().toString().trim());
                break;
            case R.id.mqtt_button_sub:
                Log.d("@@@", mqttSubTopic.getText().toString().trim());
                if (mqttButtonSub.getText().equals("订阅")) {
                    subscription(mqttSubTopic.getText().toString().trim());
                } else {
                    unsubscribe(mqttSubTopic.getText().toString().trim());
                }
                break;
            case R.id.mqtt_clear:
                Log.d("@@@", "onViewClicked: clear");
                mqttSubList.smoothScrollToPosition(0);
                result.clear();
                adapter.setData(result);
                break;
        }
    }

    @OnTextChanged(value = R.id.mqtt_address, callback = AFTER_TEXT_CHANGED)
    public void onAddressChanged(CharSequence text) {
        edit.putString("mqtt_address", text.toString());
        edit.commit();
    }

    @OnTextChanged(value = R.id.mqtt_port, callback = AFTER_TEXT_CHANGED)
    public void onPortChanged(CharSequence text) {
        edit.putString("mqtt_port", text.toString());
        edit.commit();
    }

    @OnTextChanged(value = R.id.mqtt_send_topic, callback = AFTER_TEXT_CHANGED)
    public void onSendTopicChanged(CharSequence text) {
        edit.putString("mqtt_send_topic", text.toString());
        edit.commit();
    }

    @OnTextChanged(value = R.id.mqtt_send_mesg, callback = AFTER_TEXT_CHANGED)
    public void onSendMesgChanged(CharSequence text) {
        edit.putString("mqtt_send_mesg", text.toString());
        edit.commit();
    }

    @OnTextChanged(value = R.id.mqtt_sub_topic, callback = AFTER_TEXT_CHANGED)
    public void onSubTopicChanged(CharSequence text) {
        edit.putString("mqtt_sub_topic", text.toString());
        edit.commit();
    }


    public void connect(String server, String port) {
        try {
            mqtt.setHost("tcp://" + server + ":" + port);//设置地址
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //获得链接
        connection = mqtt.callbackConnection();

        //开始链接
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                handler.sendEmptyMessage(CONNECT_SUCCESS);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("@@@", "onFailure: connect");
            }
        });

        //订阅
        connection.listener(new Listener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            /**
             * 得到订阅的结果，发送出去
             * @param topic
             * @param body
             * @param ack
             */
            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                Message message = Message.obtain();
                message.what = GET_MESSAGE;
                message.obj = body.utf8().toString();
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("@@@", "onFailure: listener");
            }
        });
    }

    public void publish(String topic, String cmd) {
        if (connection != null) {
            connection.publish(topic, cmd.getBytes(), QoS.AT_MOST_ONCE, false, new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    Log.d("@@@", "onSuccess: ");
                }

                @Override
                public void onFailure(Throwable value) {
                    Log.d("@@@", "onFailure: publish");
                }
            });
        }
    }

    public void subscription(String topic) {
        if (connection != null) {
            Topic[] topics = {new Topic(topic, QoS.AT_MOST_ONCE)};
            connection.subscribe(topics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                    handler.sendEmptyMessage(SUBCSRIP_SUCCESS);
                }

                @Override
                public void onFailure(Throwable value) {
                    Log.d("@@@", "onFailure: subscription");
                }
            });
        }
    }

    public void unsubscribe(String topic) {
        UTF8Buffer topicU = new UTF8Buffer(topic);
        UTF8Buffer[] tArr = new UTF8Buffer[1];
        tArr[0] = topicU;
        connection.unsubscribe(tArr, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                handler.sendEmptyMessage(UNSUBCSRIP_SUCCESS);

            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("@@@", "onFailure: unsubscribe");
            }
        });
    }

    private void disconnect() {
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                handler.sendEmptyMessage(DISCONNECT_SUCCESS);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("@@@", "onFailure: disconnect");
            }
        });
    }

    @Override
    public void doClick(int pos, String data) {
        startActivity(new Intent(this, DialogActivity.class).putExtra("data", data));
    }
}
