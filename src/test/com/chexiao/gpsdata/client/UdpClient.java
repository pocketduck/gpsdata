package com.chexiao.gpsdata.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by fulei on 2016/12/22.
 */
public class UdpClient {
    private static final int TIMEOUT = 5000;  //设置接收数据的超时时间
    private static final int MAXNUM = 5;      //设置重发数据的最多次数
    public static void main(String args[])throws IOException{
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


        byte[] buf = new byte[1024];
        //客户端在9000端口监听接收到的数据
        DatagramSocket ds = new DatagramSocket(32100);
//        InetAddress localAddress = InetAddress.getLocalHost();
        byte ip[] = new byte[] { 59, 110, 28 , (byte)138};
        InetAddress address1 = InetAddress.getByAddress(ip);
        //定义用来发送数据的DatagramPacket实例
        DatagramPacket dp_send= new DatagramPacket(positionNomal,positionNomal.length,address1,32100);
        //定义用来接收数据的DatagramPacket实例
        DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
        //数据发向本地3000端口
        ds.setSoTimeout(TIMEOUT);              //设置接收数据时阻塞的最长时间
        int tries = 0;                         //重发数据的次数
        boolean receivedResponse = false;     //是否接收到数据的标志位
        //直到接收到数据，或者重发次数达到预定值，则退出循环
//        while(!receivedResponse && tries<MAXNUM){
            //发送数据
            ds.send(dp_send);
            try{
                //接收从服务端发送回来的数据
                ds.receive(dp_receive);
                //如果接收到的数据不是来自目标地址，则抛出异常
                if(!dp_receive.getAddress().equals(address1)){
                    throw new IOException("Received packet from an umknown source");
                }
                //如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                receivedResponse = true;
            }catch(InterruptedIOException e){
                //如果接收数据时阻塞超时，重发并减少一次重发的次数
                tries += 1;
                System.out.println("Time out," + (MAXNUM - tries) + " more tries..." );
            }
//        }
        if(receivedResponse){
            //如果收到数据，则打印出来
            System.out.println("client received data from server：");
            String str_receive = new String(dp_receive.getData(),0,dp_receive.getLength()) +
                    " from " + dp_receive.getAddress().getHostAddress() + ":" + dp_receive.getPort();
            System.out.println(str_receive);
            //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
            //所以这里要将dp_receive的内部消息长度重新置为1024
            dp_receive.setLength(1024);
        }else{
            //如果重发MAXNUM次数据后，仍未获得服务器发送回来的数据，则打印如下信息
            System.out.println("No response -- give up.");
        }
        ds.close();
    }

}
