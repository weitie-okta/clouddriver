/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.kubernetes.caching.agent;

import static com.netflix.spinnaker.cats.agent.AgentDataType.Authority.AUTHORITATIVE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.cats.agent.AgentDataType;
import com.netflix.spinnaker.clouddriver.kubernetes.config.KubernetesConfigurationProperties;
import com.netflix.spinnaker.clouddriver.kubernetes.description.KubernetesSpinnakerKindMap;
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesKind;
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesNamedAccountCredentials;

public class CustomKubernetesCachingAgentFactory {
  public static KubernetesCachingAgent create(
      KubernetesKind kind,
      KubernetesNamedAccountCredentials namedAccountCredentials,
      ObjectMapper objectMapper,
      Registry registry,
      int agentIndex,
      int agentCount,
      Long agentInterval,
      KubernetesConfigurationProperties configurationProperties,
      KubernetesSpinnakerKindMap kubernetesSpinnakerKindMap) {
    return new Agent(
        kind,
        namedAccountCredentials,
        objectMapper,
        registry,
        agentIndex,
        agentCount,
        agentInterval,
        configurationProperties,
        kubernetesSpinnakerKindMap);
  }

  /**
   * Instances of this class cache kinds specified in the list
   * "kubernetes.accounts[*].customResourceDefinitions" in config.
   *
   * <p>There's one instance of this class for every kind in the list, and only the kinds that are
   * allowed by the configuration in "kubernetes.cache.*" are cached.
   */
  private static class Agent extends KubernetesCachingAgent {
    private final KubernetesKind kind;

    Agent(
        KubernetesKind kind,
        KubernetesNamedAccountCredentials namedAccountCredentials,
        ObjectMapper objectMapper,
        Registry registry,
        int agentIndex,
        int agentCount,
        Long agentInterval,
        KubernetesConfigurationProperties configurationProperties,
        KubernetesSpinnakerKindMap kubernetesSpinnakerKindMap) {
      super(
          namedAccountCredentials,
          objectMapper,
          registry,
          agentIndex,
          agentCount,
          agentInterval,
          configurationProperties,
          kubernetesSpinnakerKindMap);
      this.kind = kind;
    }

    @Override
    protected ImmutableList<KubernetesKind> primaryKinds() {
      return ImmutableList.of(this.kind);
    }

    @Override
    public final ImmutableSet<AgentDataType> getProvidedDataTypes() {
      return ImmutableSet.of(AUTHORITATIVE.forType(this.kind.toString()));
    }

    @Override
    public String getAgentType() {
      return String.format(
          "%s/CustomKubernetes(%s)[%d/%d]", accountName, kind, agentIndex + 1, agentCount);
    }
  }
}
