package cn.tee3.svrc.function;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.tee3.avd.AVDLive;
import cn.tee3.avd.AVDRecord;
import cn.tee3.avd.ErrorCode;
import cn.tee3.avd.MVideo;
import cn.tee3.avd.Room;
import cn.tee3.avd.User;
import cn.tee3.svrc.Constants;
import cn.tee3.svrc.R;
import cn.tee3.svrc.SvrcApp;
import cn.tee3.svrc.adapter.CamerasAdapter;
import cn.tee3.svrc.avroom.AVRoom;
import cn.tee3.svrc.utils.StringUtils;
import cn.tee3.svrc.view.EventLogView;
import cn.tee3.svrc.view.SvrcDialog;

/**
 * 服务器旁路直播
 * Created by shengf on 2017/6/22.
 */

public class AVDLiveActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AVDLive.Listener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "AVDLiveActivity";
    private TextView tvTitle;
    private TextView tvFunction;//开始直播/停止直播
    private TextView tvHlsUrl;//HlsUrl链接
    private TextView tvRtmpUrl;//RtmpUrl链接
    private TextView tvShowLive;//查看直播
    private EventLogView logView;
    private ListView lvCameras;
    private RadioGroup rgAudioSelect;//rb_audio_no无音频;rb_audio_one仅所选中视频用户的音频;rb_audio_without_me房间内除自己外的音频;rb_audio_all房间所有音频
    private View transView;

    private AVDLive avdLive;
    private AVRoom mRoom;
    private List<MVideo.Camera> mPublishedCameras;//视频摄像头信息列表
    private CamerasAdapter cAdapter;

    private String roomId;
    private boolean isLive = false;
    private String mvideoDeviceId = "";//直播所选择视频设备Id
    private String maudioUserId = "";//直播所选择音频所属用户Id
    private AVDRecord.RecordAudioType audioType = AVDRecord.RecordAudioType.ra_user_single;//直播导出时所选的音频参数,默认选中一个用户
    private AVDRecord.RecordVideoType videoType = AVDRecord.RecordVideoType.rv_none;//默认为无
    private String liveId = "";//正在直播的id
    private String HlsUrlStr = "";
    private String RtmpUrlStr = "";
    private String playHlsurl = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avd_live_layout);

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText("房间号：" + roomId);

        transView = findViewById(R.id.trans_view);
        tvFunction = (TextView) findViewById(R.id.tv_function);
        tvHlsUrl = (TextView) findViewById(R.id.tv_hls);
        tvRtmpUrl = (TextView) findViewById(R.id.tv_rtmp);
        tvShowLive = (TextView) findViewById(R.id.tv_show_live);
        logView = (EventLogView) findViewById(R.id.event_view);
        lvCameras = (ListView) findViewById(R.id.lv_cameras);
        rgAudioSelect = (RadioGroup) findViewById(R.id.rg_audio_select);

        tvFunction.setOnClickListener(this);
        tvHlsUrl.setOnClickListener(this);
        tvRtmpUrl.setOnClickListener(this);
        tvShowLive.setOnClickListener(this);
        lvCameras.setOnItemClickListener(this);
        rgAudioSelect.setOnCheckedChangeListener(this);

        tvFunction.setText("开始直播");
        tvHlsUrl.setText("Hls链接");
        tvRtmpUrl.setText("Rtmp链接");
        tvShowLive.setText("查看直播");

        //设置回调
        avdLive = AVDLive.instance();
        avdLive.setListener(this);

        startUpVideo(roomId);//加入房间
    }

    void startUpVideo(String roomId) {
        Log.i(TAG, "startUpVideo");
        // step1: 加入房间
        mRoom = new AVRoom(roomId, new AVRoom.CameraPublishListener() {
            @Override
            public void CameraPublishEvent(boolean isCameraOpen, MVideo.Camera camera) {
                updatePublishedCameras(isCameraOpen, camera);
            }
        });
        //用户加入或者离开房间回调
        mRoom.setUserListener(new AVRoom.UserListener() {
            @Override
            public void UserEvent(boolean JoinOrLeave, User user) {
                if (StringUtils.isNotEmpty(user.getUserData())) {

                }
            }
        });
        int ret = mRoom.join("testuserId", "test_username", new Room.JoinResultListener() {
            @Override
            public void onJoinResult(int result) {
                if (ErrorCode.AVD_OK != result) {
                    check_ret(result);
                    return;
                }
                // step2: 列出房间中发布的视频
                mPublishedCameras = mRoom.getMVideoCameras();
                //筛选除选用H264编码的

                cAdapter = new CamerasAdapter(SvrcApp.getContextObject(), mPublishedCameras);
                lvCameras.setAdapter(cAdapter);
                cAdapter.notifyDataSetChanged();
            }
        });
        check_ret(ret);
    }

    boolean check_ret(int ret) {
        if (ErrorCode.AVD_OK != ret) {
            logView.addVeryImportantLog("加入房间失败：ErrorCode=" + ret);
            Log.w(TAG, "check_ret: ret=" + ret);
            mRoom.dispose();
            mRoom = null;
            return false;
        }
        logView.addImportantLog("加入房间成功");
        logView.addVeryImportantLog("请用幸会加入此房间并打开摄像头、选用H264编码，协助测试");
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_function:
                if (checkValid()) {
                    if (isLive) {
                        //停止直播
                        tvFunction.setText("开始直播");
                        transView.setBackgroundColor(getResources().getColor(R.color.transparent));
                        enableRadioGroup(rgAudioSelect);
                        lvCameras.setEnabled(true);
                        stopLive();
                        isLive = false;
                    } else {
                        //开始直播
                        tvFunction.setText("停止直播");
                        transView.setBackgroundColor(getResources().getColor(R.color.transparent_40));
                        disableRadioGroup(rgAudioSelect);
                        lvCameras.setEnabled(false);
                        startLive();
                        isLive = true;
                    }
                }
                break;
            case R.id.tv_rtmp:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(RtmpUrlStr);
                logView.addNormalLog("已复制Rtmp链接至剪切板:\n" + RtmpUrlStr);
                break;
            case R.id.tv_hls:
                SvrcDialog.HlsImgDialog(AVDLiveActivity.this, HlsUrlStr);
                break;
            case R.id.tv_show_live:
                SvrcDialog.PlayLiveDialog(AVDLiveActivity.this, playHlsurl);
                break;
            default:
                break;
        }
    }

    /**
     * 开始直播
     */
    private void startLive() {
        String userId = mRoom.mvideo.getOwnerId(Constants.SELECT_CAMERA_ID);
        if (userId == null) {
            userId = "testuserId";
        }
        AVDLive.LiveInfo liveInfo = new AVDLive.LiveInfo();
        liveInfo.setAudioType(audioType);
        liveInfo.setVideoType(videoType);
        String liveName = mRoom.mvideo.getUserName(userId) + "_liv_" + String.valueOf(System.currentTimeMillis() / 1000);
        liveInfo.setName(liveName);
        liveInfo.setUserId(userId);
        liveInfo.setRoomId(roomId);
        //以下三个url可以不传，服务端会返回；如果需要pub到指定路径则三个url必须全部都设置
        liveInfo.setPublishurl(Constants.DEMO_PARAMS.getOption().getPublishurl().replace("$livename", mRoom.mvideo.getUserName(userId)));
        liveInfo.setHlsurl(Constants.DEMO_PARAMS.getOption().getHlsurl().replace("$livename", mRoom.mvideo.getUserName(userId)));
        liveInfo.setRtmphurl(Constants.DEMO_PARAMS.getOption().getRtmpurl().replace("$livename", mRoom.mvideo.getUserName(userId)));
        int ret = avdLive.createUserLive(liveInfo);
        if (ErrorCode.AVD_OK != ret) {
            logView.addVeryImportantLog("创建直播失败 ErrorCode:" + ret);
        }
    }

    /**
     * 停止直播
     */
    private void stopLive() {
        if (StringUtils.isNotEmpty(liveId)) {
            int ret = avdLive.deleteLive(liveId);
            if (ErrorCode.AVD_OK != ret) {
                logView.addVeryImportantLog("停止直播失败 ErrorCode:" + ret);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mRoom) {
            stopLive();
            mRoom.dispose();
            //退出房间时，已选中视频设为""
            Constants.SELECT_CAMERA_ID = "";
        }
        Log.i(TAG, "onDestory");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 退出
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && KeyEvent.KEYCODE_BACK == keyCode) {
            if (isLive) {
                SvrcDialog.finishDialog(this, "正在直播,是否直接退出？", new SvrcDialog.MCallBack() {
                    @Override
                    public boolean OnCallBackDispath(Boolean bSucceed) {
                        finish();
                        return false;
                    }
                });
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MVideo.Camera remote = mPublishedCameras.get(position);
        if (remote != null) {
            mvideoDeviceId = remote.getId();
            maudioUserId = mRoom.mvideo.getOwnerId(mvideoDeviceId);
            String selectOwner = mRoom.mvideo.getUserName(maudioUserId);
            logView.addNormalLog("您已选择" + selectOwner + "的视频");
            if (audioType == AVDRecord.RecordAudioType.ra_user_single) {
                logView.addNormalLog("您已选择" + selectOwner + "的音频");
            }
            //选中视频后，改变改栏底色
            Constants.SELECT_CAMERA_ID = mvideoDeviceId;
            videoType = AVDRecord.RecordVideoType.rv_main;
            cAdapter.notifyDataSetChanged();
        }
    }

    private boolean checkValid() {
        //已选视频
        if (StringUtils.isNotEmpty(mvideoDeviceId)) {
            return true;
        }
        //未选视频,选择了以下音频
        if (audioType == AVDRecord.RecordAudioType.ra_user_all) {
            return true;
        }
        Toast.makeText(SvrcApp.getContextObject(), "未选择视频的情况下,音频只可选择2、3选项", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 更新视频列表
     *
     * @param isCameraOpen
     * @param camera
     */
    public void updatePublishedCameras(boolean isCameraOpen, MVideo.Camera camera) {
        String ownerName = mRoom.mvideo.getOwnerName(camera.getId());
        if (isCameraOpen) {//房间加入摄像头,添加至列表
            //重新设置camera的setName（）方法 在摄像头设备的name前添加用户name，便于区分
            camera.setName(ownerName + ":" + camera.getName());
            //判断加入的摄像头是否已存在列表
            boolean isRepeat = mPublishedCameras.contains(camera);
            if (!isRepeat) {
                mPublishedCameras.add(camera);
                logView.addNormalLog(ownerName + "开启摄像头");
                cAdapter.notifyDataSetChanged();
            }
        } else {//房间有摄像头离开,从列表中移除列表
            for (int i = 0; i < mPublishedCameras.size(); i++) {
                if (mPublishedCameras.get(i).getId().equals(camera.getId())) {
                    mPublishedCameras.remove(i);
                    logView.addNormalLog(ownerName + "关闭摄像头");
                    cAdapter.notifyDataSetChanged();
                    if (Constants.SELECT_CAMERA_ID.equals(camera.getId())) {
                        Constants.SELECT_CAMERA_ID = "";
                    }
                }
            }
            if (mPublishedCameras == null || mPublishedCameras.size() == 0) {
                logView.addVeryImportantLog("请用幸会加入此房间并打开摄像头、选用H264编码，协助测试");
            }
        }
    }

    /********************************AVDLive.Listener************************************/
    @Override
    public void onCreateUserLive(int result, AVDLive.LiveInfo info) {
        liveId = info.getId();
        if (ErrorCode.AVD_OK != result) {
            logView.addVeryImportantLog("创建直播失败 ErrorCode:" + result);
        } else {
            logView.addImportantLog("开始直播");
            tvRtmpUrl.setVisibility(View.VISIBLE);
            tvHlsUrl.setVisibility(View.VISIBLE);
            tvShowLive.setVisibility(View.VISIBLE);
            RtmpUrlStr = "https://3tee.cn/admin/live/rtmpPlayer.html?src=" + info.getRtmpurl();
            HlsUrlStr = "https://3tee.cn//admin/live/qrcode.html?src=https://3tee.cn//admin/live/hlsPlayer.html?src=" + info.getHlsurl();

            playHlsurl = info.getHlsurl();
            SvrcDialog.PlayLiveDialog(AVDLiveActivity.this, info.getHlsurl());
        }
        Log.i(TAG, "onCreateUserLive: ret=" + result + "\nHlsurl:" + info.getHlsurl() +
                "\nPublishurl:" + info.getPublishurl() + "\nRtmpurl:" + info.getRtmpurl());
    }

    @Override
    public void onStopLive(int result, String liveId) {
        Log.i(TAG, "onStopLive: ret=" + result);
    }

    @Override
    public void onLiveInfo(int result, AVDLive.LiveInfo info) {
        Log.i(TAG, "onLiveInfo: ret=" + result);
    }

    @Override
    public void onLiveInfos(int result, int total, int begin, List<AVDLive.LiveInfo> items) {
        Log.i(TAG, "onLiveInfos: ret=" + result);
    }

    @Override
    public void onDeleteLive(int result, String liveId) {
        tvRtmpUrl.setVisibility(View.GONE);
        tvShowLive.setVisibility(View.GONE);
        tvHlsUrl.setVisibility(View.GONE);
        if (ErrorCode.AVD_OK != result) {
            logView.addVeryImportantLog("停止直播失败 ErrorCode:" + result);
        } else {
            logView.addImportantLog("停止直播");
        }

        Log.i(TAG, "onDeleteLive: ret=" + result);
    }
    /********************************AVDLive.Listener**********************************/

    /**
     * OnCheckedChangeListener
     *
     * @param group
     * @param checkedId rb_audio_no无音频;rb_audio_one仅所选中视频用户的音频;rb_audio_without_me房间内除自己外的音频;rb_audio_all房间所有音频
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_audio_no:
                audioType = AVDRecord.RecordAudioType.ra_none;
                logView.addNormalLog("您已选择不直播房间所有的音频");
                break;
            case R.id.rb_audio_one:
                audioType = AVDRecord.RecordAudioType.ra_user_single;
                if (StringUtils.isNotEmpty(maudioUserId)) {
                    String selectOwner = mRoom.mvideo.getUserName(maudioUserId);
                    logView.addNormalLog("您已选择" + selectOwner + "的音频");
                }
                break;
            case R.id.rb_audio_all:
                audioType = AVDRecord.RecordAudioType.ra_user_all;
                logView.addNormalLog("您已选择房间内所有用户的音频");
                break;
            default:
                break;
        }
    }


    /*********************设置RadioGroup是否可以点击**************************/
    public void disableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(false);
        }
    }

    public void enableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(true);
        }
    }
}
