FROM oracle/graalvm-ce:19.0.2 as graalvm
COPY . /home/app/micronaut-grpc-graal
WORKDIR /home/app/micronaut-grpc-graal
RUN gu install native-image
RUN native-image --no-server -cp target/micronaut-grpc-graal-*.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/micronaut-grpc-graal .
ENTRYPOINT ["./micronaut-grpc-graal"]
