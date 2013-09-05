package com.bnsoft.cafe;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OrderItemsListAdapter extends BaseAdapter{
	
	Logger log = new Logger();
	
	private Context context;
	List<OrderHolder.GoodsItem> data;
    View v;

	public OrderItemsListAdapter (Context context, int textViewResourceId, List<OrderHolder.GoodsItem> data) {
		super();
		this.data = data;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		v = convertView;
         if (v == null) {
        	 LayoutInflater vi = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	 v = vi.inflate(R.layout.row_layout, null, false);
         }
         
         TextView productName = (TextView) v
                 .findViewById(R.id.product_name);
         TextView productPrice = (TextView) v
                 .findViewById(R.id.product_price);
         
         String productPriceString = data.get(position).getPrice().toString();
         String productNameString = data.get(position).getName();
         productName.setText(productNameString);
//         productName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
         productPrice.setText(productPriceString+Global.currencyIdent);
//         productPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);

		return v;
	}

	class ViewHolder {
		TextView productName;
		TextView productPrice;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
