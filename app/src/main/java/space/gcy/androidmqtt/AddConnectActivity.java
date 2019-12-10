package space.gcy.androidmqtt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import space.gcy.androidmqtt.model.MqttConnection;
import space.gcy.androidmqtt.model.MqttConnectionDao;

import static space.gcy.androidmqtt.app.MainApplication.getDaoInstant;

public class AddConnectActivity extends AppCompatActivity {

    @BindView(R.id.mqtt_name)
    EditText mMqttName;
    @BindView(R.id.mqtt_address)
    EditText mMqttAddress;
    @BindView(R.id.mqtt_port)
    EditText mMqttPort;
    @BindView(R.id.mqtt_user)
    EditText mMqttUser;
    @BindView(R.id.mqtt_password)
    EditText mMqttPassword;
    @BindView(R.id.mqtt_server)
    LinearLayout mMqttServer;
    @BindView(R.id.mqtt_client_id)
    EditText mMqttClientId;
    @BindView(R.id.mqtt_id_generate)
    Button mMqttIdGenerate;
    private long mId;
    private MqttConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_connect);
        ButterKnife.bind(this);

        mId = getIntent().getLongExtra("id", 0);
        if (mId != 0) {
            List<MqttConnection> connections = getDaoInstant().getMqttConnectionDao().queryBuilder().where(MqttConnectionDao.Properties.Id.eq(mId)).list();
            if (connections != null && connections.size() > 0){
                mConnection = connections.get(0);
                mMqttName.setText(connections.get(0).getName());
                mMqttAddress.setText(connections.get(0).getAddress());
                mMqttPort.setText(connections.get(0).getPort()+"");
                mMqttUser.setText(connections.get(0).getUsername());
                mMqttPassword.setText(connections.get(0).getPassword());
                mMqttClientId.setText(connections.get(0).getClientId());
            }
        }
    }

    @OnClick(R.id.mqtt_id_generate)
    public void onViewClicked() {
        mMqttClientId.setText(UUID.randomUUID().toString());
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.add,menu);
//        return true;
//    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mId != 0){
            getMenuInflater().inflate(R.menu.modify,menu);
        }else {
            getMenuInflater().inflate(R.menu.add,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        closeKeyboard();
        if (item.getItemId() == R.id.del){
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.zm_mqtt)
                    .setTitle("您确定要删除当前链接吗？")
                    .setCancelable(false)
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("删除", (dialog, which) -> {
                        List<MqttConnection> tempList = getDaoInstant().getMqttConnectionDao().queryBuilder().where(MqttConnectionDao.Properties.Id.eq(mId)).list();
                        getDaoInstant().getMqttConnectionDao().deleteInTx(tempList);
                        finish();
                    })
                    .create()
                    .show();
        }else{
            if (isEmpty(mMqttName)){
                Toast.makeText(this,"请自定义名称",Toast.LENGTH_SHORT).show();
                return true;
            }
            if (isEmpty(mMqttAddress)){
                Toast.makeText(this,"地址不能为空",Toast.LENGTH_SHORT).show();
                return true;
            }
            if (isEmpty(mMqttPort)){
                Toast.makeText(this,"端口号不能为空",Toast.LENGTH_SHORT).show();
                return true;
            }
            if (isEmpty(mMqttClientId)){
                Toast.makeText(this,"ID不能为空",Toast.LENGTH_SHORT).show();
                return true;
            }
            String name = mMqttName.getText().toString();
            String address = mMqttAddress.getText().toString();
            int port = Integer.parseInt(mMqttPort.getText().toString());
            String username = mMqttUser.getText().toString();
            String password = mMqttPassword.getText().toString();
            String client = mMqttClientId.getText().toString();
            if (mId == 0){
                getDaoInstant().getMqttConnectionDao().insert(new MqttConnection(name,address,port,username,password,client));
            }else {
                mConnection.setName(name);
                mConnection.setAddress(address);
                mConnection.setUsername(username);
                mConnection.setPassword(password);
                mConnection.setClientId(client);
                getDaoInstant().getMqttConnectionDao().update(mConnection);
            }

            finish();
        }
        return true;
    }

    public boolean isEmpty(EditText view){
        return view.getText() == null || view.getText().toString().length() == 0;
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
}
