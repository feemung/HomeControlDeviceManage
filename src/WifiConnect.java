import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by feemung on 18/7/1.
 */
public class WifiConnect extends ConnectDeviceBase{
    private static WifiConnect instance;
    public static WifiConnect getInstance() {
        if (instance == null) {
            instance=new WifiConnect();
        }
        return instance;

    }

    private  String doorControlUrl="192.168.1.6";
    private  int doorControlPort=8080;
    private PrintWriter pw=null;
    private boolean normal=false;
    private BufferedReader bfr=null;
    private LogFM logFM=LogFM.getInstance(WifiConnect.class);
    private int doorControlWrong=0;
    private int doorControlMsgWrong=0;
    private int doorControlRfNum=0;
    private boolean doorControlConnectFailed=true;
    private boolean lightControlConnectFailed=true;
    private int lightControlWrong=0;
    private int lightControlMsgWrong=0;
    private int lightControlRfNum=0;
    private Lock lock=new ReentrantLock();
    public  String connect(String msg){
        // TODO Auto-generated method stub
        String receive=null;
        Socket sk = null;
        long start = System.currentTimeMillis();
        try {
            sk = new Socket();

            sk.connect(new InetSocketAddress(doorControlUrl, doorControlPort), 3000);
            sk.setSoTimeout(3000);
            InputStream ips = sk.getInputStream();
            OutputStream ops = sk.getOutputStream();
            //从服务器读取信息的包装类
            BufferedReader bfr = new BufferedReader(
                    new InputStreamReader(ips));
            //输出信息流
            PrintWriter pw = new PrintWriter(ops, true);
            //消息发送到服务器端
            pw.write(msg);
            pw.flush();
            char c[]=new char[1024];
            int len=bfr.read(c);


            char newC[]=new char[len];
            for (int i = 0; i <len ; i++) {
                newC[i]=c[i];
            }
            receive=new String(newC);
           logFM.d("收到:" + receive + " 耗时:" + (System.currentTimeMillis() - start));


            pw.close();
            bfr.close();
            //keyBoard.close();
            sk.close();
        } catch (Exception e) {
            logFM.e(e);
            receive="err";

        }
        return receive;
    }

    @Override
    public String connect(int client, long msg) {
        logFM.d(client+" "+msg);

        lock.lock();
        String receive="err";
        receive=String.valueOf(sendClient(client,String.valueOf(msg)));



        lock.unlock();
        logFM.d("end");
        return receive;
    }
    public String sendClient(int client,String msg){
        for (int i = 0; i < 3; i++) {

            doorControlRfNum++;
            String rec="";
            if(client==RF24.LightControlDevice) {
                rec = connect("lightControl>"+msg);
               
            }

            if(rec.contains("err")){


                doorControlWrong++;

            }else {

                setDoorControlConnectFailed(false);

                return rec;
            }
            if(i==3-1){


                setDoorControlConnectFailed(true);


            }
            try {
                Thread.sleep(5000);
            }catch (Exception e){

            }
        }

        return "err";
    }
    public int getLightControlWrong() {
        return lightControlWrong;
    }

    public void setLightControlWrong(int lightControlWrong) {
        this.lightControlWrong = lightControlWrong;
    }

    public int getLightControlMsgWrong() {
        return lightControlMsgWrong;
    }

    public void setLightControlMsgWrong(int lightControlMsgWrong) {
        this.lightControlMsgWrong = lightControlMsgWrong;
    }

    public int getLightControlRfNum() {
        return lightControlRfNum;
    }

    public void setLightControlRfNum(int lightControlRfNum) {
        this.lightControlRfNum = lightControlRfNum;
    }

    public boolean isDoorControlConnectFailed() {
        return doorControlConnectFailed;
    }

    public void setDoorControlConnectFailed(boolean doorControlConnectFailed) {
        if(this.doorControlConnectFailed!=doorControlConnectFailed){
            if(doorControlConnectFailed){

                PushMsg.update(System.currentTimeMillis(),"DoorControl","none","door_contact_failed","连接门控制器失败",false,2);
            }else {

                PushMsg.update(System.currentTimeMillis(),"DoorControl","none",
                        ServerErr.DoorControl_contact_normal.getName(),"连接门控制器恢复正常",false,2);
            }
        }
        this.doorControlConnectFailed = doorControlConnectFailed;
    }

    public boolean isLightControlConnectFailed() {
        return lightControlConnectFailed;
    }

    public void setLightControlConnectFailed(boolean lightControlConnectFailed) {
        if(this.lightControlConnectFailed!=lightControlConnectFailed){
            if(lightControlConnectFailed){

                PushMsg.update(System.currentTimeMillis(),"LightControl","none","web_app_LightControl_contact_failed",
                        "连接灯控制器失败",false,2);
            }else {

                PushMsg.update(System.currentTimeMillis(),"LightControl","none","web_app_LightControl_contact_normal",
                        "连接灯控制器恢复正常",false,2);
            }
        }
        this.lightControlConnectFailed = lightControlConnectFailed;
    }
    public int getDoorRfNum() {
        return doorControlRfNum;
    }

    public void setDoorRfNum(int doorControlRfNum) {
        this.doorControlRfNum = doorControlRfNum;
    }


    public int getDoorWrong() {
        return doorControlWrong;
    }

    public void setDoorWrong(int doorControlWrong) {
        this.doorControlWrong = doorControlWrong;
    }
    public int getDoorMsgWrong() {
        return doorControlMsgWrong;
    }

    public void printInfo(){
        if(getDoorRfNum()==0){
            setDoorRfNum(1);
        }
        if(getLightControlRfNum()==0){
            setLightControlRfNum(1);
        }
        String msg="-----------------------------------------"+
        new Date().toString()+"\r\n"+
        "门控制运行次数: " +getDoorRfNum()+"\r\n"+
        "门控制失败次数: " +getDoorWrong()+"\r\n"+
        "门控制失败率: " +(getDoorWrong()*100/getDoorRfNum())+"\r\n"+
        "门控制消息错误次数: " +getDoorMsgWrong()+"\r\n"+
        "灯控制运行次数: " +getLightControlRfNum()+"\r\n"+
        "灯控制失败率: " +(getLightControlWrong()*100/getLightControlRfNum())+"\r\n"+
        "灯控制失败次数: " +getLightControlWrong()+"\r\n"+
        "-----------------------------------------"+"\r\n";

        lightControlWrong=0;
        lightControlMsgWrong=0;
        lightControlRfNum=0;
        doorControlWrong=0;
        doorControlMsgWrong=0;
        doorControlRfNum=0;
        // System.err.println("消息错误次数: " +getDoorMsgWrong());
        logFM.v(msg);
    }
}
