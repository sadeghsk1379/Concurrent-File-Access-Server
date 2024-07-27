import java.io.*; // وارد کردن کلاس‌های مربوط به ورودی و خروجی
import java.net.*; // وارد کردن کلاس‌های مربوط به شبکه
import java.util.concurrent.*; // وارد کردن کلاس‌های مربوط به اجرای همزمان نخ‌ها
import java.util.concurrent.locks.*; // وارد کردن کلاس‌های مربوط به قفل‌ها

public class Server {
    private static final int MAX_THREADS = 10; // تعداد حداکثر نخ‌ها برای ThreadPool
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS); // ایجاد یک ThreadPool با حداکثر تعداد نخ‌های مشخص شده
    private static ReadWriteLock fileLock = new ReentrantReadWriteLock(); // قفل خواندن و نوشتن برای محافظت از دسترسی به فایل

    public static void main(String[] args) {
        int port = 12345; // پورتی که سرور روی آن گوش می‌دهد

        try (ServerSocket serverSocket = new ServerSocket(port)) { // ایجاد یک ServerSocket برای گوش دادن به اتصالات ورودی روی پورت مشخص شده
            System.out.println("Server listening on port " + port); // نمایش پیامی که سرور در حال گوش دادن است

            while (true) { // حلقه بی‌نهایت برای قبول اتصالات جدید
                Socket clientSocket = serverSocket.accept(); // قبول اتصال کلاینت
                System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress()); // نمایش پیامی که اتصال جدید برقرار شده است

                threadPool.execute(new ClientHandler(clientSocket)); // اجرای ClientHandler در یک نخ جدید از ThreadPool
            }
        } catch (IOException e) { // مدیریت خطاهای ورودی/خروجی
            System.out.println("Error in server: " + e.getMessage()); // نمایش پیام خطا در سرور
        } finally {
            threadPool.shutdown(); // بستن ThreadPool وقتی که سرور بسته می‌شود
        }
    }

    private static class ClientHandler implements Runnable { // کلاس داخلی برای هندل کردن کلاینت‌ها، که Runnable را پیاده‌سازی می‌کند
        private Socket clientSocket; // سوکت کلاینت

        public ClientHandler(Socket socket) {
            this.clientSocket = socket; // ذخیره سوکت کلاینت
        }

        @Override
        public void run() { // متد run که توسط ThreadPool اجرا می‌شود
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // ایجاد ورودی برای دریافت داده از کلاینت
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // ایجاد خروجی برای ارسال داده به کلاینت

                String message = "Hello, you have connected to the server."; // پیام خوشامدگویی
                out.println(message); // ارسال پیام به کلاینت

                String clientMessage = in.readLine(); // خواندن پیام از کلاینت
                System.out.println("Message from client: " + clientMessage); // نمایش پیام دریافت شده از کلاینت

                fileLock.writeLock().lock(); // گرفتن قفل نوشتن برای دسترسی به فایل
                try {
                    writeToFile(clientMessage); // نوشتن پیام کلاینت به فایل
                } finally {
                    fileLock.writeLock().unlock(); // آزاد کردن قفل نوشتن
                }

                fileLock.readLock().lock(); // گرفتن قفل خواندن برای دسترسی به فایل
                try {
                    String fileContent = readFromFile(); // خواندن محتوای فایل
                    out.println("File content: " + fileContent); // ارسال محتوای فایل به کلاینت
                } finally {
                    fileLock.readLock().unlock(); // آزاد کردن قفل خواندن
                }

            } catch (IOException e) { // مدیریت خطاهای ورودی/خروجی
                System.out.println("Error in client handler: " + e.getMessage()); // نمایش پیام خطا در هندلر کلاینت
            } finally {
                try {
                    clientSocket.close(); // بستن سوکت کلاینت
                } catch (IOException e) { // مدیریت خطاهای ورودی/خروجی
                    System.out.println("Error closing client socket: " + e.getMessage()); // نمایش پیام خطا در بستن سوکت کلاینت
                }
            }
        }

        private void writeToFile(String content) { // متد برای نوشتن پیام کلاینت به فایل
            try (FileWriter writer = new FileWriter("output.txt", true)) { // باز کردن فایل برای نوشتن، در حالت append
                writer.write(content + "\n"); // نوشتن محتوای پیام به فایل
            } catch (IOException e) { // مدیریت خطاهای ورودی/خروجی
                System.out.println("Error writing to file: " + e.getMessage()); // نمایش پیام خطا در نوشتن به فایل
            }
        }

        private String readFromFile() { // متد برای خواندن محتوای فایل
            StringBuilder content = new StringBuilder(); // استفاده از StringBuilder برای ساختن رشته محتوای فایل
            try (BufferedReader reader = new BufferedReader(new FileReader("output.txt"))) { // باز کردن فایل برای خواندن
                String line;
                while ((line = reader.readLine()) != null) { // خواندن هر خط از فایل
                    content.append(line).append("\n"); // اضافه کردن خط خوانده شده به محتوای رشته
                }
            } catch (IOException e) { // مدیریت خطاهای ورودی/خروجی
                System.out.println("Error reading from file: " + e.getMessage()); // نمایش پیام خطا در خواندن از فایل
            }
            return content.toString(); // بازگشت محتوای فایل به عنوان یک رشته
        }
    }
}
