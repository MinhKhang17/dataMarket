# ===============================
# Stage 1: Build the application
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy toàn bộ code vào container
COPY . .

# Build ứng dụng, bỏ qua test cho nhanh
RUN mvn clean package -DskipTests

# ===============================
# Stage 2: Run the built JAR
# ===============================
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
