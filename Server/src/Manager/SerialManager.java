package Manager;

import Data.Constants;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

public class SerialManager {
    private static SerialManager singleton = new SerialManager();
    public static SerialManager get() { return singleton; }

    private int ldrValue = -1;

    private SerialManager() {
        System.out.println("Initializing Manager.SerialManager...");

        try {
            System.setProperty("gnu.io.rxtx.SerialPorts", Constants.SerialConfig.SERIAL_PORT);
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(Constants.SerialConfig.SERIAL_PORT);

            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("SerialManager Error : 이미 사용 중인 포트입니다. (" );
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (!(commPort instanceof SerialPort))
                    System.out.println("SerialManager Error : commPort is not SerialPort");

                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(Constants.SerialConfig.SERIAL_PORT_BIT, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                new Thread(new SerialReader(in)).start();
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            if (e instanceof Exception)
                SMTPManager.get().addMail((Exception) e);
        }
    }

    public class SerialReader implements Runnable {
        private InputStream inputStream;
        private StringBuilder inputBuilder = new StringBuilder();

        public SerialReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;

            try {
                while ((len = inputStream.read(buffer)) > -1) {
                    String str = new String(buffer, 0, len);
                    inputBuilder.append(str);

                    if (str.contains(System.lineSeparator())) {
                        readDone(inputBuilder.toString());
                        inputBuilder.delete(0, inputBuilder.length());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readDone(String str) {
            str = str.replace(System.lineSeparator(), "");
            str = str.trim();

            if (str.startsWith("ldr : ")) {
                try {
                    ldrValue = Integer.parseInt(str.substring("ldr : ".length()));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public int getLDRValue() {
        return ldrValue;
    }
}
