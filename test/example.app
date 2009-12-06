module example

// Example "WebDSL" program (see syntax/WebDSL.sdf for the grammar)

entity User {
  name     : String
  password : String 
  homepage : URL
}

entity BlogPosting {
  poster : User
  body   :: String
}

entity URL {
  location : String
}