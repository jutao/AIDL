package com.example.jutao.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class PersonService extends Service {
  private ArrayList<Person> persons;

  public PersonService() {
  }

  @Override public IBinder onBind(Intent intent) {
   persons=new ArrayList<Person>();

    return iBinder;
  }
  private IBinder iBinder=new PersonAidl.Stub(){

    @Override public List<Person> add(Person person) throws RemoteException {
      persons.add(person);
      return persons;
    }
  };
}
