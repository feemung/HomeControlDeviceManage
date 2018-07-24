import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by feemung on 18/6/10.
 */
public class SocketClient implements Runnable{
    private Socket socket;
    private LogFM logFM=LogFM.getInstance(SocketClient.class);
    public SocketClient(Socket socket) {
        this.socket=socket;
    }


    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream=null;
        try {
            inputStream = socket.getInputStream();
            outputStream=socket.getOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = inputStream.read(bytes)) != -1) {
                //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                sb.append(new String(bytes, 0, len, "UTF-8"));
            }
            JSONObject jsonObject=new JSONObject(sb.toString());
            String device=jsonObject.getString("device");
            String controlTask=jsonObject.getString("task");
            String user=jsonObject.getString("user");
            String taskType=jsonObject.getString("taskType");
            UserCheck.stopflag=true;
            logFM.d("user="+user+" device="+device+" controlTask="+controlTask);
            JSONObject r=null;
            switch (taskType) {
                case "control":
                    r = parseControlTask(user, device, controlTask);
                    break;
                case "query":
                    r=parseTask(user,device,controlTask);
                    break;
                case "resetRF24":
                    RF24.getInstance().reset();
                    break;
                default:{
                    r=new JSONObject();
                    r.put("result","noTaskType");
                    break;
                }
            }
            logFM.d("result: " + r);
            try {
                r.put("user",user);
                r.put("device",device);
                r.put("task",controlTask);
                r.put("taskType",taskType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            outputStream.write(r.toString().getBytes("UTF-8"));
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.getStackTrace();
        }finally {
            try{
                socket.close();
            }catch (Exception e){

            }
        }



    }

    public JSONObject parseControlTask(String user,String device,String controlTask){

        JSONObject r=null;
        DeviceTask deviceTask= DeviceTask.getInstance();
        try {
            switch (device) {
                case "door":
                    logFM.p();
                    switch (controlTask) {
                        case "close":
                            logFM.d("lock");
                            r = deviceTask.lockDoorDeviceTask();


                            break;
                        case "open":
                            r = deviceTask.unlockDoorDeviceTask();
                            break;
                        case "state":
                            logFM.d("doorlock query:");
                            r = deviceTask.queryDoorDeviceTask();
                            break;
                        case "closeAfterOpen":
                            logFM.d("waitLockAfterDoorOpenAndClose");
                            r = deviceTask.unlockDoorDeviceTask();
                            if(r.getString("result").equals("success")) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject=deviceTask.waitLockAfterDoorOpenAndCloseTask();
                                            String resultstr=jsonObject.getString("result");
                                            PushMsg.getInstance(user).put(System.currentTimeMillis(),
                                                    "door",user,"waitLockAfterDoorOpenAndClose_"+resultstr,
                                                    "success".equals(resultstr)?"锁门成功,请主人放心":"锁门失败,请检查错误原因",true,3);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                            break;
                        default:
                            logFM.d("default");
                            r = new JSONObject();
                            try {
                                r.put("result", "noTask");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;
                case "bedroom_light1":
                    switch (controlTask){
                        case "open":
                            r=ControlDevice.openLight1();
                            break;
                        case "close":
                            r=ControlDevice.closeLight1();
                            break;
                        default:
                            logFM.d("default");
                            r = new JSONObject();
                            try {
                                r.put("result", "noTask");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;
                case "bedroom_light2":
                    switch (controlTask){
                        case "open":
                            r=ControlDevice.openLight2();
                            break;
                        case "close":
                            r=ControlDevice.closeLight2();
                            break;
                        default:
                            logFM.d("default");
                            r = new JSONObject();
                            try {
                                r.put("result", "noTask");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;
                case "dht11":
                    r=ControlDevice.queryBedroomDht11();
                    break;
                default:
                    logFM.d("default");
                    r = new JSONObject();
                    try {
                        r.put("result", "noDevice");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }catch (Exception e){
            r = new JSONObject();
            try {
                r.put("result", "exception");
            } catch (JSONException e2) {

            }
        }
        String res=null;
        try {
           res=r.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PushMsg.update(System.currentTimeMillis(), device, user ,controlTask + "#" + res);

        return r;
    }
    public JSONObject parseTask(String user,String device,String controlTask){

        JSONObject r=null;
        try {
            switch (device) {
                case "all":
                    switch (controlTask) {
                        case "query":
                            JSONObject jsonObject = new JSONObject();
                            JSONObject doorJson = ControlDevice.queryDoor();

                            jsonObject.put("doorControl", doorJson);

                            JSONObject bedroom_dhtJson = ControlDevice.queryBedroomDht11();
                            jsonObject.put("bedroom_dht11", bedroom_dhtJson);
                            jsonObject.put("result","success");
                            r = jsonObject;
                            break;
                        default:
                            r = new JSONObject();
                            try {
                                r.put("result", "noTask");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;

                default:
                    logFM.d("default");
                    r = new JSONObject();
                    try {
                        r.put("result", "noDevice");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }catch (Exception e){
            r = new JSONObject();
            try {
                r.put("result", "exception");
            } catch (JSONException e2) {

            }
        }

        return r;
    }
}
