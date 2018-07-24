/**
 * Created by feemung on 18/5/22.
 */
public abstract class ConnectDeviceBase {
    public final static int DoorLockDevice=1;
    public final static int LightControlDevice=2;
    public abstract String connect(int client,long msg);


}
