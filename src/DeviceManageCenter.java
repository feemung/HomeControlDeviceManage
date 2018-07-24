/**
 * Created by feemung on 18/6/13.
 */
public class DeviceManageCenter implements Runnable{
    private DeviceStateCheck deviceStateCheck;
    @Override
    public void run() {
        init();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long rF24RunTime=System.currentTimeMillis();
        boolean flag=true;
        while (true){
            if(System.currentTimeMillis()-deviceStateCheck.lastTime>10000){
                if(flag) {
                    PushMsg.update(System.currentTimeMillis(), "none", "DeviceStateCheckNotRun", "system", "设备状态检测线程异常,重新启动", false, 0);
                    flag=false;
                }
                    //DeviceStateCheck.instance=null;

                //deviceStateCheck=DeviceStateCheck.getInstance();
                //new Thread(deviceStateCheck).start();
                WifiConnect.getInstance().printInfo();
            }else {
                flag=true;
            }
            if(System.currentTimeMillis()-rF24RunTime>60*1000*1){
                rF24RunTime=System.currentTimeMillis();
               // RF24.getInstance().printInfo();

            }

            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void init(){
        PushMsg.getInstance("f");
        PushMsg.getInstance("default");
        PushMsg.update(System.currentTimeMillis(),"none","startup","system","服务器开始启动",false,0);
        SocketServer socketServer=new SocketServer();
        deviceStateCheck=DeviceStateCheck.getInstance();
        new Thread(socketServer).start();
        Thread thread=new Thread(deviceStateCheck);
        thread.setName("Thread-deviceStateCheck");
        thread.start();
        UserCheck userCheck=new UserCheck();
        new Thread(userCheck).start();
        SocketServerPush socketServerPush=new SocketServerPush();
        new Thread(socketServerPush).start();

    }
}
