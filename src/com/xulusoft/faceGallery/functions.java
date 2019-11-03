package com.xulusoft.faceGallery;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.widget.Toast;

public class functions {
	
	public  static Bitmap 		CropImageToCircle(Bitmap bitmap) {
		int iw =bitmap.getWidth();
		int ih = bitmap.getHeight();
		Bitmap bt = (bitmap.isMutable())?bitmap:bitmap.copy(Config.ARGB_8888,true);
		float roundPx = (float)(ih>iw?iw:ih)/2; 
		return CropRoundedCornerImage(bt, roundPx);
    }
	
	public  static Bitmap 		CropRoundedCornerImage(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(0,Mode.CLEAR);
        Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
	

	
    public static String 		findTargetAppPackage(Intent intent, Activity activity, String BS_PACKAGE) {
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps.size()>0)
        	return "true";
        if (availableApps != null) {
          for (ResolveInfo availableApp : availableApps) {
            String packageName = availableApp.activityInfo.packageName;
            //showMessage(packageName,activity.getApplicationContext());
            if (BS_PACKAGE==packageName) {
              return packageName;
            }
          }
        }
        return null;
    }
	
    public static AlertDialog showDownloadDialog(final Context con, final String BS_PACKAGE) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(con);
        downloadDialog.setTitle(con.getString(R.string.installTitle));
        downloadDialog.setMessage(R.string.installMessage);
        downloadDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Uri uri = Uri.parse("market://details?id=" + BS_PACKAGE);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
            	con.startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
              // Hmm, market is not installed
              showMessage(con.getString(R.string.noMarkt),con);
            }
          }
        });
        downloadDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {}
        });
        return downloadDialog.show();
      }
    
    public static void 		showMessage(String str, Context con)
    {
	    AlertDialog.Builder aadb = new AlertDialog.Builder(con);
        aadb.setMessage(str);
        aadb.create().show();
        
		//Toast toast = Toast.makeText(this, str, 3);
		//toast.show();
	}
    public static void		ToastMessage(Context con,String str)
	{
		Toast toast = Toast.makeText(con, str, 3);
		toast.show();
	}

}
