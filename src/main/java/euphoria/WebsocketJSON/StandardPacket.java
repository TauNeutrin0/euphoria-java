package euphoria.WebsocketJSON;

public class StandardPacket {
  private String id;
  private String type;
  private DataPacket data;
  private String error;
  private boolean throttled;
  private String throttled_reason;
  
  public StandardPacket(DataPacket d){
    data = d;
    type = d.getType();
    System.out.println(type);
  }
  
  public StandardPacket(){}
  
  public String getType() {
    return type;
  }
  
  public DataPacket getData() {
    return data;
  }
  
  public void setData(DataPacket dP) {
    data=dP;
  }
  
  public String getError() {
    return error;
  }
  
}
