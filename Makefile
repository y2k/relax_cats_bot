OUT_DIR = .github/bin

.PHONY: playground
playground: build
	@ set -a && source .github/.dev.vars && node .github/bin/test/playground.js

# .PHONY: test
# test: build
# 	@ echo '{"type": "module"}' > $(OUT_DIR)/package.json
# # @ export OCAMLRUNPARAM=b && clj2js compile -target js -src test/test.clj > $(OUT_DIR)/test/test.js
# 	@ cd .github && node --env-file=.dev.vars bin/test/test.js

.PHONY: run
run: hook
	@ cd .github && wrangler dev

.PHONY: build
build:
	@ cy2k -src build.clj -target repl > .github/Makefile
	@ $(MAKE) -f .github/Makefile

# @ mkdir -p $(OUT_DIR)/src && mkdir -p $(OUT_DIR)/test && mkdir -p $(OUT_DIR)/vendor/effects
# @ export OCAMLRUNPARAM=b && clj2js compile -target js -src vendor/effects/effects.2.clj > $(OUT_DIR)/vendor/effects/effects.2.js
# @ export OCAMLRUNPARAM=b && clj2js compile -target js -src src/main.clj > $(OUT_DIR)/src/main.js

.PHONY: clean
clean:
	@ rm -rf $(OUT_DIR)

.PHONY: hook
hook:
	@NGROK_API="http://localhost:4040/api/tunnels" ; \
	NGROK_URL=$$(curl -s $$NGROK_API | grep -o '"public_url":"[^"]*' | grep -o 'http[^"]*') ; \
	source .github/.dev.vars ; \
	curl "https://api.telegram.org/bot$$TG_TOKEN/setWebhook?max_connections=1&drop_pending_updates=true&url=$$NGROK_URL"

# .PHONY: db
# db:
# 	@ cd .github && wrangler d1 execute LAST_REQUEST_TIME_D1 --local --file=schema.sql
