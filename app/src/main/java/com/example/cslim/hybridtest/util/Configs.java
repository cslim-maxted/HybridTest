package com.example.cslim.hybridtest.util;

/**
 * Created by cslim on 2017. 11. 21..
 */

public class Configs {

    // HOST URL
    public static final String DEVELOP_HOST_URL = "https://m.naver.com";
    public static final String RELEASE_HOST_URL = "";

    // 개발, 운영 선택
    public static final Boolean isDevelop = true;

    // 메뉴 아이템 또는 네이티브 통신 SUB URL
    public final int MENU_ITEM_01 = 0;
    public final int MENU_ITEM_02 = 1;
    public final int MENU_ITEM_03 = 2;
    public final int MENU_ITEM_04 = 3;
    public final int MENU_ITEM_05 = 4;
    public final int MENU_ITEM_06 = 5;
    public final int MENU_ITEM_07 = 6;

    public final String url[] = {
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    // 패키지 리스트
    public static final String PACKAGE_KAKAO_TALK = "com.kakao.talk";
    public static final String PACKAGE_BAND = "com.naver.band";

    // URL Schemes
    public static final String URL_STARTWITH_KAKAO = "kakaolink:";
    public static final String URL_STARTWITH_MAIL = "mailto:";
    public static final String URL_STARTWITH_TEL = "tel:";
    public static final String URL_STARTWITH_SMS = "sms:";

    // chromium parse
    public static final String INTENT_PROTOCOL_START = "intent:";
    public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
    public static final String INTENT_PROTOCOL_END = ";end;";
    public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

}
