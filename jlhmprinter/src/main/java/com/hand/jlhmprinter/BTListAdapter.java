package com.hand.jlhmprinter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.hand.baselibrary.utils.StringUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BTListAdapter extends RecyclerView.Adapter<BTListAdapter.BTDeviceViewHolder> {

    private Context mContext;
    private ArrayList<BluetoothDevice> mData;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public BTListAdapter(Context context, ArrayList<BluetoothDevice> data) {
        this.mContext = context;
        this.mData = data;
    }


    @NonNull
    @Override
    public BTDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BTDeviceViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_bt_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BTDeviceViewHolder holder, int position) {
        BluetoothDevice btDevice = mData.get(position);
        String name = StringUtils.isEmpty(btDevice.getName()) ? "未命名设备" : btDevice.getName();
        holder.tvName.setText(name);
        holder.tvAddress.setText(btDevice.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(null, null, position, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class BTDeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;

        public BTDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
}
