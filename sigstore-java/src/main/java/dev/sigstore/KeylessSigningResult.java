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
package dev.sigstore;

import dev.sigstore.rekor.client.RekorEntry;
import java.security.cert.CertPath;
import org.immutables.value.Value;

@Value.Immutable
public interface KeylessSigningResult {
  /** The sha256 hash digest of the artifact */
  byte[] getDigest();

  /** The full certificate chain provided by fulcio for the public key used to sign the artifact */
  CertPath getCertPath();

  /** The signature over the artifact */
  byte[] getSignature();

  /** The entry in the rekor transparency log */
  RekorEntry getEntry();
}
