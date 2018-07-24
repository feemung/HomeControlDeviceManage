import org.json.JSONException;

import java.util.Iterator;

/**
 * Created by feemung on 18/6/19.
 */
public class UserCheck implements Runnable{


    public static boolean stopflag=false;
    @Override
    public void run() {
        int a=0;
        while (true){
            if(stopflag&&a<20){
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a++;
                continue;
            }
            a=0;
            stopflag=false;

            long currentTime=System.currentTimeMillis();
            if(!DevicesInfo.getInstance().isLockDoor()) {
                Iterator<UserInfo> iterator = UserInfo.getMap().values().iterator();
                boolean flag = false;
                while (iterator.hasNext()) {
                    UserInfo userInfo = iterator.next();
                    if (currentTime - userInfo.getLastTime() < 1000*60*3&&userInfo.isAtHome()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {

                    PushMsg.update(System.currentTimeMillis(),"door","forget_lockdoor","none","忘记锁门,系统将自动锁门",false,2);
                    try {
                        String result=ControlDevice.lockDoor().getString("result");
                        String text="success".equals(result)?"成功":"失败";
                        PushMsg.update(System.currentTimeMillis(),"door","auto_lockdoorIs"+result,"system",text,false,2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //FilerUtils.getFilerUtils("door").writeStringWithTimeStamp("web_app_auto_lock_door");
                }
            }
            try {
                Thread.sleep(1000*30);
            } catch (InterruptedException e) {

            }

        }
    }


}
