/*
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.simulator.security;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christoph Deppisch
 */
@ConfigurationProperties(prefix = "simulator.oauth2.client")
public class SimulatorOAuthClientProperties {

    private String id;
    private String secret;

    private Set<String> authorizedGrantTypes;

    private Set<String> scope;
    private Set<String> authorities;

    private String accessToken;
    private String refreshToken;

    private int accessTokenValiditySeconds;
    private int refreshTokenValiditySeconds;

    public String getId() {
        return id;
    }

    /**
     * Specifies the id.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    /**
     * Specifies the secret.
     *
     * @param secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Set<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    /**
     * Specifies the authorizedGrantTypes.
     *
     * @param authorizedGrantTypes
     */
    public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    public Set<String> getScope() {
        return scope;
    }

    /**
     * Specifies the scope.
     *
     * @param scope
     */
    public void setScope(Set<String> scope) {
        this.scope = scope;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    /**
     * Specifies the authorities.
     *
     * @param authorities
     */
    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Specifies the accessToken.
     *
     * @param accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Specifies the refreshToken.
     *
     * @param refreshToken
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    /**
     * Specifies the accessTokenValiditySeconds.
     *
     * @param accessTokenValiditySeconds
     */
    public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public int getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    /**
     * Specifies the refreshTokenValiditySeconds.
     *
     * @param refreshTokenValiditySeconds
     */
    public void setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
}
