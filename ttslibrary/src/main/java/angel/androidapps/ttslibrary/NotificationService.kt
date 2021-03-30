/*
 * Created by Angel on 17/1/21 12:04 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 17/1/21 11:59 AM
 */


package angel.androidapps.ttslibrary

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import angel.androidapps.ttslibrary.data.entities.PlaybackMetaData

@Suppress("unused")
open class NotificationService : Service() {

    private var clazz: Class<*>? = null

    @Volatile
    private var showingNotification = false


    private var pkName: String = ""
    private var channelId = ""
    private var channelName = ""
    private var title = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {
            pkName = getStringExtra(PACKAGE_NAME) ?: ""
            clazz = Class.forName(getStringExtra(CLASS_NAME))
            channelId = "$pkName.narrate_channel"
            // The user-visible description of the channel.
            channelName = "$packageName notification channel"


            title = getStringExtra(NOTIFICATION_TITLE) ?: ""
            val icon = getIntExtra(ICON_RES_ID, 0)
            val subTitle = getStringExtra(NOTIFICATION_SUBTITLE) ?: ""
            val isPlaying = getBooleanExtra(IS_PLAYING, false)
            val hasNext = getBooleanExtra(HAS_NEXT, false)


            startNotification(icon, title, subTitle, isPlaying, hasNext)
        } ?: startNotification()
        return START_STICKY
    }

    private fun getNotificationManager() = NotificationManagerCompat.from(this)

    //START AND STOP NOTIFICATION
    //============================
    private fun startNotification(
        icon: Int = 0,
        title: String = "",
        subtitle: String = "",
        isPlaying: Boolean = false,
        hasNext: Boolean = false
    ) {
        //print("isPlaying: $isPlaying, hasNext: $hasNext. '$title' ($subtitle)")
        val notification = prepareBuilder(icon, title, subtitle, isPlaying, hasNext)

        if (!showingNotification) {
            showingNotification = true
            startForeground(NOTIFICATION_ID, notification)

        } else {
            getNotificationManager().notify(NOTIFICATION_ID, notification)
        }
    }


    private fun endNotification() {
        if (showingNotification) {
            print("stopping notification...")
            showingNotification = false
            stopForeground(true)
        } else {
            print("notification already stopped")
        }
    }


    override fun onDestroy() {
        //print("onDestroy()")
        endNotification()
        super.onDestroy()
    }

    //CREATE NOTIFICATION
    //============================

    open fun prepareBuilder(
        icon: Int,
        title: String,
        subtitle: String,
        playing: Boolean,
        hasNext: Boolean
    ): Notification {

        val onClickIntent = Intent(applicationContext, clazz)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(
            this, 0, onClickIntent, flag
        )
        createChannelIfNeeded()

        return NotificationCompat.Builder(this, channelId).let { nb ->
            nb.setContentTitle(title)
                .setContentText(subtitle)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setStyle(MediaStyle())

            if (icon > 0) {
                nb.setSmallIcon(icon)
            }
            if (playing) {
                nb.addAction(
                    createAction(R.drawable.ic_service_pause_24, "||", ACTION_PAUSE)
                )
            } else {
                nb.addAction(
                    createAction(R.drawable.ic_service_play_24, ">", ACTION_PLAY)
                )
            }
            if (hasNext) {
                nb.addAction(
                    createAction(R.drawable.ic_service_next_24, ">|", ACTION_NEXT)
                )
            }
            nb.build()
        }

    }

    private fun createAction(
        icon: Int, title: String, intentAction: String
    ): NotificationCompat.Action? {
        val clickIntent = Intent().setAction(intentAction)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
        else 0

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, clickIntent, flag
        )
        return NotificationCompat.Action.Builder(icon, title, pendingIntent)
            .build()
    }

    //CHANNELS (OREO)
    //===============
    private fun createChannelIfNeeded() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getNotificationManager()


            //https://stackoverflow.com/questions/46086233/media-controls-notification-emits-alert-on-android-o
            val importance = NotificationManager.IMPORTANCE_LOW
            //low will not have the alert sound on api26
            // print("channelID: $channelId")
            // print("channelNAME: $channelName")
            val mChannel = NotificationChannel(channelId, channelName, importance)
            // Configure the notification channel.
            mChannel.description = channelName
            mChannel.setShowBadge(false)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    //COMPANION OBJECT
    //============================

    companion object {
        const val PACKAGE_NAME = "package_name"
        const val CLASS_NAME = "class_name"
        const val ICON_RES_ID = "icon_res_id"
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"

        private const val NOTIFICATION_ID = 901
        private const val NOTIFICATION_TITLE = "NOTIFICATION_TITLE"
        private const val NOTIFICATION_SUBTITLE = "NOTIFICATION_TEXT"
        private const val HAS_NEXT = "HAS_NEXT"
        private const val IS_PLAYING = "IS_PLAYING"

        //private const val TAG = "Angel: TtsNoti"
        private fun print(s: String) {
          //   Log.d(TAG, s)
        }

        //==============================
        //START NOTIFICATION SERVICE
        //==============================
        fun startService(activity: Activity, logoResId: Int, metaData: PlaybackMetaData) {
            val notificationIntent = makeNotificationIntent(
                activity, logoResId, metaData
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(notificationIntent)
            } else {
                activity.startService(notificationIntent)
            }
        }

        fun endService(context: Context) {
            context.stopService(Intent(context, NotificationService::class.java))
        }

        private fun makeNotificationIntent(
            activity: Activity,
            iconResId: Int,
            metaData: PlaybackMetaData
        ): Intent {

            val intent = Intent(activity, NotificationService::class.java)
            intent.putExtra(PACKAGE_NAME, activity.packageName)
            intent.putExtra(CLASS_NAME, activity.javaClass.name)
            intent.putExtra(ICON_RES_ID, iconResId)
            intent.putExtra(NOTIFICATION_TITLE, metaData.title)
            intent.putExtra(NOTIFICATION_SUBTITLE, metaData.subtitle)
            intent.putExtra(IS_PLAYING, metaData.isAutoPlay)
            intent.putExtra(HAS_NEXT, metaData.hasNext)
            return intent
        }


        //==============================
        //BROADCAST RECEIVER
        //==============================
        private var broadcastReceiver: BroadcastReceiver? = null

        @Suppress("MemberVisibilityCanBePrivate")
        fun unregisterBroadcastReceiver(context: Context) {
            if (broadcastReceiver != null) {
                try {
                    context.unregisterReceiver(broadcastReceiver)
                    print("Unregistering broadcast receiver")
                } catch (e: Exception) {
                    print("No receiver registered.")
                }
            }
            broadcastReceiver = null
        }

        fun registerNotificationBroadcastReceiver(
            context: Context,
            onPlayClickedListener: () -> Unit,
            onPauseClickedListener: () -> Unit,
            onNextClickedListener: () -> Unit
        ) {

            unregisterBroadcastReceiver(context)
            print("Setting up broadcast receiver")
            val filter = IntentFilter()
            filter.addAction(ACTION_PAUSE)
            filter.addAction(ACTION_PLAY)
            filter.addAction(ACTION_NEXT)

            broadcastReceiver = (object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        ACTION_PLAY -> {
                            print("ACTION_PLAY"); onPlayClickedListener.invoke()
                        }
                        ACTION_PAUSE -> {
                            print("ACTION_PAUSE"); onPauseClickedListener.invoke()
                        }
                        ACTION_NEXT -> {
                            print("ACTION_NEXT"); onNextClickedListener.invoke()
                        }
                        else -> print("Unknown action $intent?.action")
                    }
                }
            }).also { receiver ->
                context.registerReceiver(receiver, filter)
            }
        }


    }
}