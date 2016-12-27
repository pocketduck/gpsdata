package com.chexiao.gpsdata.enums;

/**
 * Created by fulei on 2016/12/23.
 */
public enum SignalEnum {

    normalPosition((byte)0x80,"一般位置数据");


    private byte code;
    private String desc;

    private SignalEnum(byte code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static String getDescByCode(Short code){
        for(SignalEnum refer : SignalEnum.values())
            if(code==refer.getCode())
                return refer.getDesc();
        return null;
    }

    public int getCode() {
        return code;
    }
    public void setCode(byte code) {
        this.code = code;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
