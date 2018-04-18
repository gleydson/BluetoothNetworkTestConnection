package br.ufc.quixada.bluetoothnetworktestconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String NOT_BLUETOOTH_SUPPORT = "Seu dispositivo não possui suporte a conexões bluetooth!";
    private static final String BLUETOOTH_ENABLE = "Bluetooth habilitado!";
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView status;
    private ListView listView1;
    private ListView listView2;
    private EditText editText;
    private Button send;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    private ArrayAdapter<String> mArrayAdapterPaired;
    private ArrayAdapter<String> mArrayAdapterNew;
    private IntentFilter filter;
    private Intent discoverableIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initialWork();
        discoveryPairedDevices();
    }

    private void initialize() {
        status = findViewById(R.id.status);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);
        editText = findViewById(R.id.editText);
        send = findViewById(R.id.send);

        mArrayAdapterPaired = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        mArrayAdapterNew = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);

        discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    }

    private void initialWork() {
        if (mBluetoothAdapter == null) {
            toastShow(NOT_BLUETOOTH_SUPPORT);
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                status.setText(BLUETOOTH_ENABLE);
            }
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        }
    }

    private void discoveryPairedDevices() {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            mArrayAdapterPaired.add(device.getName() + "\n" + device.getAddress());
                toastShow(device.getName() + "\n" + device.getAddress());
            }
    }
        listView1.setAdapter(mArrayAdapterPaired);
}

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapterNew.add(device.getName() + "\n" + device.getAddress());
                toastShow(device.getName() + "\n" + device.getAddress());
            }
            listView2.setAdapter(mArrayAdapterNew);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, filter);
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
