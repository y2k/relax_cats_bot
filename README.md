

```bash
 curl "https://api.telegram.org/bot<token>/setWebhook?drop_pending_updates=true&url=https://XXXX.ngrok-free.app"
 ```

```json
{
    "update_id": 569124917,
    "message": {
        "message_id": 5362,
        "from": {
            "id": 100000000,
            "is_bot": false,
            "first_name": "Igor",
            "username": "username",
            "language_code": "en"
        },
        "chat": {
            "id": 100000000,
            "first_name": "Igor",
            "username": "username",
            "type": "private"
        },
        "date": 1701129970,
        "text": "/cat",
        "entities": [
            {
                "offset": 0,
                "length": 4,
                "type": "bot_command"
            }
        ]
    }
}
```

## Button press

```json
{
    "update_id": 569124943,
    "callback_query": {
        "id": "1038758112935494983",
        "from": {
            "id": 241854720,
            "is_bot": false,
            "first_name": "Igor",
            "username": "angmarr",
            "language_code": "en"
        },
        "message": {
            "message_id": 5403,
            "from": {
                "id": 300777612,
                "is_bot": true,
                "first_name": "DEBUG",
                "username": "debug3bot"
            },
            "chat": {
                "id": 241854720,
                "first_name": "Igor",
                "username": "angmarr",
                "type": "private"
            },
            "date": 1701609066,
            "animation": {
                "file_name": "giphy.mp4",
                "mime_type": "video/mp4",
                "duration": 3,
                "width": 480,
                "height": 480,
                "thumbnail": {
                    "file_id": "AAMCBAADGQMAAhUbZWx-ae91bj3q61ldzma2_wcWjqQAAoEEAAIdlx1SeQUBWBwjSEYBAAdtAAMzBA",
                    "file_unique_id": "AQADgQQAAh2XHVJy",
                    "file_size": 19033,
                    "width": 320,
                    "height": 320
                },
                "thumb": {
                    "file_id": "AAMCBAADGQMAAhUbZWx-ae91bj3q61ldzma2_wcWjqQAAoEEAAIdlx1SeQUBWBwjSEYBAAdtAAMzBA",
                    "file_unique_id": "AQADgQQAAh2XHVJy",
                    "file_size": 19033,
                    "width": 320,
                    "height": 320
                },
                "file_id": "CgACAgQAAxkDAAIVG2VsfmnvdW496utZXc5mtv8HFo6kAAKBBAACHZcdUnkFAVgcI0hGMwQ",
                "file_unique_id": "AgADgQQAAh2XHVI",
                "file_size": 681846
            },
            "document": {
                "file_name": "giphy.mp4",
                "mime_type": "video/mp4",
                "thumbnail": {
                    "file_id": "AAMCBAADGQMAAhUbZWx-ae91bj3q61ldzma2_wcWjqQAAoEEAAIdlx1SeQUBWBwjSEYBAAdtAAMzBA",
                    "file_unique_id": "AQADgQQAAh2XHVJy",
                    "file_size": 19033,
                    "width": 320,
                    "height": 320
                },
                "thumb": {
                    "file_id": "AAMCBAADGQMAAhUbZWx-ae91bj3q61ldzma2_wcWjqQAAoEEAAIdlx1SeQUBWBwjSEYBAAdtAAMzBA",
                    "file_unique_id": "AQADgQQAAh2XHVJy",
                    "file_size": 19033,
                    "width": 320,
                    "height": 320
                },
                "file_id": "CgACAgQAAxkDAAIVG2VsfmnvdW496utZXc5mtv8HFo6kAAKBBAACHZcdUnkFAVgcI0hGMwQ",
                "file_unique_id": "AgADgQQAAh2XHVI",
                "file_size": 681846
            },
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "\ud83c\udfb2 (Next)",
                            "callback_data": "CD1"
                        }
                    ]
                ]
            }
        },
        "chat_instance": "7027187588705046566",
        "data": "CD1"
    }
}
```