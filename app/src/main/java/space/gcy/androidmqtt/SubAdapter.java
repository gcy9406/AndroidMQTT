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

class SubAdapter extends RecyclerView.Adapter<SubAdapter.ConnectViewHolder> {
    private List<String> data = new ArrayList<>();

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ConnectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConnectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_topic_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ConnectViewHolder holder, int position) {
        holder.mName.setText(data.get(position));
        holder.mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSubListener != null) {
                    int pos = holder.getLayoutPosition();
                    mSubListener.delSub(data.get(pos), pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface ISubListener {
        void delSub(String topic, int pos);
    }

    private ISubListener mSubListener;

    public void setSubListener(ISubListener subListener) {
        mSubListener = subListener;
    }

    class ConnectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView mName;
        @BindView(R.id.clear)
        ImageView mClear;
        public ConnectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
