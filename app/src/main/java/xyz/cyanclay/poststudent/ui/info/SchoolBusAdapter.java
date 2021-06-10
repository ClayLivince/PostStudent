package xyz.cyanclay.poststudent.ui.info;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.info.Bus;
import xyz.cyanclay.poststudent.ui.components.EFLRecyclerAdapter;
import xyz.cyanclay.poststudent.util.Utils;

class SchoolBusAdapter extends EFLRecyclerAdapter<Bus> {

    static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.CHINA);

    SchoolBusFragment fragment;


    public SchoolBusAdapter(Context context, SchoolBusFragment fragment) {
        super(context);
        this.fragment = fragment;
    }

    @Override
    public int getEmptyCaption() {
        return R.string.school_bus_empty;
    }

    @Override
    public void onBindCustomHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BusHolder busHolder = (BusHolder) holder;
        Bus bus = list.get(position);

        busHolder.busTimeView.setText(bus.getTime());
        busHolder.busTypeView.setText(bus.getType());

        String current = timeFormatter.format(new Date());
        int[] lapse = Utils.calcTimeLapse(current, bus.getTime());

        String toDisplay;
        if (lapse[0] > 0) {
            toDisplay = lapse[0] + "小时" + lapse[1] + "分钟后";
            busHolder.busLeftTimeView.setTextColor(context.getResources().getColor(R.color.colorCorrect));

        } else if (lapse[0] == 0) {
            if (lapse[1] > 0) {
                toDisplay = lapse[1] + "分钟后";
                busHolder.busLeftTimeView.setTextColor(context.getResources().getColor(R.color.courseTable1));
            } else {
                toDisplay = -lapse[1] + "分钟前已经发车";
                busHolder.busTimeView.setTextColor(context.getResources().getColor(R.color.textPrimary));
            }
        } else {
            toDisplay = -lapse[0] + "小时前已经发车";
            busHolder.busTimeView.setTextColor(context.getResources().getColor(R.color.textPrimary));
        }

        busHolder.busLeftTimeView.setText(toDisplay);
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BusHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.piece_bus_item, parent, false));
    }

    static class BusHolder extends RecyclerView.ViewHolder {

        TextView busTypeView;
        TextView busTimeView;
        TextView busLeftTimeView;

        BusHolder(@NonNull View itemView) {
            super(itemView);
            busTimeView = itemView.findViewById(R.id.textViewBusTime);
            busTypeView = itemView.findViewById(R.id.textViewBusType);
            busLeftTimeView = itemView.findViewById(R.id.textViewBusLeftTime);
        }
    }
}
