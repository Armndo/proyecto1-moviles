package com.example.proyecto1;
/*
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class DispositivosVinculados extends AppCompatActivity {
  private static final String TAG = "DispositivosVinculados";
  ListView IdLista;
  public static String EXTRA_DEVICE_ADDRESS = "device_address";
  private BluetoothAdapter mBtAdapter;
  private ArrayAdapter<String> mPairedDevicesArrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dispositivos_vinculados);
  }

  @Override
  public void onResume() {
    super.onResume();
    verificarEstadoBT();

    mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.dispositivos_encontrados);
    IdLista = findViewById(R.id.IdLista);
    IdLista.setAdapter(mPairedDevicesArrayAdapter);
    IdLista.setOnItemClickListener(mDeviceClickListener);

    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

    if (pairedDevices != null && pairedDevices.size() > 0) {
      for (BluetoothDevice device : pairedDevices) {
        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
      }
    }
  }

  private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
    public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
      String info = ((TextView) v).getText().toString();
      String address = info.substring(info.length() - 17);

      finishAffinity();
      Intent intent = new Intent(DispositivosVinculados.this, MainActivity.class);
      intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
      startActivity(intent);
    }
  };

  private void verificarEstadoBT() {
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBtAdapter == null) {
      Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
    } else {
      if (mBtAdapter.isEnabled()) {
        Log.d(TAG, "... Bluetooth activado ...");
      } else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
}
 */
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DispositivosVinculados extends AppCompatActivity {

  private static final String TAG = "DispositivosVinculados";
  private static final int REQUEST_BLUETOOTH_PERMISSIONS = 101;

  ListView IdLista;
  public static String EXTRA_DEVICE_ADDRESS = "device_address";
  private BluetoothAdapter mBtAdapter;
  private ArrayAdapter<String> mPairedDevicesArrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dispositivos_vinculados);
  }

  @Override
  public void onResume() {
    super.onResume();
    verificarEstadoBT();

    mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.dispositivos_encontrados);
    IdLista = findViewById(R.id.IdLista);
    IdLista.setAdapter(mPairedDevicesArrayAdapter);
    IdLista.setOnItemClickListener(mDeviceClickListener);

    // Si es Android 12 (API 31) o superior, validamos el permiso en tiempo de ejecución
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
        return;
      }
    }

    cargarDispositivos();
  }

  private void cargarDispositivos() {
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBtAdapter != null) {
      Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
      if (pairedDevices != null && pairedDevices.size() > 0) {
        for (BluetoothDevice device : pairedDevices) {
          mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        cargarDispositivos();
      } else {
        Toast.makeText(this, "Se requiere permiso de Bluetooth para listar dispositivos", Toast.LENGTH_LONG).show();
      }
    }
  }

  private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
    public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
      String info = ((TextView) v).getText().toString();
      String address = info.substring(info.length() - 17);

      finishAffinity();
      Intent intent = new Intent(DispositivosVinculados.this, MainActivity.class);
      intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
      startActivity(intent);
    }
  };

  private void verificarEstadoBT() {
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBtAdapter == null) {
      Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
    } else {
      if (mBtAdapter.isEnabled()) {
        Log.d(TAG, "... Bluetooth activado ...");
      } else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
}