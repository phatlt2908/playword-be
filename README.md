docker build -t playword-be .
docker run -d -p 4400:8080 --name playword-be playword-be