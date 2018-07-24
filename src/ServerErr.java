/**
 * Created by feemung on 18/6/22.
 */
public enum ServerErr {
    DoorControl_contact_normal("门控制器通讯正常",1);
    private String name ;
    private int index ;

    private ServerErr( String name , int index ){
        this.name = name ;
        this.index = index ;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
