package com.bnsoft.cafe;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class OrderHolder implements Serializable {
	
	public String orderID = "";
	public List<GoodsItem> orderedItems = new ArrayList<GoodsItem>();
	public String tableN = "";
	public BigDecimal orderPrice = BigDecimal.ZERO;
	
	public OrderHolder() {
		//TODO some init if needed, else - leave empty
	}
	
	static class GoodsItem implements Serializable {
		private String name = "";
		private BigDecimal price = BigDecimal.ZERO;
		
		public GoodsItem(String name, BigDecimal price) {
			this.name = name;
			this.price = price;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
	}
	
}
