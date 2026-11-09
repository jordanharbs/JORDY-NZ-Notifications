# JORDY NZ Notifications

This is the Fabric mod source project. It is source code, not a compiled `.jar`.

## Uploading the project to GitHub

1. Create a new GitHub repository named `JORDY-NZ-Notifications`.
2. Keep the repository empty when creating it (do not add a README, license, or `.gitignore`).
3. Download and extract this ZIP on your computer.
4. Open the extracted `JORDY-NZ-Notifications-GitHub` folder.
5. Upload the *contents* of that folder to the root of your GitHub repository. Do not upload the ZIP itself.
6. Keep the folder structure exactly as shown below.
7. Commit the upload. GitHub Actions should then attempt to build the mod.

## Required project files

```text
JORDY-NZ-Notifications/
├── .github/
│   └── workflows/
│       └── build.yml
├── src/
│   └── main/
│       ├── java/
│       │   └── nz/
│       │       └── jordy/
│       │           └── notifications/
│       │               ├── JordyNotificationsClient.java
│       │               └── PaymentStore.java
│       └── resources/
│           ├── fabric.mod.json
│           └── assets/
│               └── jordy_nz_notifications/
│                   └── lang/
│                       └── en_us.json
├── .gitignore
├── build.gradle
├── gradle.properties
└── settings.gradle
```

**Important:** Upload all files and folders shown above, preserving their paths. Do not upload only the `.java` files, and do not rename the files.

## Build status

This ZIP has not been built or tested in your GitHub repository. After uploading, open the repository's **Actions** tab. If the workflow fails, send the error log or a screenshot of the failed step so the exact issue can be fixed.
