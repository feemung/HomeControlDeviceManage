import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by feemung on 16/4/13.
 */
public class LogFM {
    private static Map<String,LogFM> map=new TreeMap<>();

    public synchronized static LogFM getInstance(Class c) {
        if(map.containsKey(c.getName())){
            return map.get(c.getName());
        }else {
            LogFM logFM=new LogFM(c.getName());
             map.put(c.getName(),logFM);
            return logFM;
        }

    }
    private String className;
    public boolean stopFlag=false;
    private PrintStream printStream;
    private LogFM(String name){
        className=name;
    }
    private String tagInfo(){
        StackTraceElement ste[]=Thread.currentThread().getStackTrace();
        for(int i=0;i<ste.length;i++){

            if(ste[i].isNativeMethod()){
                // System.out.println("Native=");
                continue;
            }
            if(ste[i].getClassName().equals(Thread.class.getName())){
                // System.out.println("thread=");
                continue;
            }
            if(ste[i].getClassName().equals(this.getClass().getName())){
                continue;
            }
            //return ste[i].toString();
            return "["+Thread.currentThread().getName()+"] ."+ste[i].getMethodName() + "("+ste[i].getFileName()+":"+ste[i].getLineNumber()+")"+">>>>";
        }
        return "";
    }
    public synchronized void d(Object ...obj){
        if(stopFlag){
            return;
        }
        StringBuffer sb=new StringBuffer();
        sb.append(tagInfo());
        for(Object o:obj){
            sb.append(o);
        }
        sb.append("\n\r");
        print(sb.toString());
    }
    public synchronized void i(Object ...obj){
        StringBuffer sb=new StringBuffer();
        sb.append(tagInfo());
        for(Object o:obj){
            sb.append(o);
        }
        sb.append("\n\r");
        print(sb.toString());
    }
    public synchronized void e(Exception ex){
        StackTraceElement stackTraceElement[]=ex.getStackTrace();
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i <stackTraceElement.length ; i++) {
            sb.append(stackTraceElement[i]);
            sb.append("\n\r");
        }
        print(tagInfo()+sb.toString());
    }
    public synchronized void e(String ex){
        print(tagInfo()+ex);

    }
    public synchronized void v(String v){
        print(tagInfo()+v);

    }
    public void p(){
        print(tagInfo());
    }
    private void print(String msg){
        SimpleDateFormat format=new SimpleDateFormat("MMddHH");
        System.out.println(msg);
    }

}
