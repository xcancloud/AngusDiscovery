/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.eureka.server;

import static org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration.JACKSON_JSON;

import com.netflix.appinfo.AmazonInfo;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Pair;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.cluster.PeerEurekaNode;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.StatusResource;
import com.netflix.eureka.util.StatusInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Spencer Gibb
 * @author Gang Li
 * @author Weix Sun
 * @author XiaoLong Liu
 */
@Controller
@RequestMapping("${eureka.dashboard.path:/}")
public class EurekaController {

  @Value("${eureka.dashboard.path:/}")
  private String dashboardPath = "";

  private final ApplicationInfoManager applicationInfoManager;

  private final EurekaProperties eurekaProperties;

  public EurekaController(ApplicationInfoManager applicationInfoManager,
      EurekaProperties eurekaProperties) {
    this.applicationInfoManager = applicationInfoManager;
    this.eurekaProperties = eurekaProperties;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String status(HttpServletRequest request, Map<String, Object> model) {
    populateBase(request, model);
    populateApps(model);
    StatusInfo statusInfo = getStatusInfo();
    model.put("statusInfo", statusInfo);
    populateInstanceInfo(model, statusInfo);
    filterReplicas(model, statusInfo);
    return "eureka/status";
  }

  // Extension Method
  @RequestMapping(value = "/pubapi/v1/status", method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public String statusRest(HttpServletRequest request, Map<String, Object> model) {
    populateBase(request, model);
    populateApps(model);
    StatusInfo statusInfo = getStatusInfo();
    model.put("statusInfo", statusInfo);
    populateInstanceInfo(model, statusInfo);
    filterReplicas(model, statusInfo);
    // model.remove("registry");
    return JACKSON_JSON.encode(model);
  }

  private StatusInfo getStatusInfo() {
    StatusInfo statusInfo;
    try {
      statusInfo = new StatusResource().getStatusInfo();
      Field[] fs = statusInfo.getClass().getDeclaredFields();
      for (Field f : fs) {
        if ("isHeathly".equals(f.getName())) {
          f.setAccessible(true);
          f.set(statusInfo, InstanceStatus.UP.equals(statusInfo.getInstanceInfo().getStatus()));
        }
      }
    } catch (Exception e) {
      statusInfo = StatusInfo.Builder.newBuilder().isHealthy(false).build();
    }
    return statusInfo;
  }

  @RequestMapping(value = "/lastn", method = RequestMethod.GET)
  public String lastn(HttpServletRequest request, Map<String, Object> model) {
    populateBase(request, model);
    PeerAwareInstanceRegistryImpl registry = (PeerAwareInstanceRegistryImpl) getRegistry();
    ArrayList<Map<String, Object>> lastNCanceled = new ArrayList<>();
    List<Pair<Long, String>> list = registry.getLastNCanceledInstances();
    for (Pair<Long, String> entry : list) {
      lastNCanceled.add(registeredInstance(entry.second(), entry.first()));
    }
    model.put("lastNCanceled", lastNCanceled);
    list = registry.getLastNRegisteredInstances();
    ArrayList<Map<String, Object>> lastNRegistered = new ArrayList<>();
    for (Pair<Long, String> entry : list) {
      lastNRegistered.add(registeredInstance(entry.second(), entry.first()));
    }
    model.put("lastNRegistered", lastNRegistered);
    return "eureka/lastn";
  }

  // Extension Method
  @RequestMapping(value = "/pubapi/v1/lastn", method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public String lastnRest(HttpServletRequest request, Map<String, Object> model) {
    populateBase(request, model);
    PeerAwareInstanceRegistryImpl registry = (PeerAwareInstanceRegistryImpl) getRegistry();
    ArrayList<Map<String, Object>> lastNCanceled = new ArrayList<>();
    List<Pair<Long, String>> list = registry.getLastNCanceledInstances();
    for (Pair<Long, String> entry : list) {
      lastNCanceled.add(registeredInstance(entry.second(), entry.first()));
    }
    model.put("lastNCanceled", lastNCanceled);
    list = registry.getLastNRegisteredInstances();
    ArrayList<Map<String, Object>> lastNRegistered = new ArrayList<>();
    for (Pair<Long, String> entry : list) {
      lastNRegistered.add(registeredInstance(entry.second(), entry.first()));
    }
    model.put("lastNRegistered", lastNRegistered);
    // model.remove("registry");
    return JACKSON_JSON.encode(model);
  }

  private Map<String, Object> registeredInstance(String id, long date) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("date", new Date(date));
    return map;
  }

  protected void populateBase(HttpServletRequest request, Map<String, Object> model) {
    model.put("time", new Date());
    model.put("basePath", "/");
    model.put("dashboardPath", this.dashboardPath.equals("/") ? "" : this.dashboardPath);
    populateHeader(model);
    populateNavbar(request, model);
  }

  private void populateHeader(Map<String, Object> model) {
    model.put("currentTime", StatusResource.getCurrentTimeAsString());
    model.put("upTime", StatusInfo.getUpTime());
    model.put("environment", eurekaProperties.getEnvironment());
    model.put("datacenter", eurekaProperties.getDatacenter());
    //model.put("environment", applicationInfoManager.getInfo().getMetadata().get("environment"));
    //model.put("datacenter", applicationInfoManager.getInfo().getMetadata().get("datacenter"));
    PeerAwareInstanceRegistry registry = getRegistry();
    model.put("registry", registry);
    model.put("isBelowRenewThreshold", registry.isBelowRenewThresold() == 1);
    DataCenterInfo info = applicationInfoManager.getInfo().getDataCenterInfo();
    if (info.getName() == DataCenterInfo.Name.Amazon) {
      AmazonInfo amazonInfo = (AmazonInfo) info;
      model.put("amazonInfo", amazonInfo);
      model.put("amiId", amazonInfo.get(AmazonInfo.MetaDataKey.amiId));
      model.put("availabilityZone", amazonInfo.get(AmazonInfo.MetaDataKey.availabilityZone));
      model.put("instanceId", amazonInfo.get(AmazonInfo.MetaDataKey.instanceId));
    }
  }

  private PeerAwareInstanceRegistry getRegistry() {
    return getServerContext().getRegistry();
  }

  private EurekaServerContext getServerContext() {
    return EurekaServerContextHolder.getInstance().getServerContext();
  }

  private void populateNavbar(HttpServletRequest request, Map<String, Object> model) {
    Map<String, String> replicas = new LinkedHashMap<>();
    List<PeerEurekaNode> list = getServerContext().getPeerEurekaNodes().getPeerNodesView();
    for (PeerEurekaNode node : list) {
      try {
        URI uri = new URI(node.getServiceUrl());
        String href = scrubBasicAuth(node.getServiceUrl());
        replicas.put(uri.getHost(), href);
      } catch (Exception ex) {
        // ignore?
      }
    }
    model.put("replicas", replicas.entrySet());
  }

  private void populateApps(Map<String, Object> model) {
    List<Application> sortedApplications = getRegistry().getSortedApplications();
    ArrayList<Map<String, Object>> apps = new ArrayList<>();
    for (Application app : sortedApplications) {
      LinkedHashMap<String, Object> appData = new LinkedHashMap<>();
      apps.add(appData);
      appData.put("name", app.getName());
      Map<String, Integer> amiCounts = new HashMap<>();
      Map<InstanceInfo.InstanceStatus, List<Pair<String, String>>> instancesByStatus = new HashMap<>();
      Map<String, Integer> zoneCounts = new HashMap<>();
      for (InstanceInfo info : app.getInstances()) {
        String id = info.getId();
        String url = info.getStatusPageUrl();
        InstanceInfo.InstanceStatus status = info.getStatus();
        String ami = "n/a";
        String zone = "";
        if (info.getDataCenterInfo().getName() == DataCenterInfo.Name.Amazon) {
          AmazonInfo dcInfo = (AmazonInfo) info.getDataCenterInfo();
          ami = dcInfo.get(AmazonInfo.MetaDataKey.amiId);
          zone = dcInfo.get(AmazonInfo.MetaDataKey.availabilityZone);
        }
        Integer count = amiCounts.get(ami);
        if (count != null) {
          amiCounts.put(ami, count + 1);
        } else {
          amiCounts.put(ami, 1);
        }
        count = zoneCounts.get(zone);
        if (count != null) {
          zoneCounts.put(zone, count + 1);
        } else {
          zoneCounts.put(zone, 1);
        }
        List<Pair<String, String>> list = instancesByStatus.computeIfAbsent(status,
            k -> new ArrayList<>());
        list.add(new Pair<>(id, url));
      }
      appData.put("amiCounts", amiCounts.entrySet());
      appData.put("zoneCounts", zoneCounts.entrySet());
      ArrayList<Map<String, Object>> instanceInfos = new ArrayList<>();
      appData.put("instanceInfos", instanceInfos);
      for (Map.Entry<InstanceInfo.InstanceStatus, List<Pair<String, String>>> entry : instancesByStatus
          .entrySet()) {
        List<Pair<String, String>> value = entry.getValue();
        InstanceInfo.InstanceStatus status = entry.getKey();
        LinkedHashMap<String, Object> instanceData = new LinkedHashMap<>();
        instanceInfos.add(instanceData);
        instanceData.put("status", entry.getKey());
        ArrayList<Map<String, Object>> instances = new ArrayList<>();
        instanceData.put("instances", instances);
        instanceData.put("isNotUp", status != InstanceInfo.InstanceStatus.UP);

        for (Pair<String, String> p : value) {
          LinkedHashMap<String, Object> instance = new LinkedHashMap<>();
          instances.add(instance);
          instance.put("id", p.first());
          String url = p.second();
          instance.put("url", url);
          boolean isHref = url != null && url.startsWith("http");
          instance.put("isHref", isHref);
        }
      }
    }
    model.put("apps", apps);
  }

  private void populateInstanceInfo(Map<String, Object> model, StatusInfo statusInfo) {
    InstanceInfo instanceInfo = statusInfo.getInstanceInfo();
    Map<String, String> instanceMap = new HashMap<>();
    instanceMap.put("ipAddr", instanceInfo.getIPAddr());
    instanceMap.put("status", instanceInfo.getStatus().toString());
    if (instanceInfo.getDataCenterInfo().getName() == DataCenterInfo.Name.Amazon) {
      AmazonInfo info = (AmazonInfo) instanceInfo.getDataCenterInfo();
      instanceMap.put("availability-zone", info.get(AmazonInfo.MetaDataKey.availabilityZone));
      instanceMap.put("public-ipv4", info.get(AmazonInfo.MetaDataKey.publicIpv4));
      instanceMap.put("instance-id", info.get(AmazonInfo.MetaDataKey.instanceId));
      instanceMap.put("public-hostname", info.get(AmazonInfo.MetaDataKey.publicHostname));
      instanceMap.put("ami-id", info.get(AmazonInfo.MetaDataKey.amiId));
      instanceMap.put("instance-type", info.get(AmazonInfo.MetaDataKey.instanceType));
    }
    model.put("instanceInfo", instanceMap);
  }

  protected void filterReplicas(Map<String, Object> model, StatusInfo statusInfo) {
    Map<String, String> applicationStats = statusInfo.getApplicationStats();
    if (applicationStats.get("registered-replicas").contains("@")) {
      applicationStats.put("registered-replicas",
          scrubBasicAuth(applicationStats.get("registered-replicas")));
    }
    if (applicationStats.get("unavailable-replicas").contains("@")) {
      applicationStats.put("unavailable-replicas",
          scrubBasicAuth(applicationStats.get("unavailable-replicas")));
    }
    if (applicationStats.get("available-replicas").contains("@")) {
      applicationStats.put("available-replicas",
          scrubBasicAuth(applicationStats.get("available-replicas")));
    }
    model.put("applicationStats", applicationStats);
  }

  private String scrubBasicAuth(String urlList) {
    String[] urls = urlList.split(",");
    StringBuilder filteredUrls = new StringBuilder();
    for (String u : urls) {
      if (u.contains("@")) {
        filteredUrls.append(u, 0, u.indexOf("//") + 2).append(u.substring(u.indexOf("@") + 1))
            .append(",");
      } else {
        filteredUrls.append(u).append(",");
      }
    }
    return filteredUrls.substring(0, filteredUrls.length() - 1);
  }

}
