package space.gcy.androidmqtt;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;
import space.gcy.androidmqtt.model.MqttConnection;
import space.gcy.androidmqtt.model.SubscribeContent;
import space.gcy.androidmqtt.model.SubscribeContentDao;

import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;
import static space.gcy.androidmqtt.app.MainApplication.getDaoInstant;

public class MainActivity extends AppCompatActivity implements OnItemClickListener, View.OnFocusChangeListener, ConnectAdapter.IConnectListener, SubAdapter.ISubListener {

    private static final int CONNECT_SUCCESS = 0;
    private static final int GET_MESSAGE = 1;
    private static final int SUBCSRIP_SUCCESS = 2;
    private static final int UNSUBCSRIP_SUCCESS = 3;
    private static final int DISCONNECT_SUCCESS = 4;
    @BindView(R.id.mqtt_button_send)
    Button mqttButtonSend;
    @BindView(R.id.mqtt_button_sub)
    Button mqttButtonSub;
    @BindView(R.id.mqtt_sub_list)
    RecyclerView mqttSubList;
    @BindView(R.id.msg_clear)
    ImageView msgClear;
    public MQTT mqtt;
    public CallbackConnection connection;
    @BindView(R.id.get_from_cm)
    Button getFromCm;
    @BindView(R.id.sub_topic)
    TextView subTopic;
    @BindView(R.id.sub_content)
    TextView subContent;
    @BindView(R.id.sub_copy)
    Button subCopy;
    @BindView(R.id.line_detail)
    LinearLayout lineDetail;
    @BindView(R.id.line_dismiss)
    ImageView lineDismiss;
    @BindView(R.id.get_from_ctr)
    Button getFromCtr;
    @BindView(R.id.get_from_recv)
    Button getFromRecv;
    @BindView(R.id.get_from_state)
    Button getFromState;
    @BindView(R.id.toolBar)
    Toolbar mToolBar;
    @BindView(R.id.drawer)
    DrawerLayout mDrawer;
    @BindView(R.id.connect_list)
    RecyclerView mConnectList;
    @BindView(R.id.mqtt_button_sub_list)
    Button mMqttButtonSubList;
    @BindView(R.id.sub_dismiss)
    ImageView mSubDismiss;
    @BindView(R.id.sub_list_view)
    RecyclerView mSubListView;
    @BindView(R.id.line_subs)
    LinearLayout mLineSubs;
    @BindView(R.id.mqtt_send_topic_auto)
    DeleteEditText mMqttSendTopicAuto;
    @BindView(R.id.mqtt_send_mesg_auto)
    DeleteEditText mMqttSendMesgAuto;
    @BindView(R.id.mqtt_sub_topic_auto)
    DeleteEditText mMqttSubTopicAuto;
    @BindView(R.id.head)
    ImageView mHead;
    @BindView(R.id.text_add)
    TextView mTextAdd;
    @BindView(R.id.text_clear)
    TextView mTextClear;
    private Adapter adapter;
    private List<PostInfo> result;
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;
    private boolean mConnected = false;
    private int mCurrentPosition = 0;
    private List<String> mSubDatas = new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    mDrawer.closeDrawers();
                    mConnected = true;
                    ConnectBean cb = mConnectData.get(mCurrentPosition);
                    Toast.makeText(MainActivity.this, cb.getName() + getResources().getString(R.string.contend_success), Toast.LENGTH_SHORT).show();
                    mToolBar.setSubtitle(cb.getName() + getResources().getString(R.string.connected));
                    cb.setConnected(true);
                    mConnectData.set(mCurrentPosition, cb);
                    mConnectAdapter.setData(mConnectData);
                    mqttButtonSub.setEnabled(true);
                    mqttButtonSub.setTextColor(Color.BLACK);

                    break;
                case GET_MESSAGE:
                    result.add((PostInfo) msg.obj);
                    adapter.setData(result);
                    if (result.size() > 0)
                        mqttSubList.smoothScrollToPosition(result.size() - 1);
                    break;
                case SUBCSRIP_SUCCESS:
                    mSubDatas.add(mMqttSubTopicAuto.getText().toString().trim());
                    mSubAdapter.setData(mSubDatas);
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.sub_success), Toast.LENGTH_SHORT).show();
                    mMqttSubTopicAuto.setText("");
                    break;
                case UNSUBCSRIP_SUCCESS:
                    mqttButtonSub.setText(getResources().getString(R.string.subscirbe));
                    mSubDatas.remove(mCurrentTopic);
                    mSubAdapter.setData(mSubDatas);
                    break;
                case DISCONNECT_SUCCESS:
                    mConnected = false;
                    mDrawer.closeDrawers();
                    mToolBar.setSubtitle(getResources().getString(R.string.no_connection));
                    ConnectBean cb2 = mConnectData.get(mCurrentPosition);
                    cb2.setConnected(false);
                    mConnectData.set(mCurrentPosition, cb2);
                    mConnectAdapter.setData(mConnectData);
                    mSubAdapter.setData(mSubDatas);
                    mqttButtonSub.setEnabled(false);
                    mqttButtonSub.setTextColor(Color.GRAY);
                    break;
            }
        }
    };
    private ClipboardManager cm;
    private String currentContent;
    private ActionBarDrawerToggle mToggle;
    private ConnectAdapter mConnectAdapter;
    private List<ConnectBean> mConnectData = new ArrayList<>();
    private SubAdapter mSubAdapter;
    private int mCurrentTopic;
    ArrayAdapter<String> mAutoAdapter;
    private List<String> mAutoData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);

        mToolBar.setTitle(getResources().getString(R.string.app_name));
        mToolBar.setSubtitle("www.iotzone.cn");
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setSubtitleTextColor(Color.WHITE);

        mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolBar, 0, 0){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (mConnected){
                    mCurrentPosition = sp.getInt("position",0);
                    if (mConnectData.size() > mCurrentPosition){
                        ConnectBean connectBean = mConnectData.get(mCurrentPosition);
                        connectBean.setConnected(true);
                        mConnectData.set(mCurrentPosition,connectBean);
                        mConnectAdapter.setData(mConnectData);
                    }
                }
                closeKeyboard();
            }
        };
        mToggle.syncState();
        mDrawer.addDrawerListener(mToggle);

        mConnectList.setLayoutManager(new LinearLayoutManager(this));
        mConnectList.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        mConnectAdapter = new ConnectAdapter();
        mConnectList.setAdapter(mConnectAdapter);
        mConnectAdapter.setConnectListener(this);

        mSubListView.setLayoutManager(new LinearLayoutManager(this));
        mSubListView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        mSubAdapter = new SubAdapter();
        mSubListView.setAdapter(mSubAdapter);
        mSubAdapter.setSubListener(this);

        mqttButtonSub.setEnabled(false);
        mqttButtonSub.setTextColor(Color.GRAY);


        List<SubscribeContent> subscribeContents = getDaoInstant().getSubscribeContentDao().queryBuilder().orderDesc(SubscribeContentDao.Properties.Timestamp).list();
        if (subscribeContents != null) {
            for (int i = 0; i < subscribeContents.size(); i++) {
                mAutoData.add(subscribeContents.get(i).getName());
            }
        }

        mAutoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mAutoData);
        mMqttSendTopicAuto.setAdapter(mAutoAdapter);
        mMqttSendMesgAuto.setAdapter(mAutoAdapter);
        mMqttSubTopicAuto.setAdapter(mAutoAdapter);

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

        getFromCtr.setText(sp.getString("tag1", "ctr"));
        getFromRecv.setText(sp.getString("tag2", "state"));
        getFromState.setText(sp.getString("tag3", "state=?"));

        mMqttSendTopicAuto.setText(sp.getString("last_topic",""));
        mMqttSendMesgAuto.setText(sp.getString("last_msg",""));
        mMqttSubTopicAuto.setText(sp.getString("last_sub",""));

        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        mMqttSubTopicAuto.setOnFocusChangeListener(this);
        mMqttSendTopicAuto.setOnFocusChangeListener(this);
        mMqttSendMesgAuto.setOnFocusChangeListener(this);

    }

    @OnLongClick({R.id.get_from_ctr, R.id.get_from_recv, R.id.get_from_state})
    public boolean onViewLongClicked(View view) {
        clearFocus();
        switch (view.getId()) {
            case R.id.get_from_ctr:
                showDialog(getFromCtr,"tag1",sp.getString("tag1","ctr"));
                break;
            case R.id.get_from_recv:
                showDialog(getFromRecv,"tag2",sp.getString("tag2","state"));
                break;
            case R.id.get_from_state:
                showDialog(getFromState,"tag3",sp.getString("tag3","state=?"));
                break;
        }
        return true;
    }

    public void showDialog(final Button btn, final String index, String tag){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_layout,null,false);
        dialog.setView(view);
        final EditText content = view.findViewById(R.id.content);
        content.setText(tag);
        content.setSelection(0,tag.length());
        dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeKeyboard();
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (content.getText().toString().trim().length() == 0){
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.empty_tag), Toast.LENGTH_SHORT).show();
                }else {
                    closeKeyboard();
                    edit.putString(index,content.getText().toString().trim());
                    edit.commit();
                    btn.setText(content.getText().toString().trim());
                    dialogInterface.dismiss();
                }

            }
        });
        dialog.setTitle(getResources().getString(R.string.modify_tag));
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.zm_mqtt);
        dialog.create().show();

    }

    @OnClick({R.id.mqtt_button_send,R.id.text_clear,
            R.id.mqtt_button_sub, R.id.msg_clear, R.id.get_from_cm,
            R.id.line_dismiss, R.id.sub_copy, R.id.get_from_ctr,
            R.id.get_from_state, R.id.get_from_recv,
            R.id.text_add, R.id.sub_dismiss, R.id.mqtt_button_sub_list})
    public void onViewClicked(View view) {
        clearFocus();
        switch (view.getId()) {
            case R.id.mqtt_button_send:
                if (!mConnected) {
                    Toast.makeText(this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                publish(mMqttSendTopicAuto.getText().toString().trim(), mMqttSendMesgAuto.getText().toString().trim());

                edit.putString("last_topic",mMqttSendTopicAuto.getText().toString().trim());
                edit.putString("last_msg",mMqttSendMesgAuto.getText().toString().trim());
                edit.commit();

                addContentToDB(mMqttSendTopicAuto.getText().toString().trim());
                addContentToDB(mMqttSendMesgAuto.getText().toString().trim());
                break;
            case R.id.mqtt_button_sub:
                if (!mConnected) {
                    Toast.makeText(this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mMqttSubTopicAuto.getText().toString().trim().length() > 0) {
                    subscription(mMqttSubTopicAuto.getText().toString().trim());
                    addContentToDB(mMqttSubTopicAuto.getText().toString().trim());

                    edit.putString("last_sub",mMqttSubTopicAuto.getText().toString().trim());
                    edit.commit();
                }
                break;
            case R.id.msg_clear:
                mqttSubList.smoothScrollToPosition(0);
                result.clear();
                adapter.setData(result);
                break;
            case R.id.get_from_cm:
                ClipData data = cm.getPrimaryClip();
                ClipData.Item item = data.getItemAt(0);
                String content = item.getText().toString();
                setContent(content);
                break;
            case R.id.line_dismiss:
                lineDetail.setVisibility(View.GONE);
                break;
            case R.id.sub_copy:
                cm.setText(currentContent);
                Toast.makeText(this, getResources().getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                break;
            case R.id.get_from_ctr:
                setContent(sp.getString("tag1","ctr"));
                break;
            case R.id.get_from_recv:
                setContent(sp.getString("tag2","state"));
                break;
            case R.id.get_from_state:
                setContent(sp.getString("tag3","state=?"));
                break;
            case R.id.text_add:
                startActivity(new Intent(this, AddConnectActivity.class).putExtra("id", 0));
                mDrawer.closeDrawers();
                break;
            case R.id.text_clear:
                getDaoInstant().getSubscribeContentDao().deleteAll();
                Toast.makeText(this, getResources().getString(R.string.cache_clear), Toast.LENGTH_SHORT).show();
                mDrawer.closeDrawers();
                break;
            case R.id.mqtt_button_sub_list:
                mLineSubs.setVisibility(View.VISIBLE);
                break;
            case R.id.sub_dismiss:
                mLineSubs.setVisibility(View.GONE);
                break;
        }
        closeKeyboard();
    }

    public void setContent(String content) {
        if (currentTag == 0) {
            mMqttSubTopicAuto.setText(mMqttSubTopicAuto.getText().toString() + content);
            mMqttSubTopicAuto.setSelection(mMqttSubTopicAuto.getText().toString().length());
        } else if (currentTag == 1) {
            mMqttSendTopicAuto.setText(mMqttSendTopicAuto.getText().toString() + content);
            mMqttSendTopicAuto.setSelection(mMqttSendTopicAuto.getText().toString().length());
        } else if (currentTag == 2) {
            mMqttSendMesgAuto.setText(mMqttSendMesgAuto.getText().toString() + content);
            mMqttSendMesgAuto.setSelection(mMqttSendMesgAuto.getText().toString().length());
        }
    }

    public void addContentToDB(String content) {
        if (content == null || content.trim().length() == 0) {
            return;
        }
        List<SubscribeContent> subscribeContents = getDaoInstant().getSubscribeContentDao()
                .queryBuilder()
                .where(SubscribeContentDao.Properties.Name.eq(content))
                .list();
        if (subscribeContents.size() == 0) {
            SubscribeContent s1 = new SubscribeContent();
            s1.setName(content);
            s1.setTimestamp(System.currentTimeMillis());
            getDaoInstant().getSubscribeContentDao().insert(s1);
        }
    }

    public void connect(String server, String port, String user, String password, String id) {
        try {
            mqtt.setHost("tcp://" + server + ":" + port);//设置地址
            mqtt.setUserName(user);
            mqtt.setPassword(password);
            mqtt.setClientId(id);
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
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                PostInfo p = new PostInfo(topic.utf8().toString(), body.utf8().toString(), sdf.format(date));
                message.obj = p;
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

    @Override
    protected void onResume() {
        super.onResume();
        List<MqttConnection> connections = getDaoInstant().getMqttConnectionDao().queryBuilder().list();
        if (connections != null) {
            mConnectData.clear();
            for (int i = 0; i < connections.size(); i++) {
                ConnectBean cb = new ConnectBean();
                cb.setId(connections.get(i).getId());
                cb.setName(connections.get(i).getName());
                cb.setAddress(connections.get(i).getAddress());
                cb.setPort(connections.get(i).getPort());
                cb.setUsername(connections.get(i).getUsername());
                cb.setPassword(connections.get(i).getPassword());
                cb.setClientId(connections.get(i).getClientId());
                cb.setConnected(false);
                mConnectData.add(cb);
            }
        }
        mConnectAdapter.setData(mConnectData);
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
        mSubDatas.clear();
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
    public void doClick(int pos, PostInfo data) {
        lineDetail.setVisibility(View.VISIBLE);
        currentContent = data.getData();
        subTopic.setText(data.getTopic());
        subContent.setText(data.getData());
    }

    private int currentTag = -1;

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.mqtt_send_topic_auto:
                currentTag = 1;
                break;
            case R.id.mqtt_send_mesg_auto:
                currentTag = 2;
                break;
            case R.id.mqtt_sub_topic_auto:
                currentTag = 0;
                break;
            default:
                currentTag = -1;
                break;
        }
        lineDetail.setVisibility(View.GONE);
        mLineSubs.setVisibility(View.GONE);
    }

    @Override
    public void doConnect(ConnectBean mqttConnection, int pos) {
        if (!mConnected) {
            mCurrentPosition = pos;
            edit.putInt("position",pos);
            edit.commit();
            connect(mqttConnection.getAddress(), mqttConnection.getPort() + "",
                    mqttConnection.getUsername(), mqttConnection.getPassword(), mqttConnection.getClientId());
        }
    }

    @Override
    public void disConnect(ConnectBean mqttConnection, int pos) {
        mCurrentPosition = pos;
        disconnect();
    }

    @Override
    public void editConnect(ConnectBean mqttConnection, int pos) {
        if (mConnected){
            Toast.makeText(this, getResources().getString(R.string.disconnet_tip), Toast.LENGTH_SHORT).show();
        }else {
            startActivity(new Intent(this, AddConnectActivity.class).putExtra("id", mqttConnection.getId()));
        }
    }

    @Override
    public void delSub(String topic, int pos) {
        if (!mConnected) {
            Toast.makeText(this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentTopic = pos;
        unsubscribe(topic);
    }


    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void clearFocus(){
        mMqttSubTopicAuto.clearFocus();
        mMqttSendMesgAuto.clearFocus();
        mMqttSendTopicAuto.clearFocus();
    }
}
