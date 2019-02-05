/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6363 $
 $Id: DrawerActivity.java 6363 2016-06-13 16:39:57Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import models.Constants;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.otipass.synchronization.SynchronizationService;
import com.otipass.tools.ExceptionHandler;

public class BugMsgActivity extends Activity implements OnClickListener{

	private Button btn_rtn;
	private Intent intent;
	private Bundle bundle;
	private SynchronizationService synchroService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_bug);

		btn_rtn = (Button) findViewById(R.id.btn_return);
		btn_rtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (btn_rtn.equals(v)) {
			intent = new Intent(BugMsgActivity.this, DrawerActivity.class);
			intent.putExtra(Constants.EXTERN_CALL_KEY, true);
            //Bundle params = new Bundle();
            //params.putBoolean(Constants.BUG_ACTIVITY_KEY, true);
            //intent.putExtras(params);
            this.startActivity(intent);
		}
	}

}
