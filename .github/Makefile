run: hook
	wrangler dev

test: build
	@ echo '{"type": "module"}' > bin/package.json
	@ clear && clj2js js ../test/test.clj ../vendor/prelude/js/src/prelude.clj > bin/test.js
	@ clear && node --env-file=.dev.vars bin/test.js

build:
	@ mkdir -p bin/vendor
	@ cp ../vendor/prelude/js/src/prelude.js bin/prelude.js
	@ cp ../vendor/prelude/js/src/prelude.js bin/vendor/prelude.js
	@ clear && clj2js js ../vendor/effects/src/effects.clj ../vendor/prelude/js/src/prelude.clj > bin/vendor/effects.js
	@ clear && clj2js js ../src/main.clj ../vendor/prelude/js/src/prelude.clj > bin/main.js

clean:
	@ rm -rf bin

hook:
	@NGROK_API="http://localhost:4040/api/tunnels" ; \
	NGROK_URL=$$(curl -s $$NGROK_API | grep -o '"public_url":"[^"]*' | grep -o 'http[^"]*') ; \
	source .dev.vars ; \
	curl "https://api.telegram.org/bot$$TG_TOKEN/setWebhook?max_connections=1&drop_pending_updates=true&url=$$NGROK_URL"

db:
	wrangler d1 execute LAST_REQUEST_TIME_D1 --local --file=schema.sql

.PHONY: run test build clean hook db
