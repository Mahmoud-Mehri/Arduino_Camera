package arduino.arduino_app;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PictureListAdapter extends RealmRecyclerViewAdapter<PictureRecordObject, PictureListAdapter.PictureViewHolder> {
    private Context mContext;
    private final RecyclerViewClickListener ClickListener;

    public PictureListAdapter(Context context, OrderedRealmCollection<PictureRecordObject> data, RecyclerViewClickListener clickListener) {
        super(data, true);

        mContext = context;
        ClickListener = clickListener;
    }

    @Override
    public PictureListAdapter.PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);

        return new PictureViewHolder(itemView, ClickListener);
    }

    @Override
    public void onBindViewHolder(PictureListAdapter.PictureViewHolder holder, int position) {
        PictureRecordObject obj = getItem(position);
        holder.data = obj;
        if(obj != null) {
            holder.NumberTxt.setText(Integer.toString(position + 1));
            holder.DateTxt.setText(obj.getHDate());
            holder.TimeTxt.setText(obj.getTime());
            DecimalFormat df = new DecimalFormat("#.###");
            float f = (float)(obj.getFileSize()) / 1024;
            String size = df.format(f) + " KB";
            holder.SizeTxt.setText(size);

            if(position % 2 == 0){
                holder.BackView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_even));
            }else{
                holder.BackView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_odd));
            }
        }
    }

    class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        private CardView BackView;
        private TextView NumberTxt;
        private TextView DateTxt;
        private TextView TimeTxt;
        private TextView SizeTxt;

        private ImageButton ShowBtn;
        private ImageButton DelBtn;

        public PictureRecordObject data;

        private WeakReference<RecyclerViewClickListener> listenerRef;

        public PictureViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);

            listenerRef = new WeakReference<>(listener);

            NumberTxt = itemView.findViewById(R.id.picItemNumber);
            DateTxt = itemView.findViewById(R.id.picItemDate);
            TimeTxt = itemView.findViewById(R.id.picItemTime);
            SizeTxt = itemView.findViewById(R.id.picItemSize);
            ShowBtn = itemView.findViewById(R.id.picItemShowBtn);
            ShowBtn.setOnClickListener(this);
            DelBtn = itemView.findViewById(R.id.picItemDelBtn);
            DelBtn.setOnClickListener(this);
            BackView = itemView.findViewById(R.id.picItemBack);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == ShowBtn.getId()){
                listenerRef.get().onPositionClicked(getAdapterPosition(), data.getID(), 1);
            }else if(view.getId() == DelBtn.getId()){
                listenerRef.get().onPositionClicked(getAdapterPosition(), data.getID(), 2);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
