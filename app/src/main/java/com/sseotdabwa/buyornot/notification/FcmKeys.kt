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

    /**
     * 알림 탭으로 열린 Intent임을 표시하는 마커.
     *
     * 마케팅 알림은 [FEED_ID]·[NOTIFICATION_ID]가 없어 이 마커가 없으면 탭 자체를 감지할 수 없다.
     */
    const val FROM_PUSH = "fromPush"

    /** 서버가 [TYPE]을 주지 않았거나 알 수 없는 값일 때 로깅에 사용할 기본값. */
    const val UNKNOWN_TYPE = "UNKNOWN"
}
