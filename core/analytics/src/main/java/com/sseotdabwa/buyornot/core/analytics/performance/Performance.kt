package com.sseotdabwa.buyornot.core.analytics.performance

/**
 * 커스텀 구간 성능 계측 진입점.
 *
 * feature 모듈이 Firebase에 직접 의존하지 않도록 하는 추상화 계층이다.
 * 구현은 [FirebasePerformanceTracer] 하나이며, DI 시점에 주입된다.
 */
interface Performance {
    /** 아직 시작되지 않은 Trace를 만든다. 이름은 [TraceNames] 상수를 쓴다. */
    fun newTrace(name: String): PerfTrace
}

/**
 * 커스텀 Trace 이름 모음.
 *
 * Firebase 콘솔에서 이름으로 지표를 찾게 되므로 한 곳에 모아 관리한다.
 * 이름은 100자 이내이며 선행 공백/밑줄을 쓸 수 없다.
 */
object TraceNames {
    /**
     * 스플래시 진입부터 홈/로그인 분기가 결정되기까지.
     * 고정 딜레이는 포함되고 업데이트 팝업 대기(사용자 반응 시간)는 제외된다.
     */
    const val SPLASH_TO_NAVIGATION = "splash_to_navigation"

    /** 홈 피드 최초 로딩 — 로딩 시작부터 목록이 상태에 반영되기까지. */
    const val FEED_FIRST_LOAD = "feed_first_load"

    /** 투표 요청 왕복. UI는 낙관적 업데이트라 체감 시간과 분리해 본다. */
    const val VOTE_REQUEST = "vote_request"
}
