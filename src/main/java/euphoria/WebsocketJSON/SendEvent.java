package euphoria.WebsocketJSON;

public class SendEvent extends DataPacket{
  private String id;
  private String parent;
  private String previous_edit_id;
  private int time;
  private SessionView sender;
  private String content;
  private String encryption_key_id;
  private int edited;
  private int deleted;
  private boolean truncated;
  
  public SendEvent() {}
  
  public StandardPacket createReply(String message) {
    return new Send(message,id).createPacket();
  }
  
  public String getMessage() {return content;}
  public int getTime() {return time;}
  public SessionView getSession() {return sender;}
  public String getParentId() {return id;}
}
