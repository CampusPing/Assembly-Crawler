## 서울시 집회 정보 크롤러

### 기본 설정
#### build.gradle.kts
```kotlin
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.CampusPing:Assembly-Crawler:1.0.0-rc10")
}
```

<hr>

### 사용 예시
`crawl(pageSize: Int)` : 서울시 집회 정보를 크롤링하여 한 페이지에 해당하는 집회 정보를 반환합니다.

```kotlin
fun main() {
    val assemblies = AssemblyCrawler.crawl(
        targetYear = 2025,
        targetMonth = 6
    )
    println("크롤링된 집회 정보: $assemblies")
}
```
- `targetYear: Int` : 크롤링할 집회 정보의 연도 (예: 2025)
- `targetMonth: Int` : 크롤링할 집회 정보의 월 (1월: 1, 2월: 2, ..., 12월: 12)

<hr>

### 클래스 설명
`OneDayAssembly` : 특정 날짜의 하루치 집회 정보 리스트를 제공합니다.

```kotlin
data class OneDayAssembly(
    val date: LocalDate,
    val assemblies: List<Assembly>
)
```
- `date` : 집회 날짜
- `assemblies` : 날짜에 해당하는 집회 정보 리스트

<br>

`Assembly` : 집회 상세 정보를 제공합니다.

```kotlin
data class Assembly(
    val id: Int,
    val date: LocalDate,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val location: String,
    val district: String,
    val dong: String,
    val participants: Int,
)
```
- `id` : 집회 고유 ID
- `date` : 집회 날짜
- `startTime` : 집회 시작 시간
- `endTime` : 집회 종료 시간 (간헐적으로 집회 종료 시간이 없는 경우가 있기 때문에 nullable)
- `location` : 집회 장소
- `district` : 구 (e.g. 서대문구)
- `dong` : 동 (e.g. 연희동)
- `participants` : 집회 신고 인원 (e.g. 3000)


