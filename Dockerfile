# -------- 1단계: Builder (Gradle + JDK) --------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Gradle Wrapper 및 설정 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# 의존성 미리 받아서 레이어 캐시 (코드보다 먼저)
RUN ./gradlew --no-daemon dependencies

# 나머지 소스 복사 후 빌드
COPY src ./src

# Spring Boot fat JAR 생성
RUN ./gradlew --no-daemon clean bootJar

# 레이어 추출 (Spring Boot layertools 사용)
# build/libs 안에 생성된 JAR가 하나라고 가정
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers


# -------- 2단계: Runtime (JRE + 레이어드 구조) --------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 레이어별 복사 (변경 빈도가 낮은 것부터)
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# JVM 옵션은 환경변수로 분리
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# Spring Boot 레이어드 JAR 런처
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
