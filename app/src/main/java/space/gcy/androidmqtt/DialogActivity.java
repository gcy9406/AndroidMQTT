package space.gcy.androidmqtt;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DialogActivity extends AppCompatActivity {

    @BindView(R.id.mesg)
    TextView mesg;
    private ClipboardManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);
        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mesg.setText(getIntent().getStringExtra("data"));
    }

    public void copy(View view) {
        cm.setText(mesg.getText().toString().trim());
        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
    }
}
