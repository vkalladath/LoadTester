# Build stage
FROM vkalladath/maven-springboot-builder AS build
COPY src /home/LoadTester/src
COPY pom.xml /home/LoadTester
RUN mvn -f /home/LoadTester/pom.xml clean package

# Package stage
FROM amazoncorretto:21-alpine3.21
COPY --from=build /home/LoadTester/target/LoadTester-1.0.jar /usr/local/lib/LoadTester.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/LoadTester.jar"]
