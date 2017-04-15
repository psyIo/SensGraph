package org.psylo.sensgraph;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;
//import android.widget.TextView;

/**
 * Created by psylo on 17.3.11.
 */

//public class ExpandedTextView extends TextView{
public class ExpandedTextView extends AppCompatTextView {

    public int clicks; //0 - neutral, 1 - clicked once (name), 2 - clicked twice (value)
    static DevTools dt = new DevTools(); //dev

    public ExpandedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.clicks = 0;
    }

    public void setClicks(int newClickCount) {
        this.clicks = newClickCount;
//        this.setBackground(clicks);
    }

    public int getClicks() {
        return this.clicks;
    }

//    public void nextState() {
//        switch (getClicks()) {
//            case 0:
//                setClicks(1);  //clicked once
//                setBackgroundResource(R.color.blue);
//                break;
//            case 1:
//                setClicks(2);  //clicked twice
//                setBackgroundResource(R.color.magenta);
//                break;
//            case 2:
//                setClicks(0);  //not clicked
//                setBackgroundResource(R.color.white);
//                break;
//        }
//    }

//    private void setBackground(int clicks) {
//        dt.logV("this", this); //dev
//        switch (clicks) {
//            case 0: //not clicked
//                this.setBackgroundResource(R.color.white);
//                break;
//            case 1: //clicked once
//                this.setBackgroundResource(R.color.blue);
//                break;
//            case 2: //clicked twice
//                this.setBackgroundResource(R.color.magenta);
//                break;
//        }
//    }
}
