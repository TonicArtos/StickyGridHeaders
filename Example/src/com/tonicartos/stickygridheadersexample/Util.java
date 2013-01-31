package com.tonicartos.stickygridheadersexample;

import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

public class Util {
    public static void goToGitHub(Context context) {
        Uri uriUrl = Uri.parse("http://github.com/TonicArtos/StickyGridHeaders");
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        context.startActivity(launchBrowser);
    }

    public static void showHtmlDialog(Context context, int titleResId, int contentResId) {
        new AlertDialog.Builder(context).setTitle(titleResId)
                                        .setMessage(Html.fromHtml(context.getString(contentResId)))
                                        .show();
    }

    public static Intent makeSendEmailIntent(Context context, int emailResId, int subjectResId) {
        final Intent email = new Intent(android.content.Intent.ACTION_SENDTO);
        @SuppressWarnings("deprecation")
        String uriText = "mailto:" + context.getString(emailResId) + "?subject=" + URLEncoder.encode(context.getString(subjectResId));
        email.setData(Uri.parse(uriText));
        return email;
    }
}
