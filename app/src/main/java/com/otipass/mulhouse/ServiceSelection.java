/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6450 $
 $Id: ServiceSelection.java 6450 2016-07-06 09:43:10Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceSelection extends DialogFragment {
    private NoticeDialogListener mListener;
    private int selectedIndex = 0;
    private String[] servicesNames;
    private int[] servicesIds;
    private HashMap<Integer, String> servicesList;
    private TextView tvError;

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(int service);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(getActivity());
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedIndex = -1;
        servicesList = (HashMap) getArguments().getSerializable("servicesList");
        servicesNames = new String[servicesList.size()];
        servicesIds = new int[servicesList.size()];
        Iterator it = servicesList.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            servicesIds[i] = (int) elt.getKey();
            servicesNames[i++] = (String) elt.getValue();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_select_service, null);
        builder.setView(v) ;
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        TextView tvTitle = (TextView) v.findViewById(R.id.dialog_title);
        tvError = (TextView) v.findViewById(R.id.rb_error);
        tvError.setVisibility(TextView.GONE);
        tvTitle.setText(R.string.several_services_available);
        Button button = (Button) v.findViewById(R.id.btn_ok);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectedIndex >= 0) {
                    getDialog().dismiss();
                    mListener.onDialogPositiveClick(servicesIds[selectedIndex]);
                } else {
                    tvError.setVisibility(TextView.VISIBLE);
                }
            }
        });
        RadioGroup rg = (RadioGroup) v.findViewById(R.id.radio_group);

        for(i=0; i<servicesNames.length; i++){
            RadioButton rb = (RadioButton) getActivity().getLayoutInflater().inflate(R.layout.radiobutton, null);
            rb.setText(servicesNames[i]);
            rg.addView(rb);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                tvError.setVisibility(TextView.GONE);
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        selectedIndex = x;
                    }
                }
            }
        });

        return dialog;
    }

}
