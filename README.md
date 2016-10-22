# Stetho-Couchbase

Stetho-Couchbase is a plugin for Stetho to enable visualizing and debugging Android Couchbase databases in Chrome. More details can be found here: http://www.sureshjoshi.com/mobile/visualizing-couchbase-chrome-stetho/

## Usage

In your Application class (where you would otherwise initiate Stetho), use the Stetho builder to initialize Stetho-Couchbase. 

```java
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(new CouchbaseInspectorModulesProvider.Builder(this)
                                    .showMetadata(true) // Default: true
                                    .build())
                            .build());
        }
    }
}
```

You can either wrap Stetho in a BuildConfig.DEBUG statement, or use a more involved method as described [here](http://littlerobots.nl/blog/stetho-for-android-debug-builds-only/).

## Download

```groovy
compile 'com.facebook.stetho:stetho:1.4.1'
compile 'com.robotpajamas.stetho:stetho-couchbase:0.2.0'
```

## License

The Apache License (Apache)

    Copyright (c) 2016 Robot Pajamas

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
