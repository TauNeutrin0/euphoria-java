package euphoria.WebsocketJSON;

public class SessionView {
  private String id;
  private String name;
  private String server_id;
  private String server_era;
  private String session_id;
  private String is_staff;
  private String is_manager;
  private String client_address;
  private String real_client_address;
  
  public SessionView() {}
  
  public String getName() {return name;}
  public String getId() {return id;}
}
