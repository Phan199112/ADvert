package com.international.advert.utility;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

import com.international.advert.R;
import com.kaopiz.kprogresshud.KProgressHUD;


/**
 * Created by resea on 29/04/2017.
 */

public class Utils {

    public static Integer waterResource = R.raw.water1;
    private static KProgressHUD progressHUD;

    public static void transferAnimation(Activity activity, boolean flag)
    {
        if (flag){

            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {

            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    //region Toast dialog

    public static void long_Toast(Context context, String str)
    {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static void short_Toast(Context context, String str)
    {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region progress dialog

    public static void showProgressHUD(Context context)
    {
        progressHUD = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        progressHUD.show();
    }

    public static void dismissProgressHUD()
    {
        progressHUD.dismiss();
    }

    //endregion

    public static boolean isValidateEmail(CharSequence target)
    {
        if (target == null){
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
