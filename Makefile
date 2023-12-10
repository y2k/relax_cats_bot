.PHONY: run build_wrangler release test ngrok

run:
	wrangler dev

build_wrangler:
	dune build bin && clear

release:
	dune clean && dune build bin --profile=release

test:
	 OCAMLRUNPARAM=b && dune test -f

ngrok:
	@NGROK_API="http://localhost:4040/api/tunnels" ; \
	NGROK_URL=$$(curl -s $$NGROK_API | grep -o '"public_url":"[^"]*' | grep -o 'http[^"]*') ; \
	source .dev.vars ; \
	curl "https://api.telegram.org/bot$$TG_TOKEN/setWebhook?max_connections=1&drop_pending_updates=true&url=$$NGROK_URL"
