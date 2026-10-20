package com.example.proyecto1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

  EditText edtTextoOut;
  ImageButton btnEnviar, btnAdelante, btnIzquierda, btnStop, btnDerecha, btnReversa;
  TextView tvtMensaje;
  Button btnDesconectar;

  Handler bluetoothIn;
  final int handlerState = 0;
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private ConnectedThread MyConexionBT;

  // UUID estándar para el perfil serie SPP
  private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private static String address = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    bluetoothIn = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
        if (msg.what == handlerState) {
          char MyCaracter = (char) msg.obj;
          if (MyCaracter == 'a') {
            tvtMensaje.setText("ACELERANDO");
          } else if (MyCaracter == 'i') {
            tvtMensaje.setText("GIRO IZQUIERDA");
          } else if (MyCaracter == 'd') {
            tvtMensaje.setText("GIRO DERECHA");
          } else if (MyCaracter == 'r') {
            tvtMensaje.setText("RETROCEDIENDO");
          } else if (MyCaracter == 's') {
            tvtMensaje.setText("DETENIDO");
          }
        }
      }
    };

    btAdapter = BluetoothAdapter.getDefaultAdapter();
    verificarEstadoBT();

    edtTextoOut = findViewById(R.id.edtTextoOut);
    btnEnviar = findViewById(R.id.btnEnviar);
    btnAdelante = findViewById(R.id.btnAdelante);
    btnIzquierda = findViewById(R.id.btnIzquierda);
    btnStop = findViewById(R.id.btnStop);
    btnDerecha = findViewById(R.id.btnDerecha);
    btnReversa = findViewById(R.id.btnReversa);
    tvtMensaje = findViewById(R.id.tvtMensaje);
    btnDesconectar = findViewById(R.id.btnDesconectar);

    btnEnviar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String GetDat = edtTextoOut.getText().toString();
        if (MyConexionBT != null) {
          MyConexionBT.write(GetDat);
        }
      }
    });

    btnAdelante.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MyConexionBT != null) MyConexionBT.write("A");
      }
    });

    btnIzquierda.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MyConexionBT != null) MyConexionBT.write("I");
      }
    });

    btnStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MyConexionBT != null) MyConexionBT.write("S");
      }
    });

    btnDerecha.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MyConexionBT != null) MyConexionBT.write("D");
      }
    });

    btnReversa.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MyConexionBT != null) MyConexionBT.write("R");
      }
    });

    btnDesconectar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (btSocket != null) {
          try {
            btSocket.close();
          } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
          }
        }
        finish();
      }
    });
  }

  private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
    return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
  }

  @Override
  public void onResume() {
    super.onResume();
    Intent intent = getIntent();
    address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);

    if (address != null && btAdapter != null) {
      BluetoothDevice device = btAdapter.getRemoteDevice(address);

      try {
        btSocket = createBluetoothSocket(device);
      } catch (IOException e) {
        Toast.makeText(getBaseContext(), "La creación del Socket falló", Toast.LENGTH_LONG).show();
      }

      try {
        btSocket.connect();
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
      } catch (IOException e) {
        try {
          btSocket.close();
        } catch (IOException e2) {}
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    try {
      if (btSocket != null) {
        btSocket.close();
      }
    } catch (IOException e2) {}
  }

  private void verificarEstadoBT() {
    if (btAdapter == null) {
      Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
    } else {
      if (!btAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }

  private class ConnectedThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
      InputStream tmpIn = null;
      OutputStream tmpOut = null;
      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {}
      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run() {
      byte[] byte_in = new byte[1];
      while (true) {
        try {
          int bytes = mmInStream.read(byte_in);
          if (bytes > 0) {
            char ch = (char) byte_in[0];
            bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
          }
        } catch (IOException e) {
          break;
        }
      }
    }

    public void write(String input) {
      try {
        mmOutStream.write(input.getBytes());
      } catch (IOException e) {
        Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
      }
    }
  }
}