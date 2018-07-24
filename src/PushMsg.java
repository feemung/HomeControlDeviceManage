import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by feemung on 18/6/11.
 */
public class PushMsg {
    private static HashMap<String,PushMsg> map=new HashMap<>();
    public static PushMsg getInstance(String user) {
        if(!map.containsKey(user)){
           map.put(user, new PushMsg());
        }
        return map.get(user);
    }
    private static LogFM log=LogFM.getInstance(PushMsg.class);
    private JSONArray msgArray=new JSONArray();
    private long lastTime=0l;

    public static void update(long timestamp,String device,String msg,String user){
        update(timestamp,device,msg,user,"none",false,3);
    }
    public static void update(long timestamp,String device,String msg,String user,String text,boolean read,int type){
        log.d("timestamp:"+timestamp+";device:"+device+";msg:"+msg);
        FilerUtils.getFilerUtils(PushMsg.class.getName()+".txt").writeString("timestamp:"+timestamp+";device:"+device
                +";msg:"+msg+";user="+user+";text="+text+";read="+read+";type="+type,true);
        Iterator<String> iter=map.keySet().iterator();
        while (iter.hasNext()){
            String key=iter.next();
            PushMsg pushMsg=map.get(key);
            if(System.currentTimeMillis()-pushMsg.lastTime>1000*60*60*24*4){
                map.remove(key);
                continue;
            }
            pushMsg.put(timestamp,device,user,msg,text,read,type);
        }
    }
    private PushMsg(){
        lastTime=System.currentTimeMillis();
    }
    public void put(long timestamp,String device,String user,String msg,String text,boolean read,int type){
        log.d("timestamp:"+timestamp+";device:"+device+";user="+user+";msg:"+msg);

        JSONObject json=new JSONObject();
        try {
            json.put("timestamp",timestamp);
            json.put("device",device);
            json.put("msg",msg);
            json.put("user",user);
            json.put("text",text);
            json.put("read",read);
            json.put("type",type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msgArray.put(json);
        log.d(json.toString());
    }
    public JSONArray getAllMsg(){
        JSONArray jsonArray=null;
        try {
            jsonArray=new JSONArray(msgArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msgArray=new JSONArray();
        return jsonArray;
    }
}
