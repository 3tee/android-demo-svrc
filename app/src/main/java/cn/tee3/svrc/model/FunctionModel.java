package cn.tee3.svrc.model;

import android.app.Activity;

/**
 * 功能model
 * Created by shengf on 2017/6/13.
 */

public class FunctionModel {
    //功能
    public static enum FunctionType {
        outgoing,//外呼
        live,//旁路直播
        record//录制
    }

    private FunctionType functionType;
    private String name;
    private String describe;
    private Activity mActivity;
    private String intentStr;
    private String appType;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getIntentStr() {
        return intentStr;
    }

    public void setIntentStr(String intentStr) {
        this.intentStr = intentStr;
    }

    public Activity getmActivity() {
        return mActivity;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

}
