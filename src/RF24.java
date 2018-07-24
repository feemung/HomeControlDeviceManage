import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by feemung on 18/5/22.
 */
public class RF24 extends ConnectDeviceBase{
    public static RF24 instance;

    public static RF24 getInstance() {
        if (instance == null) {
            instance=new RF24();
        }
        return instance;
    }
    private LogFM log= LogFM.getInstance(RF24.class);
    public HcsRF24Jni hcsRF24Jni=new HcsRF24Jni();

    private Lock lock=new ReentrantLock();
    public RF24(){

    }
    public void reset(){
        hcsRF24Jni.init(1);
    }
    public String connect(int client,long msg){
        log.d(client+" "+msg);

        lock.lock();
        String receive="err";
        switch (client){
            case DoorLockDevice:

                  receive=String.valueOf(sendClient(1,msg));
                if(msg==0&&!check(receive)){
                    receive="err";
                    doorControlMsgWrong++;
                }
                break;
            case LightControlDevice:
                receive=String.valueOf(sendClient(2,msg));
                break;
        }

        lock.unlock();
        log.d("end");
        return receive;
    }

    private long lastTime=0;
    private boolean check(String temp){
        if ((temp.equals("0")||temp.length()!=6)||(!temp.substring(1,4).matches("[0-1]*"))){

            doorControlMsgWrong++;
            return false;
        }

        int che=Integer.valueOf(temp.substring(1,2))*4+Integer.valueOf(temp.substring(2,3))*2+Integer.valueOf(temp.substring(3,4))*1;

        if(che!=Integer.valueOf(temp.substring(4,5))){

            doorControlMsgWrong++;
            return false;
        }
        return true;
    }
    public long sendClient(int client,long msg){
        for (int i = 0; i < 10; i++) {
            switch (client){
                case RF24.DoorLockDevice:
                    doorControlRfNum++;
                    break;
                case RF24.LightControlDevice:
                    lightControlRfNum++;
                    break;
            }

            long rec=hcsRF24Jni.contactRF(client,msg);
/*
            String r= "0";
            try {
                r = connectSocket(String.valueOf(client)+";"+String.valueOf(msg));
            } catch (Exception e) {
                e.printStackTrace();
            }
            long rec=Long.valueOf(r);
*/
            if(rec==0){

                switch (client){
                    case RF24.DoorLockDevice:
                        doorControlWrong++;
                        break;
                    case RF24.LightControlDevice:
                        lightControlWrong++;
                        break;
                }
            }else {
                switch (client){
                    case RF24.DoorLockDevice:
                        setDoorControlConnectFailed(false);
                        break;
                    case RF24.LightControlDevice:
                        setLightControlConnectFailed(false);
                        break;
                }
                return rec;
            }
            if(i==10-1){

                switch (client){
                    case RF24.DoorLockDevice:
                        setDoorControlConnectFailed(true);
                        break;
                    case RF24.LightControlDevice:
                        setLightControlConnectFailed(true);
                        break;
                }
                break;
            }
            try {
                Thread.sleep(300);
            }catch (Exception e){

            }
        }

        return 0;
    }
    private int lightControlWrong=0;
    private int lightControlMsgWrong=0;
    private int lightControlRfNum=0;
    private int doorControlWrong=0;
    private int doorControlMsgWrong=0;
    private int doorControlRfNum=0;
    private boolean doorControlConnectFailed=true;
    private boolean lightControlConnectFailed=true;

    public int getDoorWrong() {
        return doorControlWrong;
    }

    public void setDoorWrong(int doorControlWrong) {
        this.doorControlWrong = doorControlWrong;
    }

    public int getDoorRfNum() {
        return doorControlRfNum;
    }

    public void setDoorRfNum(int doorControlRfNum) {
        this.doorControlRfNum = doorControlRfNum;
    }

    public int getDoorMsgWrong() {
        return doorControlMsgWrong;
    }
    public  String connectSocket(String msg) throws Exception {
        log.d(msg);
       // System.out.println("---------------------------------------------------------------------------------");
        // 要连接的服务端IP地址和端口
        String host = "192.168.1.2";
        int port = 55536;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        socket.setSoTimeout(3000);
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
        //System.out.println("from server: " + sb);

        inputStream.close();
        outputStream.close();
        socket.close();
        String s=sb.toString();
        if(s.isEmpty()){
            s="0";
        }
        log.d(s);
        return s;
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

                PushMsg.update(System.currentTimeMillis(),"DoorControl","none","door_contact_failed");
            }else {

                PushMsg.update(System.currentTimeMillis(),"DoorControl","none",ServerErr.DoorControl_contact_normal.getName());
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

                PushMsg.update(System.currentTimeMillis(),"LightControl","none","web_app_LightControl_contact_failed");
            }else {

                PushMsg.update(System.currentTimeMillis(),"LightControl","none","web_app_LightControl_contact_normal");
            }
        }
        this.lightControlConnectFailed = lightControlConnectFailed;
    }

    public void printInfo(){
        if(getDoorRfNum()==0){
            setDoorRfNum(1);
        }
        if(getLightControlRfNum()==0){
            setLightControlRfNum(1);
        }
        /*
        System.err.println();
        System.err.println("-----------------------------------------");
        System.err.println(new Date().toString());

        System.err.println("门控制运行次数: " +getDoorRfNum());
        System.err.println("门控制失败次数: " +getDoorWrong());
        System.err.println("门控制失败率: " +(getDoorWrong()*100/getDoorRfNum()));
        System.err.println("门控制消息错误次数: " +getDoorMsgWrong());
        System.err.println("灯控制运行次数: " +getLightControlRfNum());
        System.err.println("灯控制失败率: " +(getLightControlWrong()*100/getLightControlRfNum()));
        System.err.println("灯控制失败次数: " +getLightControlWrong());
        System.err.println("-----------------------------------------");
        System.err.println();
        */
        lightControlWrong=0;
        lightControlMsgWrong=0;
        lightControlRfNum=0;
        doorControlWrong=0;
        doorControlMsgWrong=0;
        doorControlRfNum=0;
       // System.err.println("消息错误次数: " +getDoorMsgWrong());
    }
}
