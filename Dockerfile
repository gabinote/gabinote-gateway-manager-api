# Multi-stage build를 사용하여 최적화된 이미지 생성
FROM openjdk:21-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle.kts 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Gradle wrapper 실행 권한 부여
RUN chmod +x gradlew

# 의존성 다운로드 (레이어 캐싱을 위해 별도로 실행)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon

# 프로덕션 이미지 생성
FROM openjdk:21-jre-slim

# 메타데이터 설정
LABEL maintainer="gabinote"
LABEL version="1.0.0"
LABEL description="Gateway Manager API"

# 작업 디렉토리 설정
WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 설정 파일 권한 설정
RUN chown -R appuser:appuser /app

# non-root 사용자로 전환
USER appuser

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
