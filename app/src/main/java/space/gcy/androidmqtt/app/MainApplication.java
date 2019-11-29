package space.gcy.androidmqtt.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import space.gcy.androidmqtt.model.DaoMaster;
import space.gcy.androidmqtt.model.DaoSession;

public class MainApplication extends Application {
    private static MainApplication sMainApplication;

    public static MainApplication get() {
        return sMainApplication;
    }

    private static DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();
        sMainApplication = this;
        setupDatabase();
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库shop.db"
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "mqtt.db", null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
    }
    public static DaoSession getDaoInstant() {
        return daoSession;
    }
}
