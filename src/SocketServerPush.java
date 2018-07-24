import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by feemung on 18/6/10.
 */
public class SocketServerPush implements Runnable{
    private LogFM logFM=LogFM.getInstance(SocketServerPush.class);
    @Override
    public void run() {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void start()throws Exception{
        // 监听指定的端口
        int port = 55538;
        ServerSocket server = new ServerSocket(port);

        // server将一直等待连接的到来
        while (true) {
            logFM.d("server等待客户端连接的到来");
            try {
                Socket socket = server.accept();
                // 建立好连接后，从socket中获取输入流，并建立缓冲区进行读取
                logFM.d(socket.getInetAddress()+"客户端建立连接成功");
                SocketClientPush client = new SocketClientPush(socket);
                new Thread(client).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
