package euphoria.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import euphoria.Bot;
import euphoria.events.PacketEvent;

public class StandardEventListener implements PacketEventListener{
  String nick;
  String helpText;
  Bot bot;
  public StandardEventListener(Bot bot, String nick, String helpText){
    this.bot=bot;
    this.nick=nick;
    this.helpText=helpText;
  }
  @Override
  public void onSendEvent(MessageEvent evt) {
    if(evt.getMessage().matches("^!ping(?: @"+nick+")?$")){
      evt.reply("Pong!");
    }
    if(evt.getMessage().matches("^!help @"+nick+"$")){
      evt.reply(helpText);
    }
    if(evt.getMessage().matches("^!kill @"+nick+"$")){
      evt.reply("/me is now exiting.");
      evt.getRoomConnection().closeConnection("Killed by room user.");
    }
    if(evt.getMessage().matches("^!pause @"+nick+"$")){
      evt.reply("/me has been paused.");
      evt.getRoomConnection().pause(nick);
    }
    if(evt.getMessage().matches("^!uptime @"+nick+"$")){
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
      
      Calendar upDate = bot.getStartupTime();
      long upTime = (new GregorianCalendar(TimeZone.getTimeZone("UTC"))).getTimeInMillis()-upDate.getTimeInMillis();
      String upTimeStr = TimeUnit.MILLISECONDS.toDays(upTime)+"d "+
        TimeUnit.MILLISECONDS.toHours(upTime) % TimeUnit.DAYS.toHours(1)+"h "+
        TimeUnit.MILLISECONDS.toMinutes(upTime) % TimeUnit.HOURS.toMinutes(1)+"m "+
        TimeUnit.MILLISECONDS.toSeconds(upTime) % TimeUnit.MINUTES.toSeconds(1)+"."+
        String.format("%03d", upTime % TimeUnit.SECONDS.toMillis(1))+"s";
      evt.reply("/me has been up since "+sdf.format(upDate.getTime())+" UTC ("+upTimeStr+").");
      
      upDate = evt.getRoomConnection().getStartupTime();
      upTime = (new GregorianCalendar(TimeZone.getTimeZone("UTC"))).getTimeInMillis()-upDate.getTimeInMillis();
      upTimeStr = TimeUnit.MILLISECONDS.toDays(upTime)+"d "+
        TimeUnit.MILLISECONDS.toHours(upTime) % TimeUnit.DAYS.toHours(1)+"h "+
        TimeUnit.MILLISECONDS.toMinutes(upTime) % TimeUnit.HOURS.toMinutes(1)+"m "+
        TimeUnit.MILLISECONDS.toSeconds(upTime) % TimeUnit.MINUTES.toSeconds(1)+"."+
        String.format("%03d", upTime % TimeUnit.SECONDS.toMillis(1))+"s";
      evt.reply("/me has been online in this room since "+sdf.format(upDate.getTime())+" UTC ("+upTimeStr+").");
    }
  }
  @Override
  public void onSnapshotEvent(PacketEvent evt) {
    evt.getRoomConnection().changeNick(nick);
  }
  public void onHelloEvent(PacketEvent evt) {}
  @Override
  public void onNickEvent(PacketEvent evt) {}
  @Override
  public void onJoinEvent(PacketEvent evt) {}
  @Override
  public void onPartEvent(PacketEvent evt) {}
  @Override
  public void onBounceEvent(PacketEvent arg0) {}
  @Override
  public void packetRecieved(PacketEvent evt) {}
}
