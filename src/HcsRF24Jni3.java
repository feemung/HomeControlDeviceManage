/**
 * Created by feemung on 17/12/31.
 */
public class HcsRF24Jni3 {
/*
    public native void init(int client);
    public native int sender(long arg);
    public native long receive();



    static {


            System.load("/home/pi/lib/libHcsRF24Jni.so");

    }

    private int client=0;
    private LogFM logFM=LogFM.getInstance(HcsRF24Jni3.class);
    public HcsRF24Jni3(){
        initRf2(client);
    }
    public void initRf2(int client){
        if(client==1){
            client=0;
        }
        init(client);
    }

    public long contactRF(int client,long arg){



        long re = 0;
        try {
            if (client != this.client) {

                initRf2(client);
                this.client = client;
            }

            sender(arg);
            re = receive();



        }catch (Exception e){
            e.printStackTrace();
        }finally {



        }
        //logFM.p();
        return re;
    }
*/
}
