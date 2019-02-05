/**
 ================================================================================

 OTIPASS
 adapters package

 @author ED ($Author: ede $)

 @version $Rev: 6352 $
 $Id: ViewPagerAdapter.java 6352 2016-06-10 15:53:06Z ede $

 ================================================================================
 */
package com.otipass.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.otipass.mulhouse.R;
import com.otipass.tools.Messages;

/**
 * Created by metica on 09/06/2016.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private Spanned[] mTextViewStr;
    private String[] mButtonStr;
    private Button[] btns;
    private int[] msgTypes;

    public ViewPagerAdapter(Context mContext, Spanned[] mTextViewStr, String[] mButtonStr, int[] msgTypes) {
        this.mContext = mContext;
        this.mTextViewStr = mTextViewStr;
        this.mButtonStr = mButtonStr;
        btns = new Button[mTextViewStr.length];
        this.msgTypes = msgTypes;
    }

    @Override
    public int getCount() {
        return mTextViewStr.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.pager_item, container, false);

        TextView tv = (TextView) itemView.findViewById(R.id.txt_pager_item);
        tv.setText(mTextViewStr[position]);
        Button btn = (Button) itemView.findViewById(R.id.btn_pager_item);
        if (msgTypes[position] == Messages.cMsgGeneral) {
            btn.setText(Html.fromHtml(mButtonStr[position]));
        } else {
            btn.setText(mButtonStr[position]);
        }
        container.addView(itemView);
        btns[position] = btn;
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public Button getButon(int position) {
        return btns[position];
    }

}
