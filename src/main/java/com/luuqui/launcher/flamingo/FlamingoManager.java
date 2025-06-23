package com.luuqui.launcher.flamingo;

import com.google.inject.Singleton;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.util.FileUtil;
import com.luuqui.util.RequestUtil;
import com.luuqui.util.SystemUtil;
import com.luuqui.util.TextUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.luuqui.launcher.flamingo.Log.log;

@Singleton
public class FlamingoManager
{
  @SuppressWarnings("all")
  private final String ENDPOINT = "flamingo.luuqui.com";

  @SuppressWarnings("all")
  private final int PORT = 6060;

  private List<Server> serverList = new ArrayList<>();
  private Server selectedServer = null;
  private boolean online = false;

  public FlamingoManager () { }

  public void init ()
  {

  }

  public List<Server> fetchServerList ()
  {
    List<Server> servers = new ArrayList<>();

    try {
      JSONObject response = sendRequest("GET", "/server-list/", new String[]{ "machineId=" + SystemUtil.getHashedMachineId() });
      log.info("Got server list from flamingo");

      // we got an empty server list, so empty we return it.
      if(response.toString().equalsIgnoreCase("{}")) return servers;

      for(Object serverJsonObj : response.getJSONArray("serverlist")) {
        JSONObject serverJson = (JSONObject) serverJsonObj;

        Server server = new Server();
        server.name = serverJson.getString("name");
        server.description = serverJson.getString("description");
        server.managedBy = serverJson.getString("managedBy");
        server.beta = serverJson.getInt("beta");
        server.version = serverJson.getString("version");
        server.deployMethod = serverJson.getInt("deployMethod");
        server.deployUrl = serverJson.getString("deployUrl");
        server.playerCountUrl = serverJson.getString("playerCountUrl");
        server.siteUrl = serverJson.getString("siteUrl");
        server.communityUrl = serverJson.getString("communityUrl");
        server.sourceCodeUrl = serverJson.getString("sourceCodeUrl");
        server.announceType = serverJson.getString("announceType");
        server.announceBanner = serverJson.getString("announceBanner");
        server.announceContent = serverJson.getString("announceContent");
        server.announceBannerLink = serverJson.getString("announceBannerLink");
        server.announceBannerStartsAt = serverJson.getLong("announceBannerStartsAt");
        server.announceBannerEndsAt = serverJson.getLong("announceBannerEndsAt");
        server.fromCode = serverJson.getString("fromCode");
        server.serverIcon = serverJson.getString("serverIcon");
        server.enabled = serverJson.getInt("enabled");

        servers.add(server);
      }
    } catch (Exception e) {
      log.error(e);
    }

    return servers;
  }

  public String activateBetaCode (String code)
  {
    try {
      JSONObject response = sendRequest("POST", "/beta-code/activate/" + code, new String[]{"machineId=" + SystemUtil.getHashedMachineId()});
      log.info("Got response for beta code activation: " + response);

      return response.getString("result");
    } catch (Exception e) {
      log.error(e);
      return "failure";
    }
  }

  public Status getStatus ()
  {
    try {
      JSONObject response = sendRequest("GET", "/status/", new String[]{});
      log.info("Got status from flamingo: " + response);

      Status status = new Status();
      status.version = response.getString("version");
      status.uptime = response.getLong("uptime");

      return status;
    } catch (Exception e) {
      log.error(e);
      return new Status();
    }
  }

  private JSONObject sendRequest (String method, String endpoint, String[] request)
      throws Exception
  {
    try {
      request = Arrays.copyOf(request, request.length + 1);
      request[request.length - 1] = "version=" + TextUtil.extractNumericFromString(LauncherGlobals.LAUNCHER_VERSION);
      return RequestUtil.makeRequest(method, "http://" + ENDPOINT + ":" + PORT + endpoint, request);
    } catch (Exception e) {
      throw new Exception();
    }
  }

  public Server findServerByName (String serverName)
  {
    List<Server> results = getServerList().stream()
        .filter(s -> serverName.equals(s.name)).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
  }

  public Server findServerBySanitizedName (String sanitizedServerName)
  {
    List<Server> results = getServerList().stream()
        .filter(s -> sanitizedServerName.equals(s.getSanitizedName())).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
  }

  public String getLocalGameVersion ()
  {
    try {
      return FileUtil.readFile(this.selectedServer.getRootDirectory() + File.separator + "version.txt").trim();
    } catch (IOException e) {
      log.error(e);
    }
    return "-1";
  }

  public List<Server> getServerList ()
  {
    return this.serverList;
  }

  public void setServerList (List<Server> serverList)
  {
    this.serverList = serverList;
  }

  public Server getSelectedServer ()
  {
    return this.selectedServer;
  }

  public void setSelectedServer (Server server)
  {
    this.selectedServer = server;
  }

  public boolean getOnline ()
  {
    return this.online;
  }

  public void setOnline (boolean online)
  {
    this.online = online;
  }

}
