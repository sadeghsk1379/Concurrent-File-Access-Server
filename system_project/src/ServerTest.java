import java.io.*; // وارد کردن کلاس‌های مربوط به ورودی و خروجی
import java.net.*; // وارد کردن کلاس‌های مربوط به شبکه
import java.util.concurrent.*; // وارد کردن کلاس‌های مربوط به اجرای همزمان نخ‌ها

class ServerTest {
    private static final int PORT = 12345; // پورت استفاده شده توسط سرور
    private static ExecutorService threadPool; // تعریف ThreadPool برای مدیریت نخ‌ها
    private static Server server; // تعریف سرور

    public static void main(String[] args) {
        setup(); // تنظیمات اولیه
        runTests(); // اجرای تست‌ها
        teardown(); // بستن منابع و پایان کار
    }

    private static void setup() {
        threadPool = Executors.newFixedThreadPool(10); // ایجاد ThreadPool با 10 نخ
        server = new Server(); // ایجاد یک نمونه از سرور
        new Thread(() -> server.main(new String[0])).start(); // شروع سرور در یک نخ جدید
    }

    private static void teardown() {
        threadPool.shutdown(); // بستن ThreadPool
    }

    private static void runTests() {
        testSingleClient(); // تست اتصال یک کلاینت
        testSingleClientMultipleWrites(); // تست چندین نوشتن توسط یک کلاینت
        testClientDisconnection(); // تست قطع ارتباط کلاینت
        testMultipleClientsConnections(); // تست اتصالات چندین کلاینت
        testConcurrentFileWrites(); // تست نوشتن همزمان به فایل
        testConcurrentFileReads(); // تست خواندن همزمان از فایل
        testInvalidMessage(); // تست پیام نامعتبر
        testNetworkInterruption(); // تست قطع ارتباط شبکه
        testInvalidFileName(); // تست نام فایل نامعتبر
    }

    private static void testSingleClient() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            String message = in.readLine(); // خواندن پیام خوشامدگویی از سرور
            assert message.equals("Hello, you have connected to the server."); // بررسی پیام خوشامدگویی
    
            out.println("Hello from client"); // ارسال پیام به سرور
            String fileContent = in.readLine(); // خواندن محتوای فایل از سرور
            assert fileContent.contains("Hello from client"); // بررسی محتوای فایل
    
            System.out.println("Test Single Client: Success"); // نمایش موفقیت تست
        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
            assert false; // شکست تست در صورت خطا
        }
    }
    
    private static void testSingleClientMultipleWrites() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            for (int i = 0; i < 5; i++) { // ارسال چندین پیام به سرور
                out.println("Message " + i);
            }
    
            String fileContent = in.readLine(); // خواندن محتوای فایل از سرور
            String[] lines = fileContent.split("\n"); // جدا کردن خطوط محتوای فایل
            assert lines.length == 5; // بررسی تعداد خطوط فایل
            for (int i = 0; i < 5; i++) {
                assert fileContent.contains("Message " + i); // بررسی وجود هر پیام در فایل
            }
    
            System.out.println("Test Single Client Multiple Writes: Success"); // نمایش موفقیت تست
        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
            assert false; // شکست تست در صورت خطا
        }
    }

    private static void testClientDisconnection() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            socket.close(); // بستن سوکت (قطع ارتباط)
            try {
                in.readLine(); // تلاش برای خواندن از سوکت بسته شده
                assert false; // نباید به این خط برسد (شکست تست)
            } catch (IOException e) {
                // خطای مورد انتظار، نیاز به شکست تست نیست
            }
        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
            assert false; // شکست تست در صورت خطا
        }
        System.out.println("Test Client Disconnection: Success"); // نمایش موفقیت تست
    }

    private static void testMultipleClientsConnections() {
        for (int i = 0; i < 5; i++) { // ایجاد چندین نخ برای اتصال کلاینت‌ها
            final int localI = i;
            threadPool.execute(() -> {
                try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
                    String message = in.readLine(); // خواندن پیام خوشامدگویی از سرور
                    assert message.equals("Hello, you have connected to the server."); // بررسی پیام خوشامدگویی
    
                    out.println("Hello from client " + localI); // ارسال پیام به سرور
                    String fileContent = in.readLine(); // خواندن محتوای فایل از سرور
                    assert fileContent.contains("Hello from client " + localI); // بررسی محتوای فایل
                } catch (IOException e) {
                    System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
                    assert false; // شکست تست در صورت خطا
                }
            });
        }
        System.out.println("Test Multiple Clients Connections: Success"); // نمایش موفقیت تست
    }
    
    private static void testConcurrentFileWrites() {
        for (int i = 0; i < 5; i++) { // ایجاد چندین نخ برای نوشتن همزمان به فایل
            final int localI = i;
    
            threadPool.execute(() -> {
                try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
                    out.println("Message " + localI); // ارسال پیام به سرور
                    String fileContent = in.readLine(); // خواندن محتوای فایل از سرور
                    String[] lines = fileContent.split("\n"); // جدا کردن خطوط محتوای فایل
                    assert lines.length == localI + 1; // بررسی تعداد خطوط فایل
                    for (int j = 0; j <= localI; j++) {
                        assert fileContent.contains("Message " + j); // بررسی وجود هر پیام در فایل
                    }
                } catch (IOException e) {
                    System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
                    assert false; // شکست تست در صورت خطا
                }
            });
        }
        System.out.println("Test Concurrent File Writes: Success"); // نمایش موفقیت تست
    }
    
    private static void testConcurrentFileReads() {
        for (int i = 0; i < 5; i++) { // ایجاد چندین نخ برای خواندن همزمان از فایل
            final int localI = i;
    
            threadPool.execute(() -> {
                try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
                    out.println("Message " + localI); // ارسال پیام به سرور
                    String fileContent = in.readLine(); // خواندن محتوای فایل از سرور
                    assert fileContent.contains("Message " + localI); // بررسی محتوای فایل
                } catch (IOException e) {
                    System.out.println("Error in client: " + e.getMessage()); // نمایش خطا
                    assert false; // شکست تست در صورت خطا
                }
            });
        }
        System.out.println("Test Concurrent File Reads: Success"); // نمایش موفقیت تست
    }

    private static void testInvalidMessage() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            out.println((String) null); // ارسال پیام نامعتبر (null)
            String errorMessage = in.readLine(); // خواندن پیام خطا از سرور
            assert errorMessage.contains("Error in client handler"); // بررسی پیام خطا
        } catch (IOException e) {
            // خطای مورد انتظار، نیاز به شکست تست نیست
        }
        System.out.println("Test Invalid Message: Success"); // نمایش موفقیت تست
    }
    
    private static void testNetworkInterruption() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            socket.close(); // شبیه‌سازی قطع ارتباط شبکه
            try {
                in.readLine(); // تلاش برای خواندن از سوکت بسته شده
                assert false; // نباید به این خط برسد (شکست تست)
            } catch (IOException e) {
                // خطای مورد انتظار، نیاز به شکست تست نیست
            }
        } catch (IOException e) {
            // خطای مورد انتظار، نیاز به شکست تست نیست
        }
        System.out.println("Test Network Interruption: Success"); // نمایش موفقیت تست
    }
    
    private static void testInvalidFileName() {
        try (Socket socket = new Socket("localhost", PORT)) { // اتصال به سرور
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // تنظیم ورودی
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // تنظیم خروجی
    
            out.println(""); // ارسال نام فایل نامعتبر (خالی)
            String errorMessage = in.readLine(); // خواندن پیام خطا از سرور
            assert errorMessage.contains("Error reading from file"); // بررسی پیام خطا
        } catch (IOException e) {
            // خطای مورد انتظار، نیاز به شکست تست نیست
        }
        System.out.println("Test Invalid File Name: Success"); // نمایش موفقیت تست
    }
}
