import java.io.*;
import java.math.RoundingMode;
import java.net.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
/**
 * Created by ASUS on 2018/4/24.
 */


public class P2pDownloadClient extends Socket{
    private static DecimalFormat df = null;


    private static String filePath ="C:\\Users\\ASUS\\Desktop\\P2P\\p2p_download";    //download path

    private static String filename;

    private static int filesize;

    static{
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public P2pDownloadClient(String ip, String fileName) throws Exception{

        super(ip,8889);
        filename = fileName;
        //filesize = fileSize;

    }

    public void load(){
        new Thread(new Task(this)).start();
    }

    class Task implements Runnable {
        private Socket socket;

        private DataInputStream dis;

        private FileOutputStream fos;

        private OutputStream outputStream;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override

        public void run() {
            try {
                outputStream = this.socket.getOutputStream();

                String message = "DOWNLOAD " + filename;
                byte[] sendBytes = message.getBytes("UTF-8");
                outputStream.write(sendBytes.length >> 8);
                outputStream.write(sendBytes.length);
                outputStream.write(sendBytes);
                outputStream.flush();

                dis = new DataInputStream(socket.getInputStream());

                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                File directory = new File(filePath);//cache file path
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                fos = new FileOutputStream(file);

                byte[] bytes = new byte[1024];
                int length, i = 0;
                long progress = 0;
                while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                   /* progress += length;
                    if (i % 10 == 0) {
                        System.out.print("|" + (100 * progress / filesize) + "%|");
                    }
                    i++;*/
                }

                System.out.println("\n[DownloadClient info]: Catch file " + fileName + " successful!");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                    if (dis != null)
                        dis.close();
                    socket.close();
                    System.out.println("[DownloadClient info]: Disconnected");
                } catch (Exception e) {
                }
            }
        }
    }
}
