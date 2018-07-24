
import oracle.jrockit.jfr.jdkevents.ThrowableTracer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class Main {
    private static Thread thread;
    public static void main(String[] args)throws Exception {
       // PushMsg.getInstance("f");
      // System.out.println(ControlDevice.lockDoorWithWifi());
       // start();
       // main("192.168.1.6",8080);
      //  System.out.println(new Date(1530699829511l).toString());
       // System.out.println(new Date().toString());
       // FilerUtils.getFilerUtils("log3").writeStringWithTimeStamp("heoll");
        ControlDevice.closeLight1();
    }
    public static void main(String url,int port) {
        // TODO Auto-generated method stub

        Socket sk=null;
        try {
            sk=new Socket(InetAddress.getByName(url),
                   port);
           //输出信息流

            OutputStream ops=sk.getOutputStream();
            PrintWriter pw=new PrintWriter(ops,true);
            InputStream ips=sk.getInputStream();
            //从服务器读取信息的包装类
            BufferedReader bfr=new BufferedReader(
                    new InputStreamReader(ips));

            //从键盘接收信息
            BufferedReader keyBoard=new BufferedReader(
                    new InputStreamReader(System.in));
                 try {
                        while(true)
                       {
                            String strWord =keyBoard.readLine();
                            long start=System.currentTimeMillis();
                            //消息发送到服务器端

                            pw.println(strWord);
                            //pw.println("0");
                     pw.flush();
                     ops.close();

                     String str=bfr.readLine();
                            if(strWord.equalsIgnoreCase("quit"))
                            {
                                System.out.println("客户端退出！");
                                break;
                            }

                            System.out.println("收到:"+str+" 耗时:"+(System.currentTimeMillis()-start));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



            //pw.close();
            //bfr.close();
            //keyBoard.close();
            //sk.close();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static JSONObject connectServerSocketPush(String user,String wifi)throws Exception{
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("user",user);
            jsonObject.put("wifi",wifi);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String result= connectServerSocket(jsonObject.toString(),55538);
        return new JSONObject(result);
    }
    public static String connectServerSocket(String message,int port) throws Exception {
        System.out.println("---------------------------------------------------------------------------------");
        // 要连接的服务端IP地址和端口
        String host = "192.168.1.2";

        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        // 建立连接后获得输出流
        OutputStream outputStream = socket.getOutputStream();


        socket.getOutputStream().write(message.getBytes("UTF-8"));
        socket.shutdownOutput();

        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(bytes)) != -1) {
            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
            sb.append(new String(bytes, 0, len,"UTF-8"));
        }
        System.out.println("from server: " + sb);
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        }catch (Exception e){

        }
        return sb.toString();
    }
    public static void start()throws Exception {
        LogFM.getInstance(DeviceStateCheck.class).stopFlag=true;
        LogFM.getInstance(DeviceTask.class).stopFlag=true;
        LogFM.getInstance(RF24.class).stopFlag = true;
        LogFM.getInstance(WifiConnect.class).stopFlag=true;
        LogFM.getInstance(PushMsg.class).stopFlag = true;
        LogFM.getInstance(DevicesInfo.class).stopFlag = true;
        LogFM.getInstance(ControlDevice.class).stopFlag = true;
        LogFM.getInstance(HcsRF24Jni3.class).stopFlag = true;
        LogFM.getInstance(SocketClient.class).stopFlag=true;
        LogFM.getInstance(SocketServer.class).stopFlag=true;
        LogFM.getInstance(SocketClientPush.class).stopFlag=true;
        LogFM.getInstance(SocketServerPush.class).stopFlag=true;
        DeviceManageCenter deviceManageCenter=new DeviceManageCenter();
        Thread t=new Thread(deviceManageCenter);
        t.start();
        //System.out.println(new SocketClient(null).parseTask("f","lightControl","lookDht11"));
        //ControlDevice.query();

    }
    public static void t(long wait)throws Exception{


        for (int i = 0; i <50 ; i++) {
            //test();
            DeviceTask.getInstance().queryDoorDeviceTask();
            Thread.sleep(500);

        }
        System.err.println("-------------------------------");
        System.err.println("-------------------------------"+wait);
        RF24.getInstance().printInfo();
        System.err.println("-------------------------------"+wait);
        RF24.getInstance().setDoorRfNum(0);
        RF24.getInstance().setDoorWrong(0);
    }
    private static ArrayList<Long> suoMap=new ArrayList<>();
    private static ArrayList<Long> kaiMap=new ArrayList<>();
    public static void test()throws Exception{
        long start=System.currentTimeMillis();
        DeviceTask.getInstance().unlockDoorDeviceTask();
        long t=System.currentTimeMillis()-start;
        kaiMap.add(t);
        Thread.sleep(1000);
        start=System.currentTimeMillis();
        DeviceTask.getInstance().lockDoorDeviceTask();
        t=System.currentTimeMillis()-start;
        suoMap.add(t);
        Thread.sleep(1000);


    }
    public static void main2()throws Exception{
        LogFM.getInstance(RF24.class).stopFlag=true;
        LogFM.getInstance(PushMsg.class).stopFlag=true;
        DeviceManageCenter deviceManageCenter=new DeviceManageCenter();
        Thread t=new Thread(deviceManageCenter);
        t.start();
        /*
       Thread.sleep(8000);
        System.out.println("----------------------------------");
        long start=System.currentTimeMillis();
        DeviceTask.getInstance().unlockDoorDeviceTask();
        System.out.println("开锁耗时"+(System.currentTimeMillis()-start));
        */
    }


    public static void m(String msg) throws Exception {
        System.out.println("---------------------------------------------------------------------------------");
        // 要连接的服务端IP地址和端口
        String host = "192.168.1.15";
        int port = 8080;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        OutputStream outputStream = socket.getOutputStream();
        // 建立连接后获得输出流
        new Thread(){
            @Override
            public void run() {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int len;
                    StringBuilder sb = new StringBuilder();
                    while ((len = inputStream.read(bytes)) != -1) {
                        //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                        sb.append(new String(bytes, 0, len));
                        //sb.append(new String(bytes, 0, len,"UTF-8"));
                    }
                    System.out.println("from server: " + sb);

                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("device","door");
        jsonObject.put("task",msg);
        jsonObject.put("taskType","resetRF24");
       jsonObject.put("user","f");
       // String message=jsonObject.toString();
        String message="nihao";
        socket.getOutputStream().write("nihao".getBytes());
        //socket.getOutputStream().write(message.getBytes("UTF-8"));
        socket.getOutputStream().flush();

         InputStream inputStream = socket.getInputStream();

        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(bytes)) != -1) {
            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
            sb.append(new String(bytes, 0, len));
            //sb.append(new String(bytes, 0, len,"UTF-8"));
        }
        System.out.println("from server: " + sb);

        inputStream.close();
        outputStream.close();
        socket.close();
    }
    public static void m2(String msg) throws Exception {
        System.out.println("---------------------------------------------------------------------------------");
        // 要连接的服务端IP地址和端口
        String host = "192.168.1.15";
        int port = 8080;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        // 建立连接后获得输出流
        OutputStream outputStream = socket.getOutputStream();


        socket.getOutputStream().write(msg.getBytes("UTF-8"));
        socket.shutdownOutput();

        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(bytes)) != -1) {
            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
            sb.append(new String(bytes, 0, len,"UTF-8"));
        }
        System.out.println("from server: " + sb);

        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
