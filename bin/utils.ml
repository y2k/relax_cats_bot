open Js_of_ocaml

module Response = struct
  type t = Response

  let text (r : t) : string Promise.t = Js.Unsafe.meth_call r "text" [||]

  let status (r : t) : int = Js.Unsafe.get r "status"
end

module Fetch = struct
  type request_init = {todo: int}

  let fetch (url : string) props : Response.t Promise.t =
    let _body =
      props
      |> List.map (fun prop ->
             match prop with
             | `Method (name : string) ->
                 ("method", Js.Unsafe.inject name)
             | `Body (body : string) ->
                 ("body", Js.string body |> Js.Unsafe.inject)
             | `Headers headers ->
                 ( "headers"
                 , headers
                   |> List.map (fun (k, (v : string)) ->
                          (k, Js.Unsafe.inject v) )
                   |> Array.of_list |> Js.Unsafe.obj ) )
      |> Array.of_list |> Js.Unsafe.obj
    in
    Js.Unsafe.global##fetch (Js.Unsafe.inject (Js.string url)) _body
end

let get_unixtime_sec () = int_of_float (Js.date##now /. 1000.0)

class type request = object
  method url : string Js.readonly_prop

  method text : string Promise.t Js.meth
end
