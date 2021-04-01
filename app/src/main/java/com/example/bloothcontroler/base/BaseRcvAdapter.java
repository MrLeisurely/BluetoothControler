package com.example.bloothcontroler.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author Hanwenhao
 * @date 2020/4/9
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public abstract class BaseRcvAdapter<T,R extends ViewDataBinding> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private R binding;
    private List<T> data;

    private int layoutId;
    protected Context context;
    private ItemClickListener listener;
    public BaseRcvAdapter(Context context,int layoutId) {
        this.layoutId = layoutId;
        this.context = context;
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),layoutId,parent,false);
        return new MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        binding = DataBindingUtil.getBinding(holder.itemView);
        handleItem(data.get(position),binding,position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener){
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        else return data.size();
    }

    public abstract void handleItem(T t,R binding,int position);

    public void setData(List<T> data){
        this.data = data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return data;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface ItemClickListener{
        void onItemClick(int position);
    }
}
