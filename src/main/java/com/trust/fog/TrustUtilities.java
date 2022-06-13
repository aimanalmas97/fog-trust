package com.trust.fog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TrustUtilities {

  static float weightedTotalTrust(float weightedDirect, float weightedIndirect) {
    return weightedDirect + weightedIndirect;
  }

  static float getWeightedDirectTrust(float direct, float weight) {
    return direct * weight;
  }

  static float getWeightedIndirectTrust(float indirect, float weight) {
    return indirect * (1 - weight);
  }

  static float getEntropyWeightedIndirectTrust(float indirect, float weight) {
    return indirect * weight;
  }

//  static float getWeight(float direct, float indirect, float total, float experience) {
//    float weight = 0;
//    if (direct - experience < 0.01f && direct != indirect) {
//      weight = 0.81f + (new Random()).nextFloat() * (1.0f - 0.81f);
//    } else if (total - experience < 0.01f && (indirect - experience) > (direct - experience)) {
//      weight = 0.61f + (new Random()).nextFloat() * (0.8f - 0.61f);
//    } else if (direct - experience < 0.01f &&(indirect - experience) < 0.01f && direct == indirect) {
//      weight = 0.41f + (new Random()).nextFloat() * (0.6f - 0.41f);
//    } else if (total - experience < 0.01f &&(indirect - experience) < (direct - experience)) {
//      weight = 0.21f + (new Random()).nextFloat() * (0.4f - 0.21f);
//    }  else if (indirect - experience < 0.01f && direct != indirect) {
//      weight = 0 + (new Random()).nextFloat() * (0.2f - 0f);
//    }
//    return weight;
//  }

  static float getEntropyDirectWeight(float direct, float indirect) {
    float hd = getHDirect(direct, indirect);
    float hr = getHIndirect(direct, indirect);
    float directTrustWeight = (1 - (hd / ((float)(Math.log(direct) / Math.log(2))))) / ((1 - (hd / ((float)(Math.log(direct) / Math.log(2))))) + (1 - (hr / ((float)(Math.log(indirect) / Math.log(2))))));
    return directTrustWeight;
  }

  static float getEntropyIndirectWeight(float direct, float indirect) {
    float hd = getHDirect(direct, indirect);
    float hr = getHIndirect(direct, indirect);
    float indirectTrustWeight = (1 - (hr / ((float)(Math.log(indirect) / Math.log(2))))) / ((1 - (hd / ((float)(Math.log(direct) / Math.log(2))))) + (1 - (hr / ((float)(Math.log(indirect) / Math.log(2))))));
    return indirectTrustWeight;
  }

  static float getHDirect(float direct, float indirect) {
    return (direct * (float)(Math.log(direct) / Math.log(2)) * -1) - ((1 - direct) * (float)(Math.log(1 - direct) / Math.log(2)));
  }

  static float getHIndirect(float direct, float indirect) {
    return (indirect * (float)(Math.log(indirect) / Math.log(2)) * -1) - ((1 - indirect) * (float)(Math.log(1 - indirect) / Math.log(2)));
  }

  static float totalTrust(float direct, float indirect) {
    return direct + indirect;
  }

  static float directTrust(float experience, int time, float pastPositiveExp, float pastNegativeExp) {
    float positiveExp = getPositiveExperience(experience, time, pastPositiveExp, pastNegativeExp);
    float negativeExp = getNegativeExperience(experience, time, pastPositiveExp, pastNegativeExp);
    return positiveExp / (positiveExp + negativeExp);
  }

  static float getPositiveExperience(float experience, int time, float pastPositiveExp, float pastNegativeExp) {
    float decay = 0.001f;
    return ((float) Math.exp(-decay * time) * pastPositiveExp) + experience;
  }

  static float getNegativeExperience(float experience, int time, float pastPositiveExp, float pastNegativeExp) {
    float decay = 0.001f;
    return ((float) Math.exp(-decay * time) * pastNegativeExp) + (1 - experience);
  }

  static float indirectTrust(Server requestor, float directTrust) {
    List<Float> trust = new ArrayList<>();
    float shortlistedTotalSimilarities = requestor.getTotalSimilarities().values().stream().reduce(0.0f, Float::sum);
    requestor.getTotalSimilarities().forEach((key, value) -> {
      trust.add((value / shortlistedTotalSimilarities) * key.getDirectTrust());
    });
    return trust.stream().reduce(0.0f, Float::sum);
  }

  static void finalCardinality(Server requestor, List<Server> servers, Method method) {
    shortList(requestor, servers, method);
    for (Map.Entry<Server, Float> server : requestor.getShortListedServicesServers().entrySet()) {
      float totalSimilarity = requestor.getShortListedServicesServers().get(server.getKey()) +
        requestor.getShortlistedContacts().get(server.getKey()) +
        requestor.getShortlistedServiceProviders().get(server.getKey());
      requestor.addTotalSimilarities(server.getKey(), totalSimilarity);
    }
  }

  static void shortList(Server requestor, List<Server> servers, Method method) {
    shortList(requestor, servers, ShortList.RECOMMENDOR, method, (s, c) -> {
      requestor.addShortlistedServer(s, c);
      return null;
    });

    List<Server> shortListedServers = new ArrayList<>(requestor.getShortlistedServiceProviders().keySet());
    shortList(requestor, shortListedServers, ShortList.CONTACT, method, (s, c) -> {
      requestor.addShortlistedContact(s, c);
      return null;
    });

    List<Server> shortListedContacts = new ArrayList<>(requestor.getShortlistedContacts().keySet());
    shortList(requestor, shortListedContacts, ShortList.SERVICE, method, (s, c) -> {
      requestor.addshortListedServicesServers(s, c);
      return null;
    });
  }

  static void shortList(Server requestor,
        List<Server> servers,
        ShortList shortLister,
        Method method,
        BiFunction<Server, Float, Void> shortlister) {
    List<String> requestorsServerNames = shortLister == ShortList.RECOMMENDOR ?
      serversToNames(requestor.getServers()) :
      shortLister == ShortList.CONTACT ?
      serversToNames(requestor.getContacts()) :
      requestor.getServices();
    for (Server server : servers) {
      List<String> recommendorsServerNames = shortLister == ShortList.RECOMMENDOR ?
        serversToNames(server.getServers()) :
        shortLister == ShortList.CONTACT ?
        serversToNames(server.getContacts()) :
        server.getServices();
      int intersection = getIntersection(requestorsServerNames, recommendorsServerNames);
      int union = getUnion(requestorsServerNames, recommendorsServerNames);
      float similarity = method == Method.JACCARD ?
          (float) intersection / (float) union :
          (float) intersection / (float) Math.sqrt(requestorsServerNames.size() * recommendorsServerNames.size());
      String similarityType = shortLister == ShortList.CONTACT ?
          "Social Contacts" :
          shortLister == ShortList.RECOMMENDOR ?
          "Server Similarity" :
          "Service Similarity";
      System.out.println(similarityType + " Similarity with " + server.getName() + " using " + method + " is " + similarity);
      if (similarity >= 0.5) {
        shortlister.apply(server, similarity);
      }
    }
  }

  static int getUnion(List<String> requestorsServers, List<String> recommendorsServers) {
    Set<String> unionSet = new HashSet<String>(requestorsServers);
    unionSet.addAll(recommendorsServers);
    return unionSet.size();
  }

  static int getIntersection(List<String> requestorsServers, List<String> recommendorsServers) {
    Set<String> intersectionSet = new HashSet<String>(requestorsServers);
    intersectionSet.retainAll(recommendorsServers);
    return intersectionSet.size();
  }

  static List<String> serversToNames(List<Server> servers) {
    return servers.stream().map(Server::getName).collect(Collectors.toList());
  }
  
}