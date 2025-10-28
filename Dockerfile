# ===============================
# Stage 1: Build the application
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy file pom.xml trước để tải dependency (cache tốt hơn)
COPY pom.xml .
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"
# Tải toàn bộ dependency (để lần sau build nhanh hơn)
RUN mvn dependency:go-offline -B

# Sau đó mới copy code
COPY src ./src

# Build ứng dụng (bỏ qua test)
RUN mvn clean package -DskipTests

# ===============================
# Stage 2: Run the built JAR
# ===============================
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080 cho Spring Boot
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
