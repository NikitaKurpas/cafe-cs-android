package com.bnsoft.cafe;

import android.text.Editable;
import android.text.TextWatcher;

public class OnButtonedAutoCompleteTextChangeListener implements TextWatcher {

	ButtonedAutoCompleteTextView tv;

	public OnButtonedAutoCompleteTextChangeListener(
			ButtonedAutoCompleteTextView TextView) {
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
			tv.justCleared = false;

	}
}
