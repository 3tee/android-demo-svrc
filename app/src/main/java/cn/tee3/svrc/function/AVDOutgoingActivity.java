package cn.tee3.svrc.function;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tee3.avd.AVDOutgoing;
import cn.tee3.avd.ErrorCode;
import cn.tee3.avd.MVideo;
import cn.tee3.avd.Room;
import cn.tee3.avd.User;
import cn.tee3.avd.VideoRenderer;
import cn.tee3.svrc.Constants;
import cn.tee3.svrc.R;
import cn.tee3.svrc.adapter.OutgoingUserAdapter;
import cn.tee3.svrc.avroom.AVRoom;
import cn.tee3.svrc.utils.AppKey;
import cn.tee3.svrc.view.EventLogView;
import cn.tee3.svrc.utils.StringUtils;
import cn.tee3.svrc.view.SvrcDialog;

/**
 * 服务器外呼设备（rtsp/323）等
 * Created by shengf on 2017/6/20.
 */

public class AVDOutgoingActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AVDOutgoing.Listener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "AVDOutgoingActivity";
    private TextView tvTitle;
    private TextView tvOutgoing;
    private EventLogView logView;

    private AVDOutgoing avdOutgoing;
    private AVRoom mRoom;
    private VideoRenderer mLocalRender;
    private GLSurfaceView mViewLocal;
    private CheckBox cbAddSubStream;
    private ListView lvOutgoingUsers;

    private List<User> outgoingUsers = new ArrayList<User>();//外呼设备用户列表
    private OutgoingUserAdapter oAdapter;
    private String roomId;
    private boolean isImport = false;
    private String userId = "";//创建的外呼设备的userId

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avd_outgoing_layout);
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText("房间号：" + roomId);

        tvOutgoing = (TextView) findViewById(R.id.tv_outgoing);
        logView = (EventLogView) findViewById(R.id.event_view);
        mViewLocal = (GLSurfaceView) findViewById(R.id.gl_local);
        cbAddSubStream = (CheckBox) findViewById(R.id.cb_add_sub_stream);
        lvOutgoingUsers = (ListView) findViewById(R.id.lv_outgoing_users);

        tvOutgoing.setOnClickListener(this);
        cbAddSubStream.setOnCheckedChangeListener(this);
        lvOutgoingUsers.setOnItemClickListener(this);

        oAdapter = new OutgoingUserAdapter(this, outgoingUsers);
        lvOutgoingUsers.setAdapter(oAdapter);
        oAdapter.notifyDataSetChanged();

        mLocalRender = new VideoRenderer(mViewLocal);
        //服务器外呼设备
        avdOutgoing = AVDOutgoing.instance();
        //设置回调
        avdOutgoing.setListener(this);

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
        int ret = mRoom.join(StringUtils.getUUID(), "androidUser" + (int) (Math.random() * 100000000), new Room.JoinResultListener() {
            @Override
            public void onJoinResult(int result) {
                if (ErrorCode.AVD_OK != result) {
                    check_ret(result);
                    return;
                }
            }
        });
        //用户加入或者离开房间回调
        mRoom.setUserListener(new AVRoom.UserListener() {
            @Override
            public void UserEvent(boolean JoinOrLeave, User user) {
                if (StringUtils.isNotEmpty(user.getUserData())) {
                    if ("outgoing".equals(user.getUserData())) {//外呼用户
                        if (JoinOrLeave) {
                            outgoingUsers.add(user);
                            oAdapter.notifyDataSetChanged();
                        } else {
                            outgoingUsers.remove(user);
                            oAdapter.notifyDataSetChanged();
                        }
                    }
                }
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
        logView.addVeryImportantLog("请用幸会加入此房间协助测试");
        return true;
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
            logView.addNormalLog(ownerName + "开启摄像头");
        } else {//房间有摄像头离开,从列表中移除列表
            if (StringUtils.isNotEmpty(ownerName)) {
                logView.addNormalLog(ownerName + "摄像头已关闭");
            } else {
                logView.addNormalLog(ownerName + "外呼摄像头已关闭");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_outgoing:
                if (isImport) {
                    isImport = false;
                    tvOutgoing.setText("开始外呼");
                    logView.addImportantLog("停止外呼设备");
                    cbAddSubStream.setClickable(true);
                    stopOutgoing();
                } else {
                    isImport = true;
                    tvOutgoing.setText("停止外呼");
                    logView.addImportantLog("开始外呼设备");
                    cbAddSubStream.setClickable(false);
                    startOutgoing();
                }
                break;
        }
    }

    /**
     * 展示外呼设备的视频
     *
     * @param userId
     */
    public void showOutgoing(String userId) {
        List<MVideo.Camera> list = mRoom.mvideo.getRemoteCameras(userId);
        if (!mRoom.mvideo.isCameraSubscribed(list.get(0).getId())) {//如果不是已订阅的
            mRoom.mvideo.unsubscribe(Constants.SELECT_OUTGOING_DEVICE_ID);//先取消订阅
            mRoom.mvideo.subscribe(list.get(0).getId());
            mRoom.mvideo.attachRender(list.get(0).getId(), mLocalRender);
            logView.addNormalLog("已选择" + mRoom.mvideo.getUserName(userId) + "的外呼设备");
        }
        Constants.SELECT_OUTGOING_USER_ID = userId;
        Constants.SELECT_OUTGOING_DEVICE_ID = list.get(0).getId();
        oAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showOutgoing(outgoingUsers.get(position).getUserId());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            logView.addNormalLog("已选择同时导入设备子码流");
        } else {
            logView.addNormalLog("已取消同时导入设备子码流");
        }
    }

    /**
     * 开始外呼
     */
    private void startOutgoing() {
        //创建外呼设备
        userId = "og" + (int) (Math.random() * 100000000);//8位的随机数的随机数
        String userName = setOutgoingUserName(Constants.DEMO_PARAMS.getOption().getUserAddress());
        User user = new User(userId, userName, "outgoing");
        int ret;
        if (cbAddSubStream.isChecked()) {//是否导入子码流
            ret = avdOutgoing.createOutgoingUser(roomId, user, Constants.DEMO_PARAMS.getOption().getUserAddress(), Constants.DEMO_PARAMS.getOption().getLogin_name(), Constants.DEMO_PARAMS.getOption().getLogin_password(), Constants.DEMO_PARAMS.getOption().getSub_rtsp_uri());
        } else {
            ret = avdOutgoing.createOutgoingUser(roomId, user, Constants.DEMO_PARAMS.getOption().getUserAddress(), Constants.DEMO_PARAMS.getOption().getLogin_name(), Constants.DEMO_PARAMS.getOption().getLogin_password(), "");
        }
        if (ret != 0) {
            Log.e(TAG, "createOutgoingUser ret:" + ret);
            logView.addVeryImportantLog("创建外呼设备失败 ErrorCode:" + ret);
        }
    }

    /**
     * 停止外呼
     */
    private void stopOutgoing() {
        //去除外呼设备
        int ret = avdOutgoing.destoryOutgoingUser(roomId, userId, Constants.DEMO_PARAMS.getOption().getUserAddress());
        if (ret != 0) {
            Log.e(TAG, "destoryOutgoingUser ret:" + ret);
            logView.addVeryImportantLog("去除外呼设备失败 ErrorCode:" + ret);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopOutgoing();
        if (null != mRoom) {
            mRoom.dispose();
            Log.i(TAG, "onDestory");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 退出
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && KeyEvent.KEYCODE_BACK == keyCode) {
            if (isImport) {
                SvrcDialog.finishDialog(this, "正在外呼,是否直接退出？", new SvrcDialog.MCallBack() {
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

    /************************************************** AVDOutgoing.Listener********************************************************/

    //创建外呼设备返回
    @Override
    public void onCreateOutgoingUser(int result, String roomId, String userid, String user_address) {
        Log.i(TAG, "onCreateOutgoingUser result:" + result + "roomId:" + roomId + "userid:" + userid + "user_address:" + user_address);

        if (result != ErrorCode.AVD_OK) {
            Log.e(TAG, "onCreateOutgoingUser ret:" + result);
            logView.addVeryImportantLog("创建外呼设备失败 ErrorCode:" + result);
        } else {
            showOutgoing(userid);
        }
    }

    //取消外呼设备返回
    @Override
    public void onDestoryOutgoingUser(int result, String roomId, String userid, String user_address) {
        Log.i(TAG, "onDestoryOutgoingUser result:" + result + "roomId:" + roomId + "userid:" + userid + "user_address:" + user_address);
        if (result != ErrorCode.AVD_OK) {
            Log.e(TAG, "onDestoryOutgoingUser ret:" + result);
            logView.addVeryImportantLog("去除外呼设备失败 ErrorCode:" + result);
        }
    }

    //获取外呼设备列表返回
    @Override
    public void onGetOutgoingUsers(int result, String roomId, List<User> users) {
        Log.i(TAG, "onGetOutgoingUsers result:" + result + "roomId:" + roomId + "users:" + users.size());
    }

    /************************************************** AVDOutgoing.Listener********************************************************/


    /**
     * 获取外呼设备的名称
     *
     * @param main_rtsp_uri
     * @return
     */
    public String setOutgoingUserName(String main_rtsp_uri) {
        //rtsp://192.168.1.121:554/h264/ch1/main/av_stream;
        String userName = "";
        String rtsp_uri = main_rtsp_uri.replace("://", "_");
        int indexEnd = rtsp_uri.indexOf("/");
        userName = userName + rtsp_uri.substring(0, indexEnd);
        return userName;
    }
}
