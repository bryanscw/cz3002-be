all:
	run-db test stop-db build deploy

test:
	mvn clean install

build:
	cd infra && docker-compose build

deploy:
	cd infra && docker-compose up -d

stop:
	cd infra && docker-compose down

restart:
	cd infra && docker-compose down && docker-compose up -d

clean:
	docker system prune -f

javadocs:
	sudo mvn javadoc:javadoc && rm -rf target/ && rm -rf eduamp/target/javadoc-bundle-options

run-db:
	docker run --name eduamp-mysql -e MYSQL_ROOT_PASSWORD=12345 -e MYSQL_USER=user -e MYSQL_PASSWORD=my5ql -p 3306:3306 -d mysql:latest

stop-db:
	docker stop eduamp-mysql && docker rm eduamp-mysql