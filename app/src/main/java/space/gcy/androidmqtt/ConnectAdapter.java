package space.gcy.androidmqtt;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class ConnectAdapter extends RecyclerView.Adapter<ConnectAdapter.ConnectViewHolder> {
    private List<ConnectBean> data = new ArrayList<>();

    public void setData(List<ConnectBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ConnectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConnectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_connect_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ConnectViewHolder holder, int position) {
        holder.mName.setText(data.get(position).getName());
        if (data.get(position).isConnected()){
            holder.mConnect.setImageResource(R.mipmap.icon_connected);
        }else {
            holder.mConnect.setImageResource(R.mipmap.icon_connect);
        }
        holder.mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConnectListener != null){
                    int pos = holder.getLayoutPosition();
                    if (data.get(pos).isConnected()){
                        mConnectListener.disConnect(data.get(pos),pos);
                    }else {
                        mConnectListener.doConnect(data.get(pos),pos);
                    }
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConnectListener != null) {
                    int pos = holder.getLayoutPosition();
                    mConnectListener.editConnect(data.get(pos),pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface IConnectListener{
        void doConnect(ConnectBean mqttConnection,int pos);
        void disConnect(ConnectBean mqttConnection,int pos);
        void editConnect(ConnectBean mqttConnection,int pos);
    }

    private IConnectListener mConnectListener;

    public void setConnectListener(IConnectListener connectListener) {
        mConnectListener = connectListener;
    }

    class ConnectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView mName;
        @BindView(R.id.connect)
        ImageView mConnect;
        public ConnectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
