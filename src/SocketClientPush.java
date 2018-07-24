import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by feemung on 18/6/10.
 */
public class SocketClientPush implements Runnable{
    private Socket socket;
    private LogFM logFM= LogFM.getInstance(SocketClient.class);
    public SocketClientPush(Socket socket) {
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
            logFM.d("receive:"+sb.toString());
            JSONObject jsonObject=new JSONObject();
            try {
                JSONObject receiveJson=new JSONObject(sb.toString());
                String user=receiveJson.getString("user");
                if(user==null||user.isEmpty()){
                    user="default";
                }
                JSONArray jsonArray = parseTask(sb.toString());
                jsonObject.put("lastTime",UserInfo.getUser(user).getLastTime());

                    jsonObject.put("content", jsonArray);
                    jsonObject.put("result", "success");

            }catch (Exception e){
                jsonObject.put("result","err");
            }
            logFM.d("sender:"+jsonObject.toString());
            outputStream.write(jsonObject.toString().getBytes("UTF-8"));
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


    public JSONArray parseTask(String msg)throws Exception{

        JSONObject r=null;

        JSONObject msgJson=new JSONObject(msg);

        String user=msgJson.getString("user");
        if(user==null||user.isEmpty()){
            user="default";
        }
        String wifiStr=msgJson.getString("connectWifi");
        String connectWifiSSID="unknown";
        String connectWifiBSSID="unknown";

        if(wifiStr==null||wifiStr.isEmpty()){

        }else {
            JSONObject jsonObject=new JSONObject(wifiStr);
            connectWifiBSSID=jsonObject.getString("BSSID");
            connectWifiSSID=jsonObject.getString("SSID");
        }
        UserInfo userInfo=UserInfo.getUser(user);
        boolean atHome="MERCURY_16F2".equals(connectWifiSSID);
        userInfo.updateFromClient(connectWifiSSID,connectWifiBSSID,atHome);
        JSONArray jsonArray=PushMsg.getInstance(user).getAllMsg();


        return jsonArray;
    }
}
