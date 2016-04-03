# 什么是 AIDL #
在 Android 中，每一个应用程序独自拥有一个虚拟机，这样做虽然保证了进程之内数据的安全性，保证一个应用程序的数据不受其他应用程序的影响，也保证了一个应用程序挂掉了不至于影响其他应用程序。但是这样也造成了一个应用程序和另外一个应用程序没办法直接进行通讯。AIDL 的作用就是使来自不同应用的客户端跨进程通信访问 Service。
AIDL 是 Android Interface Definition Language 的缩写，就是安卓内部通信接口描述语言。关于 AIDL 的描述和用法我主要参考了 Google 的官方 API，找到一个中文的，链接：[AIDL](http://www.android-doc.com/guide/components/aidl.html)。需要补充的是，API 里描述 AIDL 支持 Java 语言中的所有基本数据类型，但是经过查证和实验，实际上 AIDL 是不支持 Short 类型的。

# AIDL 用法 #
## 创建 AIDL 接口 ##
![](http://i.imgur.com/hIeNjic.png)
如果是 Eclipse 的话方法是创建 File 并且不要忘记自己打上后缀 .aidl。

## 写 AIDL 接口 ##
AIDL 的用法基本和写普通 Java 接口相同，需要注意的是包名一定要自己检查一下，还有导包也要自己写一下。

    package com.example.jutao.aidl;
    interface IServiceAidl {
    //计算两个数的和
       int add(int num1,int num2);
    }

写完之后需要注意，如果你写的 AIDL 接口正确，那么 Eclipse 是会自动编译的，而 Android Studio 需要手动编译，编译按钮如下图所示：
![](http://i.imgur.com/NnSnU2a.png)
编译通过后，Android Studio 所生成的文件在图下所示目录，Eclipse 在 gen 目录下。
![](http://i.imgur.com/VTAkxIR.png)

## 写 Service ##
      //当客户端绑定到该服务的时候
      @Override public IBinder onBind(Intent intent) {
        //当别人绑定服务的时候，就会得到AIDL接口
        return iBinder;
      }
      IBinder iBinder = new IServiceAidl.Stub() {
        @Override public int add(int num1, int num2) throws RemoteException {
        Log.d("TAG", "收到服务端请求,求出" + num1 + "和" + num2 + "的和");
        return num1 + num2;
    }
    };

## 写客户端 ##
客户端的主要功能是用户通过界面输入两个数字，点击远程计算按钮后通过服务端代码计算出结果返回给客户端并显示。
需要注意的是，客户端也需要有一模一样的 AIDL 包，连包名都要一模一样！！
首先是绑定服务：

    //1、获取服务端
    Intent intent = new Intent();
    //Android 5.0之后不支持隐式意图，必须是显式意图来启动绑定服务
    intent.setComponent(
    new ComponentName("com.example.jutao.aidl", "com.example.jutao.aidl.RemoteService"));
    //第三个参数是一个flag，绑定时自动启动
    bindService(intent, conn, Context.BIND_AUTO_CREATE);
conn的定义:

	ServiceConnection conn = new ServiceConnection() {
    //绑定服务时
    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      //拿到了远程的服务
      iServiceAidl = IServiceAidl.Stub.asInterface(service);
    }

    //当服务断开时
    @Override public void onServiceDisconnected(ComponentName name) {
      //回收资源
      iServiceAidl = null;
    }
      };
运行后的效果如下
![](http://i.imgur.com/zoqkeKX.png)
点击按钮后
![](http://i.imgur.com/JAs8dhp.png)

# AIDL 自定义类型 #
![](http://i.imgur.com/xnkwiMg.png)
AIDL 默认支持的数据类型如上图所示，虽然支持List类型，但是需要 在 List 前注明是输入 List 还是输出 List。如果需要使用自定义类型，需要小费一番周折，下面的是一个利用 AIDL 传输定义好的 Person 类的小例子。
首先 person 类要实现 Parcelable 接口，详细代码可以在我贴的 Demo 里看。
然后就要写 AIDL 接口了。

    import com.example.jutao.aidl.Person;
    
    interface PersonAidl {
       List<Person> add(in Person person);
    }
可以看到，我没有忘记导包，但是点击编译键，依然报错，提示找不到 Person 类，还要再写一个 AIDL 接口才可以解决这个问题。

    parcelable Person;
这个接口很简单，只有一个话。写好后点击编译就可以编译通过了。接下来是写 Service。

    public class PersonService extends Service {
        private ArrayList<Person> persons;
    
        public PersonService() {
        }
    
      @Override public IBinder onBind(Intent intent) {
        persons = new ArrayList<Person>();
    
        return iBinder;
       }
    
      private IBinder iBinder = new PersonAidl.Stub() {
    
     @Override public List<Person> add(Person person) throws RemoteException {
        persons.add(person);
        return persons;
        }
      };
    }
Service 写的是一个 List 存储所有客户端传来的 Person，并将它返回给客户端。起到一个容器的作用。服务端完成，接下来去写客户端。客户端首先要配置包括包名都和服务端一模一样的 Person 类和 AIDL 接口。然后在 MainActivity 中通过点击按钮调用 AIDL 将 Person 传给服务端。

    List<Person> persons = iPersonAidl.add(new Person("奥巴马", age++));
    Log.d("Person", persons.toString());
age 是我定义的一个全局变量，初值为 20，用来区别每个 Perosn。
点击三下按钮，看一下输出。
![](http://i.imgur.com/78H8w2N.png)
可以看到，我每次输出的都是 persons 这一List，这是通过服务端返回的，说明我传输过去的值已经被服务端接收并存储。

# Demo 地址 #
[AIDL服务端](https://github.com/jutao/aidl)
[AIDL客户端](https://github.com/jutao/aidlclient)

注意：运行客户端的时候一定要保证服务端已经跑起来了，这里没有做处理，如果没有按要去运行会报错。
