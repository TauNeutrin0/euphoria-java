package euphoria;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileIO {
  private JsonObject jsonObject;
  String path = "default.json";
  
  public FileIO(String fileName) {
    path=System.getProperty("user.dir")+"\\"+fileName+".json";
    try {
      JsonParser parser = new JsonParser();
      JsonElement jsonElement = parser.parse(new FileReader(path));
      jsonObject = jsonElement.getAsJsonObject();
    } catch (FileNotFoundException e) {
      File f = new File(path);
      //f.getParentFile().mkdirs(); 
      try {
        f.createNewFile();
        File file = new File(path);
        byte[] myBytes = "{}".getBytes();
        FileOutputStream fileStream = new FileOutputStream(file, false);
        fileStream.write(myBytes);
        fileStream.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } catch (IllegalStateException e) {
      File file = new File(path);
      byte[] myBytes = "{}".getBytes();
      FileOutputStream fileStream;
      try {
        fileStream = new FileOutputStream(file, false);
        fileStream.write(myBytes);
        fileStream.close();
      } catch (FileNotFoundException e1) {
        e1.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }
  
  public JsonObject getJson() {
    try {
      JsonParser parser = new JsonParser();
      JsonElement jsonElement = parser.parse(new FileReader(path));
      jsonObject = jsonElement.getAsJsonObject();
    } catch (FileNotFoundException e) {
      //Shouldn't happen, unless file is deleted.
      
    }
    return jsonObject;
  }
  
  public void setJson(JsonObject obj) throws IOException {
    Gson gson = new Gson();
    File file = new File(path);
    byte[] myBytes = gson.toJson(obj).getBytes();
    FileOutputStream fileStream = new FileOutputStream(file, false);
    fileStream.write(myBytes);
    fileStream.close();
  }
}
