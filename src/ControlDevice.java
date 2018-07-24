import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by feemung on 18/6/10.
 */
public class ControlDevice {
    private static LogFM logFM=LogFM.getInstance(ControlDevice.class);
    public static JSONObject queryDoor(){
        return queryDoorWithWifi();
    }

    public static JSONObject queryDoorWithWifi(){

        ConnectDeviceBase connectDeviceBase= WifiConnect.getInstance();
        String receive=connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice,0l);

        logFM.d("query:"+receive);
        JSONObject result=null;
        if(!"err".contains(receive)){
            long doorControlRunTime= DevicesInfo.getInstance().getDoorControlRunTime();
            String receives[]=receive.split(":");
            switch (receives[0].split("_")[0]){
                case "wifiRST":
                    PushMsg.update(System.currentTimeMillis(),"DoorControl","wifi_RST:","none");
                    break;
                case "wifiConnectAP":
                    PushMsg.update(System.currentTimeMillis(),"DoorControl","wifiConnectAP:","none");
                    break;
            }
            receive=receives[1];
            long doorControlRunTimeTemp=Long.valueOf(receives[2]);
            if(doorControlRunTimeTemp<doorControlRunTime){
                //门控制正在启动中
                PushMsg.update(System.currentTimeMillis(),"DoorControl","start>>>:","none");
                result=parseQueryDoorStateData(receive);
            }else {
                result=parseQueryDoorStateData2(receive);
            }

            DevicesInfo.getInstance().setDoorControlRunTime(doorControlRunTimeTemp);
            try {
                result.put("doorControlRunTime",String.valueOf(doorControlRunTimeTemp));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
    public static boolean startupUpdateDoorControlState(){
        ConnectDeviceBase connectDeviceBase= WifiConnect.getInstance();
        String receive=connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice,0l);

        logFM.d("query:"+receive);
        if(!"err".contains(receive)){
             String receives[]=receive.split(":");
            receive=receives[1];
            long doorControlRunTimeTemp=Long.valueOf(receives[2]);
            parseQueryDoorStateData(receive);


            DevicesInfo.getInstance().setDoorControlRunTime(doorControlRunTimeTemp);
            DevicesInfo.getInstance().setLockDoor(parseLockDoor(receive));
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
            PushMsg.update(System.currentTimeMillis(),"DoorControl","run_time:"+doortimeStr,"none");
          return true;
        }else {
            return false;
        }
    }
    /**
     * 等待开门再关门后锁门
     *
     * */
    public static JSONObject waitLockAfterDoorOpenAndClose(){
        return waitLockAfterDoorOpenAndCloseWithWifi();
    }
    /**
     * 等待开门再关门后锁门
     *
     * */
    public static JSONObject waitLockAfterDoorOpenAndCloseWithWifi(){
        queryDoorWithWifi();
        boolean doorstate=DevicesInfo.getInstance().isDoorState();
        boolean dooropened=false;
        if(doorstate){
            for (int i = 0; i < 60; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queryDoorWithWifi();
                boolean doorstatetemp=DevicesInfo.getInstance().isDoorState();
                if(!doorstatetemp){
                    dooropened=true;
                    //break;
                }
                if(dooropened&&doorstatetemp){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return lockDoor();
    }


    public static JSONObject unlockDoorWithWifi(){
        DeviceStateCheck devicecheck=DeviceStateCheck.getInstance();
        devicecheck.setStopFlag(true);
        DevicesInfo.getInstance().setLockDoor(false);

        ConnectDeviceBase connectDeviceBase= WifiConnect.getInstance();
        JSONObject jsonObject=new JSONObject();

        String rec = connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice,2l);
        String receive="";
        if(rec.contains("err")){
            devicecheck.setStopFlag(false);
            try {
                jsonObject.put("result","unlockDoorIsFailed_doorControl_connectErr");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }else if(rec.contains("success")){
           // logFM.d("开锁耗时:"+(System.currentTimeMillis()-time));

            receive=rec.split(":")[1];
            parseQueryDoorStateData(receive);
            devicecheck.setStopFlag(false);
            try {
                jsonObject.put("result","success");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
        String r="";
        if(receive.contains("err")){
            r="unlockDoorIsFailed_doorControl_connectErr";
        }else
        if(parseDoorRelayLock(receive)) {
            r="unlockDoorIsFailed_DoorRelayLockIsOpen" ;
            // task.setResult(RFDoorTask.Failed_doorIsOpen);
        }else {
            r="unlockDoorIsFailed_doorControl_notNormal" ;
            //task.setResult(RFDoorTask.FAILED);
        }
        devicecheck.setStopFlag(false);
        try {
            jsonObject.put("result",r);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        logFM.d(jsonObject);
        return jsonObject;
    }
    public static JSONObject unlockDoor(){
        return unlockDoorWithWifi();
    }
    public static JSONObject unlockDoorWithRF24(){
        DeviceStateCheck devicecheck=DeviceStateCheck.getInstance();
        devicecheck.setStopFlag(true);
        DevicesInfo.getInstance().setLockDoor(false);

        ConnectDeviceBase connectDeviceBase= RF24.getInstance();
        JSONObject jsonObject=new JSONObject();
        long time=System.currentTimeMillis();
        String rec = connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice,2l);
        if(rec.contains("err")){
            devicecheck.setStopFlag(false);
            try {
                jsonObject.put("result","unlockDoorIsFailed_doorControl_connectErr");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
        String receive="";
        long wait=System.currentTimeMillis();
        for (int i = 0; i < 40; i++) {
            if(System.currentTimeMillis()-wait>1000){
                break;
            }
            receive=connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice,0l);

            if (!receive.contains("err")&&!parseDoorLockState(receive)) {
                logFM.d("开锁耗时:"+(System.currentTimeMillis()-time));
                parseQueryDoorStateData(receive);
                devicecheck.setStopFlag(false);
                try {
                    jsonObject.put("result","success");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String r="";
        if(receive.contains("err")){
            r="unlockDoorIsFailed_doorControl_connectErr";
        }else
        if(parseDoorRelayLock(receive)) {
            r="unlockDoorIsFailed_DoorRelayLockIsOpen" ;
            // task.setResult(RFDoorTask.Failed_doorIsOpen);
        }else {
            r="unlockDoorIsFailed_doorControl_notNormal" ;
            //task.setResult(RFDoorTask.FAILED);
        }
        devicecheck.setStopFlag(false);
        try {
            jsonObject.put("result",r);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        logFM.d(jsonObject);
        return jsonObject;


    }
    public static JSONObject lockDoor() {
        return lockDoorWithWifi();
    }
    public static JSONObject lockDoorWithWifi() {
        logFM.p();

        DeviceStateCheck devicecheck = DeviceStateCheck.getInstance();
        devicecheck.setStopFlag(true);
        DevicesInfo.getInstance().setLockDoor(true);
        ConnectDeviceBase connectDeviceBase = WifiConnect.getInstance();
        long time = System.currentTimeMillis();
        String rec = connectDeviceBase.connect(ConnectDeviceBase.DoorLockDevice, 1l);
        logFM.d(rec);
        JSONObject jsonObject = new JSONObject();
        String receive="";
        if (rec.contains("err")) {
            devicecheck.setStopFlag(false);
            try {
                jsonObject.put("result", "lockDoorIsFailed_doorControl_connectErr");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }else if(rec.contains("success")){
            logFM.d("锁门耗时:"+(System.currentTimeMillis()-time));
            receive=rec.split(":")[1];
            parseQueryDoorStateData(receive);
            devicecheck.setStopFlag(false);
            try {
                jsonObject.put("result","success");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        String r="";
        if(receive.contains("err")){
            r="lockDoorIsFailed_doorControl_connectErr";
        }else
        if(!parseDoorState(receive)) {
            r="lockDoorIsFailed_doorIsOpen" ;
            // task.setResult(RFDoorTask.Failed_doorIsOpen);
        }else {
            r="lockDoorIsFailed_doorControl_notNormal" ;
            //task.setResult(RFDoorTask.FAILED);
        }
        devicecheck.setStopFlag(false);
        logFM.d("failed="+r);
        try {
            jsonObject.put("result",r);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static JSONObject parseQueryDoorStateData(String msg)  {
        logFM.d("msg="+msg);
        String temp=msg;
        JSONObject jsonObject=new JSONObject();
        DevicesInfo devicesInfo=DevicesInfo.getInstance();
        if (temp.equals("0")||temp.contains("err")){
            try {
                jsonObject.put("result","err");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
        try {
            String lastControl=parseLastControl(temp);

            jsonObject.put("LastControl",lastControl);
            devicesInfo.setLastControl(lastControl);

            Boolean doorRelayLockFlag=parseDoorRelayLock(temp);
            jsonObject.put("DoorRelayLock",doorRelayLockFlag);
            devicesInfo.setDoorRelayLock(doorRelayLockFlag);
            Boolean doorLockStateFlag=parseDoorLockState(temp);
            jsonObject.put("DoorLockState",doorLockStateFlag);
            devicesInfo.setDoorLockState(doorLockStateFlag);
            Boolean doorStateFlag=parseDoorState(temp);
            jsonObject.put("DoorState",doorStateFlag);
            devicesInfo.setDoorState(doorStateFlag);
            jsonObject.put("result","success");
        } catch (JSONException e) {

        }
        return jsonObject;

    }
    private static JSONObject parseQueryDoorStateData2(String msg)  {
        logFM.d("msg="+msg);
        String temp=msg;
        JSONObject jsonObject=new JSONObject();
        StringBuffer sb=new StringBuffer();
        if (temp.equals("0")||temp.contains("err")){
            try {
                jsonObject.put("result","err");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
        DevicesInfo devicesInfo=DevicesInfo.getInstance();

        try {
            String lastControl=parseLastControl(temp);
            boolean flag=lastControl.equals("Blue")||lastControl.equals("Switch");
            boolean lockdoor=parseLockDoor(temp);
            if(flag&&lockdoor!=devicesInfo.isLockDoor()){
                DeviceStateCheck.getInstance().setStopFlag(true);
                UserCheck.stopflag=true;
                try {
                    Thread.sleep(10);
                }catch (Exception e){

                }
            }
            jsonObject.put("LastControl",lastControl);
            devicesInfo.setLastControl(lastControl);

            Boolean doorRelayLockFlag=parseDoorRelayLock(temp);
            jsonObject.put("DoorRelayLock",doorRelayLockFlag);
            devicesInfo.setDoorRelayLock(doorRelayLockFlag);
            Boolean doorLockStateFlag=parseDoorLockState(temp);
            jsonObject.put("DoorLockState",doorLockStateFlag);
            devicesInfo.setDoorLockState(doorLockStateFlag);
            Boolean doorStateFlag=parseDoorState(temp);
            jsonObject.put("DoorState",doorStateFlag);
            devicesInfo.setDoorState(doorStateFlag);


            jsonObject.put("result","success");
            if(flag){

               // jsonObject.put("LockDoor",lockdoor);
                devicesInfo.setLockDoor(lockdoor);
                DeviceStateCheck.getInstance().setStopFlag(false);
                UserCheck.stopflag=false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    private static Boolean parseDoorRelayLock(String msg){
        if("1".equals(msg.substring(1,2))) {

            return false;

        }else if("0".equals(msg.substring(1,2))) {

            return true;
        }
        return null;
    }
    private static Boolean parseDoorState(String msg){
        if ("1".equals(msg.substring(3, 4))) {
            return true;
        }else if ("0".equals(msg.substring(3, 4))) {
            return false;
        }
        return null;
    }
    private static Boolean parseDoorLockState(String msg){
        if ("0".equals(msg.substring(2, 3))) {
            return false;
        }else  if ("1".equals(msg.substring(2, 3))) {
           return true;

        }
        return null;
    }
    private static String parseLastControl(String msg){
        String s=(msg.substring(0, 1));
        switch (s){
            case "1":
                return "RF24";
            case "2":
                return "Blue";
            case "3":
                return "Switch";
            case "4":
                return "Wifi";
        }
        return "unknown";
    }
    private static Boolean parseLockDoor(String msg){
        if ("0".equals(msg.substring(5, 6))) {
            return false;
        }else  if ("1".equals(msg.substring(5, 6))) {
            return true;

        }
        return null;
    }
    /**
     * 灯控制器
     *
     *
     * */
    public static JSONObject openLight1(){
       return connectLightControl(1l);

    }
    public static JSONObject openLight2(){
        return connectLightControl(4l);

    }
    public static JSONObject closeLight1(){
        return connectLightControl(2l);

    }
    public static JSONObject closeLight2(){
        return connectLightControl(3l);
    }
    public static JSONObject connectLightControl(long msg){
        logFM.d(msg);
        ConnectDeviceBase connectDeviceBase= WifiConnect.getInstance();
        String receive=connectDeviceBase.connect(ConnectDeviceBase.LightControlDevice,msg);
        JSONObject jsonObject=new JSONObject();
        if("1".equals(receive)){
            try {
                jsonObject.put("result","success");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            try {
                jsonObject.put("result","LightControlIsFailed_dataIsWrong");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        logFM.d(jsonObject.toString());
        return jsonObject;
    }
    public static JSONObject queryBedroomDht11(){
        JSONObject jsonObject=new JSONObject();
        ConnectDeviceBase connectDeviceBase= WifiConnect.getInstance();
        String receive=connectDeviceBase.connect(ConnectDeviceBase.LightControlDevice,5l);

        if(receive.contains("err")){
            try {
                jsonObject.put("result","LightControlIsFailed_connectErr");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if("3".equals(receive)||receive.length()!=5) {
            try {
                jsonObject.put("result","LightControlIsFailed_dataIsWrong");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            try {
                String temp=receive.substring(1,3);
                String humi=receive.substring(3,5);
                jsonObject.put("result","success");
                jsonObject.put("temp",temp);
                jsonObject.put("humi",humi);
                DevicesInfo.getInstance().setBedroom_temp(Integer.valueOf(temp));
                DevicesInfo.getInstance().setBedroom_humi(Integer.valueOf(humi));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        return jsonObject;
    }
}
