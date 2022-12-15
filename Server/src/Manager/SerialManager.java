package Manager;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialManager {
    private static SerialManager singleton = new SerialManager();
    public static SerialManager get() { return singleton; }

    private SerialManager() {
        System.out.println("Initializing Manager.SerialManager...");

        try {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyACM0");
            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (commPort instanceof SerialPort) {
                    SerialPort serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

                    InputStream in = serialPort.getInputStream();
                    OutputStream out = serialPort.getOutputStream();

                    (new Thread(new SerialReader(in))).start();
                } else {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            System.out.println(e);
            //SMTPManager.get().addMail(e);
        }
    }

    public class SerialReader implements Runnable {
        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    System.out.print(new String(buffer, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
