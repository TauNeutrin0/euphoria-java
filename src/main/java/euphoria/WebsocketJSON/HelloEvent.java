package euphoria.WebsocketJSON;

public class HelloEvent extends DataPacket{
  private String id;
  private PersonalAccountView account;
  private SessionView session;
  private boolean account_has_access;
  private boolean account_email_verified;
  private boolean room_is_private;
  private String version;
  
  public HelloEvent() {}
  
  public SessionView getSession() {return session;}
  public String getId() {return id;}
}
