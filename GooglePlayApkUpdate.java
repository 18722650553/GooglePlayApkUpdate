import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

/**
 * @ClassName: GooglePlayApkUpdate
 * @Author: 于学智
 * @Description: Google Play应用内更新
 * @CreateDate: 2022/5/11 16:55
 * @Version: 1.0
 * @E-mail:18722650553@139.com
 * @Link:https://github.com/18722650553
 */
public class GooglePlayApkUpdate {
    private static final String TAG = GooglePlayApkUpdate.class.getSimpleName();

    private static int MY_REQUEST_CODE = 10086;

    /**
     * 进行更新
     *
     * @param context
     */
    public static void update(final Context context) {
        //创建管理器的实例
        final AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        //返回用于检查更新的对象
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        //检查平台是否允许指定类型的更新
        appUpdateInfoTask.addOnSuccessListener(
                new OnSuccessListener<AppUpdateInfo>() {
                    @Override
                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
                        //是否允许更新
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                //Google Play商店通知用户更新以来已过去了多少时间
                                && appUpdateInfo.clientVersionStalenessDays() != null
                                && appUpdateInfo.clientVersionStalenessDays() >= 7
                                //检查更新优先级
                                && appUpdateInfo.updatePriority() >= 5
                                //检查更新模式
                                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            //启动更新
                            try {
                                appUpdateManager.startUpdateFlowForResult(
                                        //传递getAppUpdateInfo()返回意图
                                        appUpdateInfo,
                                        //或使用AppUpdateType.FLEXIBLE进行灵活更新
                                        AppUpdateType.IMMEDIATE,
                                        (Activity) context,
                                        MY_REQUEST_CODE
                                );
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
        );
        //灵活更新
        //创建一个侦听器以跟踪请求状态更新
        InstallStateUpdatedListener listenter = new InstallStateUpdatedListener() {

            @Override
            public void onStateUpdate(@NonNull InstallState state) {
                //（可选）提供下载禁毒栏
                if (state.installStatus() == InstallStatus.DOWNLOADING) {
                    //进度
                    long bytesDownloaded = state.bytesDownloaded();
                    long totalBytesToDownload = state.totalBytesToDownload();
                }
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //下载完成，提示用户并触发安装
                    appUpdateManager.completeUpdate();
                }
            }
        };
        //在开始更新之前，注册一个监听器以进行更新
        appUpdateManager.registerListener(listenter);
        //当不在需要状态更新时，请取消注册监听器
        appUpdateManager.unregisterListener(listenter);
    }

    /**
     * 更新状态回调
     *
     * @param requestCode
     * @param resultCode
     * @param date
     */
    public void onActivityResult(int requestCode, int resultCode, Intent date) {
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "Update flow failed! Result code:" + resultCode);
                //如果更新被取消或失败，您可以请求开始更新
            }
        }
    }

//    /**
//     * 如果更新下载但不安装，通知用户来完成更新
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
//                new OnSuccessListener<AppUpdateInfo>() {
//                    @Override
//                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
//                        //已下载成功
//                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
//                            //通知并执行安装
//                            popupSnckbarForCompleteUpdate();
//                        }
//                    }
//                }
//        );
//    }

//    /**
//     * 当应用返回前台时，应该确认更新没有停滞在该UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS状态
//     * 如果更新处于这种状态，请继续更新
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
//                new OnSuccessListener<AppUpdateInfo>() {
//                    @Override
//                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
//                        //已下载成功
//                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
//                            //通知并执行安装
//                            try {
//                                appUpdateManager.startUpdateFlowForResult(
//                                        appUpdateInfo,
//                                        AppUpdateType.IMMEDIATE,
//                                        (Activity) context,
//                                        MY_REQUEST_CODE
//                                );
//                            } catch (IntentSender.SendIntentException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//        );
//    }
}
