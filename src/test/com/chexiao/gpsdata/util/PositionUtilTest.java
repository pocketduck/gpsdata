package com.chexiao.gpsdata.util;

import com.chexiao.gpsdata.entity.gps.Gps;
import org.junit.Test;

/**
 * Created by fulei on 2016/12/28.
 */
public class PositionUtilTest {

    @Test
    public void testConvert () {

        // 北斗芯片获取的经纬度为WGS84地理坐标 31.426896,119.496145
//        Gps gps = new Gps(31.426896, 119.496145);
        //
//        Gps gps = new Gps(30.5894, 104.054283);


        Gps gps = new Gps(40.0432, 116.4078);

        System.out.println("gps :" + gps);
        Gps gcj = PositionUtil.gps84_To_Gcj02(gps.getWgLat(), gps.getWgLon());
        System.out.println("gcj :" + gcj);
        Gps star = PositionUtil.gcj_To_Gps84(gcj.getWgLat(), gcj.getWgLon());
        System.out.println("star:" + star);
        Gps bd = PositionUtil.gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
        System.out.println("bd  :" + bd);
        Gps gcj2 = PositionUtil.bd09_To_Gcj02(bd.getWgLat(), bd.getWgLon());
        System.out.println("gcj :" + gcj2);
    }
}
