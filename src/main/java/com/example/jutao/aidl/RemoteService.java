package com.example.jutao.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {
    public RemoteService() {
    }
    //当客户端绑定到该服务的时候
    @Override
    public IBinder onBind(Intent intent) {
        //当别人绑定服务的时候，就会得到AIDL接口
        return iBinder;
    }
    IBinder iBinder=new IServiceAidl.Stub(){
        @Override
        public int add(int num1, int num2) throws RemoteException {
            Log.d("TAG","收到服务端请求,求出"+num1+"和"+num2+"的和");
            return num1+num2;
        }
    };
}
