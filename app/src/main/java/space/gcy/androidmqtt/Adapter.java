package space.gcy.androidmqtt;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by gcy on 2017/6/3.
 */

class Adapter extends RecyclerView.Adapter<Adapter.DataViewHolder> {
    private Context context;
    private List<String> data = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public Adapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, int position) {
        holder.itemText.setText(data.get(position));
        final Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        holder.itemTime.setText("时间:" + sdf.format(date));
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                onItemClickListener.doClick(pos,data.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_text)
        TextView itemText;
        @BindView(R.id.item_time)
        TextView itemTime;
        @BindView(R.id.item_layout)
        LinearLayout itemLayout;

        public DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
