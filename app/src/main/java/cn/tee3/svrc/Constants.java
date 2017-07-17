package cn.tee3.svrc;

import cn.tee3.svrc.model.DemoParams;
import cn.tee3.svrc.model.FunctionModel;

/**
 * Created by shengf on 2017/6/7.
 */
public class Constants {
    public static FunctionModel SELECT_FUNCTION;//已选中功能
    public static String SELECT_CAMERA_ID = "";//已选中摄像头设备
    public static String SELECT_OUTGOING_USER_ID = "";//已选外呼设备的用户Id
    public static String SELECT_OUTGOING_DEVICE_ID = "";//已选外呼设备的设备Id

    public static String APP_TYPE="";//上一次请求的appType
    public static DemoParams DEMO_PARAMS=new DemoParams();
}
