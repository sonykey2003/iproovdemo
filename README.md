# iProov Android Example App

This is an example Android application that integrates the **iProov Biometrics SDK** for Genuine Presence Assurance™ and Liveness Assurance™. The app demonstrates user enrollment and authentication using iProov's facial recognition technology.

## 📌 Features
- **GPA Onboarding Experience** with iProov.
- **Custom UI Enhancements**, adopted a few UX customisation.
- **Retry Mechanism** for failed scans.
- **Security Best Practices** to protect the API keys.

---

## 🚀 Getting Started

### 1️⃣ Prerequisites
- **Android Studio** (Latest version recommended)
- **Android SDK 26+** (Minimum)
- **Gradle 7+**
- **An iProov Developer Account** ([Sign up here](https://portal.iproov.com/))
- **API Key & Secret** from iProov

---

### 2️⃣ Clone this Repository


### 3️⃣ Configure API Credentials Securely
To prevent exposing API keys in your repository, store them in local.properties instead of hardcoding them in the source code.

Step 1: Add API Keys to `local.properties`, open local.properties (located in the project root) and add:

```bash
iproovApiKey=your_api_key_here
iproovApiSecret=your_secret_here
```

Step 2: The API Keys will be loaded in `Constants.kt`, and you can set the baseUrl for iProov APIClient:
```kt
const val BASE_URL = "wss://<region>.rp.secure.iproov.me/ws"
const val FUEL_URL = "https://<region>.rp.secure.iproov.me/api/v2/"
```

Step 3: Make sure `local.properties` is in `.gitignore`.


#### 🔒 Why Use local.properties? 
✅ Prevents API key leaks in public repositories.

✅ Avoids hardcoding sensitive data in source code.

✅ Keeps credentials out of version control (local.properties is ignored by .gitignore by default).

### 4️⃣ Build and Run the App
**Via Android Studio:**

1. Create a virtual device - i.e. Pixel 9 Pro XL in Android Studio, or connect to an android device (Android 8 minimum).
2. Open the project in Android Studio.
3. Sync Gradle files (File > Sync Project with Gradle).
4. Run the app (Shift + F10 or click ▶️) on the device.
5. Enter the user name to start the onboarding:

<img src="https://github.com/user-attachments/assets/f814eda5-d270-450a-8b4d-3b0e143f4d7a" width="265" height="600"/>

6. Tap continue to kick off the face scanning: 

<img src="https://github.com/user-attachments/assets/1eb17ba7-d0bf-4e92-a970-1cdc2a2a3c06" width="265" height="600"/>

7. Follow the instruction, and done!

<img src="https://github.com/user-attachments/assets/9a3ab070-cd97-4f9f-b144-82f4aa7ee050" width="265" height="600"/>
