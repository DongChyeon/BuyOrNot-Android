package com.sseotdabwa.buyornot.notification

/**
 * FCM data 페이로드 / Intent extras 키 모음.
 *
 * feedId 키 이름은 백엔드와 아직 미확정이므로 이 곳 한 곳만 수정하면 되도록 상수화한다.
 */
object FcmKeys {
    const val FEED_ID = "feedId"
    const val NOTIFICATION_ID = "notificationId"
    const val TYPE = "type"
}
