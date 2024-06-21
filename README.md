docker build -t playword-be .
docker run -d -p 8080:8080 --name playword-be playword-be