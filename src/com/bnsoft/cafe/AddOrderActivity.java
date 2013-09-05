package com.bnsoft.cafe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

public class AddOrderActivity extends Activity {

	List<String> table_numbers;
	ButtonedAutoCompleteTextView edit_item;
	ListView order_items;
	String TIME_FORMAT = "dd/MM/yy-HH:mm:ss";
	
	static final int POPUP_DELETE = 1;
	static int POSITION = -1;
	Global Global;
	private static final String PRE_ERROR_STRING = "An error occured:\n";
	private String ERROR_MSG = "";
	
	Logger log = new Logger();
	
	int ORDER_POS = 0;
	
	ArrayAdapter<String> adapterGoods;
	OrderItemsListAdapter adapterOrderItems;
	List<String> goodsNames = new ArrayList<String>();
	ContextThemeWrapper dialogWrapper = new ContextThemeWrapper(AddOrderActivity.this, R.style.AlertDialogTheme);
	
	static SharedPreferences prefs;
	static SharedPreferences.Editor prefsEditor;
	
	DataFormatter df = new DataFormatter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_order);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = prefs.edit();
		
		table_numbers = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(getExternalFilesDir(null), MainActivity.TABLES_FILE_NAME))));
			String tmp = null;
			while ((tmp = in.readLine()) != null) {
				table_numbers.add(tmp);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Sheet sheet = Global.goods.getSheetAt(0);
			if (sheet.getLastRowNum() != 0)
				for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
					goodsNames.add(sheet.getRow(i).getCell(0).getStringCellValue()+" ("+df.formatCellValue(sheet.getRow(i).getCell(1))+Global.currencyIdent+")");
				}
			else Toast.makeText(getBaseContext(), "Database is empty!", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Error! Couldn't initialize database!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		Spinner spinner = (Spinner) findViewById(R.id.picker_1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_item, table_numbers);
//		ArrayAdapter<String> adapter = new SpinnerArrayAdapter(this, R.layout.spinner_item, table_numbers);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(SpinnerOnItemSelectedListener);
		
		Resources res = getResources();
		int color = res.getColor(android.R.color.black);
		
		edit_item = (ButtonedAutoCompleteTextView) findViewById(R.id.edit_item_name);
		edit_item.imgCloseButton = getResources().getDrawable(R.drawable.ximage);
		edit_item.addTextChangedListener(new OnButtonedAutoCompleteTextChangeListener(edit_item));
		edit_item.setTextColor(color);
//		edit_item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
		
		adapterGoods = new ArrayAdapter<String>(this, R.layout.list_item, goodsNames);
		edit_item.setAdapter(adapterGoods);
		
		ORDER_POS = getIntent().getExtras().getInt("ORDER_POS");
		
		order_items = (ListView) findViewById(R.id.order_items);
		order_items.setOnItemClickListener(listViewOnItemClickListener);
		adapterOrderItems = new OrderItemsListAdapter(this, R.layout.row_layout, Global.orders.get(ORDER_POS).orderedItems);
		order_items.setAdapter(adapterOrderItems);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_add_order, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
		switch (item.getItemId()) {
        case R.id.add_order_menu_return:
        	this.finish();
            return true;
        case R.id.add_order_menu_send:
        	Global.HOST = prefs.getString(getString(R.string.edit_ip_key), getString(R.string.edit_ip_defaultValue));
    		String tempString = prefs.getString(getString(R.string.edit_port_key), getString(R.string.edit_port_defaultValue));
    		Global.PORT = Integer.parseInt(tempString);
    		new sendOrder().execute(""+ORDER_POS);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
//	public void OnOkClick(View view) {
//		
//	}
	
//	public void OnCompleteOrderClick(View view) {
//		
//	}
	
	OnItemClickListener listViewOnItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			POSITION = position;
			log.l(log.TAG_DBG, "[ListViewItemClick] position = "+POSITION);
			Dialog alert = onCreateDialog(POPUP_DELETE);
			alert.show();
			
//			TextView qText = (TextView) view.findViewById(R.id.text);
//
//			String item = qText.getText().toString();
//
//			Toast.makeText(getBaseContext(), item + "\n LONG CLICK",
//					Toast.LENGTH_LONG).show();
		}
	};
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case POPUP_DELETE:
			final String[] mItems ={"Delete", "Cancel"};

			AlertDialog.Builder builder = new AlertDialog.Builder(dialogWrapper);
			builder.setTitle("Item");

			builder.setItems(mItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					// TODO Auto-generated method stub
					switch (item) {
					case 0:
						List<OrderHolder.GoodsItem> orderedItems = Global.orders.get(ORDER_POS).orderedItems;
						BigDecimal productPrice = orderedItems.get(POSITION).getPrice();
						String productName = orderedItems.get(POSITION).getName();
						
				        Global.orders.get(ORDER_POS).orderPrice = Global.orders.get(ORDER_POS).orderPrice.subtract(productPrice);
						Global.orders.get(ORDER_POS).orderedItems.remove(POSITION);
						adapterOrderItems.notifyDataSetChanged();
						MainActivity.adapterOrders.notifyDataSetChanged();
					case 1:
						dialog.dismiss();
					}
						
				}
			});
			builder.setCancelable(true);
			
			return builder.create();
		default:
			return null;
		}
	}
	
	public void OnAddOrderItemClick(View view) {
		try {
			if (!edit_item.getText().toString().equals("")) {
				if (goodsNames.indexOf(edit_item.getText().toString()) != -1) {
					Row row = Global.goods.getSheetAt(0).getRow(goodsNames.indexOf(edit_item.getText().toString()));
					log.l(log.TAG_DBG, row.getCell(0).getStringCellValue());
					
					String productName = row.getCell(0).getStringCellValue();
					BigDecimal productPrice = new BigDecimal(df.formatCellValue(row.getCell(1)).replace(",", "."));
					Global.orders.get(ORDER_POS).orderedItems.add(new OrderHolder.GoodsItem(productName, productPrice));
			        Global.orders.get(ORDER_POS).orderPrice = Global.orders.get(ORDER_POS).orderPrice.add(productPrice);
					
					adapterOrderItems.notifyDataSetChanged();
					MainActivity.adapterOrders.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Error adding an item!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			if (prefs.getBoolean(getString(R.string.debug_key), false)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(AddOrderActivity.this);
				builder.setTitle("Error!");
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				String tmp = "";
				for (StackTraceElement ste : e.getStackTrace()) {
					tmp += "\n"+ste.toString();
				}
				builder.setMessage(PRE_ERROR_STRING+e.toString()+"\n\nStackTrace:"+tmp);
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}
	
	OnItemSelectedListener SpinnerOnItemSelectedListener = new OnItemSelectedListener() {
	
		@Override
		public void onItemSelected(AdapterView<?> parent,
		    View view, int pos, long id) {
			log.l(log.TAG_DBG, parent.getItemAtPosition(pos).toString());
		      Global.orders.get(ORDER_POS).tableN = parent.getItemAtPosition(pos).toString();
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		  // TODO do nothing
		}

	};
	
	private class sendOrder extends AsyncTask<String,Void,Byte> {

		@Override
		protected Byte doInBackground(String... arg) {
			byte retVal = -1;
			try {
				int POS = Integer.parseInt(arg[0]);
				
				Socket connection = new Socket();
				log.l(log.TAG_DBG, "HOST = "+Global.HOST+" | PORT = "+Global.PORT);
				connection.connect(new InetSocketAddress(Global.HOST, Global.PORT), 2000);
				if(connection.isConnected()) {
					PrintWriter outCon = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF8")), true);
					outCon.println("order");
//					outCon.println(table);
//					outCon.println(id);
//					outCon.println(time);
//					outCon.println(orderSize);
//					for (int i = 0; i < orderSize; i++) {
//						outCon.println(Global.orders.get(POS).orderedItems.get(i));
//					}
					OrderHolder object = Global.orders.get(POS);
					Gson gson = new Gson();
					outCon.println(gson.toJson(object));
					connection.close();
					retVal = 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				retVal = -1;
			}
			return retVal;
		}
		
		@Override
		protected void onPostExecute(Byte output) {
			// TODO
			if (output.byteValue() == 1) {
			Toast.makeText(getBaseContext(), "Order sent!",
					Toast.LENGTH_LONG).show();
			} else if (output.byteValue() == -1){
				Toast.makeText(getBaseContext(), "Error! Order not sent!",
						Toast.LENGTH_LONG).show();
			}
			AddOrderActivity.this.finish();
		}
		
	}
	
}
