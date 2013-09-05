package com.bnsoft.cafe;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class OrdersListAdapter extends ArrayAdapter<OrderHolder>{
	
	Logger log = new Logger();
	
	private Context context;
    View v;

	public OrdersListAdapter (Context context, int textViewResourceId, List<OrderHolder> data) {
		super(context, textViewResourceId, data);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		v = convertView;
         if (v == null) {
        	 LayoutInflater vi = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	 v = vi.inflate(R.layout.row_layout_orders, null, false);
         }
         
         TextView orderTable = (TextView) v
                 .findViewById(R.id.table_number);
         TextView orderId = (TextView) v
                 .findViewById(R.id.order_id);
         TextView orderPrice = (TextView) v
        		 .findViewById(R.id.order_price);
//         TextView tv4 = (TextView) v
//        		 .findViewById(R.id.textView4);
//         tv4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
//         TextView tv5 = (TextView) v
//        		 .findViewById(R.id.textView4);
//         tv5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
         
         if (Global.orders.size() > 0) {
	         List<OrderHolder> orders = Global.orders;
		     String orderPriceString = orders.get(position).orderPrice.toString(); 
	         orderTable.setText(orders.get(position).tableN);
//	         orderTable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
	         orderId.setText(orders.get(position).orderID);
//	         orderId.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
	         orderPrice.setText(orderPriceString+Global.currencyIdent);
//	         orderPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
         }
         
		return v;
	}

	class ViewHolder {
		TextView productName;
		TextView productPrice;
	}
}

