package com.lucasluqui.launcher.flamingo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.launcher.*;
import com.lucasluqui.launcher.flamingo.data.Event;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.flamingo.data.Status;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.launcher.mod.ui.ModListUI;
import com.lucasluqui.launcher.setting.ui.SettingsUI;
import com.lucasluqui.launcher.ui.LauncherUI;
import com.lucasluqui.util.*;
import org.json.JSONArray;
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
    _machineId = SystemUtil.getHashedMachineId(localId);

    // Make sure we at least have the official server on init.
    Server official = new Server("Official");
    _serverList.add(official);
    _selectedServer = official;
  }

  public void load ()
  {
    getStatus();

    if (_status == null) {
      log.info("Flamingo status is null, we are offline!");
      setAsOffline();

      // Still at least TRY to get GitHub data, perhaps the issue is on flamingo's side?
      // Helpful too in case we need to update the launcher with flamingo off.
      _ctx.getApp().fetchGithubData();

      return;
    }

    updateServerList();
    _ctx.getApp().fetchGithubData();
  }

  public List<Server> fetchServerList ()
  {
    List<Server> servers = new ArrayList<>();

    try {
      JSONObject response = sendRequest("GET", "/server-list/", new String[]{"machineId=" + this._machineId});
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
        server.defaultEvent = new Event(
          serverJson.getString("defaultEventType"),
          serverJson.getString("defaultEventBanner"),
          serverJson.getString("defaultEventContent"),
          serverJson.getString("defaultEventLink"),
          serverJson.getLong("defaultEventStartsAt"),
          serverJson.getLong("defaultEventEndsAt")
        );

        List<Event> events = new ArrayList<>();
        if (!serverJson.getJSONArray("events").isEmpty()) {
          for (int i = 0; i < serverJson.getJSONArray("events").length(); i++) {
            JSONObject eventJson = serverJson.getJSONArray("events").getJSONObject(i);
            Event event = new Event(
              eventJson.getString("type"),
              eventJson.getString("banner"),
              eventJson.getString("content"),
              eventJson.getString("link"),
              eventJson.getLong("startsAt"),
              eventJson.getLong("endsAt")
            );
            events.add(event);
          }
        }
        server.events = events;

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
      JSONObject response = sendRequest("POST", "/beta-code/activate/" + code, new String[]{"machineId=" + this._machineId});
      log.info("Got response for beta code activation: " + response);

      return response.getString("result");
    } catch (Exception e) {
      log.error(e);
      return "failure";
    }
  }

  public Status getStatus ()
  {
    if (this._status != null) {
      return this._status;
    }

    try {
      JSONObject response = sendRequest("GET", "/status/", new String[]{});
      log.info("Got status from flamingo: " + response);

      Status status = new Status();
      status.version = response.getString("version");
      status.uptime = response.getLong("uptime");

      this._status = status;

      return status;
    } catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  private void setAsOffline ()
  {
    _ctx.getApp().getUI(LauncherUI.class)
      .showWarning(_localeManager.getValue("error.flamingo_offline"));
  }

  /**
   * Fetches the current player count from Steam and stores the estimated total in the given official server.
   *
   * @return The player count, or null when Steam didn't give us one.
   */
  public PlayerCount fetchOfficialPlayerCount (Server official)
  {
    PlayerCount playerCount = _ctx.getApp().getPlayerCount();
    official.playerCountUrl = playerCount == null ? "???" : "~" + playerCount.getEstimated() + " ";
    return playerCount;
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
      String buildString = ZipUtil.readFileInsideZip(this._selectedServer.getRootDirectory() + File.separator + "code/config.jar", "build.properties");
      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(buildString.getBytes(StandardCharsets.UTF_8)));
      String version = properties.getProperty("version");
      properties.clear();
      return version;
    } catch (IOException e) {
      try {
        String version = FileUtil.readFile(this._selectedServer.getRootDirectory() + File.separator + "version.txt").trim();
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
          PlayerCount playerCount = fetchOfficialPlayerCount(official);

          // TODO: Too hacky, why not just have the values lie in the server object itself?
          if (playerCount != null) _ctx.getApp().getUI(LauncherUI.class).updatePlayerCountTooltip(playerCount);

          official.maintenanceStartsAt = server.maintenanceStartsAt;
          official.maintenanceEndsAt = server.maintenanceEndsAt;
          official.defaultEvent = server.defaultEvent;
          official.events = server.events;
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
        registerServer(server);
      }
    } else {
      // Offline: only Official and manually added servers are available.
      log.info("Could not reach the server list, keeping manually added servers only");
    }

    // Manually added servers are always available, whether we're online or not.
    for (Server custom : loadCustomServers()) {
      if (findServerByName(custom.name) != null) {
        log.info("Tried to add duplicate server", "server", custom.name);
        continue;
      }

      serverList.add(custom);
      registerServer(custom);
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

    _ctx.getApp().selectedServerChanged();
  }

  /**
   * Makes sure a server has its folder structure, a clean base zip and its server-specific settings keys.
   */
  private void registerServer (Server server)
  {
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
    _ctx.getApp().getUI(SettingsUI.class).eventHandler.checkServerSettingsKeys(serverName);
    _ctx.getApp().getUI(ModListUI.class).eventHandler.checkServerSettingsKeys(serverName);
  }

  /**
   * Loads the servers the user added by hand. These are stored as JSON in the launcher
   * properties file, which survives launcher updates (the updater only replaces the jar).
   */
  public List<Server> loadCustomServers ()
  {
    List<Server> servers = new ArrayList<>();
    String raw = _settingsManager.getValue(CUSTOM_SERVERS_KEY, "");
    if (raw.trim().isEmpty()) return servers;

    try {
      JSONArray array = new JSONArray(raw);
      for (int i = 0; i < array.length(); i++) {
        JSONObject json = array.getJSONObject(i);
        String name = json.optString("name", "").trim();
        if (name.isEmpty() || name.equalsIgnoreCase("Official")) continue;

        Server server = new Server(name);
        server.useStockClient = true;
        server.enabled = json.optInt("enabled", 1);
        server.description = "";
        server.managedBy = "";
        server.version = "";
        server.deployUrl = "null";
        server.playerCountUrl = "null";
        server.siteUrl = "null";
        server.communityUrl = "null";
        server.sourceCodeUrl = "null";
        server.serverIcon = "null";
        servers.add(server);
      }
    } catch (Exception e) {
      log.error("Failed to parse manually added servers", e);
    }

    return servers;
  }

  /**
   * Persists a manually added server and makes it immediately available in the switcher.
   */
  public void addCustomServer (Server server)
  {
    server.useStockClient = true;
    List<Server> servers = loadCustomServers();
    for (Server existing : servers) {
      if (existing.name.equalsIgnoreCase(server.name)) return;
    }
    servers.add(server);
    persistCustomServers(servers);

    getServerList().add(server);
    registerServer(server);
    setSelectedServer(server);
  }

  /**
   * Removes a manually added server and falls back to the official server if it was selected.
   */
  public void removeCustomServer (Server server)
  {
    List<Server> servers = loadCustomServers();
    servers.removeIf(existing -> existing.name.equalsIgnoreCase(server.name));
    persistCustomServers(servers);

    getServerList().remove(server);
    if (getSelectedServer() == server) {
      Server official = findServerByName("Official");
      if (official != null) setSelectedServer(official);
    }
    _ctx.getApp().selectedServerChanged();
  }

  public boolean isCustomServer (Server server)
  {
    if (server == null) return false;
    for (Server custom : loadCustomServers()) {
      if (custom.name.equalsIgnoreCase(server.name)) return true;
    }
    return false;
  }

  private void persistCustomServers (List<Server> servers)
  {
    JSONArray array = new JSONArray();
    for (Server server : servers) {
      JSONObject json = new JSONObject();
      json.put("name", server.name);
      json.put("enabled", server.enabled);
      array.put(json);
    }
    _settingsManager.setValue(CUSTOM_SERVERS_KEY, array.toString());
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
      request[request.length - 1] = "version=" + TextUtil.extractNumeric(BuildConfig.getVersion());
      return RequestUtil.makeRequest(method, "http://" + ADDRESS + ":" + PORT + endpoint, request);
    } catch (Exception e) {
      log.error("Request failed");
      log.error(e);
    }
    return null;
  }

  public List<Server> getServerList ()
  {
    return this._serverList;
  }

  public void setServerList (List<Server> serverList)
  {
    this._serverList = serverList;
  }

  public Server getSelectedServer ()
  {
    return this._selectedServer;
  }

  public void setSelectedServer (Server server)
  {
    if (server == null) {
      log.warning("Tried to set a null server, what??");
      return;
    }

    this._selectedServer = server;
    _ctx.getApp().selectedServerChanged();
    saveSelectedServer(server.getSanitizedName().toLowerCase());
  }

  public boolean isOnline ()
  {
    return this._status != null;
  }

  public void setStatus (Status status) {
    this._status = status;
  }

  public String getThirdPartyBaseDir ()
  {
    return THIRD_PARTY_BASE_DIR;
  }

  @Inject protected LauncherContext _ctx;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected LocaleManager _localeManager;

  protected List<Server> _serverList = new ArrayList<>();
  protected Server _selectedServer = null;
  protected String _machineId = null;
  protected Status _status = null;

  private final String ADDRESS = DeployConfig.getFlamingoAddress();
  private final int PORT = DeployConfig.getFlamingoPort();
  private final String THIRD_PARTY_BASE_DIR = LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator;

  // Manually added servers, kept in the launcher properties so they survive updates.
  private static final String CUSTOM_SERVERS_KEY = "launcher.customServers";
}
