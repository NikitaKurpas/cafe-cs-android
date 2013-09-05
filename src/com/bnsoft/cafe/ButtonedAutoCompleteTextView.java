package com.bnsoft.cafe;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class ButtonedAutoCompleteTextView extends AutoCompleteTextView {
	
	//was the text just cleared?
	boolean justCleared = false;

	//The image we defined for the clear button
	public Drawable imgCloseButton = getResources().getDrawable(R.drawable.ximage);

	/* Required methods, not used in this implementation */
	public ButtonedAutoCompleteTextView(Context context)
	{
	  super(context);
	  init();
	}
	/* Required methods, not used in this implementation */
	public ButtonedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle)
	{
	  super(context, attrs, defStyle);
	  init();
	}
	/* Required methods, not used in this implementation */
	public ButtonedAutoCompleteTextView(Context context, AttributeSet attrs)
	{
	  super(context, attrs);
	  init();
	}

	void init()
	{
		
	  // Set the bounds of the button
	  this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgCloseButton, null);

	  // button should be hidden on first draw
	  clrButtonHandler();

	  //if the clear button is pressed, clear it. Otherwise do nothing
	  this.setOnTouchListener(new OnTouchListener()
	  {
	   @Override
	   public boolean onTouch(View v, MotionEvent event)
	   {

		   ButtonedAutoCompleteTextView et = ButtonedAutoCompleteTextView.this;

	    if (et.getCompoundDrawables()[2] == null)
	     return false;

	    if (event.getAction() != MotionEvent.ACTION_UP)
	     return false;

	    if (event.getX() > et.getWidth() - et.getPaddingRight() - imgCloseButton.getIntrinsicWidth())
	    {
	     et.setText("");
	     ButtonedAutoCompleteTextView.this.clrButtonHandler();
	     justCleared = true;
	    }
	    return false;
	   }
	  });
	}

	void clrButtonHandler()
	{
	 
	  if (this == null || this.getText().toString().equals("") || this.getText().toString().length() == 0)
	  {
	   //Log.d("CLRBUTTON", "=cleared");
	   //remove clear button
	   this.setCompoundDrawables(null, null, null, null);
	  }
	  else
	  {
	   //Log.d("CLRBUTTON", "=not_clear");
	   //add clear button
	   this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgCloseButton, null);
	  }
	}
	
/*	public class OnButtonedAutoCompleteTextChangeListener implements TextWatcher {
		
		ButtonedAutoCompleteTextView tv;
	
		public OnButtonedAutoCompleteTextChangeListener(ButtonedAutoCompleteTextView TextView) {
			tv = TextView;
		}
		
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			tv.clrButtonHandler();
			if (tv.justCleared)
			{
				tv.justCleared = false;
			}
			
		}
	}*/
	
}