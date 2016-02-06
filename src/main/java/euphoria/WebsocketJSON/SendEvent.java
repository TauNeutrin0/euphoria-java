package euphoria.WebsocketJSON;

public class SendEvent extends DataPacket{
  String id;
  String parent;
  String previous_edit_id;
  int time;
  SessionView sender;
  String content;
  String encryption_key_id;
  int edited;
  int deleted;
  boolean truncated;
  
  public SendEvent() {}
  
  public StandardPacket createReply(String message) {
    return new Send(message,id).createPacket();
  }
  
  public String getMessage() {return content;}
  public int getTime() {return time;}
  public SessionView getSession() {return sender;}
  public String getParentId() {return id;}
}
