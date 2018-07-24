import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by feemung on 18/6/10.
 */
public class DevicesInfo {
    private static DevicesInfo instance;
    public static DevicesInfo getInstance() {
        if (instance == null) {
            instance=new DevicesInfo();
        }
        return instance;
    }
    private boolean isLockDoor=true;
    private boolean doorState=false;
    private boolean doorLockState=false;
    private boolean doorRelayLock=false;
    private int bedroom_temp=0;
    private int bedroom_humi=0;
    private boolean bedroom_light1=false;
    private boolean bedroom_light2=false;
    private String lastControl="Wifi";
    private long doorControlRunTime=0;

    private Lock lock=new ReentrantLock();
    private LogFM logFM=LogFM.getInstance(DevicesInfo.class);
    private DeviceStateCheck check=DeviceStateCheck.getInstance();
    public boolean isDoorLockState() {
        return doorLockState;
    }

    public void setDoorLockState(boolean doorLockState) {
        if(this.doorLockState!=doorLockState){
            logFM.d("更新门锁状态");
            this.doorLockState = doorLockState;
            if(check.isStopFlag()){
                logFM.d("推送门锁状态");
                PushMsg.update(System.currentTimeMillis(),"door","doorLockState:"+String.valueOf(doorLockState),"none",
                        doorLockState?"门锁上锁中":"门锁打开中",false,3);
            }else {
                logFM.d("检查门锁状态是否异常");
                check.check();
            }
        }

    }

    public boolean isDoorState() {
        return doorState;
    }

    public void setDoorState(boolean doorState) {
        if(this.doorState!=doorState){
            if(isLockDoor&&!doorState){
                logFM.d("门异常打开");
                PushMsg.update(System.currentTimeMillis(), "door", "web_app_unusualDoorOpen" ,"门异常打开","none",false,3);
            }else {
                logFM.d("推送门状态");
                PushMsg.update(System.currentTimeMillis(), "door", "doorState:" + String.valueOf(doorState),"none",
                        doorState?"门关闭中":"门打开中",false,3);
            }
        }
        this.doorState = doorState;
    }

    public boolean isLockDoor() {
        return isLockDoor;
    }

    public void setLockDoor(boolean lockDoor) {
        if(this.isLockDoor!=lockDoor){
            if(check.isStopFlag()){
                PushMsg.update(System.currentTimeMillis(),"door","Control="+lastControl
                        +"; isLockDoor="+String.valueOf(lockDoor),"none",lastControl+"控制"+(lockDoor?"打开":"关闭")+"门锁",
                        false,3);
            }else {
                check.check();
            }
        }
        isLockDoor = lockDoor;
    }

    public boolean isDoorRelayLock() {
        return doorRelayLock;
    }

    public void setDoorRelayLock(boolean doorRelayLock) {
        if(this.doorRelayLock!=doorRelayLock){
            logFM.d("更新门锁继电器状态");
            this.doorRelayLock = doorRelayLock;
            if(check.isStopFlag()){
                logFM.d("推送门锁继电器状态");
                PushMsg.update(System.currentTimeMillis(),"door","doorRelayLock:"+String.valueOf(doorRelayLock),"none",
                        "门锁继电器"+(doorRelayLock?"打开中":"关闭中"),false,3);
            }else {
                logFM.d("检测门锁继电器状态是否异常");
                check.check();
            }
        }

    }


    private int humiCount=0;
    private int humis[]=new int[5];
    private long humiStartTime=0;
    public void setBedroom_humi(int bedroom_humi) {
        humis[humiCount]=bedroom_humi;
        humiCount++;
        if(humiCount>=5){
            humiStartTime=System.currentTimeMillis();
            humiCount=0;
        }
        int temp=humis[0];
        if(System.currentTimeMillis()-humiStartTime>120000){
            humiStartTime=System.currentTimeMillis();
            this.bedroom_humi=bedroom_humi;
            return;
        }
        for (int i = 0; i < humis.length; i++) {
            if(temp!=humis[i]){
                return;
            }


        }
        this.bedroom_humi = bedroom_humi;
    }

    public int getBedroom_humi() {
        return bedroom_humi;
    }


    public int getBedroom_temp() {
        return bedroom_temp;
    }

    public void setBedroom_temp(int bedroom_temp) {
        this.bedroom_temp = bedroom_temp;
    }

    public boolean isBedroom_light1() {
        return bedroom_light1;
    }

    public void setBedroom_light1(boolean bedroom_light1) {
        this.bedroom_light1 = bedroom_light1;
    }

    public boolean isBedroom_light2() {
        return bedroom_light2;
    }

    public void setBedroom_light2(boolean bedroom_light2) {
        this.bedroom_light2 = bedroom_light2;
    }
    public void printAllState(){
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject doorJson = new JSONObject();
            doorJson.put("lockDoor", isLockDoor());
            doorJson.put("doorLockState", isDoorLockState());
            doorJson.put("doorRelayLock", isDoorRelayLock());
            doorJson.put("doorState", isDoorState());
            jsonObject.put("door", doorJson);

            JSONObject bedroom_dhtJson = new JSONObject();
            bedroom_dhtJson.put("humi", getBedroom_humi());
            bedroom_dhtJson.put("temp", getBedroom_temp());
            jsonObject.put("bedroom_dht11", bedroom_dhtJson);

            jsonObject.put("bedroom_light1", isBedroom_light1());
            jsonObject.put("bedroom_light2", isBedroom_light2());
            System.out.println(jsonObject.toString());
        }catch (Exception e){

        }
    }

    public String getLastControl() {
        return lastControl;
    }

    public void setLastControl(String lastControl) {
        if(!this.lastControl.equals(lastControl)){
            PushMsg.update(System.currentTimeMillis(),"door","none","lastControl:"+lastControl);
            this.lastControl=lastControl;
        }
    }

    public long getDoorControlRunTime() {
        return doorControlRunTime;
    }

    public void setDoorControlRunTime(long doorControlRunTimeTemp) {
        if(doorControlRunTimeTemp<this.doorControlRunTime){
            String doortimeStr;
            if(doorControlRunTimeTemp<1000){
                doortimeStr=doorControlRunTimeTemp+"ms";
            }else if(doorControlRunTimeTemp<1000*60){
                doortimeStr=(doorControlRunTimeTemp/1000)+"s";
            }else if(doorControlRunTimeTemp<1000*60*60){
                doortimeStr=(doorControlRunTimeTemp/1000/60)+"m";
            }else if(doorControlRunTimeTemp<1000*60*60*24){
                doortimeStr=(doorControlRunTimeTemp/1000/60/60)+"h";
            }else {
                doortimeStr=(doorControlRunTimeTemp/1000/60/60/24)+"d";
            }
            PushMsg.update(System.currentTimeMillis(),"DoorControl","doorControlStartupRun_time:"+doortimeStr,"none");

        }
        this.doorControlRunTime = doorControlRunTimeTemp;
    }
}
