package Manager;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

public class SerialManager {
    private static SerialManager singleton = new SerialManager();
    public static SerialManager get() { return singleton; }

    private SerialManager() {
        System.out.println("Initializing Manager.SerialManager...");

        try {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyACM0");

            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("SerialManager Error : 이미 사용 중인 포트입니다.");
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (!(commPort instanceof SerialPort))
                    System.out.println("SerialManager Error : commPort is not SerialPort");

                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                new Thread(new SerialReader(in)).start();
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            if (e instanceof Exception)
                SMTPManager.get().addMail((Exception) e);
            else if (e instanceof UnsatisfiedLinkError)
                System.out.println("SerialManager UnsatisfiedLinkError : " + e);
        }
    }

    public class SerialReader implements Runnable {
        private InputStream inputStream;

        public SerialReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;

            try {
                while ((len = inputStream.read(buffer)) > -1) {
                    System.out.print(new String(buffer, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
