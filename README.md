docker build -t play-word-be .
docker run -d -p 4400:8080 --name play-word-be play-word-be