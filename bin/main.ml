open Js_of_ocaml
open Utils

let get_message_text json =
  let module U = Yojson.Safe.Util in
  json |> U.path ["message"; "text"] |> Fun.flip Option.bind U.to_string_option

let get_callback_data json =
  let module U = Yojson.Safe.Util in
  json
  |> U.path ["callback_query"; "data"]
  |> Fun.flip Option.bind U.to_string_option

let get_mp4_url json =
  let module U = Yojson.Safe.Util in
  Yojson.Safe.from_string json
  |> U.path ["data"; "images"; "original"; "mp4"]
  |> Option.get |> U.to_string

let get_chat_id prefix json =
  let module U = Yojson.Safe.Util in
  json
  |> U.path (prefix @ ["message"; "chat"; "id"])
  |> Fun.flip Option.bind (function
       | `Intlit x ->
           Some x
       | `Int x ->
           Some (string_of_int x)
       | x ->
           failwith @@ Format.asprintf "%a" Yojson.Safe.pp x )
  |> Option.get

(* let send_to_telegram tg_token chat_id new_message =
   let make_telegram_body =
     `Assoc [("chat_id", `Int chat_id); ("text", `String new_message)]
     |> Yojson.Safe.pretty_to_string
   in
   let url =
     Printf.sprintf "https://api.telegram.org/bot%s/sendMessage" tg_token
   in
   Fetch.fetch url
     [ `Body make_telegram_body
     ; `Method "post"
     ; `Headers [("content-type", "application/json")] ] *)

open Promise.Syntax

let image_url tag =
  let giphy_token = Js.Unsafe.global ##. GIPHY_TOKEN_ in
  let giphy_url =
    "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=" ^ giphy_token
    ^ "&tag=" ^ tag
  in
  let* giphy_response = Fetch.fetch giphy_url [] in
  let+ g_text = Response.text giphy_response in
  get_mp4_url g_text

module Callback_data = struct
  module U = Yojson.Safe.Util

  type info = {time: int; count: int}

  let current_version = 1

  let parse text =
    let json = Yojson.Safe.from_string text in
    match json |> U.member "v" |> U.to_int_option with
    | Some v when v = current_version ->
        Some
          { time= json |> U.member "t" |> U.to_int
          ; count= json |> U.member "c" |> U.to_int }
    | _ ->
        None

  let make_button_data now count =
    `String
      (Yojson.Safe.to_string
         (`Assoc
           [("v", `Int current_version); ("t", `Int now); ("c", `Int count)] ) )
end

let send_giphy_image tg_token chat_id =
  let* video_url = image_url "cat" in
  let now = get_unixtime_sec () in
  let init_count = 10 in
  let* _ =
    Fetch.fetch
      (Printf.sprintf "https://api.telegram.org/bot%s/sendVideo" tg_token)
      [ `Body
          (Yojson.Safe.pretty_to_string
             (`Assoc
               [ ("chat_id", `String chat_id)
               ; ("video", `String video_url)
               ; ( "reply_markup"
                 , `Assoc
                     [ ( "inline_keyboard"
                       , `List
                           [ `List
                               [ `Assoc
                                   [ ( "text"
                                     , `String
                                         ( "🎲 (Next " ^ string_of_int init_count
                                         ^ ")" ) )
                                   ; ( "callback_data"
                                     , Callback_data.make_button_data now
                                         init_count ) ]
                               ; `Assoc
                                   [ ("text", `String "Done")
                                   ; ( "callback_data"
                                     , Callback_data.make_button_data now 0 ) ]
                               ] ] ) ] ) ] ) )
      ; `Method "post"
      ; `Headers [("content-type", "application/json")] ]
  in
  Promise.return ()

let remove_keyboard tg_token chat_id message_id =
  let* _ =
    Fetch.fetch
      (Printf.sprintf "https://api.telegram.org/bot%s/editMessageReplyMarkup"
         tg_token )
      [ `Body
          (Yojson.Safe.pretty_to_string
             (`Assoc
               [("chat_id", `String chat_id); ("message_id", `Int message_id)]
               ) )
      ; `Method "post"
      ; `Headers [("content-type", "application/json")] ]
  in
  Promise.return ()

let edit_giphy_image tg_token chat_id message_id (info : Callback_data.info) =
  let count = info.count - 1 in
  let* video_url = image_url "cat" in
  let now = get_unixtime_sec () in
  let* _ =
    Fetch.fetch
      (Printf.sprintf "https://api.telegram.org/bot%s/editMessageMedia" tg_token)
      [ `Body
          (Yojson.Safe.pretty_to_string
             (`Assoc
               [ ("chat_id", `String chat_id)
               ; ("message_id", `Int message_id)
               ; ( (if count > 0 then "reply_markup" else "_ignore")
                 , `Assoc
                     [ ( "inline_keyboard"
                       , `List
                           [ `List
                               [ `Assoc
                                   [ ( "text"
                                     , `String
                                         ("🎲 (Next " ^ string_of_int count ^ ")")
                                     )
                                   ; ( "callback_data"
                                     , Callback_data.make_button_data now count
                                     ) ]
                               ; `Assoc
                                   [ ("text", `String "Done")
                                   ; ( "callback_data"
                                     , Callback_data.make_button_data now 0 ) ]
                               ] ] ) ] )
               ; ( "media"
                 , `Assoc
                     [("type", `String "video"); ("media", `String video_url)]
                 ) ] ) )
      ; `Method "post"
      ; `Headers [("content-type", "application/json")] ]
  in
  Promise.return ()

let handle_callback_action tg_token message_json =
  let chat_id = get_chat_id ["callback_query"] message_json in
  let module U = Yojson.Safe.Util in
  let message_id =
    U.path ["callback_query"; "message"; "message_id"] message_json
    |> Option.get |> U.to_int
  in
  let info =
    U.path ["callback_query"; "data"] message_json
    |> Option.get |> U.to_string |> Callback_data.parse
  in
  match info with
  | Some info ->
      let now = get_unixtime_sec () in
      if info.count > 0 && abs (now - info.time) < 10 then
        edit_giphy_image tg_token chat_id message_id info
      else remove_keyboard tg_token chat_id message_id
  | None ->
      remove_keyboard tg_token chat_id message_id

let event_handler event =
  let tg_token = Js.Unsafe.global ##. TG_TOKEN_ in
  let open Promise.Syntax in
  let r : request Js.t = event##.request in
  let* telegram_input_text = r##text in
  let message_json = Yojson.Safe.from_string telegram_input_text in
  print_endline @@ "JSON: " ^ Yojson.Safe.pretty_to_string message_json ;
  (*  *)
  let* _ =
    ( match get_message_text message_json with
    | Some "/cat" ->
        let chat_id = get_chat_id [] message_json in
        send_giphy_image tg_token chat_id
    | _ -> (
      match get_callback_data message_json with
      | Some _ ->
          handle_callback_action tg_token message_json
      | _ ->
          print_endline @@ "UNSUPPORTED MESSAGE: "
          ^ Yojson.Safe.show message_json ;
          Promise.resolve () ) )
    |> Promise.catch ~rejected:(fun e ->
           Js.Unsafe.fun_call
             (Js.Unsafe.js_expr "console.error")
             [|Js.Unsafe.inject e|]
           |> ignore ;
           Promise.return () )
  in
  (*  *)
  let ctr = Js.Unsafe.global##._Response in
  let a : _ Js.t = new%js ctr "" in
  Promise.return a

let () =
  Dom.addEventListener Js.Unsafe.global (Dom.Event.make "fetch")
    (Dom.handler (fun event -> event##respondWith (event_handler event)))
    Js._false
  |> ignore
