# Releasing to Maven Central

Artifacts: `io.github.anjalymsajeev:kollavarsham-{core,astronomy,locale,panchangam,festivals}`. `kollavarsham-android` will follow after Sprint 6.

## One-time setup (project owner)

Secrets are entered only in the Central Portal and GitHub settings, never in chat, issues or commits.

### 1. Central Portal account and namespace

1. Go to <https://central.sonatype.com>, choose **Sign in**, and sign in **with GitHub** as `anjalymsajeev`.
2. Open **Namespaces**. `io.github.anjalymsajeev` should be listed as verified, because signing in with GitHub verifies it automatically. If it isn't, add it and follow the verification steps the portal shows.

### 2. Publishing token

In the portal, open **Account** and choose **Generate User Token**. Keep the page open; you need the *username* and *password* it shows for step 4.

### 3. Signing key (GPG)

Maven Central requires every file to be signed. On your own computer:

```bash
gpg --full-generate-key
```

Choose RSA and 4096 bits, enter your name and email, and set a passphrase.

```bash
gpg --list-secret-keys --keyid-format=long
```

Note the key id (the part after `rsa4096/`).

```bash
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
```

Maven Central checks signatures against public keyservers. keys.openpgp.org emails you to confirm the address; do that.

```bash
gpg --armor --export-secret-keys <KEY_ID>
```

This prints the private key for step 4. Don't share it anywhere else.

### 4. GitHub secrets

In GitHub, open **malayalam-calendar-kotlin**, then **Settings**, **Environments**, **New environment**, and name it `maven-central`. Optionally add yourself as a required reviewer, so every release needs your approval. Then add these **environment secrets**:

| Secret | Value |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Token username from step 2 |
| `MAVEN_CENTRAL_PASSWORD` | Token password from step 2 |
| `SIGNING_KEY` | The whole armored private key from step 3, including the `-----BEGIN/END PGP PRIVATE KEY BLOCK-----` lines |
| `SIGNING_KEY_PASSWORD` | The key's passphrase |

## Each release

1. Set `VERSION_NAME` in `gradle.properties`, update `CHANGELOG.md`, and merge to `main` with CI green.
2. Tag and push:

   ```bash
   git tag v0.1.0-alpha01
   ```

   ```bash
   git push origin v0.1.0-alpha01
   ```

3. The **Release to Maven Central** workflow builds, tests, signs and uploads.
4. In the Central Portal, open **Deployments**, check the files and press **Publish**.
   - **A published version can never be changed or deleted.** If something is wrong, choose **Drop** instead and fix it first.
5. The artifacts appear on Maven Central within about 30 minutes; search can take longer.

## Verifying locally

```bash
./gradlew publishToMavenLocal
```

This builds unsigned artifacts (jar, sources, javadoc and POM) into `~/.m2` for inspection.
