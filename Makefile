.PHONY: test package run up down logs clean

test:
	mvn -B clean test

package:
	mvn -B clean package

run:
	mvn spring-boot:run

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f app

clean:
	mvn clean
