import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by feemung on 18/6/13.
 */
public class DeviceTask {
    private LogFM logFM=LogFM.getInstance(DeviceTask.class);
    private static DeviceTask instance;

    public static DeviceTask getInstance() {
        if (instance == null) {
            instance=new DeviceTask();
        }
        return instance;

    }

    public   JSONObject lockDoorDeviceTask()throws Exception{
        LockDoor lockDoor=new LockDoor();
        FutureTask<JSONObject> result=new FutureTask<JSONObject>(lockDoor);
        Thread t=new Thread(result);
        t.setName("Thread-lockDoorDeviceTask");
        t.start();

        JSONObject jsonObject=result.get();
        logFM.d(jsonObject);
        return jsonObject;
    }
     class LockDoor implements Callable<JSONObject>{
        public LockDoor(){}
        @Override
        public JSONObject call() throws Exception {
            return ControlDevice.lockDoor();
        }
    }
    public  JSONObject unlockDoorDeviceTask()throws Exception{
        UnlockDoor instance=new UnlockDoor();
        FutureTask<JSONObject> result=new FutureTask<JSONObject>(instance);
        Thread t=new Thread(result);
        t.setName("Thread-unlockDoorDeviceTask");
        t.start();

        JSONObject jsonObject=result.get();
        logFM.d(jsonObject);
        return jsonObject;
    }
    class UnlockDoor implements Callable<JSONObject>{
        public UnlockDoor(){}
        @Override
        public JSONObject call() throws Exception {
            return ControlDevice.unlockDoor();
        }
    }

    public  JSONObject waitLockAfterDoorOpenAndCloseTask()throws Exception{
        WaitLockAfterDoorOpenAndClose instance=new WaitLockAfterDoorOpenAndClose();
        FutureTask<JSONObject> result=new FutureTask<JSONObject>(instance);
        Thread t=new Thread(result);
        t.setName("Thread-waitLockAfterDoorOpenAndCloseTask");
        t.start();
        JSONObject jsonObject=result.get();
        return jsonObject;
    }
    class WaitLockAfterDoorOpenAndClose implements Callable<JSONObject>{
        public WaitLockAfterDoorOpenAndClose(){}
        @Override
        public JSONObject call() throws Exception {
            return ControlDevice.waitLockAfterDoorOpenAndClose();
        }
    }
    public  JSONObject queryDoorDeviceTask()throws Exception{
        QueryDoor instance=new QueryDoor();
        FutureTask<JSONObject> result=new FutureTask<JSONObject>(instance);
        Thread t=new Thread(result);
        t.setName("Thread-QueryDoorDeviceTask");
        t.start();


        JSONObject jsonObject=result.get();
        logFM.d(jsonObject);
        return jsonObject;
    }
    class QueryDoor implements Callable<JSONObject>{
        public QueryDoor(){}
        @Override
        public JSONObject call() throws Exception {
            return ControlDevice.queryDoor();
        }
    }
}
