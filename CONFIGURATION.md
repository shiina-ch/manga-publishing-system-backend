# Backend configuration

The backend requires external database, JWT, CORS, and storage configuration. Real credentials must never be committed.

## Required environment variables

| Variable | Format |
| --- | --- |
| `DB_URL` | Complete SQL Server JDBC URL, including host, database, and TLS options |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Base64-encoded key containing at least 32 decoded bytes |
| `JWT_EXPIRATION` | Positive token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | Comma-separated HTTP/HTTPS origins without paths or wildcards |
| `UPLOAD_DIRECTORY` | Writable upload directory; an absolute path is recommended |
| `UPLOAD_MAX_SIZE` | Spring data size such as `10MB` |

Optional settings are `SERVER_PORT` (default `8386`), `HIBERNATE_DDL_AUTO` (default `validate`), `SQL_LOGGING_ENABLED` (default `false`), and `SQL_FORMAT_SQL` (default `false`).

## PowerShell setup

Set variables in the PowerShell process that will launch Maven:

```powershell
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=YOUR_LOCAL_DATABASE;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME = 'YOUR_LOCAL_SQL_USERNAME'
$env:DB_PASSWORD = 'YOUR_LOCAL_SQL_PASSWORD'

$keyBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
$env:JWT_EXPIRATION = '86400000'

$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
$env:UPLOAD_DIRECTORY = (Join-Path $PWD 'uploads')
$env:UPLOAD_MAX_SIZE = '10MB'
$env:HIBERNATE_DDL_AUTO = 'update'
$env:BOOTSTRAP_ADMIN_ENABLED = 'false'
```

These values exist only in the current PowerShell process. `.env.example` is documentation only: Spring Boot does not automatically load `.env` files, and this project does not add dotenv support.

## Ignored local profile

As an alternative, copy:

```text
src/main/resources/application-local.example.properties
```

to the ignored file:

```text
src/main/resources/application-local.properties
```

Replace every placeholder, then activate it:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
```

The local profile may use `spring.jpa.hibernate.ddl-auto=update` for the current demo database. The committed base configuration defaults to `validate` so normal production startup does not mutate the schema automatically.

## Bootstrap Admin

Bootstrap Admin creation is opt-in and disabled by default. Role normalization and canonical role seeding still run when it is disabled.

To enable it for initial setup:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BOOTSTRAP_ADMIN_EMAIL = 'YOUR_ADMIN_EMAIL'
$env:BOOTSTRAP_ADMIN_PASSWORD = 'YOUR_STRONG_ADMIN_PASSWORD'
```

The password must contain uppercase and lowercase letters, a digit, and a special character. Missing or invalid credentials fail startup when bootstrap is enabled. An existing account with the configured email is never overwritten.

## Uploads and Git

Uploaded files are served through the existing `/uploads/**` URL and stored under `UPLOAD_DIRECTORY`. A local repository-root `uploads` directory is ignored by Git. Production storage must be writable and persistent if files must survive deployments.

## Tests

The `test` profile uses a disposable in-memory H2 database and disables Admin bootstrap. The Spring context test explicitly activates that profile and does not use developer SQL Server settings. H2's SQL Server mode is useful for isolation but does not prove full SQL Server compatibility.

## Previously committed secrets

Removing secrets from current files does not remove them from Git history. If any previously committed database password, JWT key, or Admin password was real or reused, rotate it. JWT key rotation intentionally invalidates existing tokens. History rewriting is not performed by this project configuration change.
