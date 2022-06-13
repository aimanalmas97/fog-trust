package com.trust.fog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerReader {
  private Path path;
  private String requestor;

  private String currentServer;
  private List<String> recommendorsList;
  private Map<String, Server> serverMap;

  public ServerReader(Path path) {
    this.path = path;
    requestor = null;
    currentServer = null;
    recommendorsList = new ArrayList<>();
    serverMap = new HashMap<>();
  }

  public void readFromServers() throws IOException {
    List<String> lines = Files.lines(path).collect(Collectors.toList());

    for (String line : lines) {
      if (line.equals("")) {
          continue;
      }
      String[] parts = line.split(":");
      if (parts.length != 1) {
          String key = parts[0], value = parts[1];
          if (key.equals("All Servers")) {
              String[] allServers = value.trim().replace("[", "")
              .replace("]", "").split(", ");
              initializeServers(serverMap, allServers);
          } else if (key.equals("Requestor")) {
              requestor = value.trim();
              currentServer = requestor;
          } else if (key.equals("Service Provider")) {
              // serviceProvider = value.trim();
          } else if (key.equals("Recommendors")) {
              String recommendors = value.trim().replace("[", "")
              .replace("]", "");
              recommendorsList = Arrays.asList(recommendors.split(", "));
          } else if (key.equals("Servers")) {
              String[] servers = value.trim()
              .replace("[", "")
              .replace("]", "")
              .split(", ");
              addServers(currentServer, servers, serverMap);
          } else if (key.equals("Locations")) {
              String[] locations = value.trim()
              .replace("[", "")
              .replace("]", "")
              .split(", ");
              addLocations(currentServer, locations, serverMap);
          } else if (key.equals("Services")) {
              String[] services = value.trim()
              .replace("[", "")
              .replace("]", "")
              .split(", ");
              addServices(currentServer, services, serverMap);
          } else if (key.equals("Direct Trust")) {
              serverMap.get(currentServer).setDirectTrust(Float.parseFloat(value.trim()));
          }
      } else {
          currentServer = parts[0];
      }
    }
  }

  private void addServices(String currentServer, String[] services, Map<String, Server> sMap) {
    Server current = sMap.get(currentServer);
    for (String service : services) {
        current.addService(service);
    }
  }

  private void addLocations(String currentServer, String[] locations, Map<String, Server> sMap) {
    Server current = sMap.get(currentServer);
    for (String server : locations) {
        current.addContacts(sMap.get(server));
    }
  }

  private void addServers(String currentServer, String[] servers, Map<String, Server> sMap) {
    Server current = sMap.get(currentServer);
    for (String server : servers) {
        current.addServer(sMap.get(server));
    }
  }

  private void initializeServers(Map<String, Server> sMap, String[] allServers) {
    for (String server : allServers) {
        sMap.put(server, new Server(server));
    }
  }

  public String getRequestor() {
    return this.requestor;
  }

  public String getCurrentServer() {
    return this.currentServer;
  }

  public List<String> getRecommendorsList() {
    return this.recommendorsList;
  }

  public Map<String,Server> getServerMap() {
    return this.serverMap;
  }
  
}