package br.ufc.quixada.bluetoothnetworktestconnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String NOT_BLUETOOTH_SUPPORT = "Seu dispositivo não possui suporte a conexões bluetooth!";
    private static final String BLUETOOTH_ENABLE = "Bluetooth habilitado";
    private static final String BLUETOOTH_DISABLE = "Bluetooth desabilitado";

    private static final int STATE_LISTENING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_CONNECTION_FAILED = 4;
    private static final int STATE_MESSAGE_RECEIVED = 5;

    private static final String TEXT_STATE_LISTENING = "Ouvindo";
    private static final String TEXT_STATE_CONNECTING = "Conectando";
    private static final String TEXT_STATE_CONNECTED = "Conectado";
    private static final String TEXT_STATE_CONNECTION_FAILED = "Falha ao conectar";
    private static final String TEXT_STATE_MESSAGE_RECEIVED = "Mensagem recebida";

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("60264289-1423-405d-b5a2-934640e41bb5");

    private static final String SCAN_MODE_CONNECTABLE = "O dispositivo não está em mode de descoberta mas ainda pode receber conexões";
    private static final String SCAN_MODE_CONNECTABLE_DISCOVERABLE = "O dispositivo está em mode de descoberta";
    private static final String SCAN_MODE_NONE = "O dispositivo não está em modo de descoberta e não pode receber conexões ";
    private static final String SCAN_ERROR = "Error";

    private TextView status;
    private ListView listView1;
    private ListView listView2;
    private EditText editText;
    private TextView message;
    private Button buttonOn, buttonOff;
    private Button buttonSearchPaired, buttonSearchNewDevices;
    private Button enableVisibility;
    private Button send;

    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> mArrayAdapterNewDevices;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice[] btArray;
    private SendReceive sendReceive;

    private Intent btEnablingIntent;
    private int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initiateDiscovery();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                stringArrayList.add(device.getName());
                mArrayAdapterNewDevices.notifyDataSetChanged();
            }
        }
    };

    BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    status.setText(SCAN_MODE_CONNECTABLE);
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    status.setText(SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_NONE) {
                    status.setText(SCAN_MODE_NONE);
                } else {
                    status.setText(SCAN_ERROR);
                }
            }
        }
    };

    private void initiateDiscovery() {
        buttonSearchNewDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });
        buttonSearchPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = mBluetoothAdapter.getBondedDevices();
                ArrayAdapter<String> mArrayAdapterPaired = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
                btArray = new BluetoothDevice[bt.size()];
                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        mArrayAdapterPaired.add(device.getName() + "\n" + device.getAddress());
                    }
                    listView1.setAdapter(mArrayAdapterPaired);
                }
            }
        });
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();

                status.setText("Conectando");
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(editText.getText());
                sendReceive.write(string.getBytes());
            }
        });
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    toastShow(NOT_BLUETOOTH_SUPPORT);
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        startActivityForResult(btEnablingIntent, REQUEST_ENABLE_BT);
                    }
                }
            }
        });
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    status.setText(BLUETOOTH_DISABLE);
                }
            }
        });
        enableVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
                startActivity(intent);
            }
        });
    }

    private void initialize() {
        int MY_PERMISSIONS_REQUEST = 200;
        ContextCompat.checkSelfPermission (this,Manifest.permission.ACCESS_FINE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST);

        status = findViewById(R.id.status);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);
        editText = findViewById(R.id.editText);
        message = findViewById(R.id.message);
        buttonOn = findViewById(R.id.btnOn);
        buttonOff = findViewById(R.id.btnOff);
        buttonSearchPaired = findViewById(R.id.search_paired);
        buttonSearchNewDevices = findViewById(R.id.search_new_devices);
        enableVisibility = findViewById(R.id.search);
        send = findViewById(R.id.send);

        stringArrayList = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);

        IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(scanModeReceiver, scanIntentFilter);

        mArrayAdapterNewDevices = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
        listView2.setAdapter(mArrayAdapterNewDevices);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                status.setText(BLUETOOTH_ENABLE);
            } else if (resultCode == RESULT_CANCELED) {
                status.setText(BLUETOOTH_DISABLE);
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText(TEXT_STATE_LISTENING);
                    break;
                case STATE_CONNECTING:
                    status.setText(TEXT_STATE_CONNECTING);
                    break;
                case STATE_CONNECTED:
                    status.setText(TEXT_STATE_CONNECTED);
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText(TEXT_STATE_CONNECTION_FAILED);
                    break;
                case STATE_MESSAGE_RECEIVED:
                    status.setText(TEXT_STATE_MESSAGE_RECEIVED);
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
                    message.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;
        ServerClass() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void toastShow(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
