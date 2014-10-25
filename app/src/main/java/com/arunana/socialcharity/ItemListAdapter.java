package com.arunana.socialcharity;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dashsell on 13/9/14.
 */
public class ItemListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private List<Item> items;

    public ItemListAdapter(Context context) {
        items = new ArrayList<Item>();
        mInflater = LayoutInflater.from(context);
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.

        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.item, null);
            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.price = (TextView) convertView.findViewById(R.id.item_price);
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.

            holder = (ViewHolder) convertView.getTag();
        }

        Item item = (Item) getItem(i);
        if (!TextUtils.isEmpty(item.getImagePath()))
            holder.image.setImageURI(Uri.parse(item.getImagePath()));
        holder.name.setText(item.getName());
        holder.price.setText("SGD " + item.getPrice());


        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView price;
        ImageView image;
    }

    public void addItem(Item i) {
        items.add(i);
        notifyDataSetChanged();
    }

    public void addItems(List<Item> is) {
        items.addAll(is);
        notifyDataSetChanged();
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
