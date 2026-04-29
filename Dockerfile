FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src src

RUN mvn -B -DskipTests package
RUN sh -c 'cp "$(find target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -n 1)" app.jar'

FROM maven:3.9.9-amazoncorretto-21
WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
