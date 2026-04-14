package com.lucasluqui.launcher.flamingo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.launcher.*;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.flamingo.data.Status;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.launcher.ui.ModListUI;
import com.lucasluqui.launcher.ui.SettingsUI;
import com.lucasluqui.util.*;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.lucasluqui.launcher.flamingo.Log.log;

@Singleton
public class FlamingoManager
{
  public FlamingoManager ()
  {
    // empty.
  }

  public void init ()
  {
    String localId = getLocalId();
    this.machineId = SystemUtil.getHashedMachineId(localId);

    // Make sure we at least have the official server on init.
    Server official = new Server("Official");
    serverList.add(official);
    this.selectedServer = official;
  }

  public List<Server> fetchServerList ()
  {
    List<Server> servers = new ArrayList<>();

    try {
      JSONObject response = sendRequest("GET", "/server-list/", new String[]{"machineId=" + this.machineId});
      log.info("Got server list from flamingo");

      // we got an empty server list, so empty we return it.
      if (response.toString().equalsIgnoreCase("{}")) return servers;

      for (Object serverJsonObj : response.getJSONArray("serverlist")) {
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
        server.maintenanceStartsAt = serverJson.getLong("maintenanceStartsAt");
        server.maintenanceEndsAt = serverJson.getLong("maintenanceEndsAt");
        server.noticeTitle = serverJson.getString("noticeTitle");
        server.notice = serverJson.getString("notice");
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
      JSONObject response = sendRequest("POST", "/beta-code/activate/" + code, new String[]{"machineId=" + this.machineId});
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
      String buildString = ZipUtil.readFileInsideZip(this.selectedServer.getRootDirectory() + File.separator + "code/config.jar", "build.properties");
      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(buildString.getBytes(StandardCharsets.UTF_8)));
      String version = properties.getProperty("version");
      properties.clear();
      return version;
    } catch (IOException e) {
      try {
        String version = FileUtil.readFile(this.selectedServer.getRootDirectory() + File.separator + "version.txt").trim();
        return version;
      } catch (IOException ex) {
        log.error(ex);
      }
      log.error(e);
    }
    return "-1";
  }

  public String getLocalId ()
  {
    _settingsManager.createKeyIfNotExists("launcher.key", TextUtil.getRandomAlphanumeric(64));

    // Make sure the key is valid and wasn't modified.
    if (_settingsManager.getValue("launcher.key").length() != 64) {
      _settingsManager.setValue("launcher.key", TextUtil.getRandomAlphanumeric(64));
    }

    return _settingsManager.getValue("launcher.key");
  }

  public void updateServerList ()
  {
    List<Server> serverList = getServerList();
    serverList.clear();

    Server official = new Server("Official");
    serverList.add(official);

    List<Server> newServerList = null;
    if (isOnline()) {
      newServerList = fetchServerList();
    }

    if (newServerList != null) {
      for (Server server : newServerList) {
        if (server.name.equalsIgnoreCase("Official")) {
          official.playerCountUrl = "~" + _ctx.getApp().getOfficialApproxPlayerCount() + " ";
          official.announceBanner = server.announceBanner;
          official.announceContent = server.announceContent;
          official.announceBannerLink = server.announceBannerLink;
          official.announceBannerStartsAt = server.announceBannerStartsAt;
          official.announceBannerEndsAt = server.announceBannerEndsAt;
          official.maintenanceStartsAt = server.maintenanceStartsAt;
          official.maintenanceEndsAt = server.maintenanceEndsAt;
          official.noticeTitle = server.noticeTitle;
          official.notice = server.notice;
          continue;
        }

        // Prevent from adding duplicate servers
        if (findServerByName(server.name) != null) {
          log.info("Tried to add duplicate server", "server", server.name);
          continue;
        }

        if (server.beta == 1) server.name += " (Beta)";

        serverList.add(server);

        // make sure we have a proper folder structure for this server.
        String serverName = server.getSanitizedName();
        FileUtil.createDir(getThirdPartyBaseDir() + serverName);
        FileUtil.createDir(getThirdPartyBaseDir() + serverName + "/mods");

        // make sure there's a base zip file we can use to clean files with.
        String rootDir = server.getRootDirectory();
        if (FileUtil.fileExists(rootDir + "/rsrc")
          && !FileUtil.fileExists(rootDir + "/rsrc/base.zip")) {
          try {
            ZipUtil.zipFolderContents(new File(rootDir + "/rsrc"), new File(rootDir + "/rsrc/base.zip"), "base.zip");
          } catch (Exception e) {
            log.error(e);
          }
        }

        // check server specific settings keys.
        ((SettingsUI) _ctx.getApp().getUI("settings")).eventHandler.checkServerSettingsKeys(serverName);
        ((ModListUI) _ctx.getApp().getUI("modlist")).eventHandler.checkServerSettingsKeys(serverName);
      }
      setServerList(serverList);

      try {
        // Shouldn't sanitized name from Official just return 'official' instead of null? or empty??
        Server previousSelectedServer = findServerBySanitizedName(Settings.selectedServerName);
        setSelectedServer(previousSelectedServer == null ? official : previousSelectedServer);
      } catch (Exception e) {
        log.error(e);
        setSelectedServer(official);
      }
    } else {
      setSelectedServer(official);
    }

    _ctx.getApp().selectedServerChanged();
  }

  public void saveSelectedServer ()
  {
    String serverName = getSelectedServer().getSanitizedName();
    if (serverName.isEmpty()) serverName = "official";
    _settingsManager.setValue("launcher.selectedServerName", serverName);
  }

  public void saveSelectedServer (String serverName)
  {
    if (serverName.isEmpty()) serverName = "official";
    _settingsManager.setValue("launcher.selectedServerName", serverName);
  }

  private JSONObject sendRequest (String method, String endpoint, String[] request)
  {
    try {
      request = Arrays.copyOf(request, request.length + 1);
      request[request.length - 1] = "version=" + TextUtil.extractNumericFromString(BuildConfig.getVersion());
      return RequestUtil.makeRequest(method, "http://" + ADDRESS + ":" + PORT + endpoint, request);
    } catch (Exception e) {
      log.error("Request failed");
      log.error(e);
    }
    return null;
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
    if (server == null) {
      log.warning("Tried to set a null server, what??");
      return;
    }

    this.selectedServer = server;
    _ctx.getApp().selectedServerChanged();
    saveSelectedServer(server.getSanitizedName().toLowerCase());
  }

  public boolean isOnline ()
  {
    return this.online;
  }

  public void setOnline (boolean online)
  {
    this.online = online;
  }

  public String getThirdPartyBaseDir ()
  {
    return THIRD_PARTY_BASE_DIR;
  }

  @Inject protected LauncherContext _ctx;
  @Inject protected SettingsManager _settingsManager;

  private final String ADDRESS = DeployConfig.getFlamingoAddress();
  private final int PORT = DeployConfig.getFlamingoPort();

  private final String THIRD_PARTY_BASE_DIR = LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator;

  private List<Server> serverList = new ArrayList<>();
  private Server selectedServer = null;
  private String machineId = null;
  private boolean online = false;
}
