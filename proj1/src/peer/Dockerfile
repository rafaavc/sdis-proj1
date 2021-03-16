FROM openjdk:15

EXPOSE 1099

ENV BACKUP_SERVICE_ENV docker

WORKDIR /home

COPY . .

RUN javac Peer.java

CMD [ "java", "Peer" ]
