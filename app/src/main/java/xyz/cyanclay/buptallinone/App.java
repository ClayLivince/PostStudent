package xyz.cyanclay.buptallinone;

import android.app.Application;
import android.widget.Toast;

import org.apache.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class App extends Application {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    @Override
    public void onCreate() {
        super.onCreate();

        File exFilesDir = this.getExternalFilesDir(null);
        File logDir = new File(exFilesDir, ".log");

        Date date = new Date();
        File logFile = new File(logDir, dateFormat.format(date) + "_" + System.currentTimeMillis() + ".log");

        if (!logDir.exists())
            if (!logDir.mkdir()) {
                Toast.makeText(this, R.string.failed_to_create_log_dir, Toast.LENGTH_LONG).show();

                requestPermission();

                if (!logDir.mkdir()) {
                    Toast.makeText(this, R.string.failed_to_create_log_dir, Toast.LENGTH_LONG).show();
                }
            }

        final LogConfigurator logConfigurator = new LogConfigurator();
        String fileName = dateFormat.format(new Date()) + ".log";

        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.setLevel("org.apache", Level.INFO);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setLogCatPattern("%m%n");
        //设置总文件大小 (1M)
        logConfigurator.setMaxFileSize(1024 * 1024);
        //设置最大产生的文件个数
        logConfigurator.setMaxBackupSize(2);
        //设置所有消息是否被立刻输出 默认为true,false 不输出
        logConfigurator.setImmediateFlush(true);
        //是否本地控制台打印输出 默认为true ，false不输出
        logConfigurator.setUseLogCatAppender(true);
        //设置是否启用文件附加,默认为true。false为覆盖文件
        logConfigurator.setUseFileAppender(true);
        //设置是否重置配置文件，默认为true
        logConfigurator.setResetConfiguration(true);
        //是否显示内部初始化日志,默认为false
        logConfigurator.setInternalDebugging(false);

        logConfigurator.setFileName(logFile.getAbsolutePath());

        logConfigurator.configure();
    }

    void requestPermission() {

    }
}
