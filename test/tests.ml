module TestUtils = struct
  let assert_ expected actual =
    if expected <> actual then failwith @@ actual ^ " <> " ^ expected

  let read_sample filename =
    let channel = open_in ("../../../test/samples/" ^ filename) in
    let size = in_channel_length channel in
    let content = really_input_string channel size in
    close_in channel ; content
end

let handle_command token (telegram_request_json : string) =
  let get_text json =
    let module U = Yojson.Safe.Util in
    Yojson.Safe.from_string json
    |> U.path ["message"; "text"]
    |> Fun.flip Option.bind U.to_string_option
    |> Option.value ~default:""
  in
  let startsWith s prefix = String.starts_with ~prefix s in
  let command = get_text telegram_request_json in
  if startsWith command "/cat" then
    Ok
      ( "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=" ^ token
      ^ "&tag=cat" )
  else Error "Unknown command"

(*
let handle msg =
  msg
  |> get_image_url_result
  |> Promise.Result.return
  >>= Fetch.request
  >>| create_telegram_response
  >>= Telegram.send
*)

let () =
  let assert_ expected actual =
    let pp_my_type fmt = function
      | Ok x ->
          Format.fprintf fmt "Ok(%s)" x
      | Error x ->
          Format.fprintf fmt "Error(%s)" x
    in
    let sexpected = Format.asprintf "%a" pp_my_type expected in
    let sactual = Format.asprintf "%a" pp_my_type actual in
    TestUtils.assert_ sexpected sactual
  in
  let json = TestUtils.read_sample "message-input-1.json" in
  let actual = handle_command "2a685538ce39" json in
  assert_
    (Ok
       "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=2a685538ce39&tag=cat"
    )
    actual

let () =
  let module U = Yojson.Safe.Util in
  TestUtils.read_sample "giphy.json"
  |> Yojson.Safe.from_string
  |> U.path ["data"; "images"; "original"; "mp4"]
  |> Fun.flip Option.bind U.to_string_option
  |> Option.value ~default:""
  |> TestUtils.assert_
       "https://media0.giphy.com/media/bDL3BsB4ViI2k/giphy.mp4?cid=f1dc8cf8zi3s07xqigiah0i389nhnwn9k4989fcsg4s6xzct&ep=v1_gifs_random&rid=giphy.mp4&ct=g"

let () =
  let get_text json =
    let module U = Yojson.Safe.Util in
    Yojson.Safe.from_string json
    |> U.path ["message"; "text"]
    |> Fun.flip Option.bind U.to_string_option
  in
  let assert_ expected x =
    let actual = get_text x |> Option.value ~default:"" in
    if expected <> actual then failwith actual
  in
  assert_ "" {|{}|} ;
  assert_ "" {|{"message": 0}|} ;
  assert_ "" {|{"message": {}}|} ;
  assert_ "foo" {|{"message": {"text": "foo"}}|} ;
  assert_ "/cat" (TestUtils.read_sample "message-input-1.json") ;
  assert_ "/dog@some_bot" (TestUtils.read_sample "message-input-2.json") ;
  ()
