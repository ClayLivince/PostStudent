package xyz.cyanclay.poststudent.network.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BUPTLog {

    static File logFile;
    static LogLevel logLevel = LogLevel.INFO;
    static SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    static SimpleDateFormat logFormat = new SimpleDateFormat("[HH:mm:ss]", Locale.CHINA);

    public static void init(Context context) throws IOException {
        File dir = context.getExternalFilesDir(null);
        logFile = new File(dir, fileNameFormat.format(new Date()) + ".log");
        if (!logFile.exists())
            logFile.createNewFile();
    }

    public static void i(String tag, String content) {
        writeLog(tag, content, LogLevel.INFO, null);
    }

    public static void v(String tag, String content) {
        writeLog(tag, content, LogLevel.VERBOSE, null);
    }

    public static void w(String tag, String content) {
        writeLog(tag, content, LogLevel.WARN, null);
    }

    public static void d(String tag, String content) {
        writeLog(tag, content, LogLevel.DEBUG, null);
    }

    public static void e(String tag, String content) {
        writeLog(tag, content, LogLevel.ERROR, null);
    }

    public static void e(String tag, String content, Throwable t) {
        writeLog(tag, content, LogLevel.ERROR, t);
    }


    static void writeLog(String tag, String content, LogLevel level, Throwable t) {
        switch (level) {
            case VERBOSE: {
                Log.v(tag, content);
                break;
            }
            case DEBUG: {
                Log.d(tag, content);
                break;
            }
            case INFO: {
                Log.i(tag, content);
                break;
            }
            case WARN: {
                Log.w(tag, content);
                break;
            }
            case ERROR: {
                Log.e(tag, content);
                break;
            }
        }
        if (level.ordinal() >= logLevel.ordinal()) {
            try {
                FileWriter filerWriter = new FileWriter(logFile, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                String log = logFormat.format(new Date()) +
                        logLevel.name() +
                        "/" +
                        tag +
                        ": " +
                        content;
                bufWriter.write(log);
                bufWriter.newLine();
                if (t != null) {
                    bufWriter.write(Log.getStackTraceString(t));
                    bufWriter.newLine();
                }
                bufWriter.close();
                filerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    enum LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR;
    }

}
