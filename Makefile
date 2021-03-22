all:	stop run-db test stop-db build deploy

test:
	mvn clean install -Dmaven.test.skip=true

build:
	cd infra && docker-compose build

deploy:
	sudo chmod 777 target/* && cd infra && docker-compose up -d

stop:
	cd infra && docker-compose down

restart:
	cd infra && docker-compose down && docker-compose up -d

clean:
	docker system prune -f

javadocs:
	sudo mvn javadoc:javadoc && rm -rf target/ && rm -rf cogbench/target/javadoc-bundle-options

run-db:
	docker run --name app-db -e MYSQL_ROOT_PASSWORD=12345 -e MYSQL_USER=user -e MYSQL_PASSWORD=my5ql -p 3306:3306 -d mysql:latest

stop-db:
	docker stop app-db && docker rm app-db
