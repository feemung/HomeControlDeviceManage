import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by feemung on 18/6/10.
 */
public class DeviceStateCheck implements Runnable
{
    public static DeviceStateCheck instance;

    public static DeviceStateCheck getInstance() {
        if (instance == null) {
            instance=new DeviceStateCheck();
        }
        return instance;
    }

    private boolean stopFlag=false;
    public long lastTime=System.currentTimeMillis();
    private LogFM logFM=LogFM.getInstance(DeviceStateCheck.class);
    private boolean doorControlErr=false;
    @Override
    public void run() {
        logFM.d("start run");
        PushMsg.update(System.currentTimeMillis(),"system","DeviceStateCheck:start","none","开始与门控制器同步数据",false,1);
        stopFlag=true;
        boolean startupFlag=ControlDevice.startupUpdateDoorControlState();
        stopFlag=false;
        check();
        PushMsg.update(System.currentTimeMillis(),"system","DeviceStateCheck:startup_"+(startupFlag?"success":"failed"),"none",
                "与门控制器同步数据"+(startupFlag?"成功":"失败"),false,1);


        int i=0;
        long waitLookDhtTime=System.currentTimeMillis();
        while (true){
           // logFM.p();
            lastTime=System.currentTimeMillis();
            if(stopFlag){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                i++;
                if(i>30){
                    stopFlag=false;
                    i=0;
                }
                continue;
            }
            i=0;
            try {
                DeviceTask.getInstance().queryDoorDeviceTask();
               // ControlDevice.queryDoor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(lastTime-waitLookDhtTime>1000*10){
                waitLookDhtTime=System.currentTimeMillis();
                //SimpleDateFormat format=new SimpleDateFormat("HH:mm");

               // logFM.v(format.format(new Date())+" "+ControlDevice.queryBedroomDht11().toString());

            }
            if(doorControlErr){
                logFM.d("处理门锁异常");
                if(DevicesInfo.getInstance().isLockDoor()){
                    logFM.d("处理门锁异常,正在锁门");
                    try {
                        DeviceTask.getInstance().lockDoorDeviceTask();
                        check();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    logFM.d("处理门锁异常,正在开锁");
                    try {
                        DeviceTask.getInstance().unlockDoorDeviceTask();
                        check();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //logFM.d("to do ");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

        }
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }
    public void check(){
        //logFM.p();

        DevicesInfo devicesInfo=DevicesInfo.getInstance();
        if(devicesInfo.isLockDoor()&&devicesInfo.isDoorLockState()&&devicesInfo.isDoorState()){
            //处于门锁状态中
            logFM.d("恢复正常锁门状态");
            doorControlErr=false;
            PushMsg.update(System.currentTimeMillis(),"door","web_app_usualLockDoor","none","恢复到正常锁门状态",false,2);

        }else if(!(devicesInfo.isLockDoor()||devicesInfo.isDoorLockState())){
            //门锁处于开锁状态中
            logFM.d("恢复正常未锁门状态");
            PushMsg.update(System.currentTimeMillis(),"door","usualUnlockDoor","none","恢复正常未锁门状态",false,2);
            doorControlErr=false;
        }else{
            if(devicesInfo.isLockDoor()){
                if(!(devicesInfo.isDoorLockState()||devicesInfo.isDoorState())){
                    //门锁异常打开,门也异常打开
                    logFM.d("门锁异常打开,门也异常打开");
                    doorControlErr=true;
                    PushMsg.update(System.currentTimeMillis(),"door","web_app_unusualUnlockDoorAndDoorOpen","none",
                            "门锁异常打开,门也异常打开",false,2);
                }else if(devicesInfo.isDoorLockState()){
                    //门异常打开
                    logFM.d("门异常打开");
                    PushMsg.update(System.currentTimeMillis(),"door","web_app_unusualDoorOpen","none","门异常打开",false,2);

                }else {
                    //门锁异常打开
                    logFM.d("门锁异常打开");
                    doorControlErr=true;
                    PushMsg.update(System.currentTimeMillis(),"door","web_app_unusualUnlockDoor","none","门锁异常打开",false,2);

                }
            }else {
                if(devicesInfo.isDoorLockState()){
                    //异常上锁
                    logFM.d("异常上锁");
                    doorControlErr=true;
                    PushMsg.update(System.currentTimeMillis(),"door","web_app_unusualLockDoor","none","异常上锁",false,2);

                }
            }
        }
    }
}
