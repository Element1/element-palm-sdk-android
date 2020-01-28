![element](images/element.png "element")

# Element Palm SDK
The Element Palm SDK (the SDK) is an API library for creating biometrics models that can be used to authenticate users. This document contains information to integrate the SDK into an Android application by using Android Studio. The actual library is called `element-palm-core`.

## Version Support
### Android device & development environments
- The minimum Android device requirement is Android 5.0 or API 21 (Android OS Lollipop and up). A minimum of 2 GB of RAM is recommended for optimal performance.
- Android Studio 3.2.0 with Gradle Wrapper 4.6
- Android Target SDK Version 28, Build Tool Version 28.0.3, and AndroidX

### Prerequisites
Please refer to [prerequisites](prerequisites.md) for more information.

### Dependencies for `element-palm-core`
- AndroidX WorkManager: 2.0.1
- Google Play Service Location: 17.0.0
- Google Material Design: 1.0.0
- Google Guava for Android: 27.0.1-android
- Amazon AWS Mobile SDK: 2.8.5

```
  // SDK dependencies
  implementation 'androidx.work:work-runtime:2.2.0'
  implementation 'com.google.android.material:material:1.0.0'
  implementation 'com.amazonaws:aws-android-sdk-core:2.8.5'
  implementation 'com.amazonaws:aws-android-sdk-s3:2.8.5'
  implementation 'com.google.android.gms:play-services-location:17.0.0'
  implementation 'com.google.guava:guava:27.0.1-android'
```  
References of dependencies can be found in the sample project `build.gradle` file.

## SDK Integration
### Initialize the Element Palm SDK
1. Create a class which extends [android.app.Application](https://developer.android.com/reference/android/app/Application), and initialize the SDK in `onCreate()` method:
    ```
      public class MainApplication extends Application {
        @Override
        public void onCreate() {
          super.onCreate();
          ElementPalmSDK.initSDK(this);
        }
      }
    ```
1. Also need to declare the `MainApplication` class in the AndroidManifest.xml:
    ```
      <manifest>
        .....
        <application android:name=".MainApplication">
          .....
        </application>
      </manifest>
    ```

### Ask for the permissions
1. The SDK requires the following permissions:
  - `android.Manifest.permission.CAMERA`
  - `android.Manifest.permission.ACCESS_FINE_LOCATION`
  - `android.Manifest.permission.ACCESS_COARSE_LOCATION`
1. These permissions are declared in the Element Palm SDK AAR, and your app will inherit them. No need to declare them again in your app.
1. Use `PermissionUtils.verifyPermissions(Activity activity, String... permissionsToVerify)` provided by the SDK to ask for user permissions:
    ```
      PermissionUtils.verifyPermissions(
        MainActivity.this,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION);
    ```
1. For Android 6.0 (Marshmallow, API 23) and up, make sure the permissions are granted before starting any Activity provided by the Element Palm SDK.

### Element Activities
The SDK provides two Activities, `ElementPalmAuthActivity` and `ElementPalmEnrollActivity`. Usage of each Activity will be covered in the next section.

### Activity declaration
Declare the Activity in the manifest.
    ```
      <manifest>
        .....
        <application android:name=".MainApplication">
          .....
          <activity android:name="com.element.camera.ElementPalmEnrollActivity"
          android:theme="@style/CamTheme.Blue"
              android:clearTaskOnLaunch="true" />
          .....
        </application>
      </manifest>
    ```

### Activity invocation
There is an important piece of information used to invoke Element Activities by passing extras in the intent.
- `EXTRA_ELEMENT_USER_ID` - A `UserInfo` needs to be created first before starting Element Activities. Pass the `userId` with the intent extras.

## Usage of the Element Activities
### User enrollment
The `ElementPalmEnrollActivity` is used for user enrollment. It's based on the [`startActivityForResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method.
1. Enroll a user and obtain the `UserInfo`. The `UserInfo` contains a unique `userId` (Element ID). The pair of userId and appId (`context.getPackageName()`) is mainly used in the Element Palm SDK to inquire the user's information and status. In the `Activity` where you want to start the enrollment process:
    ```
      //If you want a random userId
      UserInfo userInfo = UserInfo.enrollNewUser(
        getBaseContext(),
        getPackageName(),
        firstName,
        lastName,
        new HashMap<String, String>());

      //If you want a specific userId
	  UserInfo userInfo = UserInfo.enrollUser(
        getBaseContext(),
        getPackageName(),
        userId,
        firstName,
        lastName,
        new HashMap<String, String>());
    ```
1. Declare a request code:
    ```
      public static final int ENROLL_REQ_CODE = 12800;
    ```
1. Start the `ElementPalmEnrollActivity`:
    ```
      Intent intent = new Intent(this, ElementPalmEnrollActivity.class);
      intent.putExtra(ElementPalmEnrollActivity.EXTRA_ELEMENT_USER_ID, userInfo.userId);
      startActivityForResult(intent, ENROLL_REQ_CODE);
    ```
1. Override the [`onActivityResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method to receive enrollment results:
    ```
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENROLL_REQ_CODE) {
          if (resultCode == Activity.RESULT_OK) {
            // User enrolled successfully
          } else {
            // Enrollment cancelled
          }
        }
      }
    ```

### User authentication
User authentication is similar to user enrollment, using `ElementPalmAuthActivity`.
1. Declare the request code:
    ```
      public static final int AUTH_REQ_CODE = 12801;
    ```
1. Start the `ElementPalmAuthActivity`:
    ```
      Intent intent = new Intent(this, ElementPalmAuthActivity.class);
      intent.putExtra(ElementPalmAuthActivity.EXTRA_ELEMENT_USER_ID, userInfo.userId);
      startActivityForResult(intent, AUTH_REQ_CODE);
    ```
1. Override the [`onActivityResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method to receive the authentication results:
    ```
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_REQ_CODE) {
          if (resultCode == Activity.RESULT_OK) {
            String results = data.getStringExtra(ElementPalmAuthActivity.EXTRA_RESULTS);
            if (ElementPalmAuthActivity.USER_VERIFIED.equals(results)) {
                // The user is verified
            } else if (ElementPalmAuthActivity.USER_FAKE.equals(results)) {
                // the user was spoofing
            } else {
                // The user is not verified
            }
          } else {
            // Verification cancelled
          }
        }
      }
    ```

## User management
### User enquiries
The Element Palm SDK provides a few ways to query users with `ProviderUtil`.
- List all users
    ```
      public static List<UserInfo> getUsers(@NonNull Context context, @NonNull String appId, String selection)
    ```
- Get a user
    ```
      public static UserInfo getUser(@NonNull Context context, @NonNull String appId, @NonNull String userId)
    ```
- Delete users
    ```
      public static boolean deleteUser(@NonNull Context context, @NonNull String appId, @NonNull String userId)
      public static void deleteAllUsers(@NonNull Context context, @NonNull String appId)
    ```
- Update a user
    ```
      public static int updateUserInfo(@NonNull Context context, @NonNull UserInfo userInfo)
      public static void insertUserInfo(@NonNull Context context, @NonNull UserInfo userInfo)
    ```

### User mobile enrollment status
The `isEnrolled` in ElementSDKHelper can be used to get user enrollment status.
- Find out if a user is enrolled
    ```
      ElementSDKHelper.isEnrolled(getBaseContext(), userId)
    ```

### Questions?
If you have questions, please contact devsupport@discoverelement.com.
