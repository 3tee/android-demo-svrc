package cn.tee3.svrc.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import cn.tee3.avd.AVDEngine;
import cn.tee3.avd.RoomInfo;
import cn.tee3.svrc.Constants;
import cn.tee3.svrc.R;
import cn.tee3.svrc.SvrcApp;
import cn.tee3.svrc.function.AVDLiveActivity;
import cn.tee3.svrc.function.AVDOutgoingActivity;
import cn.tee3.svrc.function.AVDRecordActivity;
import cn.tee3.svrc.model.FunctionModel;
import cn.tee3.svrc.utils.StringUtils;
import cn.tee3.svrc.view.SvrcDialog;


/**
 * 主页
 * <p>
 * Created by shengf on 2017/6/20.
 */
public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, SvrcApp.RoomListener {
    private static final String TAG = "MainActivity";

    private TextView tvExplain;//模块说明
    private TextView tvJoinroom;//加入房间
    private ImageView ivScheduleRoom;//安排房间
    private EditText etRoomid;//房间Id
    private ListView lvFunctions;//功能列表
    private FunctionAdapter fAdapter;

    private ArrayList<FunctionModel> functionModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initFunction();
        initView();
        initData();
        //设置房间查询、安排回调
        ((SvrcApp) getApplication()).setRoomListener(this);
    }

    //房间查询、安排回调
    @Override
    public void RoomEvent(SvrcApp.RoomEventType roomEventType, int ret, String roomIdResult) {
        if (roomEventType == SvrcApp.RoomEventType.GetRoomResult) {//查询房间返回
            if (ret == 0) {
                Intent intent = new Intent(MainActivity.this, Constants.SELECT_FUNCTION.getmActivity().getClass());
                intent.putExtra("roomId", roomIdResult);
                startActivity(intent);
            } else {
                SvrcDialog.ScheduleRoomDialog(this);
                Log.e(TAG, "GetRoomResult failed:" + ret);
            }
        } else {//安排房间返回
            if (ret == 0) {
                Intent intent = new Intent(MainActivity.this, Constants.SELECT_FUNCTION.getmActivity().getClass());
                intent.putExtra("roomId", roomIdResult);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "安排房间失败 ErrorCode:" + ret, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "ScheduleRoomResult failed:" + ret);
            }
        }
    }


    @Override
    public void onClick(View v) {
        String roomId = "";
        roomId = etRoomid.getText().toString().trim();
        if (StringUtils.isEmpty(roomId)) {
            roomId = "r2";
        }
        switch (v.getId()) {
            case R.id.tv_joinroom:
                //查询房间是否存在
                AVDEngine.instance().getRoomByRoomId(roomId);
                break;
            case R.id.iv_schedule_room:
                //安排房间,此处传uuid还是传roomId，可以根据服务端的配置来决定
                RoomInfo roomInfo = new RoomInfo(UUID.randomUUID().toString(), RoomInfo.RoomMode_mcu, 5);
//                RoomInfo roomInfo = new RoomInfo(roomId, RoomInfo.RoomMode_mcu, 5);
                AVDEngine.instance().scheduleRoom(roomInfo);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FunctionModel functionModel = functionModels.get(position);
        tvExplain.setText(functionModel.getDescribe());
        tvJoinroom.setText(functionModel.getIntentStr());
        Constants.SELECT_FUNCTION = functionModel;
        fAdapter.notifyDataSetChanged();
    }

    public void initView() {
        etRoomid = (EditText) findViewById(R.id.et_roomid);
        ivScheduleRoom = (ImageView) findViewById(R.id.iv_schedule_room);
        tvExplain = (TextView) findViewById(R.id.tv_explain);
        tvJoinroom = (TextView) findViewById(R.id.tv_joinroom);
        lvFunctions = (ListView) findViewById(R.id.lv_functions);
        lvFunctions.setOnItemClickListener(this);
        fAdapter = new FunctionAdapter(this, functionModels);
        lvFunctions.setAdapter(fAdapter);
        fAdapter.notifyDataSetChanged();
        etRoomid.setVisibility(View.VISIBLE);
        ivScheduleRoom.setVisibility(View.VISIBLE);
        ivScheduleRoom.setOnClickListener(this);
        tvJoinroom.setOnClickListener(this);
    }

    private void initFunction() {
        functionModels.clear();
        for (int i = 0; i < 3; i++) {
            FunctionModel functionModel = new FunctionModel();
            switch (i) {
                case 0:
                    AVDOutgoingActivity avdOutgoingActivity = new AVDOutgoingActivity();
                    functionModel.setFunctionType(FunctionModel.FunctionType.outgoing);
                    functionModel.setName(getResources().getString(R.string.AVD_outgoing));
                    functionModel.setDescribe(getResources().getString(R.string.AVD_outgoing_des));
                    functionModel.setmActivity(avdOutgoingActivity);
                    functionModel.setIntentStr(getResources().getString(R.string.AVD_outgoing_intent_str));
                    break;
                case 1:
                    AVDLiveActivity avdLiveActivity = new AVDLiveActivity();
                    functionModel.setFunctionType(FunctionModel.FunctionType.live);
                    functionModel.setName(getResources().getString(R.string.AVD_live));
                    functionModel.setDescribe(getResources().getString(R.string.AVD_live_des));
                    functionModel.setmActivity(avdLiveActivity);
                    functionModel.setIntentStr(getResources().getString(R.string.AVD_live_intent_str));
                    break;
                case 2:
                    AVDRecordActivity avdRecordActivity = new AVDRecordActivity();
                    functionModel.setFunctionType(FunctionModel.FunctionType.record);
                    functionModel.setName(getResources().getString(R.string.AVD_record));
                    functionModel.setDescribe(getResources().getString(R.string.AVD_record_des));
                    functionModel.setmActivity(avdRecordActivity);
                    functionModel.setIntentStr(getResources().getString(R.string.AVD_record_intent_str));
                    break;
                default:
                    break;
            }
            functionModels.add(functionModel);
        }
        //默认选中第一个功能
        Constants.SELECT_FUNCTION = functionModels.get(0);
    }

    private void initData() {
        tvExplain.setText(functionModels.get(0).getDescribe());
        tvJoinroom.setText(functionModels.get(0).getIntentStr());
    }
}
