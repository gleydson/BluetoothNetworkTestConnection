package br.ufc.quixada.bluetoothnetworktestconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String NOT_BLUETOOTH_SUPPORT = "Seu dispositivo não possui suporte a conexões bluetooth!";
    private static final String BLUETOOTH_ENABLE = "Bluetooth habilitado";
    private static final String BLUETOOTH_DISABLE = "Bluetooth desabilitado";

    private TextView status;
    private ListView listView1;
    private ListView listView2;
    private EditText editText;
    private Button buttonOn, buttonOff;
    private Button buttonSearchPaired, buttonSearchNewDevices;
    private Button send;

    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> mArrayAdapterNewDevices;

    private BluetoothAdapter mBluetoothAdapter;

    private Intent btEnablingIntent;
    private int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        bluetoothONMethod();
        bluetoothOFFMethod();
        discoveryPairedDevices();
        buttonSearchNewDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();
            }
        });
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);

        mArrayAdapterNewDevices = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        listView2.setAdapter(mArrayAdapterNewDevices);
    }

    private void initialize() {
        status = findViewById(R.id.status);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);
        editText = findViewById(R.id.editText);
        buttonOn = findViewById(R.id.btnOn);
        buttonOff = findViewById(R.id.btnOff);
        buttonSearchPaired = findViewById(R.id.search_paired);
        buttonSearchNewDevices = findViewById(R.id.search_new_devices);
        send = findViewById(R.id.send);

        stringArrayList = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    private void bluetoothONMethod() {
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
    }

    private void bluetoothOFFMethod() {
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    status.setText(BLUETOOTH_DISABLE);
                }
            }
        });
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

    private void discoveryPairedDevices() {
        buttonSearchPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = mBluetoothAdapter.getBondedDevices();
                ArrayAdapter<String> mArrayAdapterPaired = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        mArrayAdapterPaired.add(device.getName() + "\n" + device.getAddress());
                    }
                    listView1.setAdapter(mArrayAdapterPaired);
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void toastShow(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
