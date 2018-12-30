# NetworkProxy [ ![Download](https://api.bintray.com/packages/nongdenchet/maven/NetworkProxy/images/download.svg) ](https://bintray.com/nongdenchet/maven/NetworkProxy/_latestVersion)
A network proxy library to intercept and mock response from OkHttp

## Getting started

In your root `build.gradle`:

```groovy
allprojects {
    repositories {
        // Other configurations
        maven { url 'https://dl.bintray.com/nongdenchet/maven' }
    }
}
```

In your `app/build.gradle`:

```groovy
dependencies {
  debugImplementation 'com.github.nongdenchet:networkproxy:0.1.1'
  releaseImplementation 'com.github.nongdenchet:networkproxy-no-op:0.1.1'
}
```

In your `Application` class:

```java
public class ExampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    NetworkProxy.init(this);
    // Normal app init code...
  }
}
```

Add the `Interceptor` to your OkHttp builder:

```java
new OkHttpClient.Builder()
  // other configuration...
  .addInterceptor(NetworkProxy.interceptor())
  // other configuration...
  .build();
```

## License

    Copyright 2018 Quan Vu.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
