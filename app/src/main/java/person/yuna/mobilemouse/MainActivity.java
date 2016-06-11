package person.yuna.mobilemouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView show;
    RelativeLayout mainLayout;
    Button leftBtn,rightBtn;
    Button blueToothBtn, closeBlue, changeMode;
    Spinner blueToothSpinner;
    BluetoothSocket socket = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    boolean isConnect = false;
    int moveMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        show = (TextView) findViewById(R.id.show);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        rightBtn = (Button) findViewById(R.id.rightBtn);
        blueToothBtn = (Button) findViewById(R.id.blueToothBtn);
        blueToothSpinner = (Spinner) findViewById(R.id.blueToothSpinner);
//        closeBlue = (Button) findViewById(R.id.closeBlue);
        changeMode = (Button) findViewById(R.id.changeMode);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        blueToothBtn.setOnClickListener(new BlueButtonListener());
        leftBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("info","---action leftClick-----");
                show.setText("左键单击");
                sendMessage("leftclick");
            }
        });
//        closeBlue.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (socket != null) {
//                    try {
//                        socket.close();
//                        isConnect = false;
//                        Log.i("info","---链接断开-----");
//                    } catch (IOException e) {
//                        Log.e("TAG", e.toString());
//                    }
//                }
//            }
//        });
        changeMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch(moveMode){
                    case 0:
                        moveMode = 1;
                        mainLayout.setOnTouchListener(null);
                        sensorManager.registerListener(sensorlistener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//                        sensorManager.registerListener(sensorlistener, sensor, 10);
                        changeMode.setText("切换至触屏滑动模式");
                        break;
                    case 1:
                        moveMode = 0;
                        mainLayout.setOnTouchListener(touchListener);
                        sensorManager.unregisterListener(sensorlistener);
                        changeMode.setText("切换至重力感应模式");
                        break;
                }
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("info","---action rightClick-----");
                show.setText("右键单击");
                sendMessage("rightclick");
            }
        });
        if (moveMode == 0) {
            mainLayout.setOnTouchListener(touchListener);
        }else if(moveMode == 1){
            sensorManager.registerListener(sensorlistener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener(){
        private float deltaX;
        private float deltaY;
        private float preX;
        private float preY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("info", "---action down-----");
                    preX = event.getX();
                    preY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("info", "---action move-----");
                    deltaX = event.getX() - preX;
                    deltaY = event.getY() - preY;
                    show.setText("偏移坐标为：" + "(" + deltaX + " , " + deltaY + ")");
                    sendMessage("move:(" + deltaX + "," + deltaY + ")");
                    preX = event.getX();
                    preY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("info", "---action up-----");
            }
            return true;
        }
    };
    private SensorEventListener sensorlistener= new SensorEventListener(){
        float gravityX, gravityY, gravityZ;
        @Override
        public void onSensorChanged(SensorEvent event) {
            gravityX=event.values[0];
            gravityY=event.values[1];
            gravityZ=event.values[2];
            show.setText("加速度感应XYZ：("+gravityX+","+gravityY+","+gravityZ+")");
            sendMessage("gravity:("+gravityX+","+gravityY+","+gravityZ+")");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private boolean sendMessage(String message){
        if (isConnect) {
            try {
                OutputStream outStream = socket.getOutputStream();
                outStream.write((message + "\n").getBytes());
                Log.i("info","---发送成功-----");
                return true;
            } catch (IOException e) {
                Log.e("TAG", e.toString());
                Log.i("info","---发送失败-----");
                return false;
            }
        }else{
            Log.i("info","---发送失败-----");
            return false;
        }
    }
    public class BlueSelectorItem{
        private String mac;
        private String name;
        private BluetoothDevice device;

        public BlueSelectorItem(BluetoothDevice device){
            this.device = device;
            this.name = device.getName();
            this.mac = device.getAddress();
        }

        public String getName(){
            return this.name;
        }
        public String getMac(){
            return this.mac;
        }
        public BluetoothDevice getDevice(){
            return this.device;
        }


        //重写toString方法，设置适配器显示的数据
        @Override
        public String toString() {
            String output = this.name + "(" + this.mac + ")";
            return output;
        }
    }

    private class BlueButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null){
                Log.i("info","本机没有蓝牙设备");
                Toast.makeText(MainActivity.this, "找不到本机的蓝牙设备", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i("info","本机有蓝牙设备");
            if (isConnect){
                if (socket != null) {
                    try {
                        socket.close();
                        Toast.makeText(MainActivity.this, "链接已断开", Toast.LENGTH_SHORT).show();
                        Log.i("info","---链接断开-----");
                    } catch (IOException e) {
                        Log.e("TAG", e.toString());
                    }
                }
                blueToothBtn.setText("扫描蓝牙设备");
                isConnect = false;
            }else {
                if (!adapter.isEnabled()) {  //启动蓝牙设备
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                }
                //设置蓝牙可见性
                Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        120);
                startActivity(discoveryIntent);
                //获取已配对列表
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                if (devices.size() > 0) {
                    List<BlueSelectorItem> selectList = new ArrayList<BlueSelectorItem>();
                    for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                        selectList.add(new BlueSelectorItem(bluetoothDevice));
                        Log.i("info", bluetoothDevice.getAddress());
                    }
                    ArrayAdapter<BlueSelectorItem> selectAdapter = new ArrayAdapter<BlueSelectorItem>(MainActivity.this, android.R.layout.simple_spinner_item, selectList);
//                    selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    blueToothSpinner.setAdapter(selectAdapter);
                    blueToothSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
                }
                adapter.cancelDiscovery();
            }
        }
    }

    private class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener{
        static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = ((BlueSelectorItem) blueToothSpinner.getSelectedItem()).getDevice();
            UUID uuid = UUID.fromString(SPP_UUID);
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
                isConnect = true;
                blueToothBtn.setText("切断蓝牙链接");
                Toast.makeText(MainActivity.this, "链接成功", Toast.LENGTH_SHORT).show();
                Log.i("info","---链接成功-----");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                Log.e("TAG", e.toString());
                Toast.makeText(MainActivity.this, "链接失败", Toast.LENGTH_SHORT).show();
                Log.i("info","---链接失败-----");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (socket != null) {
                try {
                    socket.close();
                    isConnect = false;
                    Log.i("info","---链接断开-----");
                } catch (IOException e) {
                    Log.e("TAG", e.toString());
                }
            }
        }
    }
}
