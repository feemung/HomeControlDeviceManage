import java.util.HashMap;
import java.util.Map;

/**
 * Created by feemung on 18/6/19.
 */
public class UserInfo {

    private static Map<String,UserInfo> map=new HashMap<>();
    public static Map<String,UserInfo> getMap(){
        return map;
    }
    public static UserInfo getUser(String user){
        if(!map.containsKey(user)){
            map.put(user,new UserInfo(user));
        }
        return map.get(user);
    }
    private String userId;
    private String password;
    private boolean atHome=false;
    private String connectWifiSSID;//当前或者上次连接的wifi
    private String connectWifiBSSID;//当前或者上次连接的wifi
    private long connectWifiBSSIDFirstTime=0;//当前或者上次连接的wifi
    private long connectWifiBSSIDLeaveTime=0;//当前或者上次连接的wifi
    private boolean conncetWifiInterrupt=false;
    private long lastTime=0;
    private long lastTimeLeaveHomeWifi=0;
    private long lastTimeUnlockDoor=0;

    private UserInfo(String user){
        this.userId=user;
    }
    public void updateFromClient(String connectWifiSSID,String connectWifiBSSID,boolean atHome){
        this.lastTime=System.currentTimeMillis();
        setAtHome(atHome);

        if(chechIsWifi(connectWifiBSSID)) {
            if(connectWifiBSSID.equals(this.connectWifiBSSID)){
                if(conncetWifiInterrupt){
                    connectWifiBSSIDFirstTime=System.currentTimeMillis();
                    connectWifiBSSIDLeaveTime=System.currentTimeMillis();
                }else {
                    connectWifiBSSIDLeaveTime=System.currentTimeMillis();
                }
                conncetWifiInterrupt=false;
            }else {
                connectWifiBSSIDFirstTime=System.currentTimeMillis();
                connectWifiBSSIDLeaveTime=System.currentTimeMillis();
            }
            setConnectWifi(connectWifiSSID,connectWifiBSSID);
        }else {
            connectWifiBSSIDLeaveTime=System.currentTimeMillis();
            conncetWifiInterrupt=true;
        }
    }
    private boolean chechIsWifi(String BSSID){
        return (BSSID!=null&&BSSID.split(":").length==6);

    }
    public void updateFromClient(boolean atHome){
        this.lastTime=System.currentTimeMillis();
        setAtHome(atHome);
    }
    public boolean isAtHome() {
        return atHome;
    }

    public void setAtHome(boolean atHome) {
        if(this.atHome!=atHome){
            PushMsg.getInstance(userId).put(System.currentTimeMillis(),
                    "none",userId,"atHome="+String.valueOf(atHome),"none",false, 3);
            if(!atHome){
                lastTimeLeaveHomeWifi=System.currentTimeMillis();
            }
            this.atHome = atHome;
        }

    }

    public long getLastTime() {
        return lastTime;
    }

    public String getConnectWifiSSID() {
        return connectWifiSSID;
    }

    public void setConnectWifi(String connectWifiSSID,String connectWifiBSSID) {
        if(connectWifiBSSID!=null&&!connectWifiBSSID.equals(this.connectWifiBSSID)){
            PushMsg.getInstance(userId).put(System.currentTimeMillis(),
                    "none",userId,"connectWifiSSID="+String.valueOf(connectWifiSSID)+" connectWifiBSSID="+connectWifiBSSID,"none",false, 3);
            FilerUtils.getFilerUtils(this.userId+"_UserInfo.log").writeStringWithTimeStamp("connectWifiSSID="+String.valueOf(connectWifiSSID)+" connectWifiBSSID="+connectWifiBSSID);
        }
        this.connectWifiSSID = connectWifiSSID;
        this.connectWifiBSSID=connectWifiBSSID;

    }

    public String getConnectWifiBSSID() {
        return connectWifiBSSID;
    }


}
