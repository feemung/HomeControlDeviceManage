import java.net.InetAddress;

/**
 * Created by feemung on 18/1/29.
 */
public class PingHost {
    private String hosts="192.168.1.4";
    private static boolean test(String host){
        boolean alive=false;
        try {
            alive=InetAddress.getByName(host).isReachable(500);
            System.out.println(alive);
        }catch (Exception e){
            alive=false;
        }
       return alive;
    }
    public static boolean ping(String host){
        boolean flag=true;
        boolean staue=false;
        long startTime=System.currentTimeMillis();
        while (flag){
            int a=0;
            for (int i = 0; i < 200; i++) {
                if(test(host)){
                    a++;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(a==0||System.currentTimeMillis()-startTime>20000){
                staue=false;
                flag=false;

            }
            if (a == 5) {
                staue=true;
                flag=false;
            }
        }
        return  staue;
    }
}
