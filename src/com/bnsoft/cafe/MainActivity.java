package com.bnsoft.cafe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Global Global;
	int POSITION = -1;
	private static final int POPUP_OPTIONS = 2;
	private static final int POPUP_DEBUG = 3;
	private static final int POPUP_DEBUG_LIST = 4;
	private static final int POPUP_ERROR = -1;
	private static final String PRE_ERROR_STRING = "An error occured:\n";
	private String ERROR_MSG = "";
	private String ERROR_TRACE = "";
	Logger log = new Logger();
	ListView orders;
	int CONNECTION_TIMEOUT = 5000;
	UpdateDB updater = null;
	
	public static OrdersListAdapter adapterOrders;
	
	static SharedPreferences prefs;
	static SharedPreferences.Editor prefsEditor;
	static final String TABLES_FILE_NAME = "Tables.txt";
	
	MenuItem debugSampleFileItem;
	
	DataFormatter df = new DataFormatter();
	ContextThemeWrapper dialogWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = prefs.edit();
        Global.HOST = prefs.getString(getString(R.string.edit_ip_key), getString(R.string.edit_ip_defaultValue));
		String tempString = prefs.getString(getString(R.string.edit_port_key), getString(R.string.edit_port_defaultValue));
		Global.PORT = Integer.parseInt(tempString);
		Global.DB_FILE_NAME = prefs.getString(getString(R.string.db_file_name_key), getString(R.string.db_file_name_defaultValue));
		Global.currencyIdent = prefs.getString(getString(R.string.edit_currency_key), getString(R.string.edit_currency_defaultValue));
		
		try {
			File file = new File(getExternalFilesDir(null), Global.DB_FILE_NAME);
			if (!file.exists()) {
				file.createNewFile();
				Workbook wb = new HSSFWorkbook();
				wb.createSheet();
				wb.write(new FileOutputStream(file));
			}
			Global.goods = WorkbookFactory.create(file);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error! Database not loaded!",
					Toast.LENGTH_LONG).show();
		}
		
//		Button button = (Button) findViewById(R.id.button_add_order);
//		button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
//		TextView tv = (TextView) findViewById(R.id.textView1);
//		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Global.FONT_SIZE);
		
		orders = (ListView) findViewById(R.id.list_orders);
		orders.setOnItemClickListener(listViewOnItemClickListener);
		orders.setOnItemLongClickListener(listViewOnItemLongClickListener);
		adapterOrders = new OrdersListAdapter(this, R.layout.row_layout_orders, Global.orders);
		orders.setAdapter(adapterOrders);
		
	}
	
	public void OnAddOrderClick(View view) {
		Random uniqueId = new Random();
		OrderHolder oh = new OrderHolder();
		oh.orderID = Integer.toString(uniqueId.nextInt(999999));
		Global.orders.add(oh);
		adapterOrders.notifyDataSetChanged();
		Intent intent = new Intent(this, AddOrderActivity.class);
		intent.putExtra("ORDER_POS", Global.orders.size()-1);
		
		File file = new File(getExternalFilesDir(null), TABLES_FILE_NAME);
		if(!file.exists()) {
			try {
//				Toast.makeText(getBaseContext(), "Table names file not present. Generating new one.",
//						Toast.LENGTH_SHORT).show();
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);
				for (int i = 1; i < 10; i++) {
					out.write(""+i);
					out.newLine();
				}
				out.close();
				fstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		debugSampleFileItem = menu.findItem(R.id.menu_debug);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (prefs.getBoolean(getString(R.string.debug_key), false)) {
			debugSampleFileItem.setVisible(true);
		} else {
			debugSampleFileItem.setVisible(false);
		}
		return true;
	}
	
	@Override
	public void onResume() {
		adapterOrders.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_updateDB:
            	Global.HOST = prefs.getString(getString(R.string.edit_ip_key), getString(R.string.edit_ip_defaultValue));
        		String tempString = prefs.getString(getString(R.string.edit_port_key), getString(R.string.edit_port_defaultValue));
        		Global.PORT = Integer.parseInt(tempString);
        		log.l(log.TAG_DBG, "Global.PORT = "+Global.PORT+" | Global.HOST = "+Global.HOST);
            	updater = new UpdateDB();
            	updater.execute();
            	return true;
            case R.id.menu_debug:
            	Dialog alert = onCreateDialog(POPUP_DEBUG);
    			alert.show();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	OnItemLongClickListener listViewOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			POSITION = position;
			log.l(log.TAG_DBG, "[ListViewItemClick] position = "+POSITION);
			
			Dialog alert = onCreateDialog(POPUP_OPTIONS);
			alert.show();
//			alert.show();alert.getWindow().getAttributes();
//			TextView textView = (TextView) alert.findViewById(android.R.id.message);
//		    textView.setTextSize(getResources().getDimension(R.dimen.TextSize));
//			showDialog(POPUP_OPTIONS);
			return true;
		}
		
	};
	
	OnItemClickListener listViewOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {

			POSITION = position;
			Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
			intent.putExtra("ORDER_POS", POSITION);
			startActivity(intent);
		}
	};
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case POPUP_OPTIONS:
		{
			final String[] mItems ={"Delete", "Cancel"};

			AlertDialog.Builder builder = new AlertDialog.Builder(dialogWrapper);
			builder.setTitle("Item");

			builder.setItems(mItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {	
					case 0:
						Global.orders.remove(POSITION);
						adapterOrders.notifyDataSetChanged();
					case 1:
						dialog.dismiss();
					}
						
				}
			});
			builder.setCancelable(true);
			
			return builder.create();
		}
		case POPUP_DEBUG:
		{
			final String[] mItems ={"Create sample database", "List goods","Cancel"};

			AlertDialog.Builder builder = new AlertDialog.Builder(dialogWrapper);
			builder.setTitle("Debug options"); // заголовок для диалога

			builder.setItems(mItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					// TODO Auto-generated method stub
					switch (item) {	
					case 0:
						Global.goods = setSampleGoods();
					case 1:
						Dialog alert = onCreateDialog(POPUP_DEBUG_LIST);
						alert.show();
					case 2:
						dialog.dismiss();
					}
						
				}
			});
			builder.setCancelable(true);
			
			return builder.create();
		}
		case POPUP_DEBUG_LIST:
		{
			try {
				int size = Global.goods.getSheetAt(0).getLastRowNum() + 1 + 3;
				String[] mItems = new String[size];
				mItems[0] = "--[ LIST BEGIN ]--";
				try {
					for (int i = Global.goods.getSheetAt(0).getFirstRowNum(); i <= Global.goods
							.getSheetAt(0).getLastRowNum(); i++) {
						mItems[i + 1] = Global.goods.getSheetAt(0).getRow(i)
								.getCell(0).getStringCellValue()
								+ " : "
								+ df.formatCellValue(Global.goods.getSheetAt(0)
										.getRow(i).getCell(1));
					}
				} catch (Exception e) {
				}
				mItems[size - 2] = "--[ LIST END ]--";
				mItems[size - 1] = "--[ Total size is "
						+ Global.goods.getSheetAt(0).getLastRowNum() + " ]--";
				AlertDialog.Builder builder = new AlertDialog.Builder(
						dialogWrapper);
				builder.setTitle("Goods list");
				builder.setItems(mItems, null);
				return builder.create();
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "Error! Database may be empty!",
						Toast.LENGTH_LONG).show();
			}
		}
		case POPUP_ERROR:
		{
			
		}
		default:
			return null;
		}
	}
	
	private Workbook setSampleGoods() {
		Workbook RET_VAL = new HSSFWorkbook();
		Sheet sheet = RET_VAL.createSheet();
		
		sheet.createRow(0).createCell(0).setCellValue("Pizza Italiano 400g");
		sheet.getRow(0).createCell(1).setCellValue("5.62");
		sheet.createRow(1).createCell(0).setCellValue("Pizza Italiano 800g");
		sheet.getRow(1).createCell(1).setCellValue("8.00");
		sheet.createRow(2).createCell(0).setCellValue("Pizza Moccarelo 500g");
		sheet.getRow(2).createCell(1).setCellValue("6.65");
		sheet.createRow(3).createCell(0).setCellValue("Pizza Moccarelo 900g");
		sheet.getRow(3).createCell(1).setCellValue("10.50");
		sheet.createRow(4).createCell(0).setCellValue("Pizza Margarita 300g");
		sheet.getRow(4).createCell(1).setCellValue("4.00");
		sheet.createRow(5).createCell(0).setCellValue("Pizza Margarita 600g");
		sheet.getRow(5).createCell(1).setCellValue("7.25");
		sheet.createRow(6).createCell(0).setCellValue("Pizza Margarita 1000g");
		sheet.getRow(6).createCell(1).setCellValue("13.00");
		sheet.createRow(7).createCell(0).setCellValue("Pizza Benedict 500g");
		sheet.getRow(7).createCell(1).setCellValue("8.00");
		sheet.createRow(8).createCell(0).setCellValue("Pizza Benedict 900g");
		sheet.getRow(8).createCell(1).setCellValue("14.80");
		sheet.createRow(9).createCell(0).setCellValue("Pizza Milano 300g");
		sheet.getRow(9).createCell(1).setCellValue("3.00");
		sheet.createRow(10).createCell(0).setCellValue("Pizza Milano 600g");
		sheet.getRow(10).createCell(1).setCellValue("5.00");
		sheet.createRow(11).createCell(0).setCellValue("Pizza Milano 900g");
		sheet.getRow(11).createCell(1).setCellValue("8.00");
		sheet.createRow(12).createCell(0).setCellValue("Tea Queen of England small");
		sheet.getRow(12).createCell(1).setCellValue("1.50");
		sheet.createRow(13).createCell(0).setCellValue("Tea Queen of England big");
		sheet.getRow(13).createCell(1).setCellValue("3.20");
		sheet.createRow(14).createCell(0).setCellValue("Coffee Espresso");
		sheet.getRow(14).createCell(1).setCellValue("1.50");
		sheet.createRow(15).createCell(0).setCellValue("CocaCola 500ml");
		sheet.getRow(15).createCell(1).setCellValue("2.30");
		sheet.createRow(16).createCell(0).setCellValue("Sprite 500ml");
		sheet.getRow(16).createCell(1).setCellValue("2.00");
		sheet.createRow(17).createCell(0).setCellValue("Fanta 500ml");
		sheet.getRow(17).createCell(1).setCellValue("2.00");
		sheet.createRow(18).createCell(0).setCellValue("BNSoft's special");
		sheet.getRow(18).createCell(1).setCellValue("50.00");
		sheet.createRow(19).createCell(0).setCellValue("Fried chicken");
		sheet.getRow(19).createCell(1).setCellValue("15.00");
		sheet.createRow(20).createCell(0).setCellValue("Hamburger");
		sheet.getRow(20).createCell(1).setCellValue("2.00");
		sheet.createRow(21).createCell(0).setCellValue("ukr: ковбасна нарізка 250 грам");
		sheet.getRow(21).createCell(1).setCellValue("15.80");
		sheet.createRow(22).createCell(0).setCellValue("rus: Пицца \"Семейная\"");
		sheet.getRow(22).createCell(1).setCellValue("122.00");
		sheet.createRow(23).createCell(0).setCellValue("Взбитые сливки 300г");
		sheet.getRow(23).createCell(1).setCellValue("12.50");
		sheet.createRow(24).createCell(0).setCellValue("jpn: 500グラムのチーズボール");
		sheet.getRow(24).createCell(1).setCellValue("1200.00");
		sheet.createRow(25).createCell(0).setCellValue("chn: 飯糰10枚");
		sheet.getRow(25).createCell(1).setCellValue("2180.00");
		sheet.createRow(26).createCell(0).setCellValue("hnd: आइसक्रीम की 100 ग्राम");
		sheet.getRow(26).createCell(1).setCellValue("2.03");
		sheet.createRow(27).createCell(0).setCellValue("DEBUG: "+DateFormat.getDateTimeInstance().format(new Date()));
		sheet.getRow(27).createCell(1).setCellValue(DateFormat.getTimeInstance().format(new Date()).substring(0, 4).replace(":", "."));
		
		Toast.makeText(getBaseContext(), "Sample database set!",
				Toast.LENGTH_LONG).show();
		return RET_VAL;
	}
	
	public class UpdateDB extends AsyncTask<Void, Integer, Byte> {

		/*
		 * number - value
		 * 1 - Goods loaded;
		 * 2 - Goods not loaded
		 */
		final byte GOODS_LOADED = 1;
		final byte GOODS_NOT_LOADED = 2;
		
		protected ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Updating..."); 
            pd.setMessage("Updating database...");
            pd.setCancelable(true);
//            pd.setOnDismissListener(new OnDismissListener() {
//				
//				@Override
//				public void onDismiss(DialogInterface dialog) {
//					updater.cancel(true);
//				}
//			});
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setIndeterminate(false); 
            pd.setMax(0);
            pd.show();
		}

		@Override
		protected Byte doInBackground(Void...values) {
			byte RET_VAL = GOODS_NOT_LOADED;
			try {
				log.l(log.TAG_DBG, "HOST = "+Global.HOST+" | PORT = "+Global.PORT);
				Socket mainConnection = new Socket();
				mainConnection.connect(new InetSocketAddress(Global.HOST, Global.PORT), CONNECTION_TIMEOUT);
				if (mainConnection.isConnected()) {
					PrintWriter mConLineOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mainConnection.getOutputStream())), true);
					BufferedReader mConLineIn = new BufferedReader(new InputStreamReader(mainConnection.getInputStream()));
					mConLineOut.println(S.UPDATE_STRING);
					File file = new File(getExternalFilesDir(null), Global.DB_FILE_NAME);
					log.l(log.TAG_DBG, "DB location = "+getExternalFilesDir(null)+Global.DB_FILE_NAME);
					int UPDATE_PORT = Integer.parseInt(mConLineIn.readLine());
					String recievedMD5 = mConLineIn.readLine();
					int recievedFileSize = Integer.parseInt(mConLineIn.readLine());
					SocketAddress socketAddress = new InetSocketAddress(mainConnection.getInetAddress().getHostAddress(), UPDATE_PORT);
					SocketChannel channel = SocketChannel.open();
					channel.connect(socketAddress);
					channel.configureBlocking(true);
					ByteBuffer buffer = ByteBuffer.allocate(4096);
					int bytes_read = 0;
					int bytes_total = 0;
					FileOutputStream fos = new FileOutputStream(file);
					FileChannel fileOut = fos.getChannel();
					while (bytes_total != recievedFileSize) {
						try {
							bytes_read = channel.read(buffer);
							bytes_total+=bytes_read;
						} catch (IOException e) {
							e.printStackTrace();
							bytes_read = 0;
						}
						buffer.flip();
						fileOut.write(buffer);
						buffer.clear();
					}
					fos.close();
					fileOut.close();
					FileInputStream fileIn = new FileInputStream(file);
					String fileMD5 = new String(Hex.encodeHex(DigestUtils.md5(fileIn)));
					log.l(log.TAG_DBG, "generated: fileSize="+file.length()+" | MD5="+fileMD5);
					if (fileMD5.equals(recievedMD5)) {
						Global.goods = WorkbookFactory.create(file);
						RET_VAL = GOODS_LOADED;
					} else { RET_VAL = GOODS_NOT_LOADED; }
					fileIn.close();
					mainConnection.close();
				} else {
					pd.dismiss();
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Error!");
					builder.setCancelable(true);
					builder.setMessage(PRE_ERROR_STRING+"Not connected to host!");
					AlertDialog alert = builder.create();
					alert.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				ERROR_MSG = e.toString();
				ERROR_TRACE = "";
				for (StackTraceElement ste : e.getStackTrace()) {
					ERROR_TRACE += "\n"+ste.toString();
				}
					
			} 
			
			return RET_VAL;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			pd.incrementProgressBy(values[0]);
			pd.incrementSecondaryProgressBy(1);
		}
		
		@Override
		protected void onPostExecute(Byte output) {
			if (output.byteValue() == GOODS_LOADED) {
				Toast.makeText(getBaseContext(), "Database updated!",
						Toast.LENGTH_LONG).show();
			} else if (output.byteValue() == GOODS_NOT_LOADED) {
				if (prefs.getBoolean(getString(R.string.debug_key), false)) {
					pd.dismiss();
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Error!");
					builder.setCancelable(true);
					builder.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setMessage(PRE_ERROR_STRING+ERROR_MSG+"\n\nStackTrace:"+ERROR_TRACE);
					AlertDialog alert = builder.create();
					alert.show();
				}
				Toast.makeText(getBaseContext(), "Error! Database not updated!",
						Toast.LENGTH_LONG).show();
			}
			pd.dismiss();
		}
		
	}

}
