package cn.tee3.svrc.model;

/**
 * 接口返回rtsp的相关信息
 * Created by shengf on 2017/7/12.
 */

public class DemoOption {
    /********rtsp********/
    private String userAddress;
    private String login_name;
    private String login_password;
    //本地添加子码流路径
    private String sub_rtsp_uri;
    /********rtsp********/

    /*********live*********/
    private String publishurl;
    private String rtmpurl;
    private String hlsurl;

    /*********live*********/

    public String getPublishurl() {
        return publishurl;
    }

    public void setPublishurl(String publishurl) {
        this.publishurl = publishurl;
    }

    public String getRtmpurl() {
        return rtmpurl;
    }

    public void setRtmpurl(String rtmpurl) {
        this.rtmpurl = rtmpurl;
    }

    public String getHlsurl() {
        return hlsurl;
    }

    public void setHlsurl(String hlsurl) {
        this.hlsurl = hlsurl;
    }

    public String getSub_rtsp_uri() {
        return sub_rtsp_uri;
    }

    public void setSub_rtsp_uri(String sub_rtsp_uri) {
        this.sub_rtsp_uri = sub_rtsp_uri;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getLogin_name() {
        return login_name;
    }

    public void setLogin_name(String login_name) {
        this.login_name = login_name;
    }

    public String getLogin_password() {
        return login_password;
    }

    public void setLogin_password(String login_password) {
        this.login_password = login_password;
    }
}
