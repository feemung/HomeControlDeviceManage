import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by feemung on 18/2/10.
 */
public class TimeUtilsFM {
    public static String getCurrentTimeStamp(){
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format.format(new Date());
    }
    public static String parseTimeStamp(String timeStamp){
        if(timeStamp.length()<6){
            return timeStamp+"分";
        }else {
            StringBuffer sb=new StringBuffer();
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
            String today=format.format(new Date());
            String s=timeStamp.substring(0,8);

            if(!s.equals(today)){
                sb.append(timeStamp.substring(0,4));
                sb.append("年");
                sb.append(timeStamp.substring(4,6));
                sb.append("月");
                sb.append(timeStamp.substring(6,8));
                sb.append("日");
            }
            sb.append(timeStamp.substring(8,10));
            sb.append("点");
            sb.append(timeStamp.substring(10,12));
            sb.append("分");
            sb.append(timeStamp.substring(12,14));
            sb.append("秒");
            sb.append(timeStamp.substring(14));
            sb.append("毫秒");
            return sb.toString();
        }
    }
}
