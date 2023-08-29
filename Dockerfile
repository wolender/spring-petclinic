FROM amazoncorretto:17
RUN mkdir /usr/src/petclinic
WORKDIR /usr/src/petclinic
COPY target/*.jar /usr/src/petclinic/app.jar
RUN chmod 777 *.jar
EXPOSE 80
CMD ["java","-Dspring.profiles.active=mysql","-jar","app.jar"]
