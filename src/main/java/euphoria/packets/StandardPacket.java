package euphoria.packets;

public class StandardPacket {
  private String     id;
  private String     type;
  private DataPacket data;
  private String     error;
  private boolean    throttled;
  private String     throttled_reason;
  
  public StandardPacket(DataPacket d){
    data = d;
    type = d.getType();
  }
  public StandardPacket(){}
  
  public void setData(DataPacket dP) { data=dP;    }
  public void setId(String id)       { this.id=id; }
  
  public String     getId()              { return id;               }
  public String     getType()            { return type;             }
  public DataPacket getData()            { return data;             }
  public String     getError()           { return error;            }
  public boolean    getThrottled()       { return throttled;        }
  public String     getThrottledReason() { return throttled_reason; }
}
