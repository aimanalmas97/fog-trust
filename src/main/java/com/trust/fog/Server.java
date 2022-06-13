package com.trust.fog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
  private String name;
  private List<Server> servers;
  private Map<Server, Float> shortlistedServiceProviders;
  private List<Server> contacts;
  private Map<Server, Float> shortlistedContacts;
  private List<String> services;
  private Map<Server, Float> shortListedServicesServers;
  private Map<Server, Float> totalSimilarities;
  private Float directTrust;


  public Server(String name) {
    this.name = name;
    shortListedServicesServers = new HashMap<>();
    shortlistedServiceProviders = new HashMap<>();
    totalSimilarities = new HashMap<>();
    shortlistedContacts = new HashMap<>();
  }

  public Float getDirectTrust() {
    return directTrust;
  }

  public void setDirectTrust(Float directTrust) {
    this.directTrust = directTrust;
  }

  public Map<Server, Float> getTotalSimilarities() {
    return totalSimilarities;
  }

  public Map<Server, Float> getShortListedServicesServers() {
    return shortListedServicesServers;
  }

  public List<String> getServices() {
    return this.services;
  }

  public String getName() {
    return name;
  }

  public List<Server> getServers() {
    return servers;
  }

  public List<Server> getContacts() {
    return this.contacts;
  }

  public Map<Server, Float> getShortlistedContacts() {
    return this.shortlistedContacts;
  }

  public Map<Server, Float> getShortlistedServiceProviders() {
    return shortlistedServiceProviders;
  }

  public void setServices(List<String> services) {
    this.services = services;
  }

  public void addService(String service) {
    if (services == null) {
      services = new ArrayList<>();
    }
    this.services.add(service);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }

  public void addServer(Server server) {
    if (servers == null) {
      servers = new ArrayList<>();
    }
    this.servers.add(server);
  }

  public void setShortlistedServers(Map<Server, Float> shortlistedServers) {
    this.shortlistedServiceProviders = shortlistedServers;
  }

  public void setContacts(List<Server> contacts) {
    this.contacts = contacts;
  }

  public void addContacts(Server contact) {
    if (contacts == null) {
      contacts = new ArrayList<>();
    }
    this.contacts.add(contact);
  }

  public void addShortlistedContact(Server server, Float similarity) {
    if (shortlistedContacts == null) {
      shortlistedContacts = new HashMap<>();
    }
    this.shortlistedContacts.put(server, similarity);
  }

  public void addShortlistedServer(Server server, Float similarity) {
    if (shortlistedServiceProviders == null) {
      shortlistedServiceProviders = new HashMap<>();
    }
    shortlistedServiceProviders.put(server, similarity);
  }

  public void addshortListedServicesServers(Server server, Float similarity) {
    if (this.shortListedServicesServers == null) {
      this.shortListedServicesServers = new HashMap<>();
    }
    this.shortListedServicesServers.put(server, similarity);
  }

  public void addTotalSimilarities(Server server, Float similarity) {
    if (this.totalSimilarities == null) {
      this.totalSimilarities = new HashMap<>();
    }
    this.totalSimilarities.put(server, similarity);
  }

}
