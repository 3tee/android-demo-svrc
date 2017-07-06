package cn.tee3.svrc.utils;

/**
 * 叁体服务信息配置
 *
 * @note 主要配置叁体服务器信息，appkey和secretkey
 * appkey和secretkey请开发者用叁体分配的相关信息。
 */

public class AppKey {
    // updated in archive script
    public static final String
            tee3_avd_server = "3tee.cn:8080";
//            tee3_avd_server = "192.168.1.241:8080";
//            tee3_avd_server = "nice2meet.cn:8080";
//    public static final String
//            tee3_app_key = "demo_access";
//    public static final String
//            tee3_secret_key = "demo_secret";

    //    public static final String
//            tee3_avd_server = "3tee.cn:8080";

//    public static final String
//            tee3_avd_server = "60.12.6.42:441";
    public static final String
            urlbase = "https://3tee.cn/";
    public static final String
            tee3_app_key = "F89EB5C71E494850A061CC0C5F42C177";
    public static final String
            tee3_secret_key = "DDDF7445961C4D27A7DCE106001BBB4F";

    //直连设备主码流
    public static final String
            main_rtsp_uri = "rtsp://192.168.1.121:554/h264/ch1/main/av_stream";
    //直连设备子码流
    public static final String
            sub_rtsp_uri = "rtsp://192.168.1.121:554/h264/ch1/sub/av_stream";

}
