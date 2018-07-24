import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by feemung on 17/12/31.
 */
public class HcsRF24Jni {

    public native void init(int client);
    public native void openPipe(int client);
    public native int sender(long arg);
    public native long receive();

    static {


            System.load("/home/pi/Downloads/RF24-master/examples_linux/libHcsRF24Jni.so");

    }

    private int client=0;
    private LogFM logFM= LogFM.getInstance(HcsRF24Jni.class);
    private Lock lock=new ReentrantLock();


    public HcsRF24Jni(){
        initRf2(client);
    }
    public void initRf2(int client){
        if(client==1){
            client=0;
        }
        init(client);
    }
    public void openP(int client){
        if(client==1){
            client=0;
        }
        openPipe(client);
    }
    public long contactRF(int client,long arg){


        long re = 0;
        try {
            if (client != this.client) {
                openP(client);
                this.client = client;
            }

            sender(arg);

            re = receive();

        }catch (Exception e){
            e.printStackTrace();
        }finally {



        }



        return re;
    }

}
