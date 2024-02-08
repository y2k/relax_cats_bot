run: hook
	wrangler dev

test: build
	clear && clj2js ../src/vendor/effects.clj > bin/vendor/effects.js && \
	clear && clj2js ../test/test.clj > bin/test.js && \
	clear && node --env-file=.dev.vars bin/test.js

build:
	clj2js ../src/main.clj > bin/main.js

hook:
	@NGROK_API="http://localhost:4040/api/tunnels" ; \
	NGROK_URL=$$(curl -s $$NGROK_API | grep -o '"public_url":"[^"]*' | grep -o 'http[^"]*') ; \
	source .dev.vars ; \
	curl "https://api.telegram.org/bot$$TG_TOKEN/setWebhook?max_connections=1&drop_pending_updates=true&url=$$NGROK_URL"

db:
	wrangler d1 execute LAST_REQUEST_TIME_D1 --local --file=schema.sql

.PHONY: run test build hook db
