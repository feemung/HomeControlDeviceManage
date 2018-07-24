import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by feemung on 18/2/2.
 */
public class FilerUtils {
    private  String filePath;
    private static String dirsMain="/home/pi/app/hc/log";
   // private  String filePath="/Users/feemung/Desktop/";
    private  static Map<String,FilerUtils> utilsMap=new HashMap<>();
    public static FilerUtils getFilerUtils(String fileName){
        if (utilsMap.get(fileName) == null) {
            utilsMap.put(fileName,new FilerUtils(fileName));
        }
        return utilsMap.get(fileName);
    }
    private Lock lock=new ReentrantLock();
    private FilerUtils(String fileName){
        dirsMain=System.getProperty("user.dir")+"/app/hc/log";

        File dirsF=new File(dirsMain);
        if(!dirsF.exists()&&!dirsF.isDirectory()){
            dirsF.mkdirs();
        }
        filePath=dirsMain+File.separator+fileName+".txt";
        File f =new File(filePath);
        if(!f.exists()){

            try {
                f.createNewFile();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }

        System.out.println("log path:"+filePath);
    }
    public void writeString(String context,boolean append){
        if(context==null||context.isEmpty()){
            return;
        }
        lock.lock();
        try{
            FileWriter writer=new FileWriter(filePath,append);
            BufferedWriter bw=new BufferedWriter(writer);
            bw.write(context+"\r\n");
            bw.close();
            writer.close();
        }catch (Exception e){
            //e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    private void writeString(String context){
        writeString(context,true);
    }
    public void writeStringWithTimeStamp(String context){
        writeString(TimeUtilsFM.getCurrentTimeStamp()+":"+context,true);
    }
    public String readAll(){
        lock.lock();

        StringBuffer sb=new StringBuffer();
        try{
            FileReader r=new FileReader(filePath);
            BufferedReader reader=new BufferedReader(r);
            String temp="";
            while ((temp=reader.readLine())!=null){
                sb.append(temp);
                sb.append("\r\n");
            }

        }catch (Exception e){
           // e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return sb.toString();
    }
    public ArrayList<String> readAllwithList(){
        lock.lock();
        ArrayList<String> arrayList=new ArrayList<>();

        try{
            FileReader r=new FileReader(filePath);
            BufferedReader reader=new BufferedReader(r);
            String temp="";
            while ((temp=reader.readLine())!=null){
                arrayList.add(temp);
            }

        }catch (Exception e){
           // e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return arrayList;
    }
}
