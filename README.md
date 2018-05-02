# Muses
Muses是一个使用方便的Android下载器框架，作为一个刚毕业进入公司的职场萌新，接到这个组件开发任务的时候内心是十分忐忑的，经过一周的开发，基本形成了1.0版本，后续会对Muses进行不断优化升级，增加更多特性，欢迎提出issue。

### Muses有以下优点：

* 支持在Activity、Service、Fragment、Dialog、popupWindow、Notification等组件中使用
* 支持HTTP断点续传
* 多任务自动调度管理

### 截图：

![](http://bmob-cdn-16449.b0.upaiyun.com/2018/05/02/bc55e11440106d6a807494d80adb63b4.jpeg!/scale/30)


### 基本使用：
#### 依赖：
```java
compile 'com.kinsomy:Muses:1.0.0'
```
#### step1：申请权限
由于Muses是一个网络下载框架，所以会涉及到网络请求以及文件读写。所以使用之前要申请以下权限。

**如果你需要适配Android6.0及以上机型，还需要动态申请权限**。

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

#### step2：注册广播监听器

```java
private DownloadReceiver mDownloadReceiver;
mDownloadReceiver = new DownloadReceiver();
mDownloadReceiver.register(this);

//自定义Receiver继承AbsNewDownloadReceiver,接受回调
private class DownloadReceiver extends AbsNewDownloadReceiver {
		@Override
		public void onTaskErrorEvent(NewDownloadTask task, int code) {
		}

		@Override
		public void onTaskCancelEvent(NewDownloadTask task) {
		}

		@Override
		public void onTaskPauseEvent(NewDownloadTask task) {
		}

		@Override
		public void onTaskCompletedEvent(NewDownloadTask task) {
		}

		@Override
		public void onTaskStartEvent(NewDownloadTask task) {
		}

		@Override
		public void onTaskDownloadingEvent(NewDownloadTask task, boolean showProgress) {
		}
	}
```

#### step3：创建下载任务

```java
//首先实例化manager
private DownloadManager mManager;
mManager = new DownloadManager(this);

//调用manager的方法，传入文件夹、文件名、下载链接、id（可为空）
DownloadTask task = mManager.addDownloadTask(dir, fileName, url, id);
```
这样就可以创建一个下载任务了，我的设计思想是，使用者自己创建的task将由使用者自行管理，对于task的运行将交由manager管理。

这样做的好处是可以实现高度的定制化，使用者完全可以根据自己的需要来操作task。


#### step4:开始下载任务

```java
mManager.startTask(task);

```
#### 取消任务

```java
mManager.cancel(taskId);
```

#### 暂停任务

```java
mManager.pause(taskId);
```

#### 恢复任务

```java
mManager.resume(taskId);
```

Version Log
-------
v_1.0.0 : 下载器基本功能实现

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

