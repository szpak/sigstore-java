/*
 * Copyright 2022 The Sigstore Authors.
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
package dev.sigstore.oidc.client;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Key;
import dev.sigstore.http.HttpClients;
import dev.sigstore.http.HttpParams;
import dev.sigstore.http.ImmutableHttpParams;
import io.grpc.Internal;
import java.io.IOException;

/**
 * Obtain an oidc token from the github execution environment.
 * https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/about-security-hardening-with-openid-connect
 */
public class GithubActionsOidcClient implements OidcClient {
  private static final String REQUEST_TOKEN_KEY = "ACTIONS_ID_TOKEN_REQUEST_TOKEN";
  private static final String REQUEST_URL_KEY = "ACTIONS_ID_TOKEN_REQUEST_URL";

  private static final String DEFAULT_AUDIENCE = "sigstore";

  private final String audience;
  private final HttpParams httpParams;

  public static Builder builder() {
    return new Builder();
  }

  private GithubActionsOidcClient(HttpParams httpParams, String audience) {
    this.audience = audience;
    this.httpParams = httpParams;
  }

  public static class Builder {
    private HttpParams httpParams = ImmutableHttpParams.builder().build();
    private String audience = DEFAULT_AUDIENCE;

    private Builder() {}

    public Builder audience(String audience) {
      this.audience = audience;
      return this;
    }

    public Builder httpParams(HttpParams httpParams) {
      this.httpParams = httpParams;
      return this;
    }

    public GithubActionsOidcClient build() {
      return new GithubActionsOidcClient(httpParams, audience);
    }
  }

  @Override
  public OidcToken getIDToken() throws OidcException {
    var bearer = System.getenv(REQUEST_TOKEN_KEY);
    var urlBase = System.getenv(REQUEST_URL_KEY);
    if (bearer == null) {
      throw new OidcException(
          "Could not get github actions environment variable '" + REQUEST_TOKEN_KEY + "'");
    }
    if (urlBase == null) {
      throw new OidcException(
          "Could not get github actions environment variable '" + REQUEST_URL_KEY + "'");
    }
    var url = new GenericUrl(urlBase + "&audience=" + audience);
    try {
      var req = HttpClients.newRequestFactory(httpParams).buildGetRequest(url);
      req.setParser(new GsonFactory().createJsonObjectParser());
      req.getHeaders().setAuthorization("Bearer " + bearer);
      req.getHeaders().setAccept("application/json; api-version=2.0");
      req.getHeaders().setContentType("application/json");
      var resp = req.execute().parseAs(GithubOidcJsonResponse.class);

      var idToken = resp.getValue();
      var jws = JsonWebSignature.parse(new GsonFactory(), idToken);
      return ImmutableOidcToken.builder()
          .idToken(idToken)
          .subjectAlternativeName(jws.getPayload().getSubject())
          .build();
    } catch (IOException e) {
      throw new OidcException("Could not obtain github actions oidc token", e);
    }
  }

  @Internal
  public static class GithubOidcJsonResponse extends GenericJson {
    @Key("value")
    private String value;

    String getValue() {
      return value;
    }
  }
}
