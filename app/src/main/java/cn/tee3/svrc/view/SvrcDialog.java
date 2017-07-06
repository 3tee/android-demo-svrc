package cn.tee3.svrc.view;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

import cn.tee3.svrc.R;

/**
 * 弹窗
 * Created by Administrator on 2017/6/23.
 */

public class SvrcDialog {

    /**
     * 提示安排房间
     *
     * @param context
     */
    public static void ScheduleRoomDialog(final Context context) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setView(new EditText(context));
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.schedule_room_dialog);
    }

    /**
     * 获取Hls的二维码
     *
     * @param context
     * @param liveUrl
     */
    public static void HlsImgDialog(final Context context, String liveUrl) {
        AlertDialog builder = new AlertDialog.Builder(context).create();
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(true);
        View view = builder.getLayoutInflater().inflate(R.layout.hls_img_dialog, null);
        WebView webView = (WebView) view.findViewById(R.id.wv_play);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(liveUrl);
        builder.setView(view);
        builder.show();
    }

    /**
     * 播放Hls的直播流
     *
     * @param context
     * @param liveUrl
     */
    public static void PlayLiveDialog(final Context context, String liveUrl) {
        AlertDialog builder = new AlertDialog.Builder(context).create();
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        View view = builder.getLayoutInflater().inflate(R.layout.play_live_dialog, null);
        VideoView videoView = (VideoView) view.findViewById(R.id.vv_live_play);
        videoView.setMediaController(new MediaController(context));
        videoView.setVideoURI(Uri.parse(liveUrl));
        videoView.start();
        videoView.requestFocus();
        builder.setView(view);
        builder.show();
    }
}
