package com.chexiao.gpsdata.client;

import com.chexiao.gpsdata.util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by fulei on 2016/12/20.
 */
public class TcpClient {

    /**
     * @param args
     * @throws IOException
    　　  */
    public static void main(String[] args) throws IOException {

        // 如果有三个从参数那么就获取发送信息的端口号，默认端口号为8099

//        Socket socket = new Socket("59.110.28.138", 32121);
        Socket socket = new Socket("localhost", 1111);
        System.out.println("Connected to server...sending echo string");
        // 返回此套接字的输入流，即从服务器接受的数据对象
        InputStream in = socket.getInputStream();
        // 返回此套接字的输出流，即向服务器发送的数据对象
        OutputStream out = socket.getOutputStream();
        // 向服务器发送从控制台接收的数据


        byte[] data = getDate();

        for (byte d : data) {
            System.out.print(ByteUtil.byteHexToInt(d) + " ");
        }

        for (byte d : data) {
            System.out.print(ByteUtil.byteHexToString(d) + " ");
        }
        out.write(getDate());
        // 接收数据的计数器，将写入数据的初始偏移量

        // 关闭连接
        socket.close();

    }

    public static byte[] getDate() {
        byte[] positionNomal = new byte[89]  ;//位置数据 34字节

        positionNomal[0] = (byte) 0x29;
        positionNomal[1] = (byte) 0x29;
        positionNomal[2] = (byte) 0x80;
        positionNomal[3] = (byte) 0x00;
        positionNomal[4] = (byte) 0x54;
        positionNomal[5] = (byte) 0xbd;
        positionNomal[6] = (byte) 0xbd;
        positionNomal[7] = (byte) 0x01;
        positionNomal[8] = (byte) 0x5b;
        positionNomal[9] = (byte) 0x16;
        positionNomal[10] = (byte) 0x12;
        positionNomal[11] = (byte) 0x22;
        positionNomal[12] = (byte) 0x19;
        positionNomal[13] = (byte) 0x26;
        positionNomal[14] = (byte) 0x01;

        //03 03 53 64 10 40 32 57 原始数据及位置2.docx
//        positionNomal[15] = (byte) 0x03;
//        positionNomal[16] = (byte) 0x03;
//        positionNomal[17] = (byte) 0x53;
//        positionNomal[18] = (byte) 0x64;
//        positionNomal[19] = (byte) 0x10;
//        positionNomal[20] = (byte) 0x40;
//        positionNomal[21] = (byte) 0x32;
//        positionNomal[22] = (byte) 0x57;

        //  03 02 97 72 10 40 46 62 杨凌家
        positionNomal[15] = (byte) 0x03;
        positionNomal[16] = (byte) 0x02;
        positionNomal[17] = (byte) 0x97;
        positionNomal[18] = (byte) 0x72;
        positionNomal[19] = (byte) 0x10;
        positionNomal[20] = (byte) 0x40;
        positionNomal[21] = (byte) 0x46;
        positionNomal[22] = (byte) 0x62;


        positionNomal[23] = (byte) 0x00;
        positionNomal[24] = (byte) 0x00;
        positionNomal[25] = (byte) 0x00;
        positionNomal[26] = (byte) 0x00;
        positionNomal[27] = (byte) 0x78;
        positionNomal[28] = (byte) 0x00;
        positionNomal[29] = (byte) 0x01;
        positionNomal[30] = (byte) 0x61;
        positionNomal[31] = (byte) 0x7f;
        positionNomal[32] = (byte) 0xfc;
        positionNomal[33] = (byte) 0x5d;
        positionNomal[34] = (byte) 0x00;
        positionNomal[35] = (byte) 0x00;
        positionNomal[36] = (byte) 0x1e;
        positionNomal[37] = (byte) 0x00;
        positionNomal[38] = (byte) 0x00;
        positionNomal[39] = (byte) 0x00;
        positionNomal[40] = (byte) 0x88;
        positionNomal[41] = (byte) 0x00;
        positionNomal[42] = (byte) 0x00;
        positionNomal[43] = (byte) 0x00;
        positionNomal[44] = (byte) 0x12;
        positionNomal[45] = (byte) 0x00;
        positionNomal[46] = (byte) 0x24;
        positionNomal[47] = (byte) 0x34;
        positionNomal[48] = (byte) 0x36;
        positionNomal[49] = (byte) 0x30;
        positionNomal[50] = (byte) 0x3b;
        positionNomal[51] = (byte) 0x30;
        positionNomal[52] = (byte) 0x30;
        positionNomal[53] = (byte) 0x3b;
        positionNomal[54] = (byte) 0x38;
        positionNomal[55] = (byte) 0x32;
        positionNomal[56] = (byte) 0x31;
        positionNomal[57] = (byte) 0x44;
        positionNomal[58] = (byte) 0x3b;
        positionNomal[59] = (byte) 0x30;
        positionNomal[60] = (byte) 0x32;
        positionNomal[61] = (byte) 0x38;
        positionNomal[62] = (byte) 0x30;
        positionNomal[63] = (byte) 0x00;
        positionNomal[64] = (byte) 0x06;
        positionNomal[65] = (byte) 0x00;
        positionNomal[66] = (byte) 0x88;
        positionNomal[67] = (byte) 0x00;
        positionNomal[68] = (byte) 0x00;
        positionNomal[69] = (byte) 0x01;
        positionNomal[70] = (byte) 0x61;
        positionNomal[71] = (byte) 0x00;
        positionNomal[72] = (byte) 0x06;
        positionNomal[73] = (byte) 0x00;
        positionNomal[74] = (byte) 0x89;
        positionNomal[75] = (byte) 0xff;
        positionNomal[76] = (byte) 0xff;
        positionNomal[77] = (byte) 0xff;
        positionNomal[78] = (byte) 0xfd;
        positionNomal[79] = (byte) 0x00;
        positionNomal[80] = (byte) 0x06;
        positionNomal[81] = (byte) 0x00;
        positionNomal[82] = (byte) 0xa5;
        positionNomal[83] = (byte) 0x00;
        positionNomal[84] = (byte) 0x00;
        positionNomal[85] = (byte) 0x00;
        positionNomal[86] = (byte) 0x03;
        positionNomal[87] = (byte) 0xea;
        positionNomal[88] = (byte) 0x0d;

        return positionNomal;
    }


}
