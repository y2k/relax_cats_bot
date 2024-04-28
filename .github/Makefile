PRELUDE_PATH := $(shell realpath ../vendor/prelude/js/src/prelude.clj)

run: hook
	wrangler dev

test: build
	@ echo '{"type": "module"}' > bin/package.json
	@ clear && clj2js js ../test/test.clj $(PRELUDE_PATH) > bin/test/test.js
	@ clear && node --env-file=.dev.vars bin/test/test.js

build:
	@ mkdir -p bin/src && mkdir -p bin/test && mkdir -p bin/vendor/packages/effects
	@ clear && clj2js js ../vendor/packages/effects/effects.2.clj $(PRELUDE_PATH) > bin/vendor/packages/effects/effects.2.js
	@ clear && clj2js js ../src/main.clj $(PRELUDE_PATH) > bin/src/main.js

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
