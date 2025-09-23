up:
	@docker compose up -d --build

down:
	@docker compose down -v

logs:
	@docker compose logs -f backend

ps:
	@docker compose ps

test:
	@docker compose exec backend ./mvnw -q -DskipITs test
