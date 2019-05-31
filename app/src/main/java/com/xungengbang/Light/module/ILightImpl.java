package com.xungengbang.Light.module;

import android.serialport.api.SerialPort;
import android.util.Log;

import com.smartdevicesdk.utils.StringUtility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ILightImpl implements ILight {
    SerialPort mSerialport = new SerialPort();
    String serialPort_path = "/dev/ttyMT0";
    int serialPort_buad = 9600;

    private static String green_on = "aa 01 01 55";
    private static String green_off = "aa 01 00 55";

    private static String red_on = "aa 02 01 55";
    private static String red_off = "aa 02 00 55";

    private static String blue_on = "aa 03 01 55";
    private static String blue_off = "aa 03 00 55";

    private static String yellow_on = "aa 04 01 55";
    private static String yellow_off = "aa 04 00 55";

    private static String grblue_on = "aa 05 01 55";
    private static String grblue_off = "aa 05 00 55";

    private static String white_on = "aa 07 01 55";
    private static String white_off = "aa 07 00 55";

    private static String Bigwhite_on = "aa 0C 50 55";
    private static String Bigwhite_off = "aa 0C 00 55";



    AtomicBoolean keepSending = new AtomicBoolean(false);

    public ILightImpl() {
        if (mSerialport.isOpen) {
            if (mSerialport.closePort()) {

            }
        } else {
            if (mSerialport.open(serialPort_path, serialPort_buad)) {
                Log.e("light", "success_open");
                mSerialport.WriteNoSleep(new byte[]{0x1b, 0x26, 02});
                mSerialport.setOnserialportDataReceived(new android.serialport.api.SerialPortDataReceived() {
                    @Override
                    public void onDataReceivedListener(byte[] buffer, int size) {
                        if (StringUtility.ByteArrayToString(buffer, size).startsWith("E2")) {

                        } else {
                            keepSending.getAndSet(false);
                        }
                    }
                });
            } else {
                Log.e("light", "failed_open");
            }
        }

    }

    @Override
    public void white(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(white_on);
                } else {
                    buffer = hexStringToBytes(white_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }
            }
        });
    }

    @Override
    public void blue(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(blue_on);
                } else {
                    buffer = hexStringToBytes(blue_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }
            }
        });
    }

    @Override
    public void red(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(red_on);
                } else {
                    buffer = hexStringToBytes(red_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }

            }
        });
    }

    @Override
    public void yellow(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(yellow_on);
                } else {
                    buffer = hexStringToBytes(yellow_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }

            }
        });
    }

    @Override
    public void green(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(green_on);
                } else {
                    buffer = hexStringToBytes(green_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }

            }
        });
    }

    @Override
    public void grblue(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(grblue_on);
                } else {
                    buffer = hexStringToBytes(grblue_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }

            }
        });
    }

    @Override
    public void Bigwhite(final boolean status) {
        keepSending.getAndSet(true);
        MySingleTaskThreadExecutor.getInstance().submit(new Runnable() {
            byte[] buffer = null;
            @Override
            public void run() {
                if (status) {
                    buffer = hexStringToBytes(Bigwhite_on);
                } else {
                    buffer = hexStringToBytes(Bigwhite_off);
                }
                while (keepSending.get()) {
                    mSerialport.WriteGetData(buffer);
                }

            }
        });
    }

    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

}

class MySingleTaskThreadExecutor {
    private static final ExecutorService instance = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);
                public Thread newThread(Runnable r) {
                    return new Thread(r, "SingleTaskPoolThread #" + mCount.getAndIncrement());
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    Log.e("TAG", "之前的还没完，这个不干了");
                    executor.remove(r);
                }
            });

    public static ExecutorService getInstance() {
        return instance;
    }
}