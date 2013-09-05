package com.bnsoft.cafe;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

public class Global {
	static List<OrderHolder> orders = new ArrayList<OrderHolder>();
	static Workbook goods = null;
//	static int ORDER_POS = 0;
	
	static int FONT_SIZE = 18;
	static int PORT = 8800;
	static int UPDATE_PORT = PORT+1;
	static String HOST = "192.168.1.7";
	static String DB_FILE_NAME = "GoodsDB.xls";
	static String currencyIdent = "";
	
	public Global() {
		//TODO
	}

}
